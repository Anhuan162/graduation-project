package com.graduation.project.common.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileStorageService {
    String store(MultipartFile file, String subDir);

    String store(File file, String subDir);
}
