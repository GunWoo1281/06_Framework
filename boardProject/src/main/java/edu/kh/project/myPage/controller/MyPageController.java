package edu.kh.project.myPage.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.kh.project.member.model.dto.Member;
import edu.kh.project.myPage.model.dto.UploadFile;
import edu.kh.project.myPage.model.service.MyPageService;
import lombok.extern.slf4j.Slf4j;

/*
 * @SessionAttributes 의 역할
 * - Model에 추가된 속성 중 key 값이 일치하는 속성을 session scope로 변경하는 어노테이션
 * - 클래스 상단에 @SessionAttributes({"loginMember"}) 작성
 * 
 * @SessionAttribute 의 역할
 * - @SessionAttributes를 통해 session에 등록된 속성을 꺼내올 때 사용하는 어노테이션
 * - 메서드의 매개변수에 @SessionAttribute("loginMember") Member loginMember 작성
 * 
 * */

@SessionAttributes({"loginMember"})
@Controller
@RequestMapping("myPage")
@Slf4j
public class MyPageController {
	
	@Autowired
	private MyPageService service;
	
	// 내 정보 조회로 이동
	/**
	 * @param loginMember : 세션에 존재하는 loginMember를 얻어와 Member타입 매개변수 대입
	 * @return
	 */
	@GetMapping("info") //  /myPage/info GET 방식 요청 매핑
	public String info(@SessionAttribute("loginMember") Member loginMember, 
						Model model) {
		
		// 현재 로그인한 회원의 주소를 꺼내옴
		// 현재 로그인한 회원 정보 -> session scope에 등록된 상태(loginMember)
		// loginMember(memberAddress도 포함)
		// -> 만약 회원가입 당시 주소를 입력했다면 주소값 문자열(^^^ 구분자로 만들어진 문자열)
		// ->      회원가입 당시 주소를 입력하지 않았다면 null
		
		String memberAddress = loginMember.getMemberAddress();
		// 03189^^^서울 종로구 우정국로2길 21^^^3층, 302클래스 (대왕빌딩)
		// or null
		
		if(memberAddress != null) { // 주소가 있을 경우에만 동작
			// 구분자 "^^^" 를 기준으로
			// memberAddress 값을 쪼개어 String[] 로 반환
			String[] arr = memberAddress.split("\\^\\^\\^");
			// ["03189", "서울 종로구 우정국로2길 21", "3층, 302클래스 (대왕빌딩)"]
			
			model.addAttribute("postcode", arr[0]); // 우편주소
			model.addAttribute("address",  arr[1]); // 도로명/지번주소
			model.addAttribute("detailAddress", arr[2]); // 상세주소
			
		}
		
		
		return "myPage/myPage-info";
	}
	
	// 프로필 이미지 변경 화면 이동
	@GetMapping("profile") //  /myPage/profile GET 방식 요청 매핑
	public String profile() {
		return "myPage/myPage-profile";
	}
	
	// 비밀번호 변경 화면 이동
	@GetMapping("changePw") //  /myPage/changePw GET 방식 요청 매핑
	public String changePw() {
		return "myPage/myPage-changePw";
	}
	
	// 회원 탈퇴 화면 이동
	@GetMapping("secession") //  /myPage/secession GET 방식 요청 매핑
	public String secession() {
		return "myPage/myPage-secession";
	}
	
	// 파일 테스트 화면으로 이동
	@GetMapping("fileTest")  //  /myPage/fileTest  GET 방식 요청 매핑
	public String fileTest() {
		return "myPage/myPage-fileTest";
	}
	
