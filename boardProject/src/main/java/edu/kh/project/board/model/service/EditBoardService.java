package edu.kh.project.board.model.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import edu.kh.project.board.model.dto.Board;

public interface EditBoardService {

	int boardInsert(Board inputBoard, List<MultipartFile> images) throws IllegalStateException, IOException;

	int boardUpdate(Board inputBoard, List<MultipartFile> images, String deleteOrderList) throws IllegalStateException, IOException;

	int boardDelete(Board inputBoard);

}
