package edu.kh.project.board.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.kh.project.board.model.dto.Board;
import edu.kh.project.board.model.service.EditBoardService;
import edu.kh.project.member.model.dto.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("editBoard")
@RequiredArgsConstructor
@Slf4j
public class EditBoardController {

	private final EditBoardService service;

	@GetMapping("{boardCode:[0-9]+}/insert")
	public String boardInsert(@PathVariable("boardCode") int boardCode) {

		return "board/boardWrite";
	}

	/** 게시글 작성
	 * @param boardCode
	 * @param inputBoard : 입력된 값(제목,내용)세팅되어 있음 (커맨드 객체)
	 * @param loginMember : 로그인한 회원 번호를 얻어오는 용도(세션에 등록되어 있음)
	 * @param images : 제출된 file 타입 input태그가 전달한 데이터를 (이미지 파일..)
	 * @return 
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */
	@PostMapping("{boardCode:[0-9]+}/insert")
	public String boardInsert(@PathVariable("boardCode") int boardCode, @ModelAttribute Board inputBoard,
								@SessionAttribute("loginMember") Member loginMember,
								@RequestParam("images") List<MultipartFile> images,
								RedirectAttributes ra) throws IllegalStateException, IOException{

		log.debug("images@@@@@@@@@@@@@@@@@@@@@ : " + images);
		// [MultipartFile,MultipartFile,MultipartFile,MultipartFile,MultipartFile]

		/*
			List<MultipartFile> images는 실제로 제출된 파일이 있던 없던 무조건
			길이 5의 MultipartFile이 요소로 있는 List가 제출됨

			무작정 서버에 저장 X
			-> List의 각 인덱스에 들어있는 MultipartFile에 실제로 제출된 파일이 있는지 확인하는 로직을 구성
			+ List 요소의 index 번호 == IMG_ORDER 와 같음
		*/

		// 1. boardCode, 로그인한 회원 번호를 inputBoard에 세팅
		inputBoard.setBoardCode(boardCode);
		inputBoard.setMemberNo(loginMember.getMemberNo());
		// -> inputBoard 총 네가지 세팅됨(boardCode, memberNo, boardTitle, boardContent)

		// 2. 서비스 호출 후 결과 반환 받기
		// -> 성공 시 [상세 조회]를 요청할 수있도록
		// 삽입된 게시글 번호를 반환받기
		int boardNo = service.boardInsert(inputBoard, images);

		//3. 서비스 결과에 따라 message, 리다이렉트 경로 지정
		String path = null;
		String message = null;

		if (boardNo > 0) {
			path = "/board/" + boardCode + "/" + boardNo;
			message = "게시글이 작성 되었습니다!";
		} else {
			path = "insert"; //상대 경로
			message = "게시글 작성 실패";
		}

		ra.addFlashAttribute("message", message);
		return "redirect:" + path;
	}
}
