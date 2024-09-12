package com.example.gkgk.controller;

import com.example.gkgk.dto.itemDto;
import com.example.gkgk.service.itemService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
public class DetailController {

    @Autowired
    private itemService itemService;

    @GetMapping("/file/detail")
    @ResponseBody
    public List<itemDto> detail(int id) {
        List<itemDto> item = itemService.selectDetail(id);
//        model.addAttribute("item", item);

        return item;
    }

}
