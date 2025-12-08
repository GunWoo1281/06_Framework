package edu.kh.project.email.model.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import edu.kh.project.email.model.mapper.EmailMapper;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{

	private final EmailMapper mapper;
	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;
	// 타임리프를 이용해서 html 코드 -> java 코드 변환

	@Override
	public String sendEmail(String type, String email) {
		
		// 1. 인증키 생성 및 DB에 저장
		// 2. DB에 저장이 성공된 경우에 메일 발송 시도
		
		String authKey = createAuthKey();

		Map<String, String> map = new HashMap<>();
		map.put("authKey", authKey);
		map.put("email", email);
		
		if(!storeAuthKey(map)) return null;
		
		
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			
			helper.setTo(email); // 수신자 설정
			helper.setSubject("[boardPorject] 회원 가입 인증번호입니다."); // 제목 설정
			helper.setText(loadHTML(authKey, type), true); // 내용 설정
			
			helper.addInline("logo", new ClassPathResource("static/images/logo.jpg"));
			
			mailSender.send(mimeMessage);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return authKey;
	}
	
	//HTML 템플릿에 데이터를 넣어 최종 HTML 생성
	private String loadHTML(String authKey, String type) {
		// Context(org.thymeleaf.context.Context)
		// 타임리프에서 제공하는 HTML 템플릿에 데이터를 전달하기 위해 사용하는 클래스
		Context context = new Context();
		context.setVariable("authKey", authKey);
		context.setVariable("type", type);
		
		// src/main/resources/templates/email/signup.html
		return templateEngine.process("email/" + type, context);
	}

	// 인증키와 이메일을 DB에 저장하는 메서드
	@Transactional(rollbackFor = Exception.class)
	public boolean storeAuthKey(Map<String, String> map) {
		
		// 1. 기존 이메일에 대한 인증키 Update 수행
		int result = mapper.updateAuthKey(map);
		
		// 2. update 실패 시 insert 수행
		if(result == 0) {
			result = mapper.insertAuthKey(map);
		}

		// 3. 성공여부 반환
		return result > 0;
	}
	
	
	

	// 인증번호 발급 메서드
	// UUID를 사용하여 인증키생성
	// UUID(Universally Unique Identifier) : 범용 고유 식별자
	// 전세계에서 고유한 식별자를 생성하기 위한 표준
	// 매우 낮은 확률로 중복되는 식별자를 생성
	// 주로 데이터베이스 기본키, 고유한 식별자를 생성해야할 때 사용
	
	private String createAuthKey() {
		return UUID.randomUUID().toString().substring(0, 6);
	}

	@Override
	public int checkAuthKey(Map<String, String> map) {
		// TODO Auto-generated method stub
		return mapper.checkAuthKey(map);
	}
}
