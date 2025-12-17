package edu.kh.project.board.model.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import edu.kh.project.board.model.dto.Board;
import jakarta.servlet.ServletContext;

@Service
public interface BoardService {

	/** 게시판 종류 조회 서비스
	 * @param applicaton
	 * @return
	 */
	List<Map<String, Object>> selectBoardTypeList(ServletContext applicaton);

	
	/** 특정 게시판의 지정된 페이지 목록 조회
	 * @param boardCode (특정 게시판)
	 * @param cp (지정된 페이지)
	 * @return
	 */
	Map<String, Object> selectBoardList(int boardCode, int cp);


	/**검색 서비스(게시글 목록 조회 참고)
	 * @param paramMap
	 * @param cp
	 * @return
	 */
	Map<String, Object> searchList(Map<String, Object> paramMap, int cp);

	/**게시글 상세 조회
	 * @param map
	 * @return
	 */
	Board selectOne(Map<String, Integer> map);


	/** 조회 수 1 증가 서비스
	 * @param boardNo
	 * @return
	 */
	int updateReadCount(int boardNo);


	/** 게시판 좋아요 체크/해제 서비스
	 * @param map
	 * @return
	 */
	int boardLike(Map<String, Integer> map);

}
