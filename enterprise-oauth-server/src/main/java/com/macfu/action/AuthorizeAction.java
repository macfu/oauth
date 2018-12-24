package com.macfu.action;

import com.macfu.oauth.po.Client;
import com.macfu.oauth.service.IClientService;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
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
 * @Date: 2018/12/24 10:46
 * @Description: 获取authcode的操作路径
 */
@RestController
public class AuthorizeAction {
    @Resource
    private IClientService clientService;

    @RequestMapping(value = "/authorize", method = RequestMethod.GET)
    public Object authorize(HttpServletRequest request) {
        try {
            // OAuth本身是一个处理的标准，那么既然是标准就需要通过标准的路径进行访问
            OAuthAuthzRequest oAuthAuthzRequest = new OAuthAuthzRequest(request);
            // 必须获取cilent_ID,授权注册接入用户的编号(可变),利用Oauth标准的请求来处理
            String clientId = oAuthAuthzRequest.getClientId();
            // 通过clientID对这个请求的合法性做出检测
            Client client = this.clientService.getByClientId(clientId);
            // client_Id是合法的
            if (client != null) {
                // 生成一个认证码
                String authCode = null;
                // 要获得一个OAuth的responseType的信息，该信息一定是code
                String responseType = oAuthAuthzRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);
                // 明确的描述当前可以处理的responseType类型为code
                if (responseType.equals(ResponseType.CODE.toString())) {
                    // 定义一个用于分配认证码的处理程序类，这个类生成的认证码需要设置一个加密模式的处理
                    OAuthIssuerImpl oAuthIssuer = new OAuthIssuerImpl(new MD5Generator());
                    // 生成认证码
                    authCode = oAuthIssuer.authorizationCode();
                }
                // 回应状态码
                return new ResponseEntity<String>(authCode, HttpStatus.OK);
            } else {
                // 如果client_id没有获的
                OAuthResponse oAuthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                        .setErrorDescription("无效的客户端ID信息")
                        .buildJSONMessage();
                return new ResponseEntity<String>(oAuthResponse.getBody(), HttpStatus.valueOf(oAuthResponse.getResponseStatus()));
            }

        } catch (Exception e) {
            // 程序出现异常的处理方法
            e.printStackTrace();
            return new ResponseEntity<String>("服务器内部出现错误,请稍后重试", HttpStatus.valueOf(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
        }
    }
}
