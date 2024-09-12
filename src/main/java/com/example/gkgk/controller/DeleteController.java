package com.example.gkgk.controller;

import com.example.gkgk.service.itemService;
import com.example.gkgk.service.updateService;
//import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

@Controller
public class DeleteController {
    @Autowired
    private updateService updateService;

    @Autowired
    private itemService itemService;

    @GetMapping("/file/delete")
    public String delete(int id, @RequestParam String file_name,
                         HttpServletRequest request) {

        String uploadPath = request.getSession().getServletContext().getRealPath("/resources/file");

        File file = new File(uploadPath + File.separator + file_name);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("파일 삭제 성공: " + file_name);
            } else {
                System.out.println("파일 삭제 실패: " + file_name);
            }
        }

        itemService.deleteItem(id);
        updateService.deleteFile(id);

        return "redirect:/";
    }
}
