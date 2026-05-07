package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

// 通用接口
@RestController
@RequestMapping("admin/common")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @RequestMapping("upload")
    public Result<String> upload (MultipartFile file){
        log.info("文件上传：{}",file);

        try {
            //获取原始文件名
            String originalFilename = file.getOriginalFilename();
            String objectName = null;
            if (originalFilename != null) {
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                objectName = UUID.randomUUID() + extension;
            }
            //获取图片文件路径
            String filePath=aliOssUtil.upload(file.getBytes(),objectName);
            log.info("文件上传成功：{}",filePath);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败：{}",e.getMessage());
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
