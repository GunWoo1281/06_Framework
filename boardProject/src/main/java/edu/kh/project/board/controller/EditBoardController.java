package edu.kh.project.board.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import edu.kh.project.board.model.service.BoardService;
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
	private final BoardService boardService;

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


	@GetMapping("{boardCode:[0-9]+}/{boardNo:[0-9]+}/update")
	public String boardUpdate(@PathVariable("boardCode") int boardCode, @PathVariable("boardNo") int boardNo,
	@SessionAttribute("loginMember") Member loginMember, Model model, RedirectAttributes ra) {

		//수정 화면에 출력할 기존의 제목/내용/이미지 조회
		Map<String, Integer> map = new HashMap<>();
		map.put("boardCode", boardCode);
		map.put("boardNo", boardNo);

		Board board = boardService.selectOne(map);

		String message = null;
		String path = null;

		if (board == null) {
			message = "해당 게시글이 존재하지 않습니다.";
			path = "redirect:/"; //메인 페이지로 리다이렉트

			ra.addFlashAttribute("message", message);
		}
		else if(board != null && board.getMemberNo() != loginMember.getMemberNo()){
			message = "자신이 작성한 글만 수정할 수 있습니다.";

			//해당 글 상세조회 리다이렉트(board/1/2000)
			path = String.format("redirect:/board/%d/%d", boardCode, boardNo);

			ra.addFlashAttribute("message", message);
		}
		else{
			path = "board/boardUpdate";
			model.addAttribute("board", board);
		}

		return path;
	}


	/** 게시글 수정
	 * @param boardCode
	 * @param boardNo
	 * @param inputBoard
	 * @param images
	 * @param loginMember
	 * @param deleteOrderList
	 * @param ra
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@PostMapping("{boardCode:[0-9]+}/{boardNo:[0-9]+}/update")
	public String boardUpdate(@PathVariable("boardCode") int boardCode, @PathVariable("boardNo") int boardNo, 
						@ModelAttribute Board inputBoard, @RequestParam("images") List<MultipartFile> images,
						@SessionAttribute("loginMember") Member loginMember, @RequestParam(value = "deleteOrderList", required = false) String deleteOrderList, @RequestParam(value = "cp", required = false, defaultValue = "1") int cp, RedirectAttributes ra) throws IllegalStateException, IOException{

		//1. 커맨드 객체(inputBoard)에 boardCode, boardNo, memberNo 세팅
		inputBoard.setBoardCode(boardCode);
		inputBoard.setBoardNo(boardNo);
		inputBoard.setMemberNo(loginMember.getMemberNo());

		//inputBoard -> 제목, 내용, boardCode, boardNo, 회원번호

		//2. 게시글 수정 서비스 호출 후 결과 반환 받기
		int result = service.boardUpdate(inputBoard, images, deleteOrderList);

		//3. 서비스 결과에 따라 응답 제어
		String path = null;
		String message = null;

		if (result > 0) {
			path = "/board/" + boardCode + "/" + boardNo + "?cp=" + cp;
			message = "게시글이 수정 되었습니다.";
		} else {
			path = "update"; //상대 경로
			message = "수정 실패";
		}

		ra.addFlashAttribute("message", message);

		return "redirect:" + path;
	}

	@RequestMapping("{boardCode:[0-9]+}/{boardNo:[0-9]+}/delete")
	public String boardDelete(@PathVariable("boardCode") int boardCode, @PathVariable("boardNo") int boardNo, 
	@ModelAttribute Board inputBoard,
	@SessionAttribute("loginMember") Member loginMember, @RequestParam(value = "cp", required = false, defaultValue = "1") int cp, RedirectAttributes ra){
		//1. 커맨드 객체(inputBoard)에 boardCode, boardNo, memberNo 세팅
		inputBoard.setBoardCode(boardCode);
		inputBoard.setBoardNo(boardNo);
		inputBoard.setMemberNo(loginMember.getMemberNo());

		int result = service.boardDelete(inputBoard);

		String path = null;
		String message = null;

		if (result>0) {
			path = "/board/" + boardCode;
			message = "게시글이 삭제처리 되었습니다.";
		} else {
			path = "/board/" + boardCode + "/" + boardNo + "?cp=" + cp;
			message = "게시글 삭제처리 실패";
		}	

		ra.addFlashAttribute("message", message);
		return "redirect:" + path;
	}
}
