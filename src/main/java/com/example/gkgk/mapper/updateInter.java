package com.example.gkgk.mapper;

import com.example.gkgk.dto.updateDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface updateInter {
    void insert(String table_name);
    List<updateDto> fileList();
    void updateFile(Map<String, Object> map);
    void updateContent(Map<String, Object> map);
    void deleteFile(int id);
}
