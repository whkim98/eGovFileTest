package com.example.gkgk.controller;

import com.example.gkgk.ftp.ftpClientUtil;
import com.example.gkgk.service.itemService;
import com.example.gkgk.service.updateService;
//import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class updateController {

    @Autowired
    private updateService updateService;

    @Autowired
    private itemService itemService;

    //원래는 해당 정보(ex. 게시판)에 해당하는 첨부파일을 해서 pk, fk 관계를 통해 해당 정보에 해당하는 파일만 보이도록 하는
    //로직이 필요하지만 지금은 제외함. 어떤 식으로 진행될 지 모르기 때문
    @PostMapping("/file/update")
    public String update(@RequestParam("myfiles") MultipartFile[] myfiles) {
        ftpClientUtil ftp = null;
        try {
            ftp = new ftpClientUtil("localhost", 21, "whftp", "1234");

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
                        } else {
                            System.out.println("FTP 파일 업로드 실패: " + testFile);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
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
        return "redirect:/";
    }




    @PostMapping("/file/updatee")
    public String updatee(@RequestParam("myfile") MultipartFile myfile,
                          @RequestParam("previous_filename") String previousFilename,
                          HttpServletRequest request, @RequestParam("id") int id) {

        String uploadPath = request.getSession().getServletContext().getRealPath("/resources/ftpFile");
        String newFileName = myfile.getOriginalFilename();

        // 이전 파일 삭제(이전 파일의 삭제 시기는 수정 가능함) => 수정한다고 바로 삭제되는게 아닐거임 아마도
        try{
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
        }catch (Exception e){
            e.printStackTrace();
        }


        // 새 파일 저장
//        try {
//            File newFile = new File(uploadPath + File.separator + newFileName);
//            myfile.transferTo(newFile);
//            try (InputStream inputStream = new FileInputStream(newFile)) {
//                // FTP 서버에 파일 업로드
//                ftpClientUtil ftp = new ftpClientUtil("localhost", 21, "whftp", "1234");
//                boolean uploadResult = ftp.uploadFile(inputStream, "D:/whftp/" + newFileName);
//                if (uploadResult) {
//                    System.out.println("FTP 파일 업로드 성공: " + newFileName);
//                } else {
//                    System.out.println("FTP 파일 업로드 실패: " + newFileName);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.println("새 파일 저장 성공: " + newFileName);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("파일 저장 실패");
//        }

        //새 파일 저장
        ftpClientUtil ftp = null;
        try{
            ftp = new ftpClientUtil("localhost", 21, "whftp", "1234");
            if(!myfile.isEmpty()){
                try(InputStream inputStream = myfile.getInputStream()){
                    boolean uploadResult = ftp.uploadFile(inputStream, "D:/whftp/" + newFileName);
                    if(uploadResult){
                        System.out.println("FTP 새 파일 저장 성공: " + newFileName);
                    }else{
                        System.out.println("FTP 새 파일 저장 실패: " + newFileName);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                if(ftp != null){
                    ftp.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // 업데이트 테이블 업데이트
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("table_name", newFileName);
        updateService.updateFile(map);

        // 아이템 인설트
        Map<String, Object> map1 = new HashMap<>();
        map1.put("id", id);
        map1.put("field_name", newFileName);
        map1.put("from_value", previousFilename);
        itemService.insert(map1);

        // 가장 최근 insert된 아이템 테이블의 PK 데이터
        int recentiid = itemService.recentiid();

        // 기존 JSON 데이터 읽기
        File jsonFile = new File(uploadPath + File.separator + "update_content_" + id + ".json");
        JSONObject updateContentJson = new JSONObject();

        if (jsonFile.exists()) {
            try {
                // JSON 파일에서 기존 데이터 읽어오기
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

        // update 테이블 content 업데이트
        //단, 실제 비즈니스 로직에선 이렇게 하면 안 됨(같은 파일명이 insert된다던가 동시에 insert처리가 되면 잘못된 파일의 일련번호(pk)가 입력될 수 있음
        //UUID를 거친다던가 bcrypyto 암호화 및 복호화 패키지를 사용한다던가 하는 과정이 필요로 함
        /*UUID 복호화(?) 과정
        * 1. 파일 저장 시 실제 파일명과 UUID명을 동시에 DB저장
        * 2. 첨부파일 리스트 출력시에는 실제 파일명 출력
        * 3. 다운로드 로직 상에 실제 파일명과 그에 해당하는 pk값을 조건에 넣어 일치하는 UUID파일명의 파일을 다운로드 시킴
        * 4. 수정도 마찬가지
        *
        * 암호화 패키지 이용시에도 비슷하게 로직 구현하면 됨
        * */
        Map<String, Object> map2 = new HashMap<>();
        map2.put("id", id);
        map2.put("recentiid", recentiid);
        map2.put("update_content", updateContentJson.toString());
        updateService.updateContent(map2);


        // JSON 파일로 저장
        try {
            if (!jsonFile.exists()) {
                jsonFile.createNewFile();
            }

            // JSON 데이터를 파일로 저장
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
