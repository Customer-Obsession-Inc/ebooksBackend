package com.cusob.ebooks.pojo.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String country;

    private Integer permission;

    private String nickname;
}
