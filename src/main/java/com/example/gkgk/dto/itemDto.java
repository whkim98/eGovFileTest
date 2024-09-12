package com.example.gkgk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class itemDto {
    private int iid;
    private int table_update_uid;
    private String field_name;
    private String from_value;
}
