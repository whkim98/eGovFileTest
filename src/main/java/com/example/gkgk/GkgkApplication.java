package com.example.gkgk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@MapperScan("com.example.gkgk.mapper") // 매퍼 인터페이스가 위치한 패키지
public class GkgkApplication {

    public static void main(String[] args) {
        SpringApplication.run(GkgkApplication.class, args);
    }

}
