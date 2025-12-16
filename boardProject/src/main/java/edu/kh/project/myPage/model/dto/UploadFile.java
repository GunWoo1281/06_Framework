package edu.kh.project.myPage.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  // 기본생성자
@AllArgsConstructor // 매개변수생성자
@Builder
public class UploadFile {
	private int fileNo;
	private String filePath;
	private String fileOriginalName;
	private String fileRename;
	private String fileUploadDate;
	private int memberNo;
	// DTO 만들때 관련된 테이블 컬럼과 반드시 동일하게 만들어야 하는건 아니다
	// 필요에 의해 필드를 더 추가해도 되고, 또는 삭제해도 된다
	private String memberNickname;
	
}