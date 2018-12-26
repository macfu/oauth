package com.macfu.action;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PageAction {
	@RequestMapping("/loginForm")
	public String loginForm() {
		return "login";
	}
	
	@RequestMapping("/oauthFailure")
	public String oauthFailure() {
		return "oauth_failure";
	}

	@RequestMapping("/pages/back/welcome")
	public String welcome() {
		return "back/welcome";
	}

	@RequestMapping("/logout")
	public String logout() {
		return "login";
	}

	@ResponseBody
	@RequestMapping("/unauth")
	public Object unauth() {
		return "【ERROR - AUTH】没有此类处理权限。";
	}
}
