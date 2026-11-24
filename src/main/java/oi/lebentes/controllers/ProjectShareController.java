package oi.lebentes.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import oi.lebentes.domain.Project;
import oi.lebentes.domain.ProjectMember;
import oi.lebentes.domain.User;
import oi.lebentes.repositories.UserRepository;
import oi.lebentes.services.ProjectService;
import oi.lebentes.services.UserService;

@Controller
public class ProjectShareController {
	
	private ProjectService projectService;
	private UserService userService;
	private UserRepository userRepository;

	public ProjectShareController(ProjectService projectService, UserService userService, UserRepository userRepository) {
		this.projectService = projectService;
		this.userService = userService;
		this.userRepository = userRepository;
	}

	@GetMapping("/projects/{projectId}/share")
	public String showShare(@PathVariable() Integer projectId, Model model, Authentication auth) {
		User me = userService.getByEmail(auth.getName());
		Project project = projectService.getById(projectId);

		List<User> teammates = loadTeammates(projectId, me.getId());

		model.addAttribute("project", project);
		model.addAttribute("teammates", teammates);
		model.addAttribute("form", new ProjectShareForm());
		return "project_share";
	}

	@PostMapping("/projects/{projectId}/share")
	public String addShare(@PathVariable() Integer projectId,
			@ModelAttribute() ProjectShareForm form,
			Model model,
			Authentication auth) {

		User me = userService.getByEmail(auth.getName());
		Project project = projectService.getById(projectId);

		try {
			if (form.getEmail() == null || form.getEmail().trim().isEmpty()) {
				throw new IllegalArgumentException("Email is required");
			}

			User teammate = userService.getByEmail(form.getEmail().trim());
			projectService.shareProject(projectId, me.getId(), teammate.getId());

			return "redirect:/projects/" + projectId + "/share";
		} catch (IllegalArgumentException ex) {
			List<User> teammates = loadTeammates(projectId, me.getId());

			model.addAttribute("project", project);
			model.addAttribute("teammates", teammates);
			model.addAttribute("form", form);
			model.addAttribute("error", ex.getMessage());
			return "project_share";
		}
	}

	@PostMapping("/projects/{projectId}/share/remove")
	public String removeShare(@PathVariable() Integer projectId,
			@RequestParam() Integer teammateId,
			Model model,
			Authentication auth) {

		User me = userService.getByEmail(auth.getName());
		Project project = projectService.getById(projectId);

		try {
			projectService.removeMember(projectId, me.getId(), teammateId);
			return "redirect:/projects/" + projectId + "/share";
		} catch (IllegalArgumentException ex) {
			List<User> teammates = loadTeammates(projectId, me.getId());

			model.addAttribute("project", project);
			model.addAttribute("teammates", teammates);
			model.addAttribute("form", new ProjectShareForm());
			model.addAttribute("error", ex.getMessage());
			return "project_share";
		}
	}

	private List<User> loadTeammates(Integer projectId, Integer ownerId) {
		List<ProjectMember> members = projectService.listMembers(projectId, ownerId);
		List<User> teammates = new ArrayList<User>();

		for (int i = 0; i < members.size(); i++) {
			ProjectMember m = members.get(i);
			Optional<User> u = userRepository.findById(m.getUserId());
			if (u.isPresent()) {
				teammates.add(u.get());
			}
		}

		return teammates;
	}
}