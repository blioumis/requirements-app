package oi.lebentes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="projects")
public class Project {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable= false)
	private String name;
	private Integer ownerId;
	
	public Project() {
	}
	
	public Project(String name, Integer ownerId) {
		this.name = name;
		this.ownerId=ownerId;
	}
	
	public Integer getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public Integer getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}
}
