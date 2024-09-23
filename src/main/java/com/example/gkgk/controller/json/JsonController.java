package com.example.gkgk.controller.json;

import com.example.gkgk.dto.json.MyJsonDTO;
import com.example.gkgk.dto.json.UpdateRequest;
import com.example.gkgk.dto.updateDto;
import com.example.gkgk.ftp.ftpClientUtil;
import com.example.gkgk.service.itemService;
import com.example.gkgk.service.updateService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class JsonController {

    @Autowired
    updateService updateService;

    @Autowired
    itemService itemService;


    @GetMapping("json")
    public String json(Model model) {
        List<updateDto> list = updateService.fileList();
        model.addAttribute("list", list);
        return "home/JSONTest";
    }

    @PostMapping("/json/file")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(
            @RequestPart("jsonData") MyJsonDTO jsonData,  // JSON 데이터를 DTO로 받음
            @RequestParam("myfiles") MultipartFile[] myfiles) {

        System.out.println(myfiles);
        ftpClientUtil ftp = null;
        Map<String, Object> response = new HashMap<>();
        System.out.println(jsonData);
        try {
            ftp = new ftpClientUtil("localhost", 21, "whftp", "1234");

            // 받은 JSON 데이터를 처리
            System.out.println("받은 JSON 데이터: " + jsonData);

            List<String> uploadedFiles = new ArrayList<>();
            for (MultipartFile myfile : myfiles) {
                if (!myfile.isEmpty()) {
                    // 파일 이름 가져오기
                    String testFile = myfile.getOriginalFilename();
                    try (InputStream inputStream = myfile.getInputStream()) {
                        // FTP 서버에 파일 업로드
                        boolean uploadResult = ftp.uploadFile(inputStream, "D:/whftp/" + testFile);
                        if (uploadResult) {
                            System.out.println("FTP 파일 업로드 성공: " + testFile);
                            updateService.insert(testFile);
                            uploadedFiles.add(testFile);
                        } else {
                            System.out.println("FTP 파일 업로드 실패: " + testFile);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            response.put("status", "success");
            response.put("uploadedFiles", uploadedFiles); // 성공한 파일 목록 추가
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        } finally {
            try {
                if (ftp != null) {
                    ftp.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/json/updatee")
    @ResponseBody
    public String updatee(@RequestBody UpdateRequest updateRequest, HttpServletRequest request) {
        String uploadPath = request.getSession().getServletContext().getRealPath("/resources/ftpFile");
        String previousFilename = updateRequest.getPreviousFilename();
        String newFileName = updateRequest.getFileNames().get(0); // 첫 번째 파일명 사용

        System.out.println("json업데이트 왔나?");

        // 이전 파일 삭제
        try {
            ftpClientUtil ftp = new ftpClientUtil("localhost", 21, "whftp", "1234");
            File oldFile = new File(uploadPath + File.separator + previousFilename);
            if (oldFile.exists()) {
                if (oldFile.delete()) {
                    System.out.println("이전 파일 삭제 성공: " + previousFilename);
                } else {
                    System.out.println("이전 파일 삭제 실패: " + previousFilename);
                }
            }
            String remoteFilePath = URLEncoder.encode(("D:/whftp/" + previousFilename), StandardCharsets.UTF_8);
            ftp.deleteFile(remoteFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 새 파일 저장
        ftpClientUtil ftp = null;
        try {
            ftp = new ftpClientUtil("localhost", 21, "whftp", "1234");
            if (!updateRequest.getFiles().isEmpty()) {
                try (InputStream inputStream = updateRequest.getFiles().get(0).getInputStream()) {
                    boolean uploadResult = ftp.uploadFile(inputStream, "D:/whftp/" + newFileName);
                    if (uploadResult) {
                        System.out.println("FTP 새 파일 저장 성공: " + newFileName);
                    } else {
                        System.out.println("FTP 새 파일 저장 실패: " + newFileName);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ftp != null) {
                    ftp.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 업데이트 테이블 업데이트
        Map<String, Object> map = new HashMap<>();
        map.put("id", updateRequest.getId());
        map.put("table_name", newFileName);
        updateService.updateFile(map);

        // 아이템 인설트
        Map<String, Object> map1 = new HashMap<>();
        map1.put("id", updateRequest.getId());
        map1.put("field_name", newFileName);
        map1.put("from_value", previousFilename);
        itemService.insert(map1);

        // 가장 최근 insert된 아이템 테이블의 PK 데이터
        int recentiid = itemService.recentiid();

        // 기존 JSON 데이터 읽기
        File jsonFile = new File(uploadPath + File.separator + "update_content_" + updateRequest.getId() + ".json");
        JSONObject updateContentJson = new JSONObject();

        if (jsonFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8);
                updateContentJson = new JSONObject(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 새 데이터 추가
        JSONArray valueArray = new JSONArray();
        valueArray.put(previousFilename);
        valueArray.put(newFileName);
        updateContentJson.append("title", valueArray); // 기존 데이터에 추가

        // 업데이트 테이블 content 업데이트
        Map<String, Object> map2 = new HashMap<>();
        map2.put("id", updateRequest.getId());
        map2.put("recentiid", recentiid);
        map2.put("update_content", updateContentJson.toString());
        updateService.updateContent(map2);

        // JSON 파일로 저장
        try {
            if (!jsonFile.exists()) {
                jsonFile.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(jsonFile);
            fileWriter.write(updateContentJson.toString(4));
            fileWriter.flush();
            fileWriter.close();

            System.out.println("JSON 파일 저장 성공: " + jsonFile.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("JSON 파일 저장 실패");
        }

        return "redirect:/";
    }
}
