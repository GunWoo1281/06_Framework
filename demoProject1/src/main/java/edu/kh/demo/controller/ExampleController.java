package edu.kh.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExampleController {

	@GetMapping("example")
	public String exampleMethod() {
		System.out.println("테스트");
		return "example";
	}
}

