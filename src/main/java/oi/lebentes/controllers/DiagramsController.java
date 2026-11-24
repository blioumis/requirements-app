package oi.lebentes.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import oi.lebentes.services.DiagramService;

@Controller
public class DiagramsController {

	private DiagramService diagramService;

	public DiagramsController(DiagramService diagramService) {
		this.diagramService = diagramService;
	}

	@GetMapping("/projects/{projectId}/diagrams")
	public String page(@PathVariable Integer projectId, Model model) {

		model.addAttribute("projectId", projectId);

		model.addAttribute("usecasePlantUml", diagramService.useCasePlantUml(projectId));
		model.addAttribute("usecaseNomnoml", diagramService.useCaseNomnoml(projectId));

		model.addAttribute("classPlantUml", diagramService.classPlantUml(projectId));
		model.addAttribute("classNomnoml", diagramService.classNomnoml(projectId));

		return "diagrams";
	}
}