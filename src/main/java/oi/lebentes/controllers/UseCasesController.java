package oi.lebentes.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import oi.lebentes.domain.CrcCard;
import oi.lebentes.domain.UseCase;
import oi.lebentes.services.CrcCardService;
import oi.lebentes.services.ProjectService;
import oi.lebentes.services.UseCaseCrcLinkService;
import oi.lebentes.services.UseCaseService;

@Controller
public class UseCasesController {
	private ProjectService projectService;
	private UseCaseService useCaseService;
	private UseCaseCrcLinkService useCaseCrcLinkService;
	private CrcCardService crcCardService;

	public UseCasesController(ProjectService projectService, UseCaseService useCaseService,
			UseCaseCrcLinkService useCaseCrcLinkService, CrcCardService crcCardService) {
		this.projectService = projectService;
		this.useCaseService = useCaseService;
		this.useCaseCrcLinkService = useCaseCrcLinkService;
		this.crcCardService = crcCardService;
	}

	// US9: list use cases
	@GetMapping("/projects/{projectId}/usecases")
	public String list(@PathVariable Integer projectId, Model model) {
		projectService.getById(projectId);
		List<UseCase> list = useCaseService.findByProject(projectId);

		Collections.sort(list, new Comparator<UseCase>() {
			@Override
			public int compare(UseCase a, UseCase b) {
				if (a.getUcNumber() == null && b.getUcNumber() == null) {
					return 0;
				}
				if (a.getUcNumber() == null) {
					return 1;
				}
				if (b.getUcNumber() == null) {
					return -1;
				}
				return a.getUcNumber().compareTo(b.getUcNumber());
			}
		});

		Map<Integer, List<String>> linksByUseCaseId = new LinkedHashMap<Integer, List<String>>();

		for (int i = 0; i < list.size(); i++) {
			UseCase uc = list.get(i);
			List<Integer> linkedIds = useCaseCrcLinkService.linkedCrcCardIds(projectId, uc.getId());
			List<String> linkedClassNames = new ArrayList<String>();

			for (int j = 0; j < linkedIds.size(); j++) {
				Integer crcCardId = linkedIds.get(j);
				CrcCard card = crcCardService.get(projectId, crcCardId);
				linkedClassNames.add(card.getClassName());
			}

			linksByUseCaseId.put(uc.getId(), linkedClassNames);
		}

		model.addAttribute("projectId", projectId);
		model.addAttribute("usecases", list);
		model.addAttribute("linksByUseCaseId", linksByUseCaseId);
		return "usecases";
	}

	// US7: show create form
	@GetMapping("/projects/{projectId}/usecases/new")
	public String showCreate(@PathVariable Integer projectId, Model model) {
		projectService.getById(projectId);
		model.addAttribute("projectId", projectId);
		model.addAttribute("form", new UseCaseForm());
		model.addAttribute("mode", "create");
		return "usecase_form";
	}

	// US7: create
	@PostMapping("/projects/{projectId}/usecases")
	public String create(@PathVariable Integer projectId, @ModelAttribute UseCaseForm form, Model model) {
		try {
			projectService.getById(projectId);
			useCaseService.create(projectId, form.getTitle(), form.getActors(), form.getPreconditions(),
					form.getMainFlow(), form.getPostconditions(), form.getAlternativeFlow(),
					form.getPostconditionsAlt());
			return "redirect:/projects/" + projectId + "/usecases";
		} catch (IllegalArgumentException ex) {
			model.addAttribute("projectId", projectId);
			model.addAttribute("form", form);
			model.addAttribute("mode", "create");
			model.addAttribute("error", ex.getMessage());
			return "usecase_form";
		}
	}

	// US8: show edit
	@GetMapping("/projects/{projectId}/usecases/{useCaseId}/edit")
	public String showEdit(@PathVariable Integer projectId, @PathVariable Integer useCaseId, Model model) {
		projectService.getById(projectId);
		UseCase uc = useCaseService.get(projectId, useCaseId);

		UseCaseForm form = new UseCaseForm();
		form.setId(uc.getId());
		form.setTitle(uc.getTitle());
		form.setActors(uc.getActors());
		form.setPreconditions(uc.getPreconditions());
		form.setMainFlow(uc.getMainFlow());
		form.setPostconditions(uc.getPostconditions());
		form.setAlternativeFlow(uc.getAlternativeFlow());
		form.setPostconditionsAlt(uc.getPostconditionsAlt());

		model.addAttribute("projectId", projectId);
		model.addAttribute("useCaseId", useCaseId);
		model.addAttribute("ucNumber", uc.getUcNumber());
		model.addAttribute("form", form);
		model.addAttribute("mode", "edit");
		return "usecase_form";
	}

	// US8: update
	@PostMapping("/projects/{projectId}/usecases/{useCaseId}")
	public String update(@PathVariable Integer projectId, @PathVariable Integer useCaseId,
			@ModelAttribute UseCaseForm form, Model model) {
		try {
			projectService.getById(projectId);
			useCaseService.update(projectId, useCaseId, form.getTitle(), form.getActors(), form.getPreconditions(),
					form.getMainFlow(), form.getPostconditions(), form.getAlternativeFlow(),
					form.getPostconditionsAlt());
			return "redirect:/projects/" + projectId + "/usecases";
		} catch (IllegalArgumentException ex) {
			UseCase uc = useCaseService.get(projectId, useCaseId);
			model.addAttribute("projectId", projectId);
			model.addAttribute("useCaseId", useCaseId);
			model.addAttribute("ucNumber", uc.getUcNumber());
			model.addAttribute("form", form);
			model.addAttribute("mode", "edit");
			model.addAttribute("error", ex.getMessage());
			return "usecase_form";
		}
	}

	// US10: delete
	@PostMapping("/projects/{projectId}/usecases/{useCaseId}/delete")
	public String delete(@PathVariable Integer projectId, @PathVariable Integer useCaseId) {
		projectService.getById(projectId);
		useCaseService.delete(projectId, useCaseId);
		return "redirect:/projects/" + projectId + "/usecases";
	}
}