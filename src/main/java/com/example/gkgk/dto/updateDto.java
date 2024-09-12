package com.example.gkgk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class updateDto {
    private int id;
    private String table_name;
    private int table_uid;
    private String update_content;
}
