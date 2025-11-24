package edu.kh.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 * instance : 개발자가 직접 new 연산자를 통해 만든 객체, 관리하는 객체
 * bean : Spring Container가 만들고, 관리하는 객체
 * IOC (제어의 반전) : 객체의 생성 및 생명주기의 권한이 개발자가 아닌, 프레임워크에게 있다.
*/

@Controller //요청/응답을 제어하는 역할인 컨트롤러임을 명시 + Bean 등록
//@RequestMapping("/test")
public class TestController {

	/*
	 * 기존 Servlet : 클래스 단위로 하나의 요청만 처리 가능 
	 * Spring : 메서드 단위로 요청 처리 가능
	 * @RequestMapping("요청주소") 
	 * 
	 * 1. 클래스와 메서드에 함께 작성 : 공통 주소를 매핑 // ex)
	 * /test1/insert , /test/update, /test/select
	 *
	 * 2. 메서드에 작성
	 * 요청 주소와 해당 메서드를 매핑
	 * GET/POST 가리지 않고 매핑
	 * (속성을 통해서 지정 가능 or 다른 어노테이션 사용)
	 *
	 */
	
	//@RequestMapping(value="/test", method=RequestMethod.GET)
	@RequestMapping("/test") //test 요청 시 testMethod가 매핑하여 처리함
	public String testMethod() {
		System.out.println("/test 요청 받음");
				
		return "test";
	}
	
	
	
	
	/*
	@RequestMapping("/insert")
	public void methodA() {}
	/test/insert 요청을 methodA가 처리하겠다
	
	@RequestMapping("/update")
	public void methodB() {}
	/test/update 요청을 methodB가 처리하겠다
	
	@RequestMapping("/select")
	public void methodC() {}
	/test/select 요청을 methodC가 처리하겠다
	*/
}



