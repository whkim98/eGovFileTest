package com.example.gkgk.dto.json;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRequest {
    private int id;
    private String previousFilename;
    private List<MultipartFile> files;
    private List<String> fileNames;

    // Getters and Setters
}