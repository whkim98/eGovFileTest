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

    @GetMapping("/crawling")
    public String crawling() {

        String URL = "https://finance.naver.com/item/main.nhn?code=005930";
        try{
            Document doc = Jsoup.connect(URL).get();
            Elements elem = doc.select(".date");
            String[] str = elem.text().split(" ");

            Elements todaylist =doc.select(".new_totalinfo dl>dd");

            String juga = todaylist.get(3).text().split(" ")[1];
            String DungRakrate = todaylist.get(3).text().split(" ")[6];
            String siga =  todaylist.get(5).text().split(" ")[1];
            String goga = todaylist.get(6).text().split(" ")[1];
            String zeoga = todaylist.get(8).text().split(" ")[1];
            String georaeryang = todaylist.get(10).text().split(" ")[1];

            String stype = todaylist.get(3).text().split(" ")[3];

            String vsyesterday = todaylist.get(3).text().split(" ")[4];

            System.out.println("주가------------------");
            System.out.println("주가:"+juga);
            System.out.println("등락률:"+DungRakrate);
            System.out.println("시가:"+siga);
            System.out.println("고가:"+goga);
            System.out.println("저가:"+zeoga);
            System.out.println("거래량:"+georaeryang);
            System.out.println("타입:"+stype);
            System.out.println("전일대비:"+vsyesterday);
            System.out.println("가져오는 시간:"+str[0]+str[1]);

        } catch (IOException e) {

        }
        return "";
    }


}
