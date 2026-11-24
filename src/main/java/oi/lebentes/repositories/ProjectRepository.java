package oi.lebentes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import oi.lebentes.domain.Project;

public interface ProjectRepository extends JpaRepository<Project, Integer>{
	List<Project> findByOwnerIdOrderByNameAsc(Integer ownerId);
}
