package com.cusob.ebooks.pojo.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

    @Data
    public class BookDto implements Serializable {

        private static final long serialVersionUID = 1L;

        private String name;

        private Integer price;

        private String updater;

        private String resourceurl;

        private MultipartFile file;

        private String coverurl;

        private Integer type;

        private String description;

        private String[] category;


    }

