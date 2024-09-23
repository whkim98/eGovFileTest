package com.example.gkgk.dto.json;

import java.util.List;

public class MyJsonDTO {
    private List<String> fileNames;

    // Getter와 Setter
    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    @Override
    public String toString() {
        return "MyJsonDTO{" +
                "fileNames=" + fileNames +
                '}';
    }
}
