package edu.kh.project.myPage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.kh.project.member.model.dto.Member;
import edu.kh.project.myPage.model.service.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/*
	@SessionAttributes의 역할
	- model에 추가된 속성 중 key 값이 일치하는 속성을 session scope로 변경하는 어노테이션
	- 클래스 상단에 @SessionAttributes({"loginMember"}) 작성
	
	@SessionAttribute의 역할
	- @SessionAttributes를 통해 session에 등록된 속성을 꺼내올 때 사용하는 어노테이션
	- 메서드 매개변수에 @SessionAttribute("loginMember") Member loginMember 작성
*/

@Controller
@RequestMapping("myPage")
@SessionAttributes({"loginMember"})
@Slf4j
@RequiredArgsConstructor
public class MyPageController {
	private final MyPageService service;
	
	@GetMapping("info")
	public String myinfo(@SessionAttribute("loginMember") Member loginMember, Model model) {
		String memberAddress = loginMember.getMemberAddress();
		
		if(memberAddress != null) {
			String[] arr = memberAddress.split("\\^\\^\\^");

			model.addAttribute("postcode", arr[0]);
			model.addAttribute("address", arr[1]);
			model.addAttribute("detailAddress", arr[2]);
		}
		
		return "mypage/myPage-info";
	}
	
	@GetMapping("profile")
	public String profile() {
		return "mypage/myPage-profile";
	}
	
	@GetMapping("changePw")
	public String changePw() {
		return "mypage/myPage-changePw";
	}
	
	@GetMapping("secession")
	public String secession() {
		return "mypage/myPage-secession";
	}
	
	@GetMapping("fileTest")
	public String fileTest() {
		return "mypage/myPage-fileTest";
	}
	
	@GetMapping("fileList")
	public String fileList() {
		return "mypage/myPage-fileList";
	}
	
	/**
	 * @param inputMember : 커맨드 객체(@ModelAttribute가 생략된 상태)
	 * 						제출된 memberNickname, memberTel 세팅된 상태
	 * @param memberAddress : 주소만 따로 배열형태로 얻어옴
	 * 		  loginMember : 로그인한 회원 정보 (현재 로그인한 회원의 PK 회원번호)
	 * @return
	 */
	@PostMapping("info")
	public String updateInfo(Member inputMember, 
							@RequestParam("memberAddress") String[] memberAddress, 
							@SessionAttribute("loginMember") Member loginMember,
							RedirectAttributes ra) {
		
		inputMember.setMemberNo(loginMember.getMemberNo());
		
		int result = service.updateInfo(inputMember,memberAddress);
		
		String message = null;

		if(result>0){
			message = "회원 정보 수정 성공!!!";

			//loginMember에 DB상 업데이트된 내용으로 세팅
			//loginMember는 세션에 저장된 로그인한 회원 정보가 저장되어 있다. (로그인 할 당시의 기존 데이터)
			//loginMember를 수정하면 세션에 저장된 로그인한 회원의 정보가 업데이트 된다. 

			//동기화
			loginMember.setMemberNickname(inputMember.getMemberNickname());
			loginMember.setMemberTel(inputMember.getMemberTel());
			loginMember.setMemberAddress(inputMember.getMemberAddress());
			ra.addFlashAttribute("loginMember", loginMember);
		}
		else{
			message = "회원 정보 수정 실패...";
		}

		ra.addFlashAttribute("message", message);
		return "redirect:info"; //재요청 경로 : /myPage/info GET방식
	}

	@PostMapping("changePw")
	public String changePw(Member inputMember, 
							@RequestParam("currentPw") String currentPw,
							@RequestParam("newPw") String newPw,
							@SessionAttribute("loginMember") Member loginMember,
							RedirectAttributes ra){
		
		inputMember.setMemberNo(loginMember.getMemberNo());
		
		//현재 비밀번호가 접속한 계정 비밀번호와 일치하는지 확인
		int result = service.checkPw(currentPw, loginMember);
		if(result==0) {
			ra.addFlashAttribute("message", "현재 비밀번호가 일치하지 않습니다");
			return "redirect:changePw";
		}
		//새 비밀번호와 새 비밀번호 확인이 일치하는지는 JS에서 하고 있음.

		//전부 일치한다면 비밀번호 변경
		inputMember.setMemberPw(newPw);
		result = service.changePw(inputMember);
		
		String message = null;
		
		if(result>0){
			message = "비밀번호가 변경 되었습니다";
		}
		else{
			message = "비밀번호 변경에 실패하였습니다.";
		}
		
		ra.addFlashAttribute("message", message);
		return "redirect:changePw";
	}

	
	@PostMapping("secession")
	public String secession(@RequestParam("memberPw") String memberPw,
		@SessionAttribute("loginMember") Member loginMember,RedirectAttributes ra,
		SessionStatus status){

		int result = service.checkPw(memberPw, loginMember);
		if(result==0) {
			ra.addFlashAttribute("message", "현재 비밀번호가 일치하지 않습니다");
			return "redirect:secession";
		}

		result = service.secession(loginMember);
		
		if(result>0){
			ra.addFlashAttribute("message", "회원 탈퇴되셨습니다.");
		}
		else{
			ra.addFlashAttribute("message", "회원 탈퇴 실패");
		}

		status.setComplete(); // 세션을 완료 시킴
		return "redirect:/";
	}


/*
	Spring에서 파일을 처리하는 방법

	- enctype="multipart/form-data" : 로 클라이언트의 요청을 받으면
	(문자, 숫자, 파일 등이 섞여있는 요청)

	이를 MulttipartResolver (FileConfig에 정의)를 이용해서
	섞여있는 파라미터를 분리 작업을 함

	문자열, 숫자 -> String
	파일 -> MultipartFile
 */
	@PostMapping("file/test1")
	public String fileUpload1(){

		return "";
	}
}
