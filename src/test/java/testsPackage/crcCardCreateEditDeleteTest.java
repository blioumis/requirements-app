package testsPackage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.ui.ExtendedModelMap;

import oi.lebentes.controllers.CrcCardForm;
import oi.lebentes.controllers.CrcCardsController;
import oi.lebentes.domain.CrcCard;
import oi.lebentes.repositories.CrcCardRepository;
import oi.lebentes.services.CrcCardService;
import oi.lebentes.services.ProjectService;
import oi.lebentes.services.UseCaseCrcLinkService;

class crcCardCreateEditDeleteTest {

	private ProjectService crcControllerProjectService;
	private CrcCardService crcControllerCrcCardService;
	private UseCaseCrcLinkService crcControllerLinkService;
	private CrcCardsController crcCardsController;

	private CrcCardRepository crcCardRepository;
	private UseCaseCrcLinkService crcServiceLinkService;
	private CrcCardService crcCardService;

	@BeforeEach
	public void setup() {
		crcControllerProjectService = Mockito.mock(ProjectService.class);
		crcControllerCrcCardService = Mockito.mock(CrcCardService.class);
		crcControllerLinkService = Mockito.mock(UseCaseCrcLinkService.class);

		crcCardsController = new CrcCardsController(
				crcControllerProjectService,
				crcControllerCrcCardService,
				crcControllerLinkService
		);

		crcCardRepository = Mockito.mock(CrcCardRepository.class);
		crcServiceLinkService = Mockito.mock(UseCaseCrcLinkService.class);
		crcCardService = new CrcCardService(crcCardRepository, crcServiceLinkService);
	}

	// US11: otan pataw CRC Cards, pao sthn selida me thn lista twn crc cards tou sugkekrimenou project
	@Test
	public void list_returnsPageWithCards() {
		Integer projectId = 7;

		List<CrcCard> list = new ArrayList<CrcCard>();
		Mockito.when(crcControllerCrcCardService.findByProject(projectId)).thenReturn(list);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = crcCardsController.list(projectId, model);

		assertEquals("crccards", view);
		assertEquals(projectId, model.get("projectId"));
		assertEquals(list, model.get("crccards"));
		Mockito.verify(crcControllerProjectService).getById(projectId);
	}

	// US11: otan patao New CRC card pao sthn selida pou exei thn forma dhmiourgias mias crc card
	@Test
	public void showCreate_returnsCreateForm() {
		Integer projectId = 7;

		ExtendedModelMap model = new ExtendedModelMap();

		String view = crcCardsController.showCreate(projectId, model);

		assertEquals("crccard_form", view);
		assertEquals(projectId, model.get("projectId"));
		assertEquals("create", model.get("mode"));
		assertTrue(model.get("form") instanceof CrcCardForm);
		Mockito.verify(crcControllerProjectService).getById(projectId);
	}

	// US11: otan patao save me sosta stoixeia sta pedia, me gurnaei sthn selida me thn lista twn crc cards
	@Test
	public void create_success_redirectsToList() {
		Integer projectId = 7;

		Mockito.when(crcControllerCrcCardService.findByProject(projectId)).thenReturn(new ArrayList<CrcCard>());

		CrcCardForm form = new CrcCardForm();
		form.setClassName("MyClass");
		form.setResponsibilities("Resp");

		ExtendedModelMap model = new ExtendedModelMap();

		String view = crcCardsController.create(projectId, form, model);

		assertEquals("redirect:/projects/" + projectId + "/crccards", view);
		Mockito.verify(crcControllerProjectService).getById(projectId);
		Mockito.verify(crcControllerCrcCardService).create(projectId, "MyClass", "Resp", "");
	}

	// US11: an pathso save kai uparxei lathos meno sthn idia selida kai blepo mhnuma lathous
	@Test
	public void create_error_returnsFormWithError() {
		Integer projectId = 7;

		Mockito.when(crcControllerCrcCardService.findByProject(projectId)).thenReturn(new ArrayList<CrcCard>());

		CrcCardForm form = new CrcCardForm();
		form.setClassName("   ");

		Mockito.doThrow(new IllegalArgumentException("Class name is required"))
				.when(crcControllerCrcCardService).create(projectId, "   ", null, "");

		ExtendedModelMap model = new ExtendedModelMap();

		String view = crcCardsController.create(projectId, form, model);

		assertEquals("crccard_form", view);
		assertEquals(projectId, model.get("projectId"));
		assertEquals("create", model.get("mode"));
		assertEquals(form, model.get("form"));
		assertEquals("Class name is required", model.get("error"));
	}

	// US11: an sto create uparxei lathos sta collaborations meno sthn idia selida kai blepo to mhnuma lathous
	@Test
	public void create_collaborationError_returnsFormWithCollaborationError() {
		Integer projectId = 7;

		Mockito.when(crcControllerCrcCardService.findByProject(projectId)).thenReturn(new ArrayList<CrcCard>());

		CrcCardForm form = new CrcCardForm();
		form.setClassName("MyClass");
		form.setAssociation("Other");

		ExtendedModelMap model = new ExtendedModelMap();

		String view = crcCardsController.create(projectId, form, model);

		assertEquals("crccard_form", view);
		assertEquals(projectId, model.get("projectId"));
		assertEquals("create", model.get("mode"));
		assertEquals(form, model.get("form"));
		assertEquals("A class does not exist", model.get("collaborationError"));
	}

	// US12: otan patao edit pao sthn selida pou fainontai ta stoixeia tou sugkekrimenou crc card
	@Test
	public void showEdit_returnsEditForm() {
		Integer projectId = 7;
		Integer crcCardId = 4;

		CrcCard card = Mockito.mock(CrcCard.class);
		Mockito.when(card.getId()).thenReturn(crcCardId);
		Mockito.when(card.getClassName()).thenReturn("A");
		Mockito.when(card.getResponsibilities()).thenReturn("R");
		Mockito.when(card.getCollaborations()).thenReturn("dependency:Other");

		Mockito.when(crcControllerCrcCardService.get(projectId, crcCardId)).thenReturn(card);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = crcCardsController.showEdit(projectId, crcCardId, model);

		assertEquals("crccard_form", view);
		assertEquals(projectId, model.get("projectId"));
		assertEquals(crcCardId, model.get("crcCardId"));
		assertEquals("edit", model.get("mode"));

		CrcCardForm form = (CrcCardForm) model.get("form");
		assertEquals("A", form.getClassName());
		assertEquals("R", form.getResponsibilities());
		assertEquals("Other", form.getDependency());
	}

	// US12: otan kano update to crc card me sosta stoixeia, tote pao sth selida me thn lista ton crc cards 
	@Test
	public void update_success_redirectsToList() {
		Integer projectId = 7;
		Integer crcCardId = 4;

		Mockito.when(crcControllerCrcCardService.findByProject(projectId)).thenReturn(new ArrayList<CrcCard>());

		CrcCardForm form = new CrcCardForm();
		form.setClassName("MyClass");
		form.setResponsibilities("Resp");

		ExtendedModelMap model = new ExtendedModelMap();

		String view = crcCardsController.update(projectId, crcCardId, form, model);

		assertEquals("redirect:/projects/" + projectId + "/crccards", view);
		Mockito.verify(crcControllerProjectService).getById(projectId);
		Mockito.verify(crcControllerCrcCardService).update(projectId, crcCardId, "MyClass", "Resp", "");
	}

	// US12: an sto edit valw class name pou uparxei hdh, meno sthn idia selida kai blepo mhnuma lathous
	@Test
	public void update_error_returnsEditFormWithError() {
		Integer projectId = 7;
		Integer crcCardId = 4;

		Mockito.when(crcControllerCrcCardService.findByProject(projectId)).thenReturn(new ArrayList<CrcCard>());

		CrcCardForm form = new CrcCardForm();
		form.setClassName("MyClass");
		form.setResponsibilities("Resp");

		Mockito.doThrow(new IllegalArgumentException("Class name already exists"))
				.when(crcControllerCrcCardService).update(projectId, crcCardId, "MyClass", "Resp", "");

		ExtendedModelMap model = new ExtendedModelMap();

		String view = crcCardsController.update(projectId, crcCardId, form, model);

		assertEquals("crccard_form", view);
		assertEquals(projectId, model.get("projectId"));
		assertEquals(crcCardId, model.get("crcCardId"));
		assertEquals(form, model.get("form"));
		assertEquals("edit", model.get("mode"));
		assertEquals("Class name already exists", model.get("error"));
	}

	// US14: delete mias crc card tote sbhnetai to link kai to crc card, epeita ginetai redirect sthn lista ton crc cards
	@Test
	public void delete_redirectsToList() {
		Integer projectId = 7;
		Integer crcCardId = 4;

		String view = crcCardsController.delete(projectId, crcCardId);

		assertEquals("redirect:/projects/" + projectId + "/crccards", view);
		Mockito.verify(crcControllerProjectService).getById(projectId);
		Mockito.verify(crcControllerLinkService).deleteAllForCrcCard(projectId, crcCardId);
		Mockito.verify(crcControllerCrcCardService).delete(projectId, crcCardId);
	}

