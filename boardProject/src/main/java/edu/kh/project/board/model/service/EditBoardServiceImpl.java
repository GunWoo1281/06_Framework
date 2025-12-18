package edu.kh.project.board.model.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import edu.kh.project.board.model.dto.Board;
import edu.kh.project.board.model.dto.BoardImg;
import edu.kh.project.board.model.mapper.EditBoardMapper;
import edu.kh.project.common.util.Utility;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@PropertySource("classpath:/config.properties")
@Transactional(rollbackFor = Exception.class)
public class EditBoardServiceImpl  implements EditBoardService{

	@Autowired
	private EditBoardMapper mapper;

	@Value("${my.board.web-path}")
	private String webPath;

	@Value("${my.board.folder-path}")
	private String folderPath;

	//게시글 작성 서비스
	@Override
	public int boardInsert(Board inputBoard, List<MultipartFile> images) throws IllegalStateException, IOException {
		//게시글 부분 (inputBoard)을 먼저 BOARD 테이블 INSERT 하기
		// -> INSERT 된 게시글의 (시퀀스 번호) 반환 받기
		int result = mapper.boardInsert(inputBoard);

		//result의 결과 행의 갯루로 다음 과정 진행 여부 결정
		if(result == 0) return 0;

		//삽입 성공 시
		//삽입된 게시글 번호를 변수로 저장
		int boardNo = inputBoard.getBoardNo();
		// -> mapper.xml에서 <selectKey> 태그를 이요해서 생성된 boardNo가 inputBoard의 boardNo에 저장된 상태 (얕은 복사 개념 이해 필수)

		//2. 업로드된 이미지가 실제로 존재할 경우 업로드된 이미지만 별도로 저장하여 BOARD_IMG 테이블에 삽입하는 코드 작성
		//실제 업로드된 이미지만 모아둘 List 생성
		List<BoardImg> uploadList = new ArrayList<>();
		
		//images 리스트에서 하나씩 꺼내어 파일이 있는지 검사
		for(int i = 0; i < images.size(); i++) {
			//업로드된 파일이 있는 경우
			if(!images.get(i).isEmpty()) {

				// 원본명
				String originalName = images.get(i).getOriginalFilename();
				// 변경명
				String rename = Utility.fileRename(originalName);

				// BoardImg DTO 생성
				BoardImg img = BoardImg.builder()
						.boardNo(boardNo)
						.imgOriginalName(originalName)
						.imgRename(rename)
						.imgPath(webPath)
						.imgOrder(i)
						.uploadFile(images.get(i))
						.build();

				// uploadList에 추가
				uploadList.add(img);
				}
		}
		
		// uploadList가 비어있다 == 실제로 제출된 파일이 하나도 없다
		if (uploadList.isEmpty()) {
			return boardNo;
		}

		// 제출된 파일이 존재할 경우
		// -> "BOARD_IMG" 테이블 INSERT + 서버에 파일 저장(transferTo)
		result = mapper.insertUploadList(uploadList);
		// result == 삽입된 행의 갯수

		//다중 INSERT 성공 확인
		if(result == uploadList.size()) {
			
			//서버에 파일 저장
			for(BoardImg img : uploadList) {
				//transferTo() : 서버에 파일 저장
				img.getUploadFile().transferTo(new File(folderPath + img.getImgRename()));
			}
		}
		else{
			//부분적으로 삽입 실패
			// ex) uploadList에 2개 저장
			// -> 1개 삽입 성공 1개는 실패
			// -> 전체 서비스 실패로 판단
			// -> 이전에 삽입된 내용 모두 rollback
			throw new RuntimeException("게시글 관련 파일 삽입 실패");
		}
		
		return boardNo;
	}
}