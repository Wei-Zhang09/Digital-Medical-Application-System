package com.qst.medical;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 慧医数字医疗应用系统 启动类
 */
@SpringBootApplication
@MapperScan("com.qst.medical.mapper")
@EnableSwagger2
public class MedicalApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicalApplication.class, args);
    }
}
