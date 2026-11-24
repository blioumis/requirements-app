package oi.lebentes.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import oi.lebentes.domain.UseCase;
import oi.lebentes.repositories.UseCaseRepository;

@Service
public class UseCaseService {
	private UseCaseRepository useCaseRepository;
	private UseCaseCrcLinkService useCaseCrcLinkService;

	public UseCaseService(UseCaseRepository useCaseRepository, UseCaseCrcLinkService useCaseCrcLinkService) {
		this.useCaseRepository = useCaseRepository;
		this.useCaseCrcLinkService = useCaseCrcLinkService;
	}

	public List<UseCase> findByProject(Integer projectId) {
		return useCaseRepository.findByProjectIdOrderByUcNumberAsc(projectId);
	}

	public void create(Integer projectId, String title, String actors, String preconditions, String mainFlow,String postconditions, String alternativeFlow, String postconditionsAlt) {
		if (title == null || title.trim().isEmpty()) {
			throw new IllegalArgumentException("Use case is required");
		}

		Integer n = nextUcNumber(projectId);
		UseCase uc = new UseCase(projectId, title.trim(), safeTrim(actors), safeTrim(preconditions), safeTrim(mainFlow),safeTrim(postconditions), safeTrim(alternativeFlow), safeTrim(postconditionsAlt));
		uc.setUcNumber(n);
		useCaseRepository.save(uc);
	}

	public UseCase get(Integer projectId, Integer useCaseId) {
		Optional<UseCase> opt = useCaseRepository.findByIdAndProjectId(useCaseId, projectId);
		if (opt.isEmpty()) {
			throw new IllegalArgumentException("Use case not found");
		}
		return opt.get();
	}

	public void update(Integer projectId, Integer useCaseId, String title, String actors, String preconditions,String mainFlow, String postconditions, String alternativeFlow, String postconditionsAlt) {
		if (title == null || title.trim().isEmpty()) {
			throw new IllegalArgumentException("Use case is required");
		}

		UseCase uc = get(projectId, useCaseId);
		uc.setTitle(title.trim());
		uc.setActors(safeTrim(actors));
		uc.setPreconditions(safeTrim(preconditions));
		uc.setMainFlow(safeTrim(mainFlow));
		uc.setPostconditions(safeTrim(postconditions));
		uc.setAlternativeFlow(safeTrim(alternativeFlow));
		uc.setPostconditionsAlt(safeTrim(postconditionsAlt));
		useCaseRepository.save(uc);

		// unlink
		if (isBlank(uc.getActors()) || isBlank(uc.getMainFlow())) {
			useCaseCrcLinkService.deleteAllForUseCase(projectId, useCaseId);
		}
	}

	@Transactional
	public void delete(Integer projectId, Integer useCaseId) {
		get(projectId, useCaseId);
		useCaseRepository.deleteByIdAndProjectId(useCaseId, projectId);
	}

	@Transactional
	public void deleteAllForProject(Integer projectId) {
		useCaseRepository.deleteByProjectId(projectId);
	}

	private String safeTrim(String s) {
		if (s == null) {
			return "";
		}
		return s.trim();
	}

	private boolean isBlank(String s) {
		if (s == null) {
			return true;
		}
		return s.trim().isEmpty();
	}

	private Integer nextUcNumber(Integer projectId) {
		List<UseCase> list = useCaseRepository.findByProjectIdOrderByUcNumberAsc(projectId);

		int expected = 1;
		for (UseCase uc : list) {
			Integer n = uc.getUcNumber();
			if (n == null) {
				continue;
			}
			if (n == expected) {
				expected++;
				continue;
			}
			if (n > expected) {
				break;
			}
		}
		return expected;
	}
}