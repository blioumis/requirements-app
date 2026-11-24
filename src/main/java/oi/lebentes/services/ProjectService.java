package oi.lebentes.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import oi.lebentes.domain.Project;
import oi.lebentes.domain.ProjectMember;
import oi.lebentes.repositories.CommentRepository;
import oi.lebentes.repositories.ProjectMemberRepository;
import oi.lebentes.repositories.ProjectRepository;

@Service
public class ProjectService {
	private ProjectRepository projectRepository;
	private ProjectMemberRepository projectMemberRepository;
	private UseCaseService useCaseService;
	private CrcCardService crcCardService;
	private UseCaseCrcLinkService useCaseCrcLinkService;
	private CommentRepository commentRepository;

	public ProjectService(ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository,
			UseCaseService useCaseService, CrcCardService crcCardService,
			UseCaseCrcLinkService useCaseCrcLinkService, CommentRepository commentRepository) {
		this.projectRepository = projectRepository;
		this.projectMemberRepository = projectMemberRepository;
		this.useCaseService = useCaseService;
		this.crcCardService = crcCardService;
		this.useCaseCrcLinkService = useCaseCrcLinkService;
		this.commentRepository = commentRepository;
	}

	public List<Project> findOwnedForUser(Integer userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User id is required");
		}
		return projectRepository.findByOwnerIdOrderByNameAsc(userId);
	}

	public List<Project> findSharedForUser(Integer userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User id is required");
		}

		List<Project> out = new ArrayList<Project>();
		List<ProjectMember> memberships = projectMemberRepository.findByUserId(userId);

		for (int i = 0; i < memberships.size(); i++) {
			ProjectMember m = memberships.get(i);
			Optional<Project> opt = projectRepository.findById(m.getProjectId());
			if (opt.isPresent()) {
				out.add(opt.get());
			}
		}

		return out;
	}

	public void createProject(String name, Integer ownerId) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Project name is required");
		}
		projectRepository.save(new Project(name.trim(), ownerId));
	}

	@Transactional
	public void deleteProjectForUser(Integer projectId, Integer userId) {
		if (projectId == null) {
			throw new IllegalArgumentException("Project id is required");
		}

		Project p = getById(projectId);

		Integer ownerId = p.getOwnerId();
		if (ownerId != null) {
			if (userId == null || !ownerId.equals(userId)) {
				throw new IllegalArgumentException("Only the owner can delete the project");
			}
		}

		useCaseCrcLinkService.deleteAllForProject(projectId);
		commentRepository.deleteByProjectId(projectId);
		useCaseService.deleteAllForProject(projectId);
		crcCardService.deleteAllForProject(projectId);
		projectMemberRepository.deleteByProjectId(projectId);

		projectRepository.deleteById(projectId);
	}
	
	@Transactional
	public void leaveProject(Integer projectId, Integer userId) {
		if (projectId == null) {
			throw new IllegalArgumentException("Project id is required");
		}
		if (userId == null) {
			throw new IllegalArgumentException("User id is required");
		}

		Project p = getById(projectId);

		Integer ownerId = p.getOwnerId();
		if (ownerId != null && ownerId.equals(userId)) {
			throw new IllegalArgumentException("Owner cannot leave own project");
		}

		if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
			throw new IllegalArgumentException("You are not a member of this project");
		}

		projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
	}

	@Transactional
	public void removeMember(Integer projectId, Integer ownerId, Integer teammateUserId) {
		if (projectId == null) {
			throw new IllegalArgumentException("Project id is required");
		}
		if (teammateUserId == null) {
			throw new IllegalArgumentException("Teammate user id is required");
		}

		Project p = getById(projectId);
		Integer realOwner = p.getOwnerId();

		if (realOwner != null) {
			if (ownerId == null || !realOwner.equals(ownerId)) {
				throw new IllegalArgumentException("Only the owner can remove teammates");
			}
		}

		if (realOwner != null && realOwner.equals(teammateUserId)) {
			throw new IllegalArgumentException("Owner cannot be removed");
		}

		if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, teammateUserId)) {
			throw new IllegalArgumentException("Teammate is not a member of this project");
		}

		projectMemberRepository.deleteByProjectIdAndUserId(projectId, teammateUserId);
	}

	public Project getById(Integer id) {
		Optional<Project> opt = projectRepository.findById(id);
		if (opt.isEmpty()) {
			throw new IllegalArgumentException("Project not found");
		}
		return opt.get();
	}

	public void shareProject(Integer projectId, Integer ownerId, Integer teammateUserId) {
		if (projectId == null) {
			throw new IllegalArgumentException("Project id is required");
		}
		if (teammateUserId == null) {
			throw new IllegalArgumentException("Teammate user id is required");
		}
		if (ownerId != null && ownerId.equals(teammateUserId)) {
			throw new IllegalArgumentException("You cannot share with yourself");
		}

		Project p = getById(projectId);
		Integer realOwner = p.getOwnerId();

		if (realOwner != null) {
			if (ownerId == null || !realOwner.equals(ownerId)) {
				throw new IllegalArgumentException("Only the owner can share the project");
			}
		}

		if (projectMemberRepository.existsByProjectIdAndUserId(projectId, teammateUserId)) {
			throw new IllegalArgumentException("The user is already a teamate");
		}

		projectMemberRepository.save(new ProjectMember(projectId, teammateUserId));
	}

	public List<ProjectMember> listMembers(Integer projectId, Integer ownerId) {
		Project p = getById(projectId);
		Integer realOwner = p.getOwnerId();

		if (realOwner != null) {
			if (ownerId == null || !realOwner.equals(ownerId)) {
				throw new IllegalArgumentException("Only the owner can view members");
			}
		}

		return projectMemberRepository.findByProjectId(projectId);
	}

	public boolean canAccess(Integer projectId, Integer userId) {
		Project p = getById(projectId);

		Integer ownerId = p.getOwnerId();
		if (ownerId == null) {
			return true;
		}
		if (userId != null && ownerId.equals(userId)) {
			return true;
		}
		if (userId == null) {
			return false;
		}
		return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
	}
}