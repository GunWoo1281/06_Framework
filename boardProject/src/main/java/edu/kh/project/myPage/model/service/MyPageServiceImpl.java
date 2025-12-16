package edu.kh.project.myPage.model.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import edu.kh.project.common.util.Utility;
import edu.kh.project.member.model.dto.Member;
import edu.kh.project.myPage.model.dto.UploadFile;
import edu.kh.project.myPage.model.mapper.MyPageMapper;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
@PropertySource("classpath:config.properties")
public class MyPageServiceImpl implements MyPageService {

	@Autowired
	private MyPageMapper mapper;

	// BCrypt 암호화 객체 의존성 주입(SecurityConfig 참고)
	@Autowired
	private BCryptPasswordEncoder bcrypt;

	@Value("${my.profile.web-path}")
	private String profileWebPath;

	@Value("${my.profile.folder-path}")
	private String profileFolderPath;


	// 회원 정보 수정
	@Override
	public int updateInfo(Member inputMember, String[] memberAddress) {

		// 입력된 주소가 있을 경우
		// A^^^B^^^C 형태로 가공

		// 주소가 입력되었을 때
		if (!inputMember.getMemberAddress().equals(",,")) {
			String address = String.join("^^^", memberAddress);
			inputMember.setMemberAddress(address);

		} else {
			// 주소가 입력되지 않았을 때
			inputMember.setMemberAddress(null);
		}

		// inputMember : 수정 닉네임, 수정 전화번호, 수정 주소,
		// 회원 번호

		return mapper.updateInfo(inputMember);
	}

	// 비밀번호 변경 서비스
	@Override
	public int changePw(Map<String, Object> paramMap, int memberNo) {

		// 1. 현재 비밀번호가 일치하는지 확인하기
		// - 현재 로그인한 회원의 암호화된 비밀번호를 DB에서 조회
		String originPw = mapper.selectPw(memberNo);

		// 입력받은 현재 비밀번호와(평문)
		// DB에서 조회한 비밀번호(암호화)를 비교
		// -> BCryptPasswordEncoder.matches(평문, 암호화된비밀번호) 사용

		// 다를 경우
		if (!bcrypt.matches((String) paramMap.get("currentPw"), originPw)) {
			return 0;
		}

		// 2. 같을경우

		// 새 비밀번호를 암호화 (BCryptPasswordEncoder.encode(평문))
		String encPw = bcrypt.encode((String) paramMap.get("newPw"));

		// 진행후 DB에 업데이트
		// SQL 전달해야하는 데이터 2개 (암호화한 새 비밀번호, 회원번호)
		// -> SQL 전달 인자 1개뿐!
		// -> 묶어서 전달 (paramMap 재활용)

		paramMap.put("encPw", encPw);
		paramMap.put("memberNo", memberNo);

		return mapper.changePw(paramMap);
	}

	// 회원 탈퇴 서비스
	@Override
	public int secession(String memberPw, int memberNo) {

		// 1. 현재 로그인한 회원의 암호화된 비밀번호를 DB에서 조회
		String encPw = mapper.selectPw(memberNo);

		// 2. 입력받은 비번 & 암호화된 DB 비번 같은지 비교

		// 다를 경우
		if (!bcrypt.matches(memberPw, encPw)) {
			return 0;
		}

		// 같은 경우
		return mapper.secession(memberNo);
	}

	// 파일 업로드 테스트 1
	@Override
	public String fileUpload1(MultipartFile uploadFile) throws Exception {

		if (uploadFile.isEmpty()) { // 업로드한 파일이 없을 경우
			return null;
		}

		// 업로드한 파일이 있을 경우
		// C:/uploadFiles/test/파일명 으로 서버에 저장
		uploadFile.transferTo(new File("C:/uploadFiles/test/" + uploadFile.getOriginalFilename()));

		// C:/uploadFiles/test/하기와.jpg

		// 웹 에서 해당 파일에 접근할 수 있는 경로를 만들어 반환

		// 이미지가 최종 저장된 서버 컴퓨터상의 경로
		// C:/uploadFiles/test/파일명.jpg

		// 클라이언트가 브라우저에 해당 이미지를 보기위해 요청하는 경로
		// ex) <img src="경로">
		// /myPage/file/파일명.jpg -> <img src="/myPage/file/파일명.jpg">

		return "/myPage/file/" + uploadFile.getOriginalFilename();
	}

	// 파일 업로드 테스트 2 (서버 저장, DB 저장)
	@Override
	public int fileUpload2(MultipartFile uploadFile, int memberNo) throws Exception {

		// MultipartFile이 제공하는 메소드
		// - isEmpty() : 업로드된 파일이 없을경우 true / 있다면 false
		// - getSize() : 파일 크기
		// - getOriginalFileName() : 원본 파일명
		// - transferTo(경로) :
		// 메모리 또는 임시 저장 경로에 업로드된 파일을
		// 원하는 경로에 실제로 전송(서버 어떤 폴더에 저장할지 지정)

		// 업로드된 파일이 없다면
		if (uploadFile.isEmpty()) {
			return 0;
		}

		// 업로드된 파일이 있다면

		// 1. 서버에 저장될 서버 폴더 경로 만들기

		// 파일이 저장될 서버 폴더 경로
		String folderPath = "C:/uploadFiles/test/";

		// 클라이언트가 파일이 저장된 폴더에 접근할 수 있는 주소(요청 주소)
		String webPath = "/myPage/file/";

		// 2. DB에 전달할 데이터를 DTO로 묶어서 INSERT
		// webPath, memberNo, 원본파일명, 변경된파일명
		String fileRename = Utility.fileRename(uploadFile.getOriginalFilename());

		// Builder 패턴을 이용해서 UploadFile 객체 생성
		// 장점 1) 반복되는 참조변수명, set 구문 생략
		// 장점 2) method chaining을 이용하여 한줄로 작성 가능
		UploadFile uf = UploadFile.builder().memberNo(memberNo).filePath(webPath)
				.fileOriginalName(uploadFile.getOriginalFilename()).fileRename(fileRename).build();

		int result = mapper.insertUploadFile(uf);

		// 3. 삽입 (INSERT) 성공 시 파일을 지정된 서버 폴더에 저장
		// 삽입 실패 시
		if (result == 0)
			return 0;

		// 삽입 성공 시
		// C:/uploadFiles/test/변경된파일명 으로
		// 파일을 서버 컴퓨터에 저장!
		uploadFile.transferTo(new File(folderPath + fileRename));
		// C:/uploadFiles/test/20251211100330_00001.jpg

		return result; // 1
	}

	
	// 파일 목록 조회 서비스
	@Override
	public List<UploadFile> fileList(int memberNo) {
		return mapper.fileList(memberNo);
	}

	//여러 파일 업로드 서비스
	@Override
	public int fileUpload3(List<MultipartFile> aaaList, List<MultipartFile> bbbList, int memberNo) throws Exception {
		//1. aaaList 처리
		
		int result1 = 0;
		
		for(MultipartFile file : aaaList) {
			if(file.isEmpty()){	//파일이 없으면 다음 파일
				continue; //아래코드 수행 X 다음 반복으로
			}
		
			result1 += fileUpload2(file, memberNo);
			
		}
		
		//2. bbbList 처리
		int result2 = 0;

		for(MultipartFile file : bbbList) {
			if(file.isEmpty()){	//파일이 없면 다음 파일
				continue; //아래코드 수행 X 다음 반복으로
			}
			
			result2 += fileUpload2(file, memberNo);
		}
		
		if(result1 > 0 && result2 > 0) {
			return result1 + result2;
		}

		return 0;
	}	

}