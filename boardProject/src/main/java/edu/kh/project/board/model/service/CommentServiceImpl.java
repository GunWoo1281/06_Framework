package edu.kh.project.board.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kh.project.board.model.dto.Comment;
import edu.kh.project.board.model.mapper.CommentMapper;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class CommentServiceImpl implements CommentService{

	@Autowired
	private CommentMapper mapper;

	@Override
	public List<Comment> select(int boardNo) {
		return mapper.select(boardNo);
	}

	@Override
	public int insert(Comment comment) {
		return mapper.insert(comment);
	}

	@Override
	public int delete(int commentNo) {
		return mapper.delete(commentNo);
	}

	@Override
	public int update(Comment comment) {
		return mapper.update(comment);
	}
}
