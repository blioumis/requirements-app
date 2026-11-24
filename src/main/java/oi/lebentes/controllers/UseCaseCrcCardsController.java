package oi.lebentes.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import oi.lebentes.domain.CrcCard;
import oi.lebentes.domain.UseCase;
import oi.lebentes.services.CrcCardService;
import oi.lebentes.services.ProjectService;
import oi.lebentes.services.UseCaseCrcLinkService;
import oi.lebentes.services.UseCaseService;

@Controller
public class UseCaseCrcCardsController {
	private ProjectService projectService;
	private UseCaseService useCaseService;
	private CrcCardService crcCardService;
	private UseCaseCrcLinkService useCaseCrcLinkService;

	public UseCaseCrcCardsController(ProjectService projectService, UseCaseService useCaseService,CrcCardService crcCardService, UseCaseCrcLinkService useCaseCrcLinkService) {
		this.projectService = projectService;
		this.useCaseService = useCaseService;
		this.crcCardService = crcCardService;
		this.useCaseCrcLinkService = useCaseCrcLinkService;
	}

	@GetMapping("/projects/{projectId}/usecases/{useCaseId}/crccards")
	public String page(@PathVariable Integer projectId, @PathVariable Integer useCaseId, Model model) {
		load(projectId, useCaseId, model);
		return "usecase_crccards";
	}

	@PostMapping("/projects/{projectId}/usecases/{useCaseId}/crccards/link")
	public String link(@PathVariable Integer projectId, @PathVariable Integer useCaseId,@RequestParam Integer crcCardId, Model model) {
		try {
			projectService.getById(projectId);
			useCaseCrcLinkService.link(projectId, useCaseId, crcCardId);
			return "redirect:/projects/" + projectId + "/usecases/" + useCaseId + "/crccards";
		} catch (IllegalArgumentException ex) {
			load(projectId, useCaseId, model);
			model.addAttribute("error", ex.getMessage());
			return "usecase_crccards";
		}
	}

	@PostMapping("/projects/{projectId}/usecases/{useCaseId}/crccards/unlink")
	public String unlink(@PathVariable Integer projectId, @PathVariable Integer useCaseId,@RequestParam Integer crcCardId) {
		projectService.getById(projectId);
		useCaseCrcLinkService.unlink(projectId, useCaseId, crcCardId);
		return "redirect:/projects/" + projectId + "/usecases/" + useCaseId + "/crccards";
	}

	private void load(Integer projectId, Integer useCaseId, Model model) {
		projectService.getById(projectId);

		UseCase uc = useCaseService.get(projectId, useCaseId);
		List<CrcCard> cards = crcCardService.findByProject(projectId);
		List<Integer> linkedIds = useCaseCrcLinkService.linkedCrcCardIds(projectId, useCaseId);

		model.addAttribute("projectId", projectId);
		model.addAttribute("useCase", uc);
		model.addAttribute("cards", cards);
		model.addAttribute("linkedIds", linkedIds);
	}
}