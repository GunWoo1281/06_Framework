package edu.kh.project.member.model.service;

import edu.kh.project.member.model.dto.Member;

public interface MemberService {

	
	/** 로그인
	 * @param inputmember
	 * @return loginMember
	 * @throws Exception
	 */
	Member login(Member inputmember) throws Exception;

	/** 이메일 중복검사 서비스
	 * @param memberEmail
	 * @return
	 * @throws Exception
	 */
	int checkEmail(String memberEmail) throws Exception;

	/**닉네임 중복검사 서비스
	 * @param memberNickname
	 * @return
	 * @throws Exception 
	 */
	int checkNickname(String memberNickname) throws Exception;

	/** 회원 가입 서비스
	 * @param inputmember
	 * @return
	 * @throws Exception
	 */
	int signUp(Member inputmember, String[] memberAddress) throws Exception;

}
