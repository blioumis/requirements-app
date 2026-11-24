package oi.lebentes.diagrams;

import java.util.List;

import oi.lebentes.domain.UseCase;

public interface UseCaseDiagramGenerator {
	String generate(List<UseCase> useCases);
}
