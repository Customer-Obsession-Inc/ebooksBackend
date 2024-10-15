package com.cusob.ebooks.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cusob.ebooks.pojo.DTO.ForgetPasswordDto;
import com.cusob.ebooks.pojo.DTO.UpdatePasswordDto;
import com.cusob.ebooks.pojo.DTO.UserDto;
import com.cusob.ebooks.pojo.DTO.UserLoginDto;
import com.cusob.ebooks.pojo.User;
import com.cusob.ebooks.pojo.vo.UserLoginVo;
import com.cusob.ebooks.pojo.vo.UserVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {

    /**
     * add User(User Register)
     * @param userDto
     */
    void addUser(UserDto userDto);

    /**
     * get UserInfo by id
     * @return
     */
    UserVo getUserInfo();

    /**
     * update UserInfo
     * @param userDto
     */
    void updateUserInfo(UserDto userDto);


    /**
     * get UserList
     * @param pageParam
     * @return
     */

    IPage<User> getUserList(Page<User> pageParam);

    /**
     * user login
     *
     * @param userLoginDto
     * @param token
     * @return
     */
    UserLoginVo login(UserLoginDto userLoginDto);

    /**
     * update password
     * @param updatePasswordDto
     */
    void updatePassword(UpdatePasswordDto updatePasswordDto);

    /**
     * send verify code for updating password
     */
    void sendCodeForPassword(String email);

    /**
     * get User By Email
     * @param email
     * @return
     */
    User getUserByEmail(String email);

    /**
     * send Email For Register Success
     */
    void sendEmailForRegisterSuccess(String userId,String email);

    boolean checkUuid(String uuid);

    /**
     * send Verify Code for signing up
     */
    void sendVerifyCode(String email);


    /**
     * forget password
     * @param forgetPasswordDto
     */
    void forgetPassword(ForgetPasswordDto forgetPasswordDto);

    /**
     * Invite colleagues to join
     * @param email
     */
    void invite(String email);


    /**
     * Register For Invited
     * @param userDto
     */
    void registerForInvited(UserDto userDto, String encode);

    /**
     * add Admin
     * @param userId
     */
    void addAdmin(Long userId);

    /**
     * remove Admin
     * @param userId
     */
    void removeAdmin(Long userId);

    /**
     * remove User
     * @param userId
     */
    void removeUser(Long userId);

    /**
     * send email for reset
     * @param email
     */
    void sendEmailForResetPassword(String email);

    UserLoginVo islogin(String token);
}

