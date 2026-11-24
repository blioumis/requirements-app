package oi.lebentes.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import oi.lebentes.domain.CrcCard;
import oi.lebentes.services.CrcCardService;
import oi.lebentes.services.ProjectService;
import oi.lebentes.services.UseCaseCrcLinkService;

@Controller
public class CrcCardsController {
	private ProjectService projectService;
	private CrcCardService crcCardService;
	private UseCaseCrcLinkService useCaseCrcLinkService;

	public CrcCardsController(ProjectService projectService, CrcCardService crcCardService, UseCaseCrcLinkService useCaseCrcLinkService) {
		this.projectService = projectService;
		this.crcCardService = crcCardService;
		this.useCaseCrcLinkService = useCaseCrcLinkService;
	}

	@GetMapping("/projects/{projectId}/crccards")
	public String list(@PathVariable Integer projectId, Model model) {
		projectService.getById(projectId);

		List<CrcCard> list = crcCardService.findByProject(projectId);
		model.addAttribute("projectId", projectId);
		model.addAttribute("crccards", list);
		return "crccards";
	}

	@GetMapping("/projects/{projectId}/crccards/new")
	public String showCreate(@PathVariable Integer projectId, Model model) {
		projectService.getById(projectId);

		model.addAttribute("projectId", projectId);
		model.addAttribute("form", new CrcCardForm());
		model.addAttribute("mode", "create");
		return "crccard_form";
	}

	@PostMapping("/projects/{projectId}/crccards")
	public String create(@PathVariable Integer projectId, @ModelAttribute CrcCardForm form, Model model) {
		try {
			projectService.getById(projectId);

			String collaborations = buildCollaborationsForCreate(projectId, form.getClassName(), form);
			crcCardService.create(projectId, form.getClassName(), form.getResponsibilities(), collaborations);

			return "redirect:/projects/" + projectId + "/crccards";
		} catch (IllegalArgumentException ex) {
			model.addAttribute("projectId", projectId);
			model.addAttribute("form", form);
			model.addAttribute("mode", "create");

			if (isCollaborationError(ex.getMessage())) {
				model.addAttribute("collaborationError", ex.getMessage());
			} else {
				model.addAttribute("error", ex.getMessage());
			}

			return "crccard_form";
		}
	}

	@GetMapping("/projects/{projectId}/crccards/{crcCardId}/edit")
	public String showEdit(@PathVariable Integer projectId, @PathVariable Integer crcCardId, Model model) {
		projectService.getById(projectId);
		CrcCard card = crcCardService.get(projectId, crcCardId);

		CrcCardForm form = new CrcCardForm();
		form.setId(card.getId());
		form.setClassName(card.getClassName());
		form.setResponsibilities(card.getResponsibilities());
		fillFormCollaborations(form, card.getCollaborations());

		model.addAttribute("projectId", projectId);
		model.addAttribute("crcCardId", crcCardId);
		model.addAttribute("form", form);
		model.addAttribute("mode", "edit");
		return "crccard_form";
	}

	@PostMapping("/projects/{projectId}/crccards/{crcCardId}")
	public String update(@PathVariable Integer projectId, @PathVariable Integer crcCardId,
			@ModelAttribute CrcCardForm form, Model model) {

		try {
			projectService.getById(projectId);

			String collaborations = buildCollaborationsForUpdate(projectId, crcCardId, form.getClassName(), form);
			crcCardService.update(projectId, crcCardId, form.getClassName(), form.getResponsibilities(), collaborations);

			return "redirect:/projects/" + projectId + "/crccards";
		} catch (IllegalArgumentException ex) {
			model.addAttribute("projectId", projectId);
			model.addAttribute("crcCardId", crcCardId);
			model.addAttribute("form", form);
			model.addAttribute("mode", "edit");

			if (isCollaborationError(ex.getMessage())) {
				model.addAttribute("collaborationError", ex.getMessage());
			} else {
				model.addAttribute("error", ex.getMessage());
			}

			return "crccard_form";
		}
	}

	@PostMapping("/projects/{projectId}/crccards/{crcCardId}/delete")
	public String delete(@PathVariable Integer projectId, @PathVariable Integer crcCardId) {
		projectService.getById(projectId);
		useCaseCrcLinkService.deleteAllForCrcCard(projectId, crcCardId);
		crcCardService.delete(projectId, crcCardId);

		return "redirect:/projects/" + projectId + "/crccards";
	}

	private String buildCollaborationsForCreate(Integer projectId, String currentClassName, CrcCardForm form) {
		List<CrcCard> existing = crcCardService.findByProject(projectId);
		return buildCollaborations(existing, currentClassName, form);
	}

