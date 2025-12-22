package edu.kh.project.board.model.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.RowBounds;

import edu.kh.project.board.model.dto.Board;

@Mapper
public interface BoardMapper {

	/**게시판 종류 조회 SQL 수행
	 * @return
	 */
	List<Map<String, Object>> selectBoardTypeList();

	/** 게시글 수 조회 SQL 수행
	 * @param boardCode
	 * @return
	 */
	int selectListCount(int boardCode);

	/** 특정게시판의 지정된페이지 목록 조회 SQL 수행
	 * @param boardCode
	 * @param rowBounds
	 * @return
	 */
	List<Board> selectBoardList(int boardCode, RowBounds rowBounds);

	int getSearchCount(Map<String, Object> paramMap);

	List<Board> searchList(Map<String, Object> paramMap, RowBounds rowBounds);

	Board selectOne(Map<String, Integer> map);

	/** 조회수 1 증가 SQL
	 * @param boardNo
	 * @return
	 */
	int updateReadCount(int boardNo);

	/** 조회 수 조회 SQL
	 * @param boardNo
	 * @return
	 */
	int selectReadCount(int boardNo);

	/**게시판 좋아요 해제
	 * @param map
	 * @return
	 */
	int deleteBoardLike(Map<String, Integer> map);

	/**게시판 좋아요 등록
	 * @param map
	 * @return
	 */
	int insertBoardLike(Map<String, Integer> map);

	/**게시판 좋아요 갯수 확인
	 * @param integer
	 * @return
	 */
	int selectLikeCount(int boardNo);

	/** DB 이미지 파일명 목록 조회
	 * @return
	 */
	List<String> selectImageList();
	
}
