package oi.lebentes.controllers;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import oi.lebentes.domain.Project;
import oi.lebentes.domain.User;
import oi.lebentes.services.ProjectService;
import oi.lebentes.services.UserService;

@Controller
public class ProjectsController {

	private ProjectService projectService;
	private UserService userService;

	public ProjectsController(ProjectService projectService, UserService userService) {
		this.projectService = projectService;
		this.userService = userService;
	}

	@GetMapping("/projects")
	public String showProjects(Model model, Authentication auth) {
		User user = userService.getByEmail(auth.getName());

		List<Project> ownedProjects = projectService.findOwnedForUser(user.getId());
		List<Project> sharedProjects = projectService.findSharedForUser(user.getId());

		model.addAttribute("ownedProjects", ownedProjects);
		model.addAttribute("sharedProjects", sharedProjects);
		model.addAttribute("form", new ProjectForm());
		model.addAttribute("currentUserId", user.getId());
		model.addAttribute("currentUsername", user.getName());
		return "projects";
	}

	@PostMapping("/projects")
	public String createProject(@ModelAttribute ProjectForm form, Model model, Authentication auth) {
		User user = userService.getByEmail(auth.getName());

		try {
			projectService.createProject(form.getName(), user.getId());
			return "redirect:/projects";
		} catch (IllegalArgumentException ex) {
			List<Project> ownedProjects = projectService.findOwnedForUser(user.getId());
			List<Project> sharedProjects = projectService.findSharedForUser(user.getId());

			model.addAttribute("ownedProjects", ownedProjects);
			model.addAttribute("sharedProjects", sharedProjects);
			model.addAttribute("form", form);
			model.addAttribute("currentUserId", user.getId());
			model.addAttribute("currentUsername", user.getName());
			model.addAttribute("error", ex.getMessage());
			return "projects";
		}
	}

	@PostMapping("/projects/delete")
	public String deleteProject(@RequestParam Integer id, Model model, Authentication auth) {
		User user = userService.getByEmail(auth.getName());

		try {
			projectService.deleteProjectForUser(id, user.getId());
			return "redirect:/projects";
		} catch (IllegalArgumentException ex) {
			List<Project> ownedProjects = projectService.findOwnedForUser(user.getId());
			List<Project> sharedProjects = projectService.findSharedForUser(user.getId());

			model.addAttribute("ownedProjects", ownedProjects);
			model.addAttribute("sharedProjects", sharedProjects);
			model.addAttribute("form", new ProjectForm());
			model.addAttribute("currentUserId", user.getId());
			model.addAttribute("currentUsername", user.getName());
			model.addAttribute("error", ex.getMessage());
			return "projects";
		}
	}

	@PostMapping("/projects/leave")
	public String leaveProject(@RequestParam Integer id, Model model, Authentication auth) {
		User user = userService.getByEmail(auth.getName());

		try {
			projectService.leaveProject(id, user.getId());
			return "redirect:/projects";
		} catch (IllegalArgumentException ex) {
			List<Project> ownedProjects = projectService.findOwnedForUser(user.getId());
			List<Project> sharedProjects = projectService.findSharedForUser(user.getId());

			model.addAttribute("ownedProjects", ownedProjects);
			model.addAttribute("sharedProjects", sharedProjects);
			model.addAttribute("form", new ProjectForm());
			model.addAttribute("currentUserId", user.getId());
			model.addAttribute("currentUsername", user.getName());
			model.addAttribute("error", ex.getMessage());
			return "projects";
		}
	}
}