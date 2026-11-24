package oi.lebentes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="comments")
public class Comment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer projectId;
	private String targetType;
	private Integer targetId;

	private Integer authorId;
	private String authorName;

	@Column(columnDefinition = "TEXT")
	private String text;

	public Comment() {
	}

	public Comment(Integer projectId, String targetType, Integer targetId, Integer authorId, String authorName, String text) {
		this.projectId = projectId;
		this.targetType = targetType;
		this.targetId = targetId;
		this.authorId = authorId;
		this.authorName = authorName;
		this.text = text;
	}

	public Integer getId() {
		return id;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public String getTargetType() {
		return targetType;
	}

	public Integer getTargetId() {
		return targetId;
	}

	public Integer getAuthorId() {
		return authorId;
	}

	public String getAuthorName() {
		return authorName;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
