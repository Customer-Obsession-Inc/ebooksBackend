package com.cusob.ebooks.pojo.DTO;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

@Data
public class UserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String firstName;

    private String lastName;

    private String password;

    private String nickname;

    private String email;


    private String turnstileToken;

    private String phoneNumber;

    private String country;

    private Date birthday;

    // 将 avatar 改为 MultipartFile
    private MultipartFile avatar;

}