	private String buildCollaborationsForUpdate(Integer projectId, Integer crcCardId, String currentClassName, CrcCardForm form) {
		List<CrcCard> all = crcCardService.findByProject(projectId);
		List<CrcCard> existing = new ArrayList<CrcCard>();

		for (int i = 0; i < all.size(); i++) {
			CrcCard c = all.get(i);
			if (!c.getId().equals(crcCardId)) {
				existing.add(c);
			}
		}

		return buildCollaborations(existing, currentClassName, form);
	}

	private String buildCollaborations(List<CrcCard> existing, String currentClassName, CrcCardForm form) {
		List<String> tokens = new ArrayList<String>();

		addTyped(existing, currentClassName, tokens, "association", form.getAssociation());
		addTyped(existing, currentClassName, tokens, "dependency", form.getDependency());
		addTyped(existing, currentClassName, tokens, "aggregation", form.getAggregation());
		addTyped(existing, currentClassName, tokens, "composition", form.getComposition());
		addTyped(existing, currentClassName, tokens, "inheritance", form.getInheritance());

		String out = "";
		for (int i = 0; i < tokens.size(); i++) {
			if (i > 0) {
				out = out + ", ";
			}
			out = out + tokens.get(i);
		}
		return out;
	}

	private void addTyped(List<CrcCard> existing, String currentClassName, List<String> tokens, String type, String raw) {
		if (raw == null) {
			return;
		}
		String s = raw.trim();
		if (s.isEmpty()) {
			return;
		}

		String[] parts = s.split(",");
		for (int i = 0; i < parts.length; i++) {
			String name = parts[i].trim();
			if (name.isEmpty()) {
				continue;
			}
			if (!isValidClassName(name)) {
				throw new IllegalArgumentException("Wrong input. Put commas between classes (Class1, Class2)");
			}
			if (currentClassName != null && currentClassName.trim().equals(name)) {
				throw new IllegalArgumentException("A class cannot collaborate with itself");
			}
			if (!existsClassName(existing, name)) {
				throw new IllegalArgumentException("A class does not exist");
			}
			if (containsTargetName(tokens, name)) {
				throw new IllegalArgumentException("A class can only have one collaboration");
			}
			tokens.add(type + ":" + name);
		}
	}

	private boolean isCollaborationError(String message) {
		if (message == null) {
			return false;
		}
		if (message.equals("Wrong input. Put commas between classes (Class1, Class2)")) {
			return true;
		}
		if (message.equals("A class cannot collaborate with itself")) {
			return true;
		}
		if (message.equals("A class does not exist")) {
			return true;
		}
		if (message.equals("A class can only have one collaboration")) {
			return true;
		}
		return false;
	}

	private boolean containsTargetName(List<String> tokens, String name) {
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			int idx = token.indexOf(":");
			if (idx < 0) {
				continue;
			}
			String targetName = token.substring(idx + 1).trim();
			if (targetName.equals(name)) {
				return true;
			}
		}
		return false;
	}

	private boolean existsClassName(List<CrcCard> existing, String name) {
		for (int i = 0; i < existing.size(); i++) {
			CrcCard c = existing.get(i);
			if (c.getClassName() != null && c.getClassName().trim().equals(name)) {
				return true;
			}
		}
		return false;
	}

	private boolean isValidClassName(String s) {
		if (s == null) {
			return false;
		}
		String t = s.trim();
		if (t.isEmpty()) {
			return false;
		}
		for (int i = 0; i < t.length(); i++) {
			char ch = t.charAt(i);

			if (Character.isLetter(ch)) {
				continue;
			}
			if (Character.isDigit(ch)) {
				continue;
			}
			if (ch == '_') {
				continue;
			}
			return false;
		}
		return true;
	}

	private void fillFormCollaborations(CrcCardForm form, String collaborations) {
		form.setAssociation(joinByType(collaborations, "association"));
		form.setDependency(joinByType(collaborations, "dependency"));
		form.setAggregation(joinByType(collaborations, "aggregation"));
		form.setComposition(joinByType(collaborations, "composition"));
		form.setInheritance(joinByType(collaborations, "inheritance"));
	}

	private String joinByType(String collaborations, String type) {
		if (collaborations == null) {
			return "";
		}
		String[] parts = collaborations.replace("\n", ",").split(",");
		String out = "";

		for (int i = 0; i < parts.length; i++) {
			String token = parts[i].trim();
			if (token.isEmpty()) {
				continue;
			}

			int idx = token.indexOf(":");
			if (idx <= 0) {
				continue;
			}

			String t = token.substring(0, idx).trim().toLowerCase();
			String name = token.substring(idx + 1).trim();

			if (!t.equals(type)) {
				continue;
			}
			if (name.isEmpty()) {
				continue;
			}

			if (!out.isEmpty()) {
				out = out + ", ";
			}
			out = out + name;
		}

		return out;
	}
}