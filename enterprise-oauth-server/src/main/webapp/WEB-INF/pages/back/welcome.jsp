<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags"%>
<%
	request.setCharacterEncoding("UTF-8");
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ request.getContextPath() + "/";
	String login_url = basePath + "login.action" ;
%>
<html>
<head>
<base href="<%=basePath%>" />
<title>WEB开发</title>
</head>
<body> 
<h1>魔乐科技（www.mldn.cn）</h1>
<h2>当前登录用户ID：<shiro:principal/></h2>
<shiro:hasRole name="member">
	拥有member角色。
</shiro:hasRole>
</body>
</html>