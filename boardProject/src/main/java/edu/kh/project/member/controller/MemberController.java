package edu.kh.project.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.kh.project.member.model.dto.Member;
import edu.kh.project.member.model.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;


/* @SessionAttributes({"key", "key", "key"...})
 * - Model에 추가된 속성 중
 *   key값이 일치하는 속성을 session scope로 변경
 * 
 * */
@SessionAttributes({"loginMember"})
@Controller
@RequestMapping("member")
@Slf4j
public class MemberController {
	
	@Autowired // 의존성 주입(DI)
	private MemberService service; 
	
	/** [로그인]
	 * - 특정 사이트에 아이디(이메일)/비밀번호 등을 입력해서
	 *   해당 정보가 있으면 조회/서비스 이용
	 *   
	 * - 로그인 한 회원 정보를 session 에 기록하여
	 *   로그아웃 또는 브라우저 종료 시까지 
	 *   해당 정보를 계속 이용할 수 있게 함
	 *   
	 * @param inputMember : 커맨드 객체 (@ModelAttribute 생략)
	 * 						memberEmail, memberPw 세팅된 상태
	 * @return
	 */
	@PostMapping("login")  // /member/login 요청 POST 방식 매핑
	public String login(/* @ModelAttribute */ Member inputMember, 
						RedirectAttributes ra,
						Model model ,
						@RequestParam(value="saveId", required = false) String saveId,
						HttpServletResponse resp
						) {
		
		// 로그인 서비스 호출
		try {
			Member loginMember = service.login(inputMember);
			
			log.debug("loginMember : " + loginMember);
			
			// 로그인 실패 시
			if(loginMember == null) {
				ra.addFlashAttribute("message", "아이디 또는 비밀번호가 일치하지 않습니다");
				
			} else {
				// 로그인 성공 시
				model.addAttribute("loginMember", loginMember);
				// 1단계 : request scope에 세팅됨
				// 2단계 : 클래스 위에 @SessionAttributes() 
				// 어노테이션 작성하여 session scope 이동
			
				
				// ******************* 쿠키 ***********************
				// 이메일 저장
				Cookie cookie = new Cookie("saveId", loginMember.getMemberEmail());

				//쿠키가 적용될 경로를 설정
				// -> 클라리언트가 어떤 요청을 할 때 쿠키가 첨부될지 지정
				cookie.setPath("/");
				
				// 메인페이지 + 그 하위 주소 모두
				
				// 쿠키의 만료 기간 지정
				if(saveId != null) { // 체크박스가 체크된 경우
					cookie.setMaxAge(60 * 60 * 24 * 30); // 30일
					// 초 단위 지정
				}
				else {
					cookie.setMaxAge(0);
					//쿠키 삭제
				}
				
				resp.addCookie(cookie);
			}
			
			
		} catch (Exception e) {
			log.info("로그인 중 예외 발생");
			e.printStackTrace();
		}
		
		return "redirect:/"; // 메인페이지 재요청
	}
	
	/** 로그아웃 : session에 저장된 로그인된 회원 정보를 없앰
	 * @param SessionStatus : @SessionAttributes로 지정된 특정 속성을 
	 * 						세션에서 제거할 수 있는 기능을 제공하는 객체
	 * @return
	 */
	@GetMapping("logout") //  /member/logout 요청 GET 방식 매핑
	public String logout(SessionStatus status) {
		
		status.setComplete(); // 세션을 완료 시킴
		
		return "redirect:/";
		
	}
	
	/** 회원 가입 페이지로 이동
	 * @return
	 */
	@GetMapping("signup")
	public String signUppage() {
		return "member/signup";
	}
	
	
	/** 이메일 중복검사 
	 * @return
	 */
	@ResponseBody //응답 본문으로 응답값을 돌려보냄
	@GetMapping("checkEmail") // GET 방식 /member/checkEmail 요청 매핑
	public int checkEmail(@RequestParam("memberEmail") String memberEmail) {
		
		try {
			return service.checkEmail(memberEmail);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}

	/** 닉네임 중복검사
	 * @return 중복1, 아님0
	 */
	@ResponseBody
	@GetMapping("checkNickname")
	public int checkNickname(@RequestParam("memberNickname") String memberNickname) {
		try {
			return service.checkNickname(memberNickname);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/** 회원 가입
	 * @param inputmember : 커맨드 객체(입력된 회원 정보)
	 * @param 입력한 주소 input 3개의 값을 배열로 전달
	 * @return
	 */
	@PostMapping("signup")
	public String signUp(@ModelAttribute Member inputmember, @RequestParam("memberAddress") String[] memberAddress, RedirectAttributes ra) {

		String path = null;
		
		try {
			int result = service.signUp(inputmember, memberAddress);
			
			String message = null;
			
			if(result > 0) { //성공
				message = inputmember.getMemberNickname() + "님의 가입을 환영합니다!";
				path = "/";
			} else { //실패
				message = "회원 가입 실패...";
				path = "signup";
			}
			
			ra.addFlashAttribute("message", message);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "redirect:" + path;
	}
}