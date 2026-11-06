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
        File dir = new File(uploadPath).getAbsoluteFile();

        // 启动时就建好目录：目录不存在时 toURI() 不带结尾斜杠，
        // Spring 会当成「文件」而不是「目录」解析，导致 /image/** 全部 404。
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("无法创建上传目录：" + dir);
        }

        String location = dir.toURI().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/image/**").addResourceLocations(location);
    }
}
