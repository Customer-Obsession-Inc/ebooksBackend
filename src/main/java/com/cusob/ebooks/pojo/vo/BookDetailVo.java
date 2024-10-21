package com.cusob.ebooks.pojo.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BookDetailVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String author;
    private Integer price;
    private String resourceurl;

    private String coverurl;

    private String description;
}
