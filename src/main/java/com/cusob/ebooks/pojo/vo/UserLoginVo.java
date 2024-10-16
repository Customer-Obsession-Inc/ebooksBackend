package com.cusob.ebooks.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class UserLoginVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String firstName;

    private String lastName;

    private String avatar;

    private String nickName;

    private String token;
}
