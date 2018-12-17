package com.macfu.util.enctype;

import java.util.Base64;

/**
 * @Author: liming
 * @Date: 2018/12/17 17:34
 * @Description: 密码加密的处理程序类
 */
public class PasswordUtils {
    private static final int REPEAT_COUNT = 3;
    private static final String SALT = "mldnjava";

    private PasswordUtils() {
    }

    public static String encode(String pwd) {
        byte[] data = SALT.getBytes();
        for (int x = 0; x < REPEAT_COUNT; x++) {
            data = Base64.getEncoder().encode(data);
        }
        String saltPwd = "{" + new String(data) + "}" + pwd;
        for (int x = 0 ; x < REPEAT_COUNT ; x ++) {
            saltPwd = new MD5Code().getMD5ofStr(saltPwd);
        }
        return saltPwd;
    }
}
