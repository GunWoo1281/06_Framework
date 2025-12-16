package edu.kh.project.board.model.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kh.project.board.model.dto.Board;
import edu.kh.project.board.model.dto.PageNation;
import edu.kh.project.board.model.mapper.BoardMapper;
import jakarta.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class BoardServiceImpl implements BoardService {

	@Autowired
	private BoardMapper mapper;

	@Override
	public List<Map<String, Object>> selectBoardTypeList(ServletContext applicaton) {
		
		return mapper.selectBoardTypeList();
	}

	@Override
	public Map<String, Object> selectBoardList(int boardCode, int cp) {

		// 1. 지정된 게시판(boardCode)에서
		// 삭제되지 않은 게시글 수를 조회
		int listCount = mapper.selectListCount(boardCode);

		// 2. 1번의 결과 + cp를 이용해서
		// Pagination 객체를 생성
		// * Pagination 객체 : 게시글 목록 구성에 필요한 값을 저장한 객체
		PageNation pagenation = new PageNation(cp, listCount);

		// 3. 특정 게시판의 지정된 페이지 목록 조회
		/*
			ROWBOUNDS 객체 (MyBatis 제공 객체)
			- 지정된 크기만큼 건너뛰고(offset)
			제한된 크기만큼(limit)의 행을 조회하는 객체
			--> 페이징 처리가 굉장히 간단해짐
		 */

		int limit = pagenation.getLimit();
		int offset = (cp-1) * limit;
		RowBounds rowBounds = new RowBounds(offset, limit);

		//Mapper 메서드 호출 시 원래 전달할 수 있는 매개변수 1개
		// -> 2개를 전달할 수 있는 경우가 있음
		// Rowbounds를 이용할 때
		// 1번째 매개변수 -> SQL에 전달할 파라미터
		// 2번째 매개변수 -> Rowbounds 객체
		List<Board> boardlist = mapper.selectBoardList(boardCode, rowBounds);
		// 4. Pagination 객체 + 목록 조회 결과를 Map으로 묶음

		Map<String, Object> map = new HashMap();
		map.put("pagination", pagenation);
		map.put("boardList", boardlist);
		// 5. Map 반환
		return map;
	}

	//검색 서비스(게시글 목록 조회 참고)
	@Override
	public Map<String, Object> searchList(Map<String, Object> paramMap, int cp) {

		//1.지정된 게시판(boardCode)에서 검색 조건에 맞으면서 삭제되지 않은 게시글 수를 조회
		int listCount = mapper.getSearchCount(paramMap);
		
		PageNation pageNation = new PageNation(cp, listCount);
		
		int limit = pageNation.getLimit();
		int offset = (cp -1 ) *limit;
		RowBounds rowBounds = new RowBounds(offset,limit);
		
		List<Board> boardList = mapper.searchList(paramMap, rowBounds);
		
		Map<String, Object> map = new HashMap();
		map.put("pagination", pageNation);
		map.put("boardList", boardList);
		
		return map;
	}

	//게시글 상세 조회
	@Override
	public Board selectOne(Map<String, Integer> map) {
		
		//여러 SQL을 실행하는 방법
		//1. 하나의 Service 메서드에서 여러 mapper 메서드를 호출하는 방법
		//2. 먼저 조화된 겨로가 중 일부를 이용해서
		//	나중에 수행되는 SQL의 조건으로 삼을 수 있는 경우
		// -> Mybatis의 <resultMap>, <collection>태그를 이용해서
		//Mapper 메서드 1회 호출만으로 여러 SELECT 한번에 수행 가능
		return mapper.selectOne(map);
	}
	
}
