package oi.lebentes.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import oi.lebentes.domain.User;
import oi.lebentes.services.UserService;

@Controller
public class WorkspaceController {

	private UserService userService;

	public WorkspaceController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/workspace")
	public String workspace(Model model, Authentication auth) {
		User user = userService.getByEmail(auth.getName());
		model.addAttribute("currentUsername", user.getName());
		return "workspace";
	}

	@GetMapping("/workspace/")
	public String workspaceSlash() {
		return "redirect:/workspace";
	}
}