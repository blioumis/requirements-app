package oi.lebentes.controllers;

public class UseCaseForm {
	private Integer id;
	private String title;
	private String actors;
	private String preconditions;
	private String mainFlow;
	private String postconditions;
	private String alternativeFlow;
	private String postconditionsAlt;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
	    this.id = id;
	}

	public String getTitle() {
	    return title;
	}

	public void setTitle(String title) {
	    this.title = title;
	}

	public String getActors() {
	    return actors;
	}

	public void setActors(String actors) {
	    this.actors = actors;
	}

	public String getPreconditions() {
	    return preconditions;
	}

	public void setPreconditions(String preconditions) {
	    this.preconditions = preconditions;
	}

	public String getMainFlow() {
	    return mainFlow;
	}

	public void setMainFlow(String mainFlow) {
	    this.mainFlow = mainFlow;
	}

	public String getPostconditions() {
	    return postconditions;
	}

	public void setPostconditions(String postconditions) {
	    this.postconditions = postconditions;
	}
	
	public String getAlternativeFlow() { 
		return alternativeFlow; 
	}
	
	public void setAlternativeFlow(String alternativeFlow) { 
		this.alternativeFlow = alternativeFlow; 
	}

	public String getPostconditionsAlt() { 
		return postconditionsAlt; 
	}
	
	public void setPostconditionsAlt(String postconditionsAlt) { 
		this.postconditionsAlt = postconditionsAlt; 
	}
}
