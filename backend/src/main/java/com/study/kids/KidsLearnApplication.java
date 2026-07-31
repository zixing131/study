package com.study.kids;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.study.kids.dao")
public class KidsLearnApplication {

    public static void main(String[] args) {
        SpringApplication.run(KidsLearnApplication.class, args);
    }
}
