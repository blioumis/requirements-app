package oi.lebentes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import oi.lebentes.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

	List<Comment> findByProjectIdAndTargetTypeAndTargetIdOrderByIdAsc(
			Integer projectId, String targetType, Integer targetId
	);

	public void deleteByProjectId(Integer projectId);
}
