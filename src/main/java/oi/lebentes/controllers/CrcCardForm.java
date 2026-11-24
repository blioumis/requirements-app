package oi.lebentes.controllers;

public class CrcCardForm {
	private Integer id;
	private String className;
	private String responsibilities;
	
	private String association;
	private String dependency;
	private String aggregation;
	private String composition;
	private String inheritance;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getResponsibilities() {
		return responsibilities;
	}

	public void setResponsibilities(String responsibilities) {
		this.responsibilities = responsibilities;
	}

	public String getAssociation() {
		return association;
	}

	public void setAssociation(String association) {
		this.association = association;
	}
	
	public String getAggregation() {
		return aggregation;
	}

	public void setAggregation(String aggregation) {
		this.aggregation = aggregation;
	}
	
	public String getComposition() {
		return composition;
	}

	public void setComposition(String composition) {
		this.composition = composition;
	}
	
	public String getDependency() {
		return dependency;
	}

	public void setDependency(String dependency) {
		this.dependency = dependency;
	}
	
	public String getInheritance() {
		return inheritance;
	}

	public void setInheritance(String inheritance) {
		this.inheritance = inheritance;
	}
}