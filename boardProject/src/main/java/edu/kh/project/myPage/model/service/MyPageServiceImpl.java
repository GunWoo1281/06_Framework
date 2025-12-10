package edu.kh.project.myPage.model.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kh.project.member.model.dto.Member;
import edu.kh.project.myPage.model.mapper.MyPageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
@Transactional (rollbackFor = Exception.class)
@Slf4j
public class MyPageServiceImpl implements MyPageService{

	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
	@Autowired 
	private MyPageMapper mapper;

	@Override
	public int updateInfo(Member inputMember, String[] memberAddress) {
		
		if (!inputMember.getMemberAddress().equals(",,")) {
			String address = String.join("^^^", memberAddress); // 배열 -> 하나의 문자열로 변환
			inputMember.setMemberAddress(address);
		}
		else{
			inputMember.setMemberAddress(null);
		}

		// inputMember : 수정 닉네임, 수정 전화번호, 수정 주소, 회원 번호
		return mapper.updateInfo(inputMember);
	}

	@Override
	public int changePw(Member inputMember) {
		
		String encPw = bcrypt.encode(inputMember.getMemberPw());
		inputMember.setMemberPw(encPw);
			
		return mapper.changePw(inputMember);
	}

	@Override
	public int checkPw(String currentPw, Member loginMember) {

		//기존에 있던 비밀번호
		String currentEncPw = mapper.getPw(loginMember);
		//같지 않은 경우
		if (!bcrypt.matches(currentPw, currentEncPw)) {
			return 0;
		}
		//같은 경우
		return 1;
	}

	@Override
	public int secession(Member loginMember) {

		return mapper.secession(loginMember);
	}

}
