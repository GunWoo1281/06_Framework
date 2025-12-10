package edu.kh.project.myPage.model.service;

import edu.kh.project.member.model.dto.Member;

public interface MyPageService {

	int updateInfo(Member inputMember, String[] memberAddress);

	int changePw(Member inputMember);

	int checkPw(String currentPw, Member loginMember);

	int secession(Member loginMember);

}
