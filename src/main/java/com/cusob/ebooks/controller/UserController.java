package com.cusob.ebooks.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cusob.ebooks.pojo.DTO.ForgetPasswordDto;
import com.cusob.ebooks.pojo.DTO.UpdatePasswordDto;
import com.cusob.ebooks.pojo.DTO.UserDto;
import com.cusob.ebooks.pojo.DTO.UserLoginDto;
import com.cusob.ebooks.pojo.User;
import com.cusob.ebooks.pojo.vo.UserLoginVo;
import com.cusob.ebooks.pojo.vo.UserVo;
import com.cusob.ebooks.result.Result;
import com.cusob.ebooks.service.UserService;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserService userService;

    //    @ApiOperation("add User(User Register)")
//    @GetMapping("get")
//    public Result getUser(){
//        Long userId = AuthContext.getUserId();
//
//        return Result.ok(userId);
//    }
    @ApiOperation("add User(User Register)")
    @PostMapping("register")
    public Result addUser(@RequestBody UserDto userDto){
        userService.addUser(userDto);
        return Result.ok();
    }

    @ApiOperation("add User(User Register)")
    @GetMapping("checkUuid/{uuid}")
    public Result checkUuid(@PathVariable String uuid){
        boolean status = userService.checkUuid(uuid);
        if(status){
            return Result.ok();
        }
        return Result.fail("Link timed out");
    }

    @ApiOperation("Invite colleagues to join")
    @PostMapping("invite")
    public Result invite(String email){
        userService.invite(email);
        return Result.ok();
    }

    @ApiOperation("Register For Invited")
    @PostMapping("registerForInvited")
    public Result RegisterForInvited(@RequestBody UserDto userDto, String encode){
        userService.registerForInvited(userDto, encode);
        return Result.ok();
    }

    @ApiOperation("get UserInfo by id")
    @GetMapping("getUserInfo")
    public Result getUserInfo(){
        UserVo userVo = userService.getUserInfo();
        return Result.ok(userVo);
    }

    @ApiOperation("update UserInfo")
    @PutMapping("update")
    public Result updateUserInfo(@RequestBody UserDto userDto){
        userService.updateUserInfo(userDto);
        return Result.ok();
    }

    @ApiOperation("get UserList")
    @GetMapping("getUserList/{page}/{limit}")
    public Result getUserList(@PathVariable Long page,
                              @PathVariable Long limit){
        Page<User> pageParam = new Page<>(page, limit);
        IPage<User> pageModel = userService.getUserList(pageParam);
        return Result.ok(pageModel);
    }

    @ApiOperation("update Password")
    @PostMapping("updatePassword")
    public Result updatePassword(@RequestBody UpdatePasswordDto updatePasswordDto){
        userService.updatePassword(updatePasswordDto);
        return Result.ok();
    }

    /**
     * user login
     * @param userLoginDto
     * @return
     */
    @PostMapping("login")
    @ApiOperation(value = "user login")
    public Result login(@RequestBody UserLoginDto userLoginDto) {

        UserLoginVo userLoginVo = userService.login(userLoginDto);
        return Result.ok(userLoginVo);
    }

    /**
     * user islogin
     * @param
     * @return
     */
    @GetMapping("islogin")
    @ApiOperation(value = "user islogin")
    public Result islogin(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return Result.ok("Token is missing");
        }

        UserLoginVo islogin = userService.islogin(token);
        if (islogin == null) {
            return Result.ok("Invalid token");
        }

        return Result.ok(islogin);
    }




//    @ApiOperation("upload Avatar")
//    @PostMapping("uploadAvatar")
//    public Result uploadAvatar(@RequestPart("file") MultipartFile file){
//        String url = minioService.uploadAvatar(minio.getBucketName(), file);
//        return Result.ok(url);
//    }

    @ApiOperation("send verify code for updating password")
    @PostMapping("sendCodeForPassword")
    public Result sendCodeForPassword(String email){
        userService.sendCodeForPassword(email);
        return Result.ok();
    }

    @ApiOperation("send email for reset password")
    @PostMapping("sendEmailForResetPassword")
    public Result sendEmailForPassword(String email){
        userService.sendEmailForResetPassword(email);
        return Result.ok();
    }

    @ApiOperation("forget password")
    @PostMapping("forgetPassword")
    public Result forgetPassword(@RequestParam Map<String, String> params){
        ForgetPasswordDto forgetPasswordDto = new ForgetPasswordDto();
        forgetPasswordDto.setEmail(params.get("email"));
        forgetPasswordDto.setPassword(params.get("password"));

        userService.forgetPassword(forgetPasswordDto);
        return Result.ok();
    }
    @ApiOperation("send verify code for signing up")
    @PostMapping("sendVerifyCode")
    public Result sendVerifyCode(String email){
        userService.sendVerifyCode(email);
        return Result.ok();
    }


    @ApiOperation("add Admin")
    @PostMapping("addAdmin")
    public Result addAdmin(Long userId){
        userService.addAdmin(userId);
        return Result.ok();
    }

    @ApiOperation("remove Admin")
    @PostMapping("removeAdmin")
    public Result removeAdmin(Long userId){
        userService.removeAdmin(userId);
        return Result.ok();
    }

    @ApiOperation("remove User")
    @PostMapping("removeUser")
    public Result removeUser(Long userId){
        userService.removeUser(userId);
        return Result.ok();
    }
}