	// 파일 목록 조회 화면 이동
	@GetMapping("fileList") //   /myPage/fileList  GET 방식 요청 매핑
	public String fileList(Model model, 
			@SessionAttribute("loginMember") Member loginMember) {
		
		// 파일 목록 조회 서비스 호출(현재 로그인한 회원이 올린 이미지만)
		int memberNo = loginMember.getMemberNo();
		List<UploadFile> list = service.fileList(memberNo);
		
		// model에 list 담아서 forward
		model.addAttribute("list", list);
		
		return "myPage/myPage-fileList";
	}
	
	
	/** 회원 정보 수정 
	 * @param inputMember : 커맨드 객체(@ModelAttribute가 생략된 상태) 
	 * 						제출된 memberNickname, memberTel 세팅된 상태
	 * @param memberAddress : 주소만 따로 배열형태로 얻어옴 
	 * @param loginMember : 로그인한 회원 정보 
	 * 						(현재 로그인한 회원의 회원번호(PK) 사용할 예정)
	 * 
	 * @return
	 */
	@PostMapping("info") // /myPage/info POST 방식 요청 매핑
	public String updateInfo(Member inputMember, 
							@RequestParam("memberAddress") String[] memberAddress,
							@SessionAttribute("loginMember") Member loginMember,
							RedirectAttributes ra) {
		
		// inputMember에 현재 로그인한 회원 번호 추가
		inputMember.setMemberNo( loginMember.getMemberNo() );
		// inputMember : 수정된 회원의 닉네임, 수정된 회원의 전화번호, [주소], 회원번호
		
		// 회원 정보 수정 서비스 호출
		int result = service.updateInfo(inputMember, memberAddress);
		
		String message = null;
		
		if(result > 0) {
			message = "회원 정보 수정 성공!!!";
			
			// loginMember에 DB상 업데이트된 내용으로 세팅
			// -> loginMember는 세션에 저장된 로그인한 회원 정보가 
			//   저장되어있다 (로그인 할 당시의 기존 데이터)
			// -> loginMember를 수정하면 세션에 저장된 로그인한 회원의 
			//   정보가 업데이트 된다
			// == Session에 있는 회원 정보와 DB 데이터를 동기화
			
			loginMember.setMemberNickname( inputMember.getMemberNickname() );
			loginMember.setMemberTel( inputMember.getMemberTel() );
			loginMember.setMemberAddress( inputMember.getMemberAddress() );
			
		} else {
			message = "회원 정보 수정 실패...";
			
		}
		
		ra.addFlashAttribute("message", message);
				
		return "redirect:info"; // 재요청 경로 : /myPage/info GET 요청
		
	}
	
	/** 비밀번호 변경
	 * @param paramMap : 모든 파라미터를 맵으로 저장
	 * @param loginMember : 세션에 등록된 현재 로그인한 회원 정보
	 * @param ra : 리다이렉트시 메시지 전달 역할
	 * @return
	 */
	@PostMapping("changePw")    //  /myPage/changePw   POST 요청 매핑
	public String changePw(@RequestParam Map<String, Object> paramMap,
						@SessionAttribute("loginMember") Member loginMember,
						RedirectAttributes ra ) {
	
		// 로그인한 회원 번호
		int memberNo = loginMember.getMemberNo();
		
		// 현재 + 새 (paramMap) + 회원 번호(memberNo)를 서비스로 전달
		int result = service.changePw(paramMap, memberNo);
		
		String path = null;
		String message = null;
		
		if(result > 0) {
			//변경 성공 시 
			//메시지 "비밀번호가 변경 되었습니다";
			//리다이렉트 /myPage/info
			message = "비밀번호가 변경 되었습니다";
			path = "/myPage/info";
			
		} else {
			//변경 실패 시
			//메시지 "현재 비밀번호가 일치하지 않습니다";
			//리다이렉트 /myPage/changePw
			message = "현재 비밀번호가 일치하지 않습니다";
			path = "/myPage/changePw";
			
		}

		ra.addFlashAttribute("message", message);
		
		return "redirect:" + path;
	}
	
	
	/** 회원 탈퇴
	 * @param memberPw : 제출받은(입력한) 비밀번호
	 * @param loginMember : 로그인한 회원 정보 저장 객체(세션에서 꺼내옴)
	 * 						-> 회원번호 필요!(SQL에서 조건으로 사용)
	 * @param status : @SessionAttributes() 와 함께 사용!
	 * @return
	 */
	@PostMapping("secession") // /myPage/secession  POST 요청 매핑
	public String secession(@RequestParam("memberPw") String memberPw, 
							@SessionAttribute("loginMember") Member loginMember,
							SessionStatus status, 
							RedirectAttributes ra) {
		
		// 로그인한 회원의 회원번호 꺼내오기
		int memberNo = loginMember.getMemberNo();
		
		// 서비스 호출 (입력받은 비밀번호, 로그인한 회원번호)
		int result = service.secession(memberPw, memberNo);
		
		String message = null;
		String path = null;
		
		if(result > 0) {
			message = "탈퇴 되었습니다.";
			path = "/";
			
			status.setComplete(); // 세션 비우기(로그아웃 상태 변경)
			
		} else {
			
			message = "비밀번호 일치하지 않습니다";
			path = "secession";
			
		}
		
		// 탈퇴 성공 - 메인페이지 재요청
		// 탈퇴 실패 - 탈퇴 페이지로 재요청
		ra.addFlashAttribute("message", message);
		
		return "redirect:" + path;
	}
	
