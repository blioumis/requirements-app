package oi.lebentes.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import oi.lebentes.domain.UseCase;

public interface UseCaseRepository extends JpaRepository<UseCase, Integer>{
	List<UseCase> findByProjectIdOrderByUcNumberAsc(Integer projectId);

	Optional<UseCase> findByIdAndProjectId(Integer id, Integer projectId);

	public void deleteByIdAndProjectId(Integer id, Integer projectId);

	public void deleteByProjectId(Integer projectId);
	
	
}
