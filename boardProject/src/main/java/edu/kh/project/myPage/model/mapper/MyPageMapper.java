package edu.kh.project.myPage.model.mapper;

import org.apache.ibatis.annotations.Mapper;

import edu.kh.project.member.model.dto.Member;

@Mapper
public interface MyPageMapper {

	/** 회원 정보 수정 SQL
	 * @param inputMember
	 * @return
	 */
	int updateInfo(Member inputMember);
	
	/** 비밀번호 변경
	 * @param inputMember
	 * @return
	 */
	int changePw(Member inputMember);

	/** 비밀번호 조회
	 * @param loginMember
	 * @return
	 */
	String getPw(Member loginMember);

	/** 회원탈퇴
	 * @param loginMember
	 * @return
	 */
	int secession(Member loginMember);

}
