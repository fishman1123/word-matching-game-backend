package com.wordsystem.newworldbridge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wordsystem.newworldbridge.model.dao")
public class NewworldbridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewworldbridgeApplication.class, args);
    }

}
