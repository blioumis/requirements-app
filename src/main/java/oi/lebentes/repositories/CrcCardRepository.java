package oi.lebentes.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import oi.lebentes.domain.CrcCard;

public interface CrcCardRepository extends JpaRepository<CrcCard, Integer> {

	List<CrcCard> findByProjectIdOrderByClassNameAsc(Integer projectId);

	Optional<CrcCard> findByIdAndProjectId(Integer id, Integer projectId);

	Optional<CrcCard> findByProjectIdAndClassName(Integer projectId, String className);

	Optional<CrcCard> findByProjectIdAndClassNameAndIdNot(Integer projectId, String className, Integer id);

	void deleteByIdAndProjectId(Integer id, Integer projectId);

	void deleteByProjectId(Integer projectId);
}