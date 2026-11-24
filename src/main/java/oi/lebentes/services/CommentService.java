package oi.lebentes.services;

import java.util.List;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import oi.lebentes.domain.Comment;
import oi.lebentes.repositories.CommentRepository;

@Service
public class CommentService {

	private CommentRepository commentRepository;

	public CommentService(CommentRepository commentRepository) {
		this.commentRepository = commentRepository;
	}

	public List<Comment> list(Integer projectId, String targetType, Integer targetId) {
		return commentRepository.findByProjectIdAndTargetTypeAndTargetIdOrderByIdAsc(
				projectId, targetType, targetId
		);
	}

	public void add(Integer projectId, String targetType, Integer targetId, Integer authorId, String authorName, String text) {
		if (text == null || text.trim().isEmpty()) {
			throw new IllegalArgumentException("Comment text is required");
		}

		String clean = text.trim();
		commentRepository.save(new Comment(projectId, targetType, targetId, authorId, authorName, clean));
	}

	public Comment getOwned(Integer projectId, Integer commentId, Integer authorId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new IllegalArgumentException("Comment not found"));

		if (!Objects.equals(comment.getProjectId(), projectId)
				|| authorId == null
				|| !Objects.equals(comment.getAuthorId(), authorId)) {
			throw new AccessDeniedException("No access to comment");
		}

		return comment;
	}

	@Transactional
	public Comment update(Integer projectId, Integer commentId, Integer authorId, String text) {
		Comment comment = getOwned(projectId, commentId, authorId);

		if (text == null || text.trim().isEmpty()) {
			throw new IllegalArgumentException("Comment text is required");
		}

		comment.setText(text.trim());
		commentRepository.save(comment);
		return comment;
	}

	@Transactional
	public Comment delete(Integer projectId, Integer commentId, Integer authorId) {
		Comment comment = getOwned(projectId, commentId, authorId);
		commentRepository.delete(comment);
		return comment;
	}
}
