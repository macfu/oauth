package com.macfu.oauth.po;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: liming
 * @Date: 2018/12/20 10:52
 * @Description: client实体类，应该使用mybatisGenerate生成
 */
@Data
public class Client implements Serializable {
    private Long clid;
    private String clientId;
    private String clientSecret;
}
