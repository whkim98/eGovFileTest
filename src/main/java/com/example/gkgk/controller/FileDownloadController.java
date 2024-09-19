package com.example.gkgk.controller;

import com.example.gkgk.ftp.ftpClientUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
public class FileDownloadController {

    //로컬경로 파일 다운로드 예시
//    @GetMapping("/file/download")
//    public ResponseEntity<Resource> downloadFile(@RequestParam("filename") String filename) {
//
//        String filePath = "C:/Users/USER/Desktop/김우형/gkgk/src/main/webapp/resources/file/" + filename;
//        File file = new File(filePath);
//
//        if (!file.exists()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        // 파일 리소스 생성
//        Resource resource = new FileSystemResource(file);
//
//        // HTTP 헤더 설정 (파일 다운로드를 위한 헤더)
//        HttpHeaders headers = new HttpHeaders();
//        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
//        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);
//
//        System.out.println(encodedFilename.hashCode());
//
//        // 파일을 ResponseEntity로 반환
//        return ResponseEntity.ok()
//                .headers(headers)
//                .body(resource);
//    }

    // FTP 서버 파일 다운로드
    @GetMapping("/file/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("filename") String filename) {
        ftpClientUtil ftp = null;
        try {
            // FTP 클라이언트 객체 초기화
            ftp = new ftpClientUtil("localhost", 21, "whftp", "1234");

            // URL 인코딩된 파일 이름을 디코딩
            String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8);
            String remoteFilePath = "D:/whftp/" + decodedFilename;
            String localFilePath = "C:/Users/USER/Desktop/김우형/gkgk/src/main/webapp/resources/ftpFile/" + decodedFilename;

            // 로컬 파일 저장 경로 생성
            File localFile = new File(localFilePath);

            // 로컬 파일이 존재하지 않으면 다운로드

            boolean downloadResult = ftp.downloadFile(localFilePath, remoteFilePath);

            // 로컬 파일 리소스 생성
            Resource resource = new FileSystemResource(localFile);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            String encodedFilename = URLEncoder.encode(decodedFilename, StandardCharsets.UTF_8).replace("+", "%20");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);

            // 파일을 ResponseEntity로 반환
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            try {
                if (ftp != null) {
                    ftp.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
