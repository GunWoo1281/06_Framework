package edu.kh.project.board.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.kh.project.board.model.dto.Board;
import edu.kh.project.board.model.dto.BoardImg;
import edu.kh.project.board.model.service.BoardService;
import edu.kh.project.member.model.dto.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("board")
@Slf4j
public class BoardController {

    @Autowired
    private BoardService service;


    /*
        게시글 목록 조회
        {boardCode}
        - /board/xxx : /board 이하 1레벨 자리에 어떤 주소값이 들어오든 모두 이 메서드에 매핑

        - /board/ 이하 1레벨자리에 숫자로된 요청 주소가 작성되어 있을 때만 동작 하고 싶음
        -> 정규표현식

        {boardCode:[0-9]+}
        [0-9] : 한 칸에 0~9 사이 숫자 입력 가능
        [0-9]+ : 모든 숫자
    */
    @GetMapping("{boardCode:[0-9]+}")
    public String selectBoardList(@PathVariable("boardCode") int boardCode,
                    @RequestParam(value = "cp", required = false, defaultValue = "1") int cp,
                    Model model,
                    @RequestParam Map<String, Object> paramMap
                    ) {
        
        //조회 서비스 호출 후 결과 반환
        Map<String, Object> map = null;

        //검색이 아닌 경우 --> paramMap은 null
        if (paramMap.get("key")==null) {
            map = service.selectBoardList(boardCode, cp);
        } else { //검색인 경우
        	// --> paramMap에 key라는 k에 접근하면 매핑된 value 반환
        	// --> ex) {key=w, query=짱구}
        	// --> --> w 반환됨
        	
            //검색 (내가 검색하고 싶은 게시글 목록 조회) 서비스 호출 
        	paramMap.put("boardCode", boardCode);
        	// -> paramMap {key=w, query=짱구, boardCode=1}
        	
        	map = service.searchList(paramMap, cp);
        }

        model.addAttribute("pagination", map.get("pagination"));
		model.addAttribute("boardList", map.get("boardList"));

        return "board/boardList";
    }

    @GetMapping("{boardCode:[0-9]+}/{boardNo:[0-9]+}")
    public String boardDetail(@PathVariable("boardCode") int boardCode,
                            @PathVariable("boardNo") int boardNo,
                            @SessionAttribute(value = "loginMember" , required = false) Member loginMember,
                            Model model,
                            RedirectAttributes ra,
                            HttpServletRequest req, //요청에 담긴 쿠키 얻어오기
                            HttpServletResponse resp //쿠키 응답하기
    		) {
        //게시글 상세 조회 서비스 호출
        Map<String, Integer> map = new HashMap<>();
        map.put("boardCode", boardCode);
        map.put("boardNo", boardNo);

        //로그인 상태인 경우에만 memberNo를 map추가
        //LIKE_CHECK 이용(로그인한 사람이 좋아요 누른 게시글인지 체크하기 위함)
        if(loginMember != null) {
            map.put("memberNo", loginMember.getMemberNo());
        }

        //서비스 호출
        Board board = service.selectOne(map);

        String path = null;
        if(board == null) {
            path = "redriect:/board/"+boardCode;
            ra.addFlashAttribute("message", "게시글이 존재하지 않습니다.");
            return path; //내가 현재 보고 있는 게시판 목록으로 재요청
        }
        else{   //조회결과가 있는 경우
            /* ------------------------- 쿠키를 이용한 조회 수 증가 시작----------------------------*/
            //비회원 또는 로그인한 회원의 글이 아닌 경우 (== 글쓴이를 뺸 다른 사람)
            if(loginMember == null || board.getMemberNo() != loginMember.getMemberNo()) {
                Cookie[] cookies = req.getCookies();
                Cookie c = null;
                
                for(Cookie temp : cookies){
                    //쿠키 중에 "readBoardNo" 가 존재할 때
                    if(temp.getName().equals("readBoardNo")) {
                        c = temp;
                        break;
                    }

                    int result = 0;
                    
                    //"readBoardNo" 가 쿠키에 없을 때
                    if (c==null) {
                        //쿠키 생성
                        c = new Cookie("readBoardNo", "["+boardNo+"]");
                        result = service.updateReadCount(boardNo);

                    } else { //"readBoardNo" 가 쿠키에 있을 때
                        // k : v
                        //readBoardNo : [2][30][400][2000]

                        //현재 글을 처음 읽는 경우
                        if(c.getValue().indexOf("["+boardNo+"]") == -1){

                            //해당 글 번호를 쿠키에 누적 + 서비스 호출
                            c.setValue(c.getValue() + "[" + boardNo + "]");
                            result = service.updateReadCount(boardNo);
                        }
                    }
                    
                    if(result > 0){
                        //앞서 조회했던 board의 readCount 값을
                        //result 값을 다시 세팅
                        board.setReadCount(result);

                        //쿠키 적용 경로 설정
                        // "/" 이하 경로 요청 시 쿠키 서버로 전달

                        //쿠키 적용 경로 설정
                        c.setPath("/"); // "/" 이하 경로 요청 시 쿠키를 서버로 전달

                        //쿠키 수명 지정
                        //현재 시간을 얻어오기
                        LocalDateTime now = LocalDateTime.now();

                        //다음날 자정 지정
                        LocalDateTime nextDayMidnight = now.plusDays(1).withHour(0).withMinute(0).withSecond(0);

                        //다음날 자정까지 남은 시간 계산(초단위)
                        long secondUntilNextDay = Duration.between(now, nextDayMidnight).toMillis();

                        //쿠키 수명 지정
                        c.setMaxAge((int)secondUntilNextDay);
                        resp.addCookie(c);
                    }
                }
            }


            /* ------------------------- 쿠키를 이용한 조회 수 증가 끝 ----------------------------*/
            path = "board/boardDetail";

            //board - 게시글 일반 내용 + imageList + commentList
            model.addAttribute("board", board);

            //조회된 이미지 목록(imageList)이 있을 경우
            if (!board.getImageList().isEmpty()) {
                BoardImg thumbnail = null;

                //imageList 0번 인덱스 == IMG_ORDER가 가장 빠른 순서
                if (board.getImageList().get(0).getImgOrder()==0) {
                    thumbnail = board.getImageList().get(0);
                }

                model.addAttribute("thumbnail", thumbnail);
                model.addAttribute("start", thumbnail != null ? 1 : 0);
                //썸네일 있을때 : start 1
                //썸네일 없을때 : start 0
            }
        }

        return path;
    }


    @ResponseBody
    @PostMapping("like")
    public int boardLike(@RequestBody Map<String, Integer> map) {
        return service.boardLike(map);
    }
}
