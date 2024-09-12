package com.example.gkgk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping("/test/insert")
    public String test(){

        //file insert시 사용할 메소드 매핑
        //fk는 sequence로 생긴 pk를 다시 가져옴 (mysql의 last_insert_id와 같은 기능)
        //등록일시는 now()인가 아니면 따로 지정이 가능한가? (오라클은 sysdate, now()는 mysql)
        //file_sn(파일 각각의 일련번호)은 랜덤생성? 아니면 정해진 규약 or 규칙이 있는가
        //문서관리를 통한 이력관리가 필요하다고 나와있는데 이건 파일이 바뀌면 이전 파일에 대한 기록이 필요하다는 뜻인가?
        //다른 첨부파일과는 다르게 계약관련 첨부파일만 계약서_파일_구분이 있음. 용도는? (이력관리를 위한 구분? ex. 1은 이전 이력, 2는 현재 첨부파일)



        return "";

    }

    @GetMapping("/jsTest")
    public String jsTest(){
        return "home/jsTest";
    }

}
