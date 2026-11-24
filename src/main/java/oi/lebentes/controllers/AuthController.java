package oi.lebentes.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import oi.lebentes.services.UserService;

@Controller
public class AuthController {
	private UserService userService;
	
	public AuthController(UserService userService) {
		this.userService=userService;
	}
	
	@GetMapping("/register")
	public String showRegister(Model model) {
		model.addAttribute("form",new RegisterForm());
		return "register";
	}
	
	@GetMapping("/register/")
	public String showRegisterSlash() {
		return "redirect:/register";
	}
	
	@PostMapping("/register")
	public String doRegister(@ModelAttribute RegisterForm form,Model model) {
		try {
			userService.register(form.getName(), form.getEmail(), form.getPassword());
			return "redirect:/login";	
		}catch (IllegalArgumentException ex) {
			model.addAttribute("error",ex.getMessage());
			return "register";
		}
	}
	
	@GetMapping("/login")
	public String showLogin() {
		return "login";
	}
	
	@GetMapping("/login/")
	public String showLoginSlash() {
		return "redirect:/login";
	}
}
