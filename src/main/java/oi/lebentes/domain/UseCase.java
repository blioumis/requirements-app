package oi.lebentes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="use_cases")
public class UseCase {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private Integer projectId;
	private String title;
	private String actors;

	@Column(columnDefinition = "TEXT")
	private String preconditions;

	@Column(columnDefinition = "TEXT")
	private String mainFlow;

	@Column(columnDefinition = "TEXT")
	private String postconditions;

	@Column(columnDefinition = "TEXT")
	private String alternativeFlow;

	@Column(columnDefinition = "TEXT")
	private String postconditionsAlt;

	private Integer ucNumber; //Gia otan sbhno UC na ginetai sosta h arithmisi tou epomenou

	public UseCase() {
	}

	public UseCase(Integer projectId, String title, String actors, String preconditions, String mainFlow, String postconditions, String alternativeFlow, String postconditionsAlt) {
		this.projectId = projectId;
	    this.title = title;
	    this.actors = actors;
	    this.preconditions = preconditions;
	    this.mainFlow = mainFlow;
	    this.postconditions = postconditions;
	    this.alternativeFlow=alternativeFlow;
	    this.postconditionsAlt=postconditionsAlt;
	}

	public Integer getId() {
	    return id;
	}

	public Integer getProjectId() {
	    return projectId;
	}

	public String getTitle() {
		return title;
	}

	public String getActors() {
	    return actors;
	}

	public String getPreconditions() {
	    return preconditions;
	}

	public String getMainFlow() {
	    return mainFlow;
	}

	public String getPostconditions() {
	    return postconditions;
	}
	
	public String getAlternativeFlow() { 
		return alternativeFlow; 
	}
	
	public String getPostconditionsAlt() { 
		return postconditionsAlt; 
	}

	public void setTitle(String title) {
	    this.title = title;
	}

	public void setActors(String actors) {
	    this.actors = actors;
	}

	public void setPreconditions(String preconditions) {
	    this.preconditions = preconditions;
	}
	
	public void setMainFlow(String mainFlow) {
	    this.mainFlow = mainFlow;
	}

	public void setPostconditions(String postconditions) {
	    this.postconditions = postconditions;
	}  
	
	public void setAlternativeFlow(String alternativeFlow) { 
		this.alternativeFlow = alternativeFlow; 
	}
	
	public void setPostconditionsAlt(String postconditionsAlt) { 
		this.postconditionsAlt = postconditionsAlt; 
	}
	
	public Integer getUcNumber() {
		return ucNumber;
	}
	
	public void setUcNumber(Integer ucNumber) {
		this.ucNumber= ucNumber;
	}
}
