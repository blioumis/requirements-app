package oi.lebentes.services;

import java.util.List;

import org.springframework.stereotype.Service;

import oi.lebentes.diagrams.ClassDiagramGenerator;
import oi.lebentes.diagrams.DiagramGeneratorFactory;
import oi.lebentes.diagrams.UseCaseDiagramGenerator;
import oi.lebentes.domain.CrcCard;
import oi.lebentes.domain.UseCase;

@Service
public class DiagramService {

	private ProjectService projectService;
	private UseCaseService useCaseService;
	private CrcCardService crcCardService;
	private DiagramGeneratorFactory factory;

	public DiagramService(ProjectService projectService, UseCaseService useCaseService, CrcCardService crcCardService,
			DiagramGeneratorFactory factory) {
		this.projectService = projectService;
		this.useCaseService = useCaseService;
		this.crcCardService = crcCardService;
		this.factory = factory;
	}

	public String useCasePlantUml(Integer projectId) {
		projectService.getById(projectId);
		List<UseCase> ucs = useCaseService.findByProject(projectId);

		if (hasIncompleteUseCasesForDiagrams(ucs)) {
			return "Some Use Cases are incompleted";
		}

		UseCaseDiagramGenerator gen = factory.useCaseGenerator("plantuml");
		return gen.generate(ucs);
	}

	public String useCaseNomnoml(Integer projectId) {
		projectService.getById(projectId);
		List<UseCase> ucs = useCaseService.findByProject(projectId);

		if (hasIncompleteUseCasesForDiagrams(ucs)) {
			return "Some Use Cases are incompleted";
		}

		UseCaseDiagramGenerator gen = factory.useCaseGenerator("nomnoml");
		return gen.generate(ucs);
	}

	public String classPlantUml(Integer projectId) {
		projectService.getById(projectId);
		List<CrcCard> cards = crcCardService.findByProject(projectId);

		if (hasIncompleteCrcCardsForDiagrams(cards)) {
			return "Some CRC cards are incompleted";
		}

		ClassDiagramGenerator gen = factory.classGenerator("plantuml");
		return gen.generate(cards);
	}

	public String classNomnoml(Integer projectId) {
		projectService.getById(projectId);
		List<CrcCard> cards = crcCardService.findByProject(projectId);

		if (hasIncompleteCrcCardsForDiagrams(cards)) {
			return "Some CRC cards are incompleted";
		}

		ClassDiagramGenerator gen = factory.classGenerator("nomnoml");
		return gen.generate(cards);
	}

	//use case diagrams: actors required
	private boolean hasIncompleteUseCasesForDiagrams(List<UseCase> ucs) {
		for (int i = 0; i < ucs.size(); i++) {
			UseCase uc = ucs.get(i);
			if (isBlank(uc.getActors())) {
				return true;
			}
		}
		return false;
	}

	// for class diagrams:
	// 1) responsibilities required
	// 2) (no outgoing no incoming relation)
	private boolean hasIncompleteCrcCardsForDiagrams(List<CrcCard> cards) {
		if (cards.isEmpty()) {
			return false;
		}

		// 1) responsibilities required
		for (int i = 0; i < cards.size(); i++) {
			CrcCard c = cards.get(i);
			if (isBlank(c.getResponsibilities())) {
				return true;
			}
		}

		// degree[i] = pose sundeseis exei h karta i (incoming ή outgoing)
		int[] degree = new int[cards.size()];

		// 2) build connections based on collaborations (incoming/outgoing)
		for (int i = 0; i < cards.size(); i++) {
			CrcCard fromCard = cards.get(i);
			String fromName = safeName(fromCard.getClassName());

			List<String> tokens = splitCollaborations(fromCard.getCollaborations());
			for (int j = 0; j < tokens.size(); j++) {
				String token = tokens.get(j);
				String toName = extractTargetClassName(token);

				if (isBlank(toName)) {
					continue;
				}

				toName = safeName(toName);

				// ignore self link
				if (fromName.equals(toName)) {
					continue;
				}

				int toIndex = indexOfCard(cards, toName);
				if (toIndex >= 0) {
					degree[i] = degree[i] + 1;
					degree[toIndex] = degree[toIndex] + 1;
				}
			}
		}

		// 3) if any card has zero degree => isolated => incomplete
		for (int i = 0; i < cards.size(); i++) {
			if (degree[i] == 0) {
				return true;
			}
		}

		return false;
	}

	private int indexOfCard(List<CrcCard> cards, String className) {
		for (int i = 0; i < cards.size(); i++) {
			String n = safeName(cards.get(i).getClassName());
			if (n.equals(className)) {
				return i;
			}
		}
		return -1;
	}

	private String extractTargetClassName(String token) {
		if (token == null) {
			return "";
		}
		String t = token.trim();
		if (t.isEmpty()) {
			return "";
		}

		int idx = t.indexOf(":");
		if (idx <= 0) {
			return t; 
		}
		return t.substring(idx + 1).trim();
	}

	private List<String> splitCollaborations(String s) {
		List<String> out = new java.util.ArrayList<String>();
		if (s == null) {
			return out;
		}
		String cleaned = s.replace("\n", ",");
		String[] parts = cleaned.split(",");
		for (int i = 0; i < parts.length; i++) {
			String p = parts[i].trim();
			if (!p.isEmpty() && !out.contains(p)) {
				out.add(p);
			}
		}
		return out;
	}

	private boolean isBlank(String s) {
		if (s == null) {
			return true;
		}
		return s.trim().isEmpty();
	}

	private String safeName(String s) {
		if (s == null) {
			return "Unknown";
		}
		String t = s.trim();
		if (t.isEmpty()) {
			return "Unknown";
		}
		return t;
	}
}