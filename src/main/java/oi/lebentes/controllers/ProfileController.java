package oi.lebentes.controllers;

import java.security.Principal;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import oi.lebentes.domain.User;
import oi.lebentes.services.UserService;

@Controller
public class ProfileController {
	private UserService userService;
	
	public ProfileController(UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping("/profile")
	public String showProfile(Principal principal, Model model) {
		User user = userService.getByEmail(principal.getName());
		
		ProfileForm form = new ProfileForm();
		form.setName(user.getName());
		form.setEmail(user.getEmail());
		
		model.addAttribute("form", form);
		return "profile";
	}
	
	@PostMapping("/profile")
	public String updateProfile(@ModelAttribute("form") ProfileForm form, Principal principal, Model model,
			HttpServletRequest request) {
		try {
			boolean emailChanged = userService.updateProfile(principal.getName(), form.getName(), form.getEmail(),
					form.getPassword());
			
			if (emailChanged) {
				SecurityContextHolder.clearContext();
				HttpSession session = request.getSession(false);
				if (session != null) {
					session.invalidate();
				}
				return "redirect:/login";
			}
			return "redirect:/workspace";
		} catch (IllegalArgumentException ex) {
			model.addAttribute("form", form);
			model.addAttribute("error", ex.getMessage());
			return "profile";
		}
	}

	@PostMapping("/profile/delete")
	public String deleteAccount(Principal principal, Model model, HttpServletRequest request) {
		try {
			userService.deleteAccount(principal.getName());

			SecurityContextHolder.clearContext();
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}

			return "redirect:/login";
		} catch (IllegalArgumentException ex) {
			User user = userService.getByEmail(principal.getName());

			ProfileForm form = new ProfileForm();
			form.setName(user.getName());
			form.setEmail(user.getEmail());

			model.addAttribute("form", form);
			model.addAttribute("error", ex.getMessage());
			return "profile";
		}
	}
}