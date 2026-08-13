package com.nikookinn.librarymanagement.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String saveFile(MultipartFile file, String subDirectory);
    byte[] loadFile(String fileName, String subDirectory);
    void deleteFile(String fileName, String subDirectory);
}
