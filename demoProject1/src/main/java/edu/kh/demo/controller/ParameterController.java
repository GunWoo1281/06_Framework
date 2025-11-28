package edu.kh.demo.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.kh.demo.model.dto.Member;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("param")
@Slf4j
public class ParameterController {

	@GetMapping ("main")
	public String paramMain() {
		
		return "param/param-main";
	}
	
	@PostMapping ("test1")
	public String paramTest1(HttpServletRequest req) {
		
		String inputName = req.getParameter("inputName");
		String inputAddress = req.getParameter("inputAddress");
		int inputAge = Integer.parseInt(req.getParameter("inputAge"));
		
		log.debug("inputName : " + inputName);
		log.debug("inputAddress : " + inputAddress);
		log.debug("inputAge : " + inputAge);
		
		/*
			Spring에서 Redirect(재요청)하는 방법
			Controller 메서드 반환값에
			"redirect:재요청주소"; 작성
		*/
		return "redirect:/param/main";
	}
	
	
	/* 2. @RequestParam 어노테이션 - 낱개 파라미터 얻어오기
	 *
	 * - request 객체를 이용한 파라미터 전달 어노테이션
	 * - 매개변수 앞에 해당 어노테이션을 작성하면, 매개변수에 값이 주입됨.
	 * - 주입되는 데이터는 매개변수의 타입에 맞게 형변환이 자동으로 수행됨.
	 *
	 * [기본 작성법]
	 * @RequestParam("key") 자료형 매개변수명
	 *
	 * [속성 추가 작성법]
	 * @RequestParam(value="key", required=false, defaultValue="1")
	 *
	 * value : 전달받은 input 태그의 name 속성값(파라미터 key)
	 * required : 입력된 name 속성값 파라미터 필수 여부 지정(기본값 true)
	 * -> required=true인 파라미터가 존재하지 않는다면 400(Bad Request) 에러 발생
	 * -> "" (빈문자열)일 때는 에러 발생 X
	 * (파라미터가 존재하지 않는것이 아니라 name속성값="" 로 넘어오기 때문에)
	 *
	 * defaultValue : 파라미터 중 일치하는 name속성값이 없을 경우에 대입할 값 지정.
	 * -> required=false 인 경우 사용
	 *
	 *
	 * */
	@PostMapping ("test2")
	public String paramTest2(@RequestParam("title") String title,
							@RequestParam("writer") String writer,
							@RequestParam("price") int price,
							@RequestParam(value="publisher", required = false, defaultValue = "kh출판사") String publisher) {
		
		log.debug("title : " + title);
		log.debug("writer : " + writer);
		log.debug("price : " + price);
		log.debug("publisher : " + publisher);
		
		return "redirect:/param/main";
	}
	
	@PostMapping ("test3")
	public String paramTest3(@RequestParam("color") String[] colorArr,
							@RequestParam("fruit") List<String> fruitList,
							@RequestParam Map<String, String> paramMap) {
		
		log.debug("colorArr : " + Arrays.toString(colorArr));
		log.debug("fruitList : " + fruitList);
		log.debug("paramMap : " + paramMap.toString());
		//첫번째로 제출된 1개의 값만 저장함.
		
		return "redirect:/param/main";
	}
	
	@PostMapping ("test4")
	public String paramTest4(@ModelAttribute Member inputMember) {
		
		log.debug("inputMember : " + inputMember);
		
		//상대주소
		//return "redirect:main";
		return "redirect:/param/main";
	}
	
}
