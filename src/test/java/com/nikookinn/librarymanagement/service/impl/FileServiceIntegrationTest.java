package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileServiceIntegrationTest {

    private FileServiceImpl fileService;
    private final String testDir = "test-uploads";

    @BeforeEach
    void setUp() throws IOException {
        fileService = new FileServiceImpl(testDir);
    }

    @AfterEach
    void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(Paths.get(testDir));
    }

    @Test
    @DisplayName("should save and load file successfully")
    void shouldSaveAndLoadFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content".getBytes());

        String savedName = fileService.saveFile(file, "covers");
        
        assertThat(savedName).endsWith(".jpg");
        byte[] loaded = fileService.loadFile(savedName, "covers");
        assertThat(loaded).isEqualTo("content".getBytes());
    }

    @Test
    @DisplayName("should throw exception when saving empty file")
    void shouldThrowExceptionOnEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> fileService.saveFile(file, "covers"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Failed to store empty file.");
    }

    @Test
    @DisplayName("should throw exception for malicious subdirectory")
    void shouldThrowExceptionForMaliciousPath() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content".getBytes());

        assertThatThrownBy(() -> fileService.saveFile(file, "../malicious"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Cannot store file outside current directory.");
    }

    @Test
    @DisplayName("should delete file successfully")
    void shouldDeleteFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content".getBytes());
        String savedName = fileService.saveFile(file, "covers");

        fileService.deleteFile(savedName, "covers");

        assertThatThrownBy(() -> fileService.loadFile(savedName, "covers"))
                .isInstanceOf(com.nikookinn.librarymanagement.exception.ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should not throw error when deleting non-existent file")
    void shouldNotThrowOnDeletingNonExistent() {
        fileService.deleteFile("non-existent.jpg", "covers");
        // No exception means success
    }
}
