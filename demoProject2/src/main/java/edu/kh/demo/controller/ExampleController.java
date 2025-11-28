package edu.kh.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.kh.demo.model.dto.Student;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("example")	
@Slf4j
public class ExampleController {

	/*
	* page < request < session < application
	*
	* Model (org.springframework.ui.Model)
	*
	* - Spring에서 데이터 전달 역할을 하는 객체
	*
	* - 기본 scope : request
	* 
	* - @SessionAttribute와 함께 사용 시 session scope 변환
	* 
	* [기본 사용법]
	* model.addAttribute("key", value);
	* 
	*/
	
	@RequestMapping("ex1")
	public String ex1(HttpServletRequest req , Model model) {
		req.setAttribute("test1", "HttpServletRequest로 전달한 값");
		model.addAttribute("test2","Model로 전달한 값");
	
		//단일값
		model.addAttribute("productName", "마이크");
		model.addAttribute("price", 20000);
		
		//복수값
		
		List<String> fruitList = new ArrayList<>();
		fruitList.add("사과");
		fruitList.add("딸기");
		fruitList.add("바나나");
		
		model.addAttribute("fruitList", fruitList);
	
		//DTO 객체를 Model 객체를 이용해서 html로 전달하기
		Student std = new Student();
		std.setStudentNo("12345");
		std.setName("홍길동");
		std.setAge(22);
		
		model.addAttribute("std", std);
		
		// List<Student> 객체 Model을 이용해서 html로 전달
		List<Student> stdList = new ArrayList<>();
		stdList.add( new Student("11111","김일번",20) );
		stdList.add( new Student("22222","최이번",23) );
		stdList.add( new Student("33333","홍삼번",22) );
		
		model.addAttribute("stdList", stdList);
		
		return "example/ex1";
	}
	
	@PostMapping("ex2")
	public String ex2(Model model) {
		
		model.addAttribute("str","<h1>테스트 중 &times; </h1>");
		
		return "example/ex2";	
	}
	
	@GetMapping("ex3")
	public String ex3(Model model) {
	
		model.addAttribute("key","제목");
		model.addAttribute("query","검색어");
		model.addAttribute("boardNo", 10);
		
		return "example/ex3";
	}

	@GetMapping("ex3/{path}")
	public String pathVariableTest(@PathVariable("path") int path) {
		//controller에서 해야하는 로직이 동일한 경우
		//example/ex3/1, example/ex3/2, example/ex3/3 ...
		//주소 중 {path} 부분의 값을 가져와서 매개변수로 저장
		//이 매개변수 값을 controller단의 메서드에서
		//사용할 수 있도록 해줌 (이 값을 Service -> DAO -> DB)
		//+ Request scopre에 자동 세팅됨
		//변수명 = 값


		return "example/testResult";
	}

	@GetMapping("ex4")
	public String ex4(Model model) {
		Student std = new Student("67890", "잠만보", 22);
		model.addAttribute("std", std);
		model.addAttribute("num", 100);

		return "example/ex4";
	}

	@GetMapping("ex5")
	public String ex5(Model model) {
	
		model.addAttribute("message", "타임리프 + Javascript 사용연습");
		model.addAttribute("num1", 12345);

		Student std = new Student();
		std.setStudentNo("22222");
		model.addAttribute("std", std);

		return "example/ex5";
	}
}
