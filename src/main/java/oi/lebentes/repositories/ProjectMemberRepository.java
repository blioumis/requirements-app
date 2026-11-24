package oi.lebentes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import oi.lebentes.domain.ProjectMember;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {

	List<ProjectMember> findByUserId(Integer userId);

	List<ProjectMember> findByProjectId(Integer projectId);

	public boolean existsByProjectIdAndUserId(Integer projectId, Integer userId);

	public void deleteByProjectId(Integer projectId);

	public void deleteByProjectIdAndUserId(Integer projectId, Integer userId);

	public void deleteByUserId(Integer userId);
}