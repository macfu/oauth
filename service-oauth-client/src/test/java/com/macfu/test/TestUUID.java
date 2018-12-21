package com.macfu.test;

import java.util.UUID;

/**
 * @Author: liming
 * @Date: 2018/12/21 14:26
 * @Description: 生成规则
 */
public class TestUUID {
    public static void main(String args[]) {
        String sal = "mldnjava";
        String uuid = UUID.randomUUID().toString();
        String cre = UUID.nameUUIDFromBytes((sal + uuid).getBytes()).toString();
        System.out.println(uuid);
        System.out.println(cre);
    }
}
