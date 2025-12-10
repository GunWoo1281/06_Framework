package edu.kh.project.common.config;

import java.util.Arrays;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import edu.kh.project.common.filter.LoginFilter;

@Configuration
public class FilterConfig {

	//만들어놓은 loginfilter 클래스가 언제 적용될지 설정
	@Bean
	//LoginFilter로 타입을 제한한 객체를 Bean으로 등록
	public FilterRegistrationBean<LoginFilter> loginFilter(){
		FilterRegistrationBean<LoginFilter> filter = new FilterRegistrationBean<>();
		
		//사용할 필터 객체 세팅
		filter.setFilter(new LoginFilter());
		
		//필터가 동작할 URL 세팅
		String[] filteringURL = {"/myPage/*","/chatting/*", "/editBoard/*"};
		filter.setUrlPatterns(Arrays.asList(filteringURL));
		
		//필터 이름 지정
		filter.setName("loginFilter");
		//필터 우선순위 지정
		filter.setOrder(1);
		
		return filter;
	}
}
