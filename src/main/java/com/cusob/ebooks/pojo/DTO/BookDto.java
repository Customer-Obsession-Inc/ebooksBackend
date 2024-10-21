package com.cusob.ebooks.pojo.DTO;

import lombok.Data;

import java.io.Serializable;

    @Data
    public class BookDto implements Serializable {

        private static final long serialVersionUID = 1L;

        private String name;

        private String price;
        private String author;

        private String resourceurl;

        private String coverurl;

        private Integer type;


    }

