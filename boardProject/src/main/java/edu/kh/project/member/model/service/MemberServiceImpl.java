package edu.kh.project.member.model.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kh.project.member.model.dto.Member;
import edu.kh.project.member.model.mapper.MemberMapper;
import lombok.extern.slf4j.Slf4j;

@Transactional(rollbackFor = Exception.class)
@Service
@Slf4j
public class MemberServiceImpl implements MemberService {
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
	@Autowired
	private MemberMapper mapper;
	
	//로그인 서비스
	@Override
	public Member login(Member inputmember) throws Exception{

		//암호화
		//bycryptPassword = bcrypt.encode(inputmember.getMemberPw());
		
		Member loginMember = mapper.login(inputmember.getMemberEmail());
		
		log.debug("loginMember@@@@@@@@@@@@@@@@@ : " + loginMember);
		
		if(loginMember == null) {
			return null;
		}
		
		//입력 받은 비밀번호(평문 : inputmember.getMemberPw()) 와 암호화된 비밀번호(loginMember.getMemberPw()) 비교
		//bcrypt.matches(평문, 암호화) : 평문과 암호화가 내부적으로 일치한다고 판단이 되면 true, 아니면 false
		if(!bcrypt.matches(inputmember.getMemberPw(), loginMember.getMemberPw())) {
			return null;
		}
		
		loginMember.setMemberPw(null); //비밀번호 제거
		
		return loginMember;
	}

	@Override
	public int checkEmail(String memberEmail) throws Exception {

		return mapper.checkEmail(memberEmail);
	}

	@Override
	public int checkNickname(String memberNickname) throws Exception {
		return mapper.checkNickname(memberNickname);
	}

	@Override
	public int signUp(Member inputmember , String[] memberAddress) throws Exception {
		// 주소가 입력된 경우
		if(!inputmember.getMemberPw().equals("")) {
			String address = String.join("^^^", memberAddress); // 배열 -> 하나의 문자열로 변환
			inputmember.setMemberAddress(address);
		}
		else {
			inputmember.setMemberAddress(null);
		}

		//비밀번호 암호화
		String encPw = bcrypt.encode(inputmember.getMemberPw());
		inputmember.setMemberPw(encPw);
		return mapper.signUp(inputmember);
	}

}
