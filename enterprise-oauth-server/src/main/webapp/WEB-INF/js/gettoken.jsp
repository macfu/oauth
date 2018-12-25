<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags"%>
<%
    request.setCharacterEncoding("UTF-8");
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
            + request.getContextPath() + "/";
%>
<html>
<head>
    <base href="<%=basePath%>"/>
    <title>OAuth服务器Token检测</title>
    <script type="text/javascript" src="jquery/jquery.min.js"></script>
    <script type="text/javascript">
        var tokenUrl = "http://localhost/enterpriseoauth-oauht-server/accessToken.action";
        var authCode = "EAB62A7769F0313F8D69CEBA32F4347E";
        $(function(){
            $(tokenBut).on("click",function(){
                $.ajax({
                    url: tokenUrl,
                    method: "post",
                    datatype: "json",
                    data: {
                        client_id: "",
                        client_secret: "",
                        grant_type: "authorization_code",
                        code: authCode,
                        redirect_uri: "http://www.client.com/enterprise-oauth-client"
                    },
                    success: function(data) {
                        console.log(data);
                    }
                });
            });
        })
    </script>
    <body>
        <button id="tokenBut">获取token信息</button>
</body>
</head>
</html>