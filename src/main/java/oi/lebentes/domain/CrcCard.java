package oi.lebentes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "crc_cards")
public class CrcCard {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer projectId;

	@Column(nullable = false)
	private String className;

	@Column(columnDefinition = "TEXT")
	private String responsibilities;

	@Column(columnDefinition = "TEXT")
	private String collaborations;

	public CrcCard() {
	}

	public CrcCard(Integer projectId, String className, String responsibilities, String collaborations) {
		this.projectId = projectId;
		this.className = className;
		this.responsibilities = responsibilities;
		this.collaborations = collaborations;
	}

	public Integer getId() {
		return id;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public String getClassName() {
		return className;
	}

	public String getResponsibilities() {
		return responsibilities;
	}

	public String getCollaborations() {
		return collaborations;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setResponsibilities(String responsibilities) {
		this.responsibilities = responsibilities;
	}

	public void setCollaborations(String collaborations) {
		this.collaborations = collaborations;
	}
}