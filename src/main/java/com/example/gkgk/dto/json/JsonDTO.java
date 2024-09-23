package com.example.gkgk.dto.json;

import java.util.List;

public class JsonDTO {
    private List<String> fileNames;

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