	/*
	 * Spring에서 파일을 처리하는 방법
	 * 
	 * - enctype="multipart/form-data" 로 클라이언트의 요청을 받으면
	 *   (문자, 숫자, 파일 등이 섞여있는 요청)
	 * 
	 *   이를 MultipartResolver(FileConfig에 정의)를 이용해서
	 *   섞여있는 파라미터를 분리 작업을 함
	 *   
	 *   문자열, 숫자 -> String
	 *   파일         -> MultipartFile
	 * 	 
	 * 
	 * */
	
	@PostMapping("file/test1")  //  /myPage/file/test1  POST 요청 매핑
	public String fileUpload1(@RequestParam("uploadFile") MultipartFile uploadFile,
							RedirectAttributes ra) {
		
		try {
			String path = service.fileUpload1(uploadFile);
			// /myPage/file/파일명.jpg
			
			// 파일이 실제로 서버 컴퓨터에 저장이 되어
			// 웹에서 접근할 수 있는 경로가 반환되었을 때
			if(path != null) {
				ra.addFlashAttribute("path", path);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			log.info("파일 업로드 예제1 중 예외 발생");
		}
		
		return "redirect:/myPage/fileTest";
	}
	
	@PostMapping("file/test2")  // /myPage/file/test2 POST 요청 매핑
	public String fileUpload2(@RequestParam("uploadFile") MultipartFile uploadFile, 
							@SessionAttribute("loginMember") Member loginMember,
							RedirectAttributes ra) {
		
		try {
			
			// 로그인한 회원의 번호 얻어오기 (누가 업로드 했는가)
			int memberNo = loginMember.getMemberNo();
			
			// 업로드된 파일 정보를 DB에 INSERT 후 결과 행의 갯수 반환 받아옴
			int result = service.fileUpload2(uploadFile, memberNo);
			
			String message = null;
			
			if(result > 0) {
				message = "파일 업로드 성공";
				
			} else {
				message = "파일 업로드 실패..";
			}
			
			ra.addFlashAttribute("message", message);
			
		} catch (Exception e) {
			e.printStackTrace();
			log.info("파일 업로드 테스트2 중 예외발생");
		}
		
		return "redirect:/myPage/fileTest";
	}
	
	@PostMapping("file/test3")  // /myPage/file/test3 POST 요청 매핑
	public String fileUpload3(@RequestParam("aaa") List<MultipartFile> aaaList, @RequestParam("bbb") List<MultipartFile> bbbList,
							@SessionAttribute("loginMember") Member loginMember,
							RedirectAttributes ra) throws Exception
	{
		
		int result = service.fileUpload3(aaaList, bbbList, loginMember.getMemberNo());


		return "redirect:/myPage/fileTest";
	}
	
	@PostMapping("profile")
	public String profile(@RequestParam("profileImg") MultipartFile profileImg,
							@SessionAttribute("loginMember") Member loginMember,
							RedirectAttributes ra) throws Exception {

		

		return "redirect:profile";
	}
}