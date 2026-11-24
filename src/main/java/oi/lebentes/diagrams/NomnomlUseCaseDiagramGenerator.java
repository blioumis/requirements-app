package oi.lebentes.diagrams;

import java.util.ArrayList;
import java.util.List;

import oi.lebentes.domain.UseCase;

public class NomnomlUseCaseDiagramGenerator implements UseCaseDiagramGenerator {

	@Override
	public String generate(List<UseCase> useCases) {
		String out = "";
		out = out + "#direction: right\n\n";

		for (int k = 0; k < useCases.size(); k++) {
			UseCase uc = useCases.get(k);

			int num = 0;
			Integer n = uc.getUcNumber();
			if (n != null) {
				num = n.intValue();
			}

			String ucNode = "[" + safe("UC" + num + ": " + uc.getTitle()) + "]";
			List<String> ucActors = splitActors(uc.getActors());

			if (ucActors.isEmpty()) {
				out = out + ucNode + "\n";
			} else {
				for (int i = 0; i < ucActors.size(); i++) {
					String a = ucActors.get(i);
					out = out + "[" + safe(a) + "] - " + ucNode + "\n";
				}
			}
		}

		return out;
	}

	private List<String> splitActors(String s) {
		List<String> out = new ArrayList<String>();
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

	private String safe(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("[", "(").replace("]", ")").replace("\n", " ").trim();
	}
}