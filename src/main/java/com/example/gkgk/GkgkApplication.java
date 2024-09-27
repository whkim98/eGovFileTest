package com.example.gkgk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;

//@MapperScan("com.example.gkgk.mapper") // 매퍼 인터페이스가 위치한 패키지
@SpringBootApplication
@EnableCaching
public class GkgkApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(GkgkApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(GkgkApplication.class, args);
    }

}
