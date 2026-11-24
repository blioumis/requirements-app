package oi.lebentes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import oi.lebentes.domain.UseCaseCrcLink;

public interface UseCaseCrcLinkRepository extends JpaRepository<UseCaseCrcLink, Integer> {
	List<UseCaseCrcLink> findByProjectIdAndUseCaseId(Integer projectId, Integer useCaseId);
	
	boolean existsByProjectIdAndUseCaseIdAndCrcCardId(Integer projectId, Integer useCaseId, Integer crcCardId);

	void deleteByProjectIdAndUseCaseIdAndCrcCardId(Integer projectId, Integer useCaseId, Integer crcCardId);

	void deleteByProjectIdAndUseCaseId(Integer projectId, Integer useCaseId);

	void deleteByProjectIdAndCrcCardId(Integer projectId, Integer crcCardId);

	void deleteByProjectId(Integer projectId);
}