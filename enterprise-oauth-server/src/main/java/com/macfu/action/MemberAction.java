package com.macfu.action;

import com.macfu.util.cache.RedisCache;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.shiro.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: liming
 * @Date: 2018/12/25 15:36
 * @Description: 用户信息的Action类,返回用户的编号
 */
@Controller
public class MemberAction {
    private RedisCache<Object, Object> redisCacheToken;

    @Resource(name = "cacheManager")
    public void setCacheManager(CacheManager cacheManager) {
        this.redisCacheToken = (RedisCache<Object, Object>) cacheManager.getCache("token");
    }

    @RequestMapping("/memberInfo")
    public Object memberInfo(HttpServletRequest request) {
        try {
            OAuthAccessResourceRequest oAuthRequest = new OAuthAccessResourceRequest(request, ParameterStyle.QUERY);
            String accessToken = oAuthRequest.getAccessToken();
            String mid = null;
            try {
                mid = (String) this.redisCacheToken.get(accessToken);
            } catch (Exception ew) {

            }
            if (mid == null) {
                OAuthResponse response = OAuthResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                        .setError(OAuthError.ResourceResponse.INVALID_TOKEN)
                        .setErrorDescription("Token信息已经失效").buildJSONMessage();
                return new ResponseEntity<>(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
            }
            return new ResponseEntity<>(mid, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("服务器内部发生错误，请稍后重试！", HttpStatus.valueOf(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
        }
    }
}
