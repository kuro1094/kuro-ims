package com.kuro.ims;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.kuro.ims.mapper")
public class KuroImsApplication {

    public static void main(String[] args) {
        SpringApplication.run(KuroImsApplication.class, args);
    }
}
