package oi.lebentes.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import oi.lebentes.domain.CrcCard;
import oi.lebentes.repositories.CrcCardRepository;

@Service
public class CrcCardService {
	private CrcCardRepository crcCardRepository;
	private UseCaseCrcLinkService useCaseCrcLinkService;

	public CrcCardService(CrcCardRepository crcCardRepository, UseCaseCrcLinkService useCaseCrcLinkService) {
		this.crcCardRepository = crcCardRepository;
		this.useCaseCrcLinkService = useCaseCrcLinkService;
	}

	public List<CrcCard> findByProject(Integer projectId) {
		return crcCardRepository.findByProjectIdOrderByClassNameAsc(projectId);
	}

	public void create(Integer projectId, String className, String responsibilities, String collaborations) {
		if (className == null || className.trim().isEmpty()) {
			throw new IllegalArgumentException("Class name is required");
		}

		String classNameTrim = className.trim();

		if (crcCardRepository.findByProjectIdAndClassName(projectId, classNameTrim).isPresent()) {
			throw new IllegalArgumentException("Class name already exists");
		}

		CrcCard card = new CrcCard(projectId, classNameTrim, safeTrim(responsibilities), safeTrim(collaborations));
		crcCardRepository.save(card);
	}

	public CrcCard get(Integer projectId, Integer crcCardId) {
		Optional<CrcCard> opt = crcCardRepository.findByIdAndProjectId(crcCardId, projectId);
		if (opt.isEmpty()) {
			throw new IllegalArgumentException("CRC card not found");
		}
		return opt.get();
	}

	public void update(Integer projectId, Integer crcCardId, String className, String responsibilities, String collaborations) {
		if (className == null || className.trim().isEmpty()) {
			throw new IllegalArgumentException("Class name is required");
		}

		String classNameTrim = className.trim();

		if (crcCardRepository.findByProjectIdAndClassNameAndIdNot(projectId, classNameTrim, crcCardId).isPresent()) {
			throw new IllegalArgumentException("Class name already exists");
		}

		CrcCard card = get(projectId, crcCardId);
		card.setClassName(classNameTrim);
		card.setResponsibilities(safeTrim(responsibilities));
		card.setCollaborations(safeTrim(collaborations));
		crcCardRepository.save(card);

		if (isBlank(card.getResponsibilities())) {
			useCaseCrcLinkService.deleteAllForCrcCard(projectId, crcCardId);
		}
	}

	@Transactional
	public void delete(Integer projectId, Integer crcCardId) {
		get(projectId, crcCardId);
		crcCardRepository.deleteByIdAndProjectId(crcCardId, projectId);
	}

	@Transactional
	public void deleteAllForProject(Integer projectId) {
		crcCardRepository.deleteByProjectId(projectId);
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
}