package edu.kh.project.common.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


/*
	요청, 응답 시 걸러내거나 추가 할 수 있는 객체

	[필터 클래스 생성 방법]
	1. jakarta.servlet.Filter 인터페이스를 상속
	2. doFilter() 메서드 오버라이딩
	


*/

//로그인이 되어 있지 않은 경우 특정 페이지 접근 불가하도록 필터링
public class LoginFilter implements jakarta.servlet.Filter {

	//필터 동작을 정의하는 메서드
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		//ServletRequest : HttpServletRequest의 부모
		//ServletResponse : HttpServletResponse의 부모
		
		//loginMember가 session에서 담기기 때문에 session이 필요함.
		
		//자식형태로 다운캐스팅
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		
		String path = req.getRequestURI(); //요청된 주소
		
		//url이 /myPage/profile 시작하는지 확인
		if(path.startsWith("/myPage/profile/")){
			chain.doFilter(request, response); // 필터 통과
			return;
		}
		
		//session 얻어오기
		HttpSession session = req.getSession();
	
		if(session.getAttribute("loginMember") == null) { //로그인 X

			//응답 화면에 스트림으로 출력
			resp.sendRedirect("/loginError");
		}
		else {
			chain.doFilter(request, response); // 필터 통과
		}

		//FilterChain :다음 필터 또는 dispatcherServlet과 연결된 객체
		
	}

}
