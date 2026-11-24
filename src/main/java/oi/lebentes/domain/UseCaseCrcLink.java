package oi.lebentes.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usecase_crc_links")
public class UseCaseCrcLink {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer projectId;
	private Integer useCaseId;
	private Integer crcCardId;

	public UseCaseCrcLink() {
	}

	public UseCaseCrcLink(Integer projectId, Integer useCaseId, Integer crcCardId) {
		this.projectId = projectId;
		this.useCaseId = useCaseId;
		this.crcCardId = crcCardId;
	}

	public Integer getId() {
		return id;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public Integer getUseCaseId() {
		return useCaseId;
	}

	public Integer getCrcCardId() {
		return crcCardId;
	}
}