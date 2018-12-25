package com.macfu.action;

import com.macfu.oauth.po.Client;
import com.macfu.oauth.service.IClientService;
import com.macfu.util.cache.RedisCache;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: liming
 * @Date: 2018/12/24 17:40
 * @Description: token生成
 */
@RestController
@PropertySource("classpath:config/oauth.properties")
public class TokenAciton {
    @Value("${token.expire}")
    private String expire;
    @Resource
    private IClientService clientService;
    private RedisCache<Object, Object> redisCacheAuthcode;
    private RedisCache<Object, Object> redisCacheToken;

    @Resource(name = "cacheManager")
    public void setCacheManager(CacheManager cacheManager) {
        this.redisCacheAuthcode = (RedisCache<Object, Object>) cacheManager.getCache("authCode");
        this.redisCacheToken = (RedisCache<Object, Object>) cacheManager.getCache("token");
    }

    @RequestMapping(value = "/accessToken", method = RequestMethod.POST)
    public Object accessToken(HttpServletRequest request) {
        try {
            // 构建一个OAuthToken请求，如果发现不是post请求会报错
            OAuthTokenRequest tokenRequest = new OAuthTokenRequest(request);
            // 通过authcode来获得用户名
            String mid = null;
            // 如果要获得Token，那么这个时候需要获得两个重要的信息：authCode是否存在，client是否合法
            if (tokenRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.AUTHORIZATION_CODE.toString())) {
                // 只之前获取authcode的时候将用户名保存在了authcode之中，于是此时获得用户名
                String authCode = tokenRequest.getParam(OAuth.OAUTH_CODE);
                try {
                    // 根据autocode查询出保存的用户名
                    mid = (String) this.redisCacheAuthcode.get(authCode);
                } catch (Exception e) {

                }
            }
            // autocode不合法
            if (mid == null) {
                OAuthResponse response = OAuthResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT)
                        .setErrorDescription("授权码错误")
                        .buildJSONMessage();
                return new ResponseEntity<String>(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
            }
            // 可以继续执行下去，就应该对client的信息进行完整判断
            String clientId = tokenRequest.getClientId();
            Client client = this.clientService.getByClientId(clientId);
            // 客户非法
            if (client == null) {
                OAuthResponse oAuthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                        .setErrorDescription("无效的客户端ID信息")
                        .buildJSONMessage();
                return new ResponseEntity<String>(oAuthResponse.getBody(), HttpStatus.valueOf(oAuthResponse.getResponseStatus()));
            }
            // 以上表示一切的检测都已经完成，下面需要处理的部分就是要进行accessToken的生成
            // 设置一个OAuth的编码指派器，此编码继续使用MD5加密处理
            OAuthIssuer oAuthIssuer = new OAuthIssuerImpl(new MD5Generator());
            String accessToken = oAuthIssuer.accessToken();
            // 当存在有accessToken之后，该token肯定是要发给客户端服务器的，但是随后客户端服务器需要通过此token获得内容
            this.redisCacheToken.putEx(accessToken, mid, this.expire);
            // 将token信息发送给客户端，需要创建一个响应处理
            OAuthResponse response = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK).setAccessToken(accessToken)
                    .setExpiresIn(this.expire).buildJSONMessage();
            return new ResponseEntity<String>(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("服务器内部发生错误，请稍候重试!", HttpStatus.valueOf(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
        }
    }
}
