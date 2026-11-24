package oi.lebentes.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import oi.lebentes.domain.CrcCard;
import oi.lebentes.domain.UseCase;
import oi.lebentes.domain.UseCaseCrcLink;
import oi.lebentes.repositories.CrcCardRepository;
import oi.lebentes.repositories.UseCaseCrcLinkRepository;
import oi.lebentes.repositories.UseCaseRepository;

@Service
public class UseCaseCrcLinkService {
	private UseCaseCrcLinkRepository linkRepository;
	private UseCaseRepository useCaseRepository;
	private CrcCardRepository crcCardRepository;

	public UseCaseCrcLinkService(UseCaseCrcLinkRepository linkRepository, UseCaseRepository useCaseRepository, CrcCardRepository crcCardRepository) {
		this.linkRepository = linkRepository;
		this.useCaseRepository = useCaseRepository;
		this.crcCardRepository = crcCardRepository;
	}

	public List<Integer> linkedCrcCardIds(Integer projectId, Integer useCaseId) {
		checkUseCase(projectId, useCaseId);
		List<UseCaseCrcLink> links = linkRepository.findByProjectIdAndUseCaseId(projectId, useCaseId);
		List<Integer> ids = new ArrayList<Integer>();
		for (UseCaseCrcLink l : links) {
			ids.add(l.getCrcCardId());
		}
		return ids;
	}

	@Transactional
	public void link(Integer projectId, Integer useCaseId, Integer crcCardId) {
		UseCase uc = checkUseCase(projectId, useCaseId);
		CrcCard card = checkCrcCard(projectId, crcCardId);

		boolean ucIncomplete = isBlank(uc.getActors()) || isBlank(uc.getMainFlow());
		boolean crcIncomplete = isBlank(card.getResponsibilities());

		if (ucIncomplete || crcIncomplete) {
			throw new IllegalArgumentException("CRC card or Use Case is incompleted");
		}

		boolean exists = linkRepository.existsByProjectIdAndUseCaseIdAndCrcCardId(projectId, useCaseId, crcCardId);
		if (exists) {
			return;
		}
		linkRepository.save(new UseCaseCrcLink(projectId, useCaseId, crcCardId));
	}

	@Transactional
	public void unlink(Integer projectId, Integer useCaseId, Integer crcCardId) {
		checkUseCase(projectId, useCaseId);
		checkCrcCard(projectId, crcCardId);
		linkRepository.deleteByProjectIdAndUseCaseIdAndCrcCardId(projectId, useCaseId, crcCardId);
	}

	@Transactional
	public void deleteAllForUseCase(Integer projectId, Integer useCaseId) {
		linkRepository.deleteByProjectIdAndUseCaseId(projectId, useCaseId);
	}

	@Transactional
	public void deleteAllForCrcCard(Integer projectId, Integer crcCardId) {
		linkRepository.deleteByProjectIdAndCrcCardId(projectId, crcCardId);
	}

	@Transactional
	public void deleteAllForProject(Integer projectId) {
		linkRepository.deleteByProjectId(projectId);
	}

	private UseCase checkUseCase(Integer projectId, Integer useCaseId) {
		Optional<UseCase> opt = useCaseRepository.findByIdAndProjectId(useCaseId, projectId);
		if (opt.isEmpty()) {
			throw new IllegalArgumentException("Use case not found");
		}
		return opt.get();
	}

	private CrcCard checkCrcCard(Integer projectId, Integer crcCardId) {
		Optional<CrcCard> opt = crcCardRepository.findByIdAndProjectId(crcCardId, projectId);
		if (opt.isEmpty()) {
			throw new IllegalArgumentException("CRC card not found");
		}
		return opt.get();
	}

	private boolean isBlank(String s) {
		if (s == null) {
			return true;
		}
		return s.trim().isEmpty();
	}
}