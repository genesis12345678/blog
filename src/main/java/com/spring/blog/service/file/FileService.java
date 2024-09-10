package com.spring.blog.service.file;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    List<String> ALLOW_FILE_TYPES = List.of(
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/webp"
    );

    /**
     * @param file 저장할 파일
     * @param dir 파일이 저장될 디렉터리
     * @return 저장된 파일의 경로
     */
    String saveFile(MultipartFile file, String dir);

    void deleteFile(String fileName);

    String generatedPreSignedUrl(String filename);

    @Async
    void cleanupFiles(String sessionId);

    default void validImageFile(MultipartFile file){
        if (!ALLOW_FILE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("지원되지 않는 이미지 파일 입니다. " + file.getContentType());
        }
    }
}