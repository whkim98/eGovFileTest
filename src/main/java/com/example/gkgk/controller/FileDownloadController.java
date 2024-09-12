package com.example.gkgk.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
public class FileDownloadController {

    @GetMapping("/file/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("filename") String filename) {

        String filePath = "C:/Users/USER/Desktop/김우형/gkgk/src/main/webapp/resources/file/" + filename;
        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        // 파일 리소스 생성
        Resource resource = new FileSystemResource(file);

        // HTTP 헤더 설정 (파일 다운로드를 위한 헤더)
        HttpHeaders headers = new HttpHeaders();
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);

        System.out.println(encodedFilename.hashCode());

        // 파일을 ResponseEntity로 반환
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
