package com.cusob.ebooks.pojo;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;


@Data
@TableName("users")
public class User extends BaseEntity  {

    public static final Integer USER = 0;
    public static final Integer ADMIN = 1;
    public static final Integer SUPER_ADMIN = 2;

    public static final Integer DISABLE = 0;
    public static final Integer AVAILABLE = 1;

    @TableField("first_name")
    private String firstName;

    @TableField("last_name")
    private String lastName;


    @TableField("password")
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    @TableField("phone_number")
    private String phone;

    @TableField("country")
    private String country;

    @TableField("email")
    private String email;

    @TableField("avatar")
    private String avatar;

    @TableField("permission")
    private Integer permission;

    @TableField("is_available")
    private Integer isAvailable;

    @TableField("nickname")
    private String nickname;

}
