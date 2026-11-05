package com.qst.medical.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 把 /image/** 映射到上传目录，药品图片才能被前端直接引用。
 *
 * <p>单独成类，避免改动素材里给的 {@code WebConfig}。</p>
 */
@Configuration
public class ImageResourceConfig implements WebMvcConfigurer {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = new File(uploadPath).getAbsoluteFile().toURI().toString();
        registry.addResourceHandler("/image/**").addResourceLocations(location);
    }
}
