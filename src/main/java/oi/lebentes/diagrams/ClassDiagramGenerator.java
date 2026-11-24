package oi.lebentes.diagrams;

import java.util.List;

import oi.lebentes.domain.CrcCard;

public interface ClassDiagramGenerator {
	String generate(List<CrcCard> cards);
}
