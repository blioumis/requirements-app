package oi.lebentes.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="project_members")
public class ProjectMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer projectId;
	private Integer userId;

	public ProjectMember() {
	}

	public ProjectMember(Integer projectId, Integer userId) {
		this.projectId = projectId;
		this.userId = userId;
	}

	public Integer getId() {
		return id;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public Integer getUserId() {
		return userId;
	}
}
