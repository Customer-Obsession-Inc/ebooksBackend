package com.cusob.ebooks.config;


import com.cusob.ebooks.pojo.Minio;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

@Configuration
public class MinioConfig {

    @Autowired
    private Minio minio;

    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(minio.getUrl())
                //传入用户名和密码
                .credentials(minio.getUsername(), minio.getPassword())
                .build();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(10)); // 设定最大文件大小
        factory.setMaxRequestSize(DataSize.ofMegabytes(10)); // 设定最大请求大小
        return factory.createMultipartConfig();
    }

}
