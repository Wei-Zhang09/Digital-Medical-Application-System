package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 图片上传：药品图片落到运行目录下的 uploads/，再由 /image/** 静态映射对外提供。
 *
 * <p>只接受 jpg/jpeg/png/gif，大小由 application.properties 的 multipart 上限（1MB）兜住。</p>
 */
@Api(tags = "文件上传")
@RestController
public class UploadController {

    private static final List<String> ALLOWED = Arrays.asList("jpg", "jpeg", "png", "gif");

    /** 上传目录（相对 jar 工作目录） */
    @Value("${upload.path:uploads}")
    private String uploadPath;

    @ApiOperation("上传药品图片，返回 /image/xxx")
    @PostMapping("/api/upload")
    @RolesAllowed({"ROLE_1", "ROLE_2"})
    public Msg upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Msg.fail("请选择要上传的图片");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED.contains(ext)) {
            return Msg.fail("只支持 jpg/jpeg/png/gif 格式");
        }

        File dir = new File(uploadPath).getAbsoluteFile();
        if (!dir.exists() && !dir.mkdirs()) {
            return Msg.fail("上传目录创建失败：" + dir);
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        file.transferTo(new File(dir, fileName));
        return Msg.success("上传成功").data("url", "/image/" + fileName);
    }
}
