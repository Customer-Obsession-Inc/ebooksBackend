package com.cusob.ebooks.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cusob.ebooks.auth.AuthContext;
import com.cusob.ebooks.common.properties.JwtProperties;
import com.cusob.ebooks.constant.MqConst;
import com.cusob.ebooks.constant.RedisConst;
import com.cusob.ebooks.mapper.UserMapper;
import com.cusob.ebooks.pojo.DTO.ForgetPasswordDto;
import com.cusob.ebooks.pojo.DTO.UpdatePasswordDto;
import com.cusob.ebooks.pojo.DTO.UserDto;
import com.cusob.ebooks.pojo.DTO.UserLoginDto;
import com.cusob.ebooks.pojo.Email;
import com.cusob.ebooks.pojo.User;
import com.cusob.ebooks.pojo.vo.UserLoginVo;
import com.cusob.ebooks.pojo.vo.UserVo;
import com.cusob.ebooks.result.ResultCodeEnum;
import com.cusob.ebooks.service.MailService;
import com.cusob.ebooks.service.UserService;
import com.cusob.ebooks.common.Exception.EbooksException;
import com.cusob.ebooks.utils.JwtUtil;
import com.cusob.ebooks.utils.ReadEmail;

import javax.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.redis.core.HashOperations;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${ebooks.cf-secret-key}")
    private String turnstileSecretKey ;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private MailService mailService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

//    @Autowired
//    private CompanyService companyService;

