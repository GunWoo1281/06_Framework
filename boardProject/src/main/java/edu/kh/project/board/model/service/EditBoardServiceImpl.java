package edu.kh.project.board.model.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	@Override
	public int boardUpdate(Board inputBoard, List<MultipartFile> images, String deleteOrderList) throws IllegalStateException, IOException {
		
		//1. 게시글 부분(제목/내용) 수정
		int result = mapper.boardUpdate(inputBoard);

		// 수정 실패 시 바로 리턴
		if (result==0) return 0;

		//2. 기존 이미지 삭제 (deleteOrderList가 있는 경우)
		if(deleteOrderList !=  null && !deleteOrderList.equals("")) {
			Map<String, Object> map = new HashMap<>();
			map.put("deleteOrderList", deleteOrderList);
			map.put("boardNo", inputBoard.getBoardNo());
			
			//BOARD_IMG 에 존재하는 행을 삭제하는 SQL 호출
			result = mapper.deleteImg(map);

			//삭제 실패한 경우
			if (result==0) {
				throw new RuntimeException("게시글 관련 파일 삭제 실패");
			}
		}

		//3. 새로 업로드된 이미지 처리
		// (클라이언트가 실제로 업로드한 이미지가 있는 경우)
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
						.boardNo(inputBoard.getBoardNo())
						.imgOriginalName(originalName)
						.imgRename(rename)
						.imgPath(webPath)
						.imgOrder(i)
						.uploadFile(images.get(i))
						.build();

				// uploadList에 추가
				uploadList.add(img);

				//4. 업로드 하려는 이미지 정보(img) 이용해서 수정 또는 삽입 수행
				//1. 기존 O -> 새 이미지로 변경 -> 수정
				result = mapper.updateImg(img);

				if (result==0) {
					//수정 실패 == 기존 해당 순서(IMG_ORDER)에 이미지가 없었음
					//-> 삽입 수행

					//2) 기존 X -> 새 이미지 추가
					result = mapper.insertImg(img);
				}

				//수정(삽입)이 실패한 경우
				if (result==0) { 
					throw new RuntimeException("게시글 관련 파일 수정/삽입 실패");
				}
			}
		}

		//5. 선택한 파일이 없을 경우
		if(uploadList.isEmpty()){
			return result;
		}

		//6. 새 이미지 파일을 서버에 저장 
		for(BoardImg img : uploadList) {
			//transferTo() : 서버에 파일 저장
			img.getUploadFile().transferTo(new File(folderPath + img.getImgRename()));
		}
		
		return result;
	}

	@Override
	public int boardDelete(Board inputBoard) {
		return mapper.boardDelete(inputBoard);
	}
}