package com.cusob.ebooks;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
@MapperScan("com.cusob.ebooks.mapper")
@EnableAsync
public class EbooksApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbooksApplication.class, args);
    }

}
