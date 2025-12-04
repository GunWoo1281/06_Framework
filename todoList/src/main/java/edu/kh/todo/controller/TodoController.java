package edu.kh.todo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.kh.todo.model.dto.Todo;
import edu.kh.todo.model.service.TodoService;

@Controller
@RequestMapping("todo")  // "/todo" 로 시작하는 모든 요청 매핑
public class TodoController {
	
	@Autowired // DI : 의존성 주입(같은 타입 + 상속관계인 Bean을 의존성주입)
	private TodoService service;
	
	
	@PostMapping("add") //  /todo/add 로 Post 방식 요청 매핑
	public String addTodo(@RequestParam("todoTitle") String todoTitle, 
						@RequestParam("todoContent") String todoContent,
						RedirectAttributes ra) {
		
		// 1. HttpServletRequest req를 통해 getParameter() 하여 얻어오기
		// 2. @RequestParam() 를 이용하여 얻어오기
		// 3. @ModelAttribute와 DTO를 이용하여 얻어오기
		
		// ---------------------------------
		
		// RedirectAttributes : 리다이렉트 시 값을 1회성으로 전달하는 객체
		// RedirectAttributes.addFlashAttribute("key", value) 형식으로 세팅
		// -> request scope -> session scope로 잠시 변환
		// 응답 전 : request scope
		// redirect : session scope 로 이동 -> 사용
		// 응답이 끝난 뒤 : request scope 복귀
		
		// 서비스 메서드 호출 후 결과 반환 받기
		int result = service.addTodo(todoTitle, todoContent);
		
		// 결과에 따라 message 값 지정
		String message = null;
		
		if(result > 0) message = "할 일 추가 성공!!!";
		else		   message = "할 일 추가 실패...";
		
		// 리다이렉트 시 1회성으로 사용할 데이터를 속성으로 추가
		// req -> session -> req
		ra.addFlashAttribute("message", message);	
		
		return "redirect:/"; // 메인페이지로 재요청
	}
	
	@GetMapping("detail") //  /todo/detail 로 Get방식 요청 매핑
	public String todoDetail(@RequestParam("todoNo") int todoNo,
							Model model, 
							RedirectAttributes ra) {
		
		Todo todo = service.todoDetail(todoNo);
		
		String path = null;
		
		// 조회 결과가 있을 경우 detail.html forward
		if(todo != null) {
			
			path = "todo/detail";
			model.addAttribute("todo", todo); // request scope 값 세팅
			
		} else {
		// 조회 결과가 없을 경우 메인페이지로 redirect
			path = "redirect:/";
			ra.addFlashAttribute("message", 
					"해당 할 일이 존재하지 않습니다");
			
		}
		
		return path;
	}
	
	//  /todo/changeComplete?todoNo=3&complete=Y 
	/** 완료 여부 변경
	 * @param todo : 커맨드 객체(@ModelAttribute 생략가능)
	 * 	-//@ModelAttribute와 함께 DTO 클래스를 사용하는 방식
	 * 	- 파라미터의 key와 Todo 객체의 필드명이 일치하면
	 * 	- 일치하는 필드값이 파라미터의 value값으로 세팅된 상태
	 *  - 즉, todo 객체의 todoNo와 complete 필드가 세팅 완료된 상태
	 * @return
	 */
	@GetMapping("changeComplete")
	public String changeComplete(/*@ModelAttribute*/ Todo todo,
								RedirectAttributes ra) {
		
		// 변경 서비스 호출
		int result = service.changeComplete(todo);
		
		// 변경 성공 시 "변경 성공!"
		// 		실패 시 "변경 실패!"
		String message = null;
		if(result > 0) message = "변경 성공!!";
		else		   message = "변경 실패..";
		
		ra.addFlashAttribute("message", message);
		
		// 상대경로 (현재 위치 중요!!!)
		// 현재 주소 : /todo/changeComplete
		// 목표 주소 : /todo/detail?todoNo=1
		return "redirect:detail?todoNo=" + todo.getTodoNo();
	}
	
	/** 수정 화면 전환 요청
	 * @return
	 */
	@GetMapping("update")
	public String todoUpdate(@RequestParam("todoNo") int todoNo, 
							Model model) {
		
		// 상세 조회 서비스 재활용 -> 수정화면에 출력할 기존 내용 필요
		Todo todo = service.todoDetail(todoNo);
		
		model.addAttribute("todo", todo);
		
		// classpath:/templates/
		// .html
		return "todo/update";
	}
	
	// todoTitle="제목"&todoContent="상세내용"&todoNo=1
	@PostMapping("update")
	public String todoUpdate(Todo todo, RedirectAttributes ra) {
		
		// 수정 서비스 호출 후 결과 반환받기
		int result = service.todoUpdate(todo);
		
		String path = "redirect:";
		String message = null;
		
		if(result > 0) {
			// 해당 Todo의 상세 조회로 리다이렉트
			path += "/todo/detail?todoNo=" + todo.getTodoNo();
			message = "수정 성공!";
			
		} else {
			// 다시 수정 화면 리다이렉트
			path += "/todo/update?todoNo=" + todo.getTodoNo();
			message = "수정 실패..";
		}
		
		ra.addFlashAttribute("message", message);
		
		return path;
	}
	
	
	// 삭제 요청/응답 메서드 todoDelete()
	// 삭제 성공 시 
	// "/" 리다이렉트
	// 메시지 : 삭제 성공
	// 삭제 실패 시
	// 해당 상세페이지로 리다이렉트
	// 메시지 : 삭제 실패
	@GetMapping("delete")
	public String todoDelete(@RequestParam("todoNo") int todoNo,
							RedirectAttributes ra) {
		
		int result = service.todoDelete(todoNo);
		
		String path = null;
		String message = null;
		
		if(result > 0) {
			path = "/";
			message = "삭제 성공";
		} else {
			path = "/todo/detail?todoNo=" + todoNo;
			message = "삭제 실패";
		}
		
		ra.addFlashAttribute("message", message);
		
		return "redirect:" + path;
		
	}
	
	
	
	
	
	
	
}