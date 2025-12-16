package edu.kh.project.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.unit.DataSize;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.MultipartConfigElement;

@Configuration
@PropertySource("classpath:/config.properties")
public class FileConfig implements WebMvcConfigurer{
	//WebMvcConfigurer : Spring MVC 프레임워크에서 제공하는
	//인터페이스 중 하나로, 스프링 구성을 커스터마이징하고
	//확장하기 위한 메서드를 제공함.
	//주로 웹 애플리케이션의 설정을 조정하거나 추가하는데 사용됨

	//파일 업로드 임계값
	@Value("${spring.servlet.multipart.file-size-threshold}")
	private long fileSizeThreshold;

	//임계값 초과시 파일의 임의 저장 경로
	@Value("${spring.servlet.multipart.location}")
	private String location;

	//요청당 파일 최대 크기
	@Value("${spring.servlet.multipart.max-request-size}")
	private long maxRequestSize;
	
	//개별 파일당 최대 크기
	@Value("${spring.servlet.multipart.max-file-size}")
	private long maxFileSize;
	
	//프로필 이미지 관련 경로
	@Value("${my.profile.resource-handler}")
	private String profileResouceHandler;
	
	@Value("${my.profile.resource-location}")
	private String profileResouceLocation;

	
	//게시판 이미지 관련 경로
	@Value("${my.board.resource-handler}")
	private String boardResouceHandler;
	
	@Value("${my.board.resource-location}")
	private String boardResouceLocation;
	
	@Bean
	public MultipartConfigElement ConfigElement() {
		//MultipartConfigElement :
		//파일 업로드를 처리하는데 사용되는 MultipartConfigElement를
		//구성하고 반환(옵션 설정하는데 사용)
		//업로드 파일의 최대크기, 임시 저장 경로 등..

		MultipartConfigFactory factory = new MultipartConfigFactory();

		factory.setFileSizeThreshold(DataSize.ofBytes(fileSizeThreshold)); //파일 업로드 임계값
		factory.setLocation(location); //임계값 초과시 파일의 임의 저장 경로
		factory.setMaxRequestSize(DataSize.ofBytes(maxRequestSize)); //요청당 파일 최대 크기
		factory.setMaxFileSize(DataSize.ofBytes(maxFileSize)); //개별 파일당 최대 크기

		return factory.createMultipartConfig();
	}

	// MultipartResolver 객체를 생성하여 Bean으로 등록
	// -> 위에서 만든 MultipartConfigElement 자동으로 이용함
	@Bean
	public MultipartResolver multipartResolver() {
		// MultipartResolver : MultipartFile 을 처리해주는 해결사
		// -> MultipartResolver는 클라이언트로부터 받은 multipart 요청을 처리하고,
		// 그 중 업로드된 파일을 추출하여 MultipartFile 객체로 제공하는 역할
		StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();

		return multipartResolver;
	}
	
	@Override
	public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
		//ResourceHandlerRegistry :
		//Spring MVC에서 정적 리소스(이미지, CSS, JS 등)의
		//요청을 처리하기 위해 사용하는 클래스
		
		//URL 요청 패턴을 서버에 실제 파일경로와 연결하여
		//클라이언트가 특정 경로로 정적파일에 접근할 수 있도록 설정
		registry.addResourceHandler("/mypage/file/**").addResourceLocations("file:///C:/uploadFiles/test/");
		// -> 클라이언트가 /mypage/file/** 패턴으로 이미지를 요청할 때
		// 서버 폴더 경로 중 C:/uploadFiles/test/ 로 연결 하겠다.
		// Windows 경로는 file:/// (슬래시 3개) 형식을 사용해야 함
		
		registry.addResourceHandler(profileResouceHandler).addResourceLocations(profileResouceLocation);

		registry.addResourceHandler("/images/board/**").addResourceLocations("file:///C:/uploadFiles/board/");
		
		registry.addResourceHandler(boardResouceHandler).addResourceLocations(boardResouceLocation);
	}
}
