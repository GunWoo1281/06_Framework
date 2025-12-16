package edu.kh.project.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import edu.kh.project.common.interceptor.BoardTypeInterceptor;

//인터셉터가 어떤 요청을 가로챌지 설정하는 클래스

@Configuration //서버가 켜지면 내부 메서드를 모두 수행
public class InterceptorConfig implements WebMvcConfigurer{

    @Bean
    public BoardTypeInterceptor boardTypeInterceptor(){
        return new BoardTypeInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        
        registry
        .addInterceptor(boardTypeInterceptor())
        .addPathPatterns("/**") //가로챌 요청 주소를 지정 /** 이하 모든 요청
        .excludePathPatterns("/css/**", "/js/**", "/images/**","/favicon.ico");
        //가로채지 않을 요청 주소를 지정(정적 리소스 요청은 가로채지 않음)
    }
}