//    @Autowired
//    private PlanPriceService planPriceService;

    @Value("${ebooks.url}")
    private String baseUrl;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RestTemplate restTemplate;

    public UserServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * add User(User Register)
     * @param userDto
     */
    @Transactional
    @Override
    public void addUser(UserDto userDto) {

        this.paramEmptyVerify(userDto); // 参数校验：检查邮箱和nickname是否为空

        // 检查 email 是否已存在
        QueryWrapper<User> emailQuery = new QueryWrapper<>();
        emailQuery.eq("email", userDto.getEmail());
        if (this.count(emailQuery) > 0) {
            throw new EbooksException(ResultCodeEnum.EMAIL_IS_REGISTERED);
        }

        // 检查 nickname 是否已存在
        QueryWrapper<User> nicknameQuery = new QueryWrapper<>();
        nicknameQuery.eq("nickname", userDto.getNickname());
        if (this.count(nicknameQuery) > 0) {
            throw new EbooksException(ResultCodeEnum.NICKNAME_ALREADY_TAKEN);
        }
        // 检查邮箱
        if (userDto.getEmail() != null && !userDto.getEmail().isEmpty()) {
            boolean flag = Pattern.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", userDto.getEmail());
            if (!flag) {
                throw new EbooksException(ResultCodeEnum.EMAIL_FORMAT_ERROR); // 邮箱格式错误
            }
         //   this.registerVerify(userDto); // 检查是否通过cf验证及邮箱是否已经注册
        }

        // 创建用户对象并复制属性
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        String password = userDto.getPassword();

        // 密码加密
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        user.setPermission(User.USER);
        user.setIsAvailable(User.DISABLE); // 默认不可用
        baseMapper.insert(user);

        System.out.println("insert-success!");

        // 将用户信息存入 Redis
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String uuid = UUID.randomUUID().toString() + System.currentTimeMillis(); // 生成 UUID
        hashOperations.put(uuid, "email:nickname", user.getEmail() + ":" + user.getNickname()); // 将用户信息存入 Redis
        hashOperations.put(uuid, "password", user.getPassword());
        hashOperations.put(uuid, "phone", user.getPhone());
        redisTemplate.expire(uuid, 30, TimeUnit.MINUTES);

        Map<String,String> usermap = new HashMap<>();
        usermap.put("uuid",uuid);
        usermap.put("email",user.getEmail());
        rabbitTemplate.convertAndSend(MqConst.EXCHANGE_REGISTER_DIRECT,
                MqConst.ROUTING_REGISTER_SUCCESS, usermap); //发送注册邮件
    }


    private void registerVerify(UserDto userDto) {  //todo验证码
        String turnstileToken = userDto.getTurnstileToken();
        if (!StringUtils.hasText(turnstileToken)){
            throw new EbooksException(ResultCodeEnum.VERIFY_CODE_EMPTY);
        }

        String url = "https://challenges.cloudflare.com/turnstile/v0/siteverify"; //验证CF是否通过了验证
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("secret", turnstileSecretKey);
        requestBody.put("response", turnstileToken);

        Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);//发送请求
        if (!(response != null && Boolean.TRUE.equals(response.get("success")))) {//验证失败
            throw new EbooksException(ResultCodeEnum.VERIFY_CODE_WRONG);
        }

        String email = userDto.getEmail();
        User userDb = this.getUserByEmail(email);
        if (userDb != null){
            throw new EbooksException(ResultCodeEnum.EMAIL_IS_REGISTERED);
        }

    }

    private void paramEmptyVerify(UserDto userDto) {
        if (!StringUtils.hasText(userDto.getEmail())){
            throw new EbooksException(ResultCodeEnum.EMAIL_IS_EMPTY);
        }
        if(!StringUtils.hasText(userDto.getNickname())){
            throw new EbooksException(ResultCodeEnum.NICKNAME_IS_EMPTY);
        }
        //todo 链接激活

    }


    /**
     * add Admin
     * @param userId
     */
    @Override
    public void addAdmin(Long userId) {
        User user = baseMapper.selectById(userId);
        Integer permission = user.getPermission();
        if (!permission.equals(User.USER)){
            throw new EbooksException(ResultCodeEnum.USER_IS_ADMIN);
        }
        user.setPermission(User.ADMIN);
        baseMapper.updateById(user);
    }

    /**
     * remove Admin
     * @param userId
     */
    @Override
    public void removeAdmin(Long userId) {

        User toRemove = baseMapper.selectById(userId);
        if (!toRemove.getPermission().equals(User.ADMIN)){
            throw new EbooksException(ResultCodeEnum.REMOVE_ADMIN_FAIL);
        }
        toRemove.setPermission(User.USER);
        baseMapper.updateById(toRemove);
    }

    /**
     * remove User
     * @param userId
     */
    @Override
    public void removeUser(Long userId) {

        Long adminId = AuthContext.getUserId();
        User admin = baseMapper.selectById(adminId);
        Integer permission = admin.getPermission();
        User toRemove = baseMapper.selectById(userId);
        Integer toRemovePermission = toRemove.getPermission();
        // Normal user can't remove others
        if (permission<=toRemovePermission){
            throw new EbooksException(ResultCodeEnum.NO_OPERATION_PERMISSIONS);
        }
        baseMapper.deleteById(userId);
    }

    /**
     * get UserInfo by id
     * @return
     */
    @Override
    public UserVo getUserInfo() {
        Long userId = AuthContext.getUserId();
        User user = baseMapper.selectById(userId);
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        return userVo;
    }



    /**
     * update UserInfo
     * @param userDto
     */
    @Override
    public void updateUserInfo(UserDto userDto) {
        this.paramEmptyVerify(userDto);
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        baseMapper.updateById(user);
    }


    /**
     * get UserList
     * @param pageParam
     * @return
     */
    @Override
    public IPage<User> getUserList(Page<User> pageParam) {
        // 创建 LambdaQueryWrapper
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 按 User ID 排序（假设您希望按照 ID 升序排列）
        wrapper.orderByAsc(User::getId);
        // 执行分页查询
        IPage<User> page = baseMapper.selectPage(pageParam, wrapper);
        return page;
    }

    /**
     * user login
     * @param userLoginDto
     * @return
     */
    @Override
    public UserLoginVo login(UserLoginDto userLoginDto) {
        String email = userLoginDto.getEmail();
        String nickname = userLoginDto.getNickname();
        String password = userLoginDto.getPassword();
        // select user from table email
        // 根据用户输入的字段进行不同的查询
        User user;
        if (email != null && !email.isEmpty()) {
            // 使用邮箱进行查询
            user = baseMapper.selectOne(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getEmail, email)
            );
        } else if (nickname != null && !nickname.isEmpty()) {
            // 使用昵称进行查询
            user = baseMapper.selectOne(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getNickname, nickname)
            );
        } else {
            // 处理输入不合法的情况
            throw new IllegalArgumentException("Either email or nickname must be provided.");
        }

        // user is not registered
        if (user == null){
            throw new EbooksException(ResultCodeEnum.USER_NOT_REGISTER);
        }

        // Password is wrong
        String psd = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!psd.equals(user.getPassword())){
            throw new EbooksException(ResultCodeEnum.PASSWORD_WRONG);
        }

        // todo User is Disable(During the internal test, you cannot log in temporarily)
        if (user.getIsAvailable().equals(User.DISABLE)){
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();//将用户信息存入redis
            String uuid = UUID.randomUUID().toString()+System.currentTimeMillis();//生成uuid
            hashOperations.put(uuid,"email:nickname",user.getEmail() + ":" +user.getNickname());//将用户信息存入redis
            hashOperations.put(uuid,"password",user.getPassword());
            hashOperations.put(uuid,"phone",user.getPhone());
            redisTemplate.expire(uuid, 30, TimeUnit.MINUTES);

        }

        // Generate token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);

        UserLoginVo userLoginVo = UserLoginVo.builder()
                .id(user.getId())
                .lastName(user.getLastName())
                .firstName(user.getFirstName())
                .avatar(user.getAvatar())
                .token(token)
                .build();
        return userLoginVo;
    }

    /**
     * update password
     * @param updatePasswordDto
     */
    @Override
    public void updatePassword(UpdatePasswordDto updatePasswordDto) {
        String oldPassword = updatePasswordDto.getOldPassword();
        // old password is empty
        if (!StringUtils.hasText(oldPassword)){
            throw new EbooksException(ResultCodeEnum.OLD_PASSWORD_EMPTY);
        }
        String oldPsd = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        Long userId = AuthContext.getUserId();
        User user = baseMapper.selectById(userId);
        // old password is wrong
        if (!oldPsd.equals(user.getPassword())){
            throw new EbooksException(ResultCodeEnum.OLD_PASSWORD_WRONG);
        }
        String newPassword = updatePasswordDto.getNewPassword();
        String newPsd = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        user.setPassword(newPsd);
        baseMapper.updateById(user);
    }

    /**
     * send verify code for updating password
     */
    @Override
    public void sendCodeForPassword(String email) {
        User user = this.getUserByEmail(email);
        if (user==null){
            throw new EbooksException(ResultCodeEnum.EMAIL_NOT_EXIST);
        }
        String code = String.valueOf((int)((Math.random() * 9 + 1) * Math.pow(10,5)));
        String subject = "Password Reset Instructions for Your Email Marketing Platform Account";
        // todo 待优化
        String content = ReadEmail.readwithcode("emails/email-forgetpassword.html",code);

        String key = RedisConst.PASSWORD_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, RedisConst.PASSWORD_TIMEOUT, TimeUnit.MINUTES);
        Email mail = new Email();
        mail.setEmail(email);
        mail.setSubject(subject);
        mail.setContent(content);
        rabbitTemplate.convertAndSend(MqConst.EXCHANGE_PASSWORD_DIRECT, MqConst.ROUTING_FORGET_PASSWORD, mail);
    }

    /**
     * send email for reset password
     */
    @Override
    public void sendEmailForResetPassword(String email) {
        // 获取用户信息
        User user = this.getUserByEmail(email);
        if (user == null) {
            throw new EbooksException(ResultCodeEnum.EMAIL_NOT_EXIST);
        }

        // 加密邮箱
        String emailKey = DigestUtils.md5DigestAsHex(email.getBytes());

        // 生成链接
        String link = baseUrl+"/resetPassword?email=" +emailKey;
        String subject = "Password Reset Instructions for Ebooks Platform Account";
        String content = ReadEmail.readwithcode("emails/reset.html", link);
        content = content.replace("{EMAIL_PLACEHOLDER}", email);

        // 将加密邮箱作为键，原始邮箱和验证码作为值存储在Redis中
        String redisKey = RedisConst.PASSWORD_PREFIX + emailKey;
        redisTemplate.opsForValue().set(redisKey, email , RedisConst.PASSWORD_TIMEOUT, TimeUnit.MINUTES);

        // 准备邮件对象
        Email mail = new Email();
        mail.setEmail(email);
        mail.setSubject(subject);
        mail.setContent(content);

        // 发送邮件
        rabbitTemplate.convertAndSend(MqConst.EXCHANGE_PASSWORD_DIRECT, MqConst.ROUTING_FORGET_PASSWORD, mail);
    }


    /**
     * get User By Email
     * @param email
     * @return
     */
    @Override
    public User getUserByEmail(String email) {
        User user = baseMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email)
        );
        return user;
    }

    /**
     * send Email For Register Success
     */
    @Override
    public void sendEmailForRegisterSuccess(String uuid,String email) {

        String subject = "Welcome to Our Ebooks Platform! New User Guide";
        // todo 待优化
        String content = ReadEmail.readwithcode("emails/activate.html",baseUrl+"/registerSuccess?uuid="+uuid);
        mailService.sendHtmlMailMessage(email, subject, content);
    }

    @Override
    public boolean checkUuid(String uuid) {
        if(uuid == null){
            return false;
        }
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        Map<String, String> entries = hashOperations.entries(uuid);
        if(!entries.isEmpty()){

            String[] emailAndNickNamearray = entries.get("email:nickname").split(":");

            String email = emailAndNickNamearray[0];
            String nickName = emailAndNickNamearray[1];


            User user = baseMapper.selectOne(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getEmail, email)
            );
            if(user.getIsAvailable() == 0){
                user.setIsAvailable(User.AVAILABLE); // TODO 开启使用//如果已经存在该账号就不要再插入
                baseMapper.updateById(user);
            }

            return true;
        }
        return false;
    }

    /**
     * send Verify Code for signing up
     */
    @Override
    public void sendVerifyCode(String email) {
        if (!StringUtils.hasText(email)){
            throw new EbooksException(ResultCodeEnum.EMAIL_IS_EMPTY);
        }
        String code = String.valueOf((int)((Math.random() * 9 + 1) * Math.pow(10,5)));

        String subject = "Welcome to Our Email Marketing Platform! Verification Code Reminder";
        String content = ReadEmail.readwithcode("emails/email-signup.html",code);
        // todo 待优化
        Email mail = new Email();
        mail.setEmail(email);
        mail.setSubject(subject);
        mail.setContent(content);
        String key = RedisConst.REGISTER_PREFIX + email;
        // set verify code ttl 10 minutes
        redisTemplate.opsForValue().set(key, code, RedisConst.REGISTER_TIMEOUT, TimeUnit.MINUTES);
        rabbitTemplate.convertAndSend(MqConst.EXCHANGE_SIGN_DIRECT, MqConst.ROUTING_SEND_CODE, mail);
    }

    /**
     * forget password
     * @param forgetPasswordDto
     */
    @Override
    public void forgetPassword(ForgetPasswordDto forgetPasswordDto) {
        String encryptedEmail = forgetPasswordDto.getEmail(); // 这个是加密后的邮箱
        if (!StringUtils.hasText(encryptedEmail)) {
            throw new EbooksException(ResultCodeEnum.EMAIL_IS_EMPTY);
        }

        String password = forgetPasswordDto.getPassword();
        if (!StringUtils.hasText(password)) {
            throw new EbooksException(ResultCodeEnum.PASSWORD_IS_EMPTY);
        }

        // Redis中根据加密后的邮箱查找存储的原始邮箱
        String redisKey = RedisConst.PASSWORD_PREFIX + encryptedEmail;
        String originalEmail = (String) redisTemplate.opsForValue().get(redisKey);
        if (originalEmail == null) {
            throw new EbooksException(ResultCodeEnum.VERIFY_CODE_WRONG); // 错误码可以调整为更合适的
        }

        // 获取用户并更新密码
        User user = this.getUserByEmail(originalEmail);
        if (user == null) {
            throw new EbooksException(ResultCodeEnum.EMAIL_NOT_EXIST);
        }

        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        baseMapper.updateById(user);
    }

    @Override
    public void invite(String email) {

    }

    @Override
    public void registerForInvited(UserDto userDto, String encode) {

    }


    @Override
    public boolean save(User entity) {
        return super.save(entity);
    }
}
