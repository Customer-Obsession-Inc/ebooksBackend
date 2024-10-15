package com.cusob.ebooks.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Email implements Serializable {

    private String email;

    private String subject;

    private String content;
}
