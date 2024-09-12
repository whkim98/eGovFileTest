package com.example.gkgk.service;

import com.example.gkgk.dto.updateDto;
import com.example.gkgk.mapper.updateInter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class updateService {
    @Autowired
    private updateInter updateInter;

    public void insert(String table_name) {
        updateInter.insert(table_name);
    }

    public List<updateDto> fileList(){
        return updateInter.fileList();
    }

    public void updateFile(Map<String, Object> map) {
        updateInter.updateFile(map);
    }

    public void updateContent(Map<String, Object> map){
        updateInter.updateContent(map);
    }

    public void deleteFile(int id){
        updateInter.deleteFile(id);
    }
}
