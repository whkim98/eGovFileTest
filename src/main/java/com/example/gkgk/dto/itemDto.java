package com.example.gkgk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class itemDto implements Serializable {
    private int iid;
    private int table_update_uid;
    private String field_name;
    private String from_value;
}
