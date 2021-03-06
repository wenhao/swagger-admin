package com.gitee.swaggeradmin.common;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * 文件上传工具类
 *
 * @author tanghc
 */
public class UploadUtil {

    /**
     * 获取上传文件
     *
     * @param request
     * @return
     */
    public static Collection<MultipartFile> getUploadFiles(HttpServletRequest request) {
        Map<String, MultipartFile> fileMap = null;
        //检查form中是否有enctype="multipart/form-data"
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("multipart")) {
            //将request变成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            fileMap = multiRequest.getFileMap();
        }
        return Optional.ofNullable(fileMap)
                .map(Map::values)
                .orElse(Collections.emptyList());
    }
}
