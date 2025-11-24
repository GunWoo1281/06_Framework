package edu.kh.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
