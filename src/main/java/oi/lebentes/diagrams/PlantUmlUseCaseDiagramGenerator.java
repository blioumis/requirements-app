package oi.lebentes.diagrams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oi.lebentes.domain.UseCase;

public class PlantUmlUseCaseDiagramGenerator implements UseCaseDiagramGenerator {
	@Override
	public String generate(List<UseCase> useCases) {
		String out = "";
		out = out + "@startuml\n";
		out = out + "left to right direction\n\n";

		Map<String, String> actorAliases = new HashMap<String, String>();
		List<String> actors = collectActors(useCases);

		int i = 1;
		for (int k = 0; k < actors.size(); k++) {
			String a = actors.get(k);
			String alias = "A" + i;
			actorAliases.put(a, alias);
			out = out + "actor \"" + escape(a) + "\" as " + alias + "\n";
			i++;
		}

		if (!actors.isEmpty()) {
			out = out + "\n";
		}

		for (int k = 0; k < useCases.size(); k++) {
			UseCase uc = useCases.get(k);

			int num = 0;
			Integer n = uc.getUcNumber();
			if (n != null) {
				num = n.intValue();
			}

			String ucAlias = "UC" + num;
			out = out + "usecase \"" + escape(ucAlias) + ": " + escape(uc.getTitle()) + "\" as " + ucAlias + "\n";
		}

		out = out + "\n";

		for (int k = 0; k < useCases.size(); k++) {
			UseCase uc = useCases.get(k);

			int num = 0;
			Integer n = uc.getUcNumber();
			if (n != null) {
				num = n.intValue();
			}

			String ucAlias = "UC" + num;

			List<String> ucActors = splitActors(uc.getActors());
			for (int j = 0; j < ucActors.size(); j++) {
				String a = ucActors.get(j);
				String alias = actorAliases.get(a);
				if (alias != null) {
					out = out + alias + " --> " + ucAlias + "\n";
				}
			}
		}

		out = out + "\n@enduml\n";
		return out;
	}

	private List<String> collectActors(List<UseCase> useCases) {
		List<String> all = new ArrayList<String>();
		for (int k = 0; k < useCases.size(); k++) {
			UseCase uc = useCases.get(k);
			List<String> list = splitActors(uc.getActors());
			for (int i = 0; i < list.size(); i++) {
				String a = list.get(i);
				if (!all.contains(a)) {
					all.add(a);
				}
			}
		}
		return all;
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

	private String escape(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("\"", "'");
	}
}