package com.macfu.action;

import com.macfu.oauth.po.Client;
import com.macfu.oauth.service.IClientService;
import com.macfu.util.cache.RedisCache;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

/**
 * @Author: liming
 * @Date: 2018/12/24 10:46
 * @Description: 获取authcode的操作路径
 */
@RestController
@PropertySource("classpath:config/oauth.properties")
public class AuthorizeAction {
    @Value("${authcode.expire}")
    private String expire;
    @Resource
    private IClientService clientService;

    private RedisCache<Object, Object> redisCache;

    @Resource(name = "cacheManager")
    public void setCacheManager(CacheManager cacheManager) {
        this.redisCache = (RedisCache<Object, Object>) cacheManager.getCache("authcode");
    }

    @RequestMapping(value = "/authorize", method = RequestMethod.GET)
    public Object authorize(HttpServletRequest request) {
        try {
            // OAuth本身是一个处理的标准，那么既然是标准就需要通过标准的路径进行访问
            OAuthAuthzRequest oAuthAuthzRequest = new OAuthAuthzRequest(request);
            // 必须获取cilent_ID,授权注册接入用户的编号(可变),利用Oauth标准的请求来处理
            String clientId = oAuthAuthzRequest.getClientId();
            // 通过clientID对这个请求的合法性做出检测
            Client client = this.clientService.getByClientId(clientId);
            if (client == null) {
                OAuthResponse oAuthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                        .setErrorDescription("无效的客户端ID信息")
                        .buildJSONMessage();
                return new ResponseEntity<String>(oAuthResponse.getBody(), HttpStatus.valueOf(oAuthResponse.getResponseStatus()));
            }
            // 获得当前用户信息
            org.apache.shiro.subject.Subject subject = SecurityUtils.getSubject();
            // 如果当前用户还没有登录过
            if (!subject.isAuthenticated()) {
                // 登录之后还要回到这个界面
                WebUtils.saveRequest(request);
                // 创建http请求头信息
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(new URI(request.getContextPath() + "/loginForm.action"));
                return new ResponseEntity<String>(headers, HttpStatus.TEMPORARY_REDIRECT);
            }
            // 生成一个认证码
            String authCode = null;
            // client_Id是合法的
            if (client != null) {
                // 要获得一个OAuth的responseType的信息，该信息一定是code
                String responseType = oAuthAuthzRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);
                // 明确的描述当前可以处理的responseType类型为code
                if (responseType.equals(ResponseType.CODE.toString())) {
                    // 定义一个用于分配认证码的处理程序类，这个类生成的认证码需要设置一个加密模式的处理
                    OAuthIssuerImpl oAuthIssuer = new OAuthIssuerImpl(new MD5Generator());
                    // 生成认证码
                    authCode = oAuthIssuer.authorizationCode();
                    this.redisCache.putEx(authCode, subject.getPrincipal(), this.expire);
                }
            }
            // 当登录完成之后应该跳转到redirect_url所指定的路径(接入客户端服务器地址)
            // 构建一个回应请求的构造器(code,redirect_url跳转)
            OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND);
            // 设置authCode的信息
            builder.setCode(authCode);
            // 获得回应路径
            String redirectUrl = oAuthAuthzRequest.getRedirectURI();
            // 创建个回应地址:redirect_url?code=authcode
            OAuthResponse oAuthResponse = builder.location(redirectUrl).buildQueryMessage();
            // 定义要返回的头部处理信息
            HttpHeaders headers = new HttpHeaders();
            // 设置地址
            headers.setLocation(new URI(oAuthResponse.getLocationUri()));
            return new ResponseEntity<String>(headers, HttpStatus.valueOf(oAuthResponse.getResponseStatus()));

        } catch (Exception e) {
            // 程序出现异常的处理方法
            e.printStackTrace();
            return new ResponseEntity<String>("服务器内部出现错误,请稍后重试", HttpStatus.valueOf(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
        }
    }
}
