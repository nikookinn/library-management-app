package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.exception.BusinessRuleViolationException;
import com.nikookinn.librarymanagement.exception.ResourceNotFoundException;
import com.nikookinn.librarymanagement.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final Path rootLocation;

    public FileServiceImpl(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String saveFile(MultipartFile file, String subDirectory) {
        if (file.isEmpty()) {
            throw new BusinessRuleViolationException("Failed to store empty file.");
        }

        try {
            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String extension = "";
            int i = originalFilename.lastIndexOf('.');
            if (i > 0) {
                extension = originalFilename.substring(i);
            }
            
            String fileName = UUID.randomUUID().toString() + extension;
            Path destinationDirectory = this.rootLocation.resolve(subDirectory).normalize();
            
            if (!destinationDirectory.startsWith(this.rootLocation)) {
                throw new BusinessRuleViolationException("Cannot store file outside current directory.");
            }

            Files.createDirectories(destinationDirectory);

            Path destinationFile = destinationDirectory.resolve(Paths.get(fileName))
                    .normalize();

            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public byte[] loadFile(String fileName, String subDirectory) {
        try {
            Path filePath = rootLocation.resolve(subDirectory).resolve(fileName).normalize();
            if (!filePath.startsWith(rootLocation)) {
                throw new BusinessRuleViolationException("Cannot access file outside root directory.");
            }
            
            if (Files.exists(filePath) && Files.isReadable(filePath)) {
                return Files.readAllBytes(filePath);
            } else {
                throw new ResourceNotFoundException("Could not read file: " + fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + fileName, e);
        }
    }

    @Override
    public void deleteFile(String fileName, String subDirectory) {
        try {
            Path filePath = rootLocation.resolve(subDirectory).resolve(fileName).normalize();
            if (!filePath.startsWith(rootLocation)) {
                throw new BusinessRuleViolationException("Cannot access file outside root directory.");
            }
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + fileName, e);
        }
    }
}
