package com.jk.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserBean implements Serializable {

    private Integer userId;

    private String birthday;

    private String gender;

    private String userName;

    private Integer powerType;

    private String typeName;
}
