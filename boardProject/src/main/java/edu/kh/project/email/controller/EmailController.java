package edu.kh.project.email.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.kh.project.email.model.service.EmailService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("email")
@RequiredArgsConstructor // final 필드에 자동으로 의존성 주입
public class EmailController {

	private final EmailService service;
	
	@PostMapping("signup")
	@ResponseBody
	public int signup(@RequestBody String email) {
		
		String authKey = service.sendEmail("signup", email);
		
		// 이메일 발송 성공
		if(authKey != null) {
			return 1; 
		}
		
		return 0;
	}
	
	/** 입력받은 이메일 확인하기
	 * @param map
	 * @return
	 */
	@ResponseBody
	@PostMapping("checkAuthKey")
	public int checkAuthKey(@RequestBody Map<String, String> map) {
		return service.checkAuthKey(map);
	}
}