	// US11: an den balo onoma sto class name den dhmiourgeitai to crc card
	@Test
	public void create_classNameRequired() {
		try {
			crcCardService.create(1, "   ", "R", "");
		} catch (IllegalArgumentException ex) {
			assertEquals("Class name is required", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US11: an pathso create kai uparxei hdh allo crc card me to idio class name tote pairno lathos
	@Test
	public void create_classNameAlreadyExists() {
		CrcCard existing = new CrcCard(1, "MyClass", "R", "C");
		Mockito.when(crcCardRepository.findByProjectIdAndClassName(1, "MyClass")).thenReturn(Optional.of(existing));

		try {
			crcCardService.create(1, "MyClass", "R", "C");
		} catch (IllegalArgumentException ex) {
			assertEquals("Class name already exists", ex.getMessage());
			Mockito.verify(crcCardRepository, Mockito.never()).save(Mockito.any(CrcCard.class));
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US11: otan dimiourgo ena CRC card to systima afairei ta kena stin arxi kai sto telos
	@Test
	public void create_savesTrimmedAndSafeTrim() {
		Mockito.when(crcCardRepository.findByProjectIdAndClassName(5, "MyClass")).thenReturn(Optional.empty());

		crcCardService.create(5, "  MyClass  ", null, "  dep:Other  ");

		ArgumentCaptor<CrcCard> captor = ArgumentCaptor.forClass(CrcCard.class);
		Mockito.verify(crcCardRepository).save(captor.capture());

		CrcCard saved = captor.getValue();
		assertEquals(5, saved.getProjectId());
		assertEquals("MyClass", saved.getClassName());
		assertEquals("", saved.getResponsibilities());
		assertEquals("dep:Other", saved.getCollaborations());
	}

	// US12: an sto edit balo class name pou uparxei hdh se allo crc card tote pairno lathos
	@Test
	public void update_classNameAlreadyExists() {
		CrcCard other = new CrcCard(7, "NewClass", "R", "C");
		Mockito.when(crcCardRepository.findByProjectIdAndClassNameAndIdNot(7, "NewClass", 4)).thenReturn(Optional.of(other));

		try {
			crcCardService.update(7, 4, "NewClass", "Resp", "Collab");
		} catch (IllegalArgumentException ex) {
			assertEquals("Class name already exists", ex.getMessage());
			Mockito.verify(crcCardRepository, Mockito.never()).save(Mockito.any(CrcCard.class));
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US12: an kano edit kai balo sosta ta stoixeia se mia crc card, tote den kanei unlink me ta use cases pou einai link
	@Test
	public void update_complete_savesAndDoesNotUnlink() {
		CrcCard card = new CrcCard(7, "Old", "OldR", "OldC");
		Mockito.when(crcCardRepository.findByProjectIdAndClassNameAndIdNot(7, "NewClass", 4)).thenReturn(Optional.empty());
		Mockito.when(crcCardRepository.findByIdAndProjectId(4, 7)).thenReturn(Optional.of(card));

		crcCardService.update(7, 4, "  NewClass  ", "Resp", "Collab");

		Mockito.verify(crcCardRepository).save(Mockito.any(CrcCard.class));
		Mockito.verify(crcServiceLinkService, Mockito.never()).deleteAllForCrcCard(Mockito.anyInt(), Mockito.anyInt());

		assertEquals("NewClass", card.getClassName());
		assertEquals("Resp", card.getResponsibilities());
		assertEquals("Collab", card.getCollaborations());
	}

	// US12: an sto edit mias crc card ta responsibilities ginoun kena tote ginetai unlink ton links me ta use cases
	@Test
	public void update_blankResponsibilities_unlinks() {
		CrcCard card = new CrcCard(7, "Old", "OldR", "OldC");
		Mockito.when(crcCardRepository.findByProjectIdAndClassNameAndIdNot(7, "NewClass", 4)).thenReturn(Optional.empty());
		Mockito.when(crcCardRepository.findByIdAndProjectId(4, 7)).thenReturn(Optional.of(card));

		crcCardService.update(7, 4, "NewClass", "   ", "C");

		Mockito.verify(crcCardRepository).save(Mockito.any(CrcCard.class));
		Mockito.verify(crcServiceLinkService).deleteAllForCrcCard(7, 4);
	}

	// US14: sbhnei mia crc card efoson uparxei
	@Test
	public void delete_callsRepositoryDelete() {
		CrcCard card = new CrcCard(7, "A", "R", "C");
		Mockito.when(crcCardRepository.findByIdAndProjectId(4, 7)).thenReturn(Optional.of(card));

		crcCardService.delete(7, 4);

		Mockito.verify(crcCardRepository).deleteByIdAndProjectId(4, 7);
	}
}
