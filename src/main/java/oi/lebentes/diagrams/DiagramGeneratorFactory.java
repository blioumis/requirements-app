package oi.lebentes.diagrams;

import org.springframework.stereotype.Service;

@Service
public class DiagramGeneratorFactory {

	public UseCaseDiagramGenerator useCaseGenerator(String tool) {
		if (tool == null) {
			return new PlantUmlUseCaseDiagramGenerator();
		}
		String t = tool.trim().toLowerCase();
		if (t.equals("nomnoml")) {
			return new NomnomlUseCaseDiagramGenerator();
		}
		return new PlantUmlUseCaseDiagramGenerator();
	}

	public ClassDiagramGenerator classGenerator(String tool) {
		if (tool == null) {
			return new PlantUmlClassDiagramGenerator();
		}
		String t = tool.trim().toLowerCase();
		if (t.equals("nomnoml")) {
			return new NomnomlClassDiagramGenerator();
		}
		return new PlantUmlClassDiagramGenerator();
	}
}
