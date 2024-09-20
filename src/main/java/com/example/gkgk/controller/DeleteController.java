package com.example.gkgk.controller;

import com.example.gkgk.ftp.ftpClientUtil;
import com.example.gkgk.service.itemService;
import com.example.gkgk.service.updateService;
//import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class DeleteController {
    @Autowired
    private updateService updateService;

    @Autowired
    private itemService itemService;

    @GetMapping("/file/delete")
    public String delete(int id, @RequestParam String file_name,
                         HttpServletRequest request) {

        String uploadPath = request.getSession().getServletContext().getRealPath("/resources/ftpFile");

        File file = new File(uploadPath + File.separator + file_name);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("파일 삭제 성공: " + file_name);
            } else {
                System.out.println("파일 삭제 실패: " + file_name);
            }
        }

        try{
            ftpClientUtil ftp = new ftpClientUtil("localhost", 21, "whftp", "1234");
            File oldFile = new File(uploadPath + File.separator + file_name);
            if (oldFile.exists()) {
                if (oldFile.delete()) {
                    System.out.println("이전 파일 삭제 성공: " + file_name);
                } else {
                    System.out.println("이전 파일 삭제 실패: " + file_name);
                }
            }
            String remoteFilePath = URLEncoder.encode(("D:/whftp/" + file_name), StandardCharsets.UTF_8);
            ftp.deleteFile(remoteFilePath);
        }catch (Exception e){
            e.printStackTrace();
        }

        itemService.deleteItem(id);
        updateService.deleteFile(id);

        return "redirect:/";
    }
}
