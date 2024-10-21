package com.cusob.ebooks.pojo.DTO;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class UserUpdateDto {
    private String firstName;
    private String lastName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;
    private String phoneNumber;
    private String country;
    private String email;
    private String nickname;

    // Getters 和 Setters
}
