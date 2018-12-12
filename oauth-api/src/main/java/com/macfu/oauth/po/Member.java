package com.macfu.oauth.po;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: liming
 * @Date: 2018/12/12 14:37
 * @Description: Member实体类，此类应该加上注解
 */
@Data
public class Member implements Serializable {
    private String mid;
    private String name;
    private String password;
    private Integer locked;
}
