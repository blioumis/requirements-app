package oi.lebentes.diagrams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oi.lebentes.domain.CrcCard;

public class NomnomlClassDiagramGenerator implements ClassDiagramGenerator {

	@Override
	public String generate(List<CrcCard> cards) {
		String out = "";
		out = out + "#direction: right\n\n";

		for (int i = 0; i < cards.size(); i++) {
			CrcCard c = cards.get(i);
			String name = safeName(c.getClassName());
			List<String> resp = splitLines(c.getResponsibilities());

			if (resp.isEmpty()) {
				out = out + "[" + name + "]\n";
			} else {
				out = out + "[" + name + "|";
				for (int j = 0; j < resp.size(); j++) {
					if (j > 0) {
						out = out + ";";
					}
					out = out + safeText(resp.get(j));
				}
				out = out + "]\n";
			}
		}

		out = out + "\n";

		Set<String> addedLines = new HashSet<String>();

		for (int i = 0; i < cards.size(); i++) {
			CrcCard c = cards.get(i);
			String from = safeName(c.getClassName());

			List<String> cols = splitCollaborations(c.getCollaborations());
			for (int j = 0; j < cols.size(); j++) {
				String raw = cols.get(j);

				String type = relationType(raw);
				String toRaw = relationTarget(raw);

				if (type.isEmpty() || toRaw.isEmpty()) {
					continue;
				}

				String to = safeName(toRaw);

				if (existsClass(cards, to)) {
					String line = buildRelationLine(type, from, to);
					if (!line.isEmpty() && !addedLines.contains(line)) {
						addedLines.add(line);
						out = out + line + "\n";
					}
				}
			}
		}

		return out;
	}

	private String buildRelationLine(String type, String from, String to) {
		if (type.equals("dependency")) {
			return "[" + from + "] --> [" + to + "]";
		}
		if (type.equals("aggregation")) {
			return "[" + from + "] o-> [" + to + "]";
		}
		if (type.equals("composition")) {
			return "[" + from + "] +-> [" + to + "]";
		}
		if (type.equals("inheritance")) {
			return "[" + to + "] <:- [" + from + "]";
		}
		if (type.equals("association")) {
			return "[" + from + "] -> [" + to + "]";
		}
		return "";
	}

	// STRICT
	private String relationType(String token) {
		if (token == null) {
			return "";
		}
		String t = token.trim();
		int idx = t.indexOf(":");
		if (idx <= 0) {
			return "";
		}

		String prefix = t.substring(0, idx).trim().toLowerCase();

		if (prefix.equals("association")) return "association";
		if (prefix.equals("dependency")) return "dependency";
		if (prefix.equals("aggregation")) return "aggregation";
		if (prefix.equals("composition")) return "composition";
		if (prefix.equals("inheritance")) return "inheritance";

		return "";
	}

	private String relationTarget(String token) {
		if (token == null) {
			return "";
		}
		String t = token.trim();
		int idx = t.indexOf(":");
		if (idx <= 0) {
			return "";
		}
		return t.substring(idx + 1).trim();
	}

	private List<String> splitLines(String s) {
		List<String> out = new ArrayList<String>();
		if (s == null) {
			return out;
		}
		String cleaned = s.replace("\r", "\n");
		String[] parts = cleaned.split("\n");
		for (int i = 0; i < parts.length; i++) {
			String p = parts[i].trim();
			if (!p.isEmpty()) {
				out.add(p);
			}
		}
		return out;
	}

	private List<String> splitCollaborations(String s) {
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

	private boolean existsClass(List<CrcCard> cards, String className) {
		for (int i = 0; i < cards.size(); i++) {
			CrcCard c = cards.get(i);
			String n = safeName(c.getClassName());
			if (n.equals(className)) {
				return true;
			}
		}
		return false;
	}

	private String safeName(String s) {
		if (s == null) {
			return "Unknown";
		}
		String t = s.trim();
		if (t.isEmpty()) {
			return "Unknown";
		}
		return t.replace(" ", "_").replace("[", "(").replace("]", ")");
	}

	private String safeText(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("[", "(").replace("]", ")").replace("\n", " ").trim();
	}
}