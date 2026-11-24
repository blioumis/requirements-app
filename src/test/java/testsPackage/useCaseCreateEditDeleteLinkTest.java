package testsPackage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.ui.ExtendedModelMap;

import oi.lebentes.controllers.UseCaseCrcCardsController;
import oi.lebentes.controllers.UseCaseForm;
import oi.lebentes.controllers.UseCasesController;
import oi.lebentes.domain.CrcCard;
import oi.lebentes.domain.UseCase;
import oi.lebentes.domain.UseCaseCrcLink;
import oi.lebentes.repositories.CrcCardRepository;
import oi.lebentes.repositories.UseCaseCrcLinkRepository;
import oi.lebentes.repositories.UseCaseRepository;
import oi.lebentes.services.CrcCardService;
import oi.lebentes.services.ProjectService;
import oi.lebentes.services.UseCaseCrcLinkService;
import oi.lebentes.services.UseCaseService;

class useCaseCreateEditDeleteLinkTest {

	private ProjectService useCasesControllerProjectService;
	private UseCaseService useCasesControllerUseCaseService;
	private UseCaseCrcLinkService useCasesControllerLinkService;
	private CrcCardService useCasesControllerCrcCardService;
	private UseCasesController useCasesController;

	private UseCaseRepository useCaseRepository;
	private UseCaseCrcLinkService useCaseServiceLinkService;
	private UseCaseService useCaseService;

	private ProjectService linkControllerProjectService;
	private UseCaseService linkControllerUseCaseService;
	private CrcCardService linkControllerCrcCardService;
	private UseCaseCrcLinkService linkControllerLinkService;
	private UseCaseCrcCardsController linkController;

	private UseCaseCrcLinkRepository linkRepository;
	private UseCaseRepository linkUseCaseRepository;
	private CrcCardRepository linkCrcCardRepository;
	private UseCaseCrcLinkService linkService;

	@BeforeEach
	public void setup() {
		useCasesControllerProjectService = Mockito.mock(ProjectService.class);
		useCasesControllerUseCaseService = Mockito.mock(UseCaseService.class);
		useCasesControllerLinkService = Mockito.mock(UseCaseCrcLinkService.class);
		useCasesControllerCrcCardService = Mockito.mock(CrcCardService.class);

		useCasesController = new UseCasesController(
				useCasesControllerProjectService,
				useCasesControllerUseCaseService,
				useCasesControllerLinkService,
				useCasesControllerCrcCardService
		);

		useCaseRepository = Mockito.mock(UseCaseRepository.class);
		useCaseServiceLinkService = Mockito.mock(UseCaseCrcLinkService.class);
		useCaseService = new UseCaseService(useCaseRepository, useCaseServiceLinkService);

		linkControllerProjectService = Mockito.mock(ProjectService.class);
		linkControllerUseCaseService = Mockito.mock(UseCaseService.class);
		linkControllerCrcCardService = Mockito.mock(CrcCardService.class);
		linkControllerLinkService = Mockito.mock(UseCaseCrcLinkService.class);

		linkController = new UseCaseCrcCardsController(
				linkControllerProjectService,
				linkControllerUseCaseService,
				linkControllerCrcCardService,
				linkControllerLinkService
		);

		linkRepository = Mockito.mock(UseCaseCrcLinkRepository.class);
		linkUseCaseRepository = Mockito.mock(UseCaseRepository.class);
		linkCrcCardRepository = Mockito.mock(CrcCardRepository.class);
		linkService = new UseCaseCrcLinkService(linkRepository, linkUseCaseRepository, linkCrcCardRepository);
	}

	// US9: otan patao na do ta use cases pao sthn selida pou deixnei thn lista me ta use cases kai to template me ta links
	@Test
	public void list_returnsUsecasesPageWithSortedListAndLinksMap() {
		Integer projectId = 5;

		UseCase uc2 = Mockito.mock(UseCase.class);
		Mockito.when(uc2.getId()).thenReturn(2);
		Mockito.when(uc2.getTitle()).thenReturn("UC2");
		Mockito.when(uc2.getUcNumber()).thenReturn(2);

		UseCase uc1 = Mockito.mock(UseCase.class);
		Mockito.when(uc1.getId()).thenReturn(1);
		Mockito.when(uc1.getTitle()).thenReturn("UC1");
		Mockito.when(uc1.getUcNumber()).thenReturn(1);

		UseCase ucNull = Mockito.mock(UseCase.class);
		Mockito.when(ucNull.getId()).thenReturn(3);
		Mockito.when(ucNull.getTitle()).thenReturn("UCNULL");
		Mockito.when(ucNull.getUcNumber()).thenReturn(null);

		List<UseCase> list = new ArrayList<UseCase>();
		list.add(uc2);
		list.add(ucNull);
		list.add(uc1);

		Mockito.when(useCasesControllerUseCaseService.findByProject(projectId)).thenReturn(list);

		Mockito.when(useCasesControllerLinkService.linkedCrcCardIds(projectId, 1)).thenReturn(new ArrayList<Integer>());
		Mockito.when(useCasesControllerLinkService.linkedCrcCardIds(projectId, 2)).thenReturn(new ArrayList<Integer>());
		Mockito.when(useCasesControllerLinkService.linkedCrcCardIds(projectId, 3)).thenReturn(new ArrayList<Integer>());

		ExtendedModelMap model = new ExtendedModelMap();

		String view = useCasesController.list(projectId, model);

		assertEquals("usecases", view);
		assertEquals(projectId, model.get("projectId"));

		@SuppressWarnings("unchecked")
		List<UseCase> out = (List<UseCase>) model.get("usecases");
		assertEquals(uc1, out.get(0));
		assertEquals(uc2, out.get(1));
		assertEquals(ucNull, out.get(2));

		@SuppressWarnings("unchecked")
		Map<Integer, List<String>> linksByUseCaseId = (Map<Integer, List<String>>) model.get("linksByUseCaseId");
		assertEquals(3, linksByUseCaseId.size());
		assertTrue(linksByUseCaseId.get(1).isEmpty());
		assertTrue(linksByUseCaseId.get(2).isEmpty());
		assertTrue(linksByUseCaseId.get(3).isEmpty());

		Mockito.verify(useCasesControllerProjectService).getById(projectId);
		Mockito.verify(useCasesControllerUseCaseService).findByProject(projectId);
	}

	// US9: otan ena use case exei linked CRC cards, fortwnontai ta class names sto template
	@Test
	public void list_returnsLinksMapWithClassNames() {
		Integer projectId = 5;

		UseCase uc = Mockito.mock(UseCase.class);
		Mockito.when(uc.getId()).thenReturn(4);
		Mockito.when(uc.getTitle()).thenReturn("Login");
		Mockito.when(uc.getUcNumber()).thenReturn(1);

		List<UseCase> list = new ArrayList<UseCase>();
		list.add(uc);

		List<Integer> linkedIds = new ArrayList<Integer>();
		linkedIds.add(10);
		linkedIds.add(11);

		CrcCard c1 = Mockito.mock(CrcCard.class);
		Mockito.when(c1.getClassName()).thenReturn("A");

		CrcCard c2 = Mockito.mock(CrcCard.class);
		Mockito.when(c2.getClassName()).thenReturn("C");

		Mockito.when(useCasesControllerUseCaseService.findByProject(projectId)).thenReturn(list);
		Mockito.when(useCasesControllerLinkService.linkedCrcCardIds(projectId, 4)).thenReturn(linkedIds);
		Mockito.when(useCasesControllerCrcCardService.get(projectId, 10)).thenReturn(c1);
		Mockito.when(useCasesControllerCrcCardService.get(projectId, 11)).thenReturn(c2);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = useCasesController.list(projectId, model);

		assertEquals("usecases", view);

		@SuppressWarnings("unchecked")
		Map<Integer, List<String>> linksByUseCaseId = (Map<Integer, List<String>>) model.get("linksByUseCaseId");

		assertEquals(2, linksByUseCaseId.get(4).size());
		assertEquals("A", linksByUseCaseId.get(4).get(0));
		assertEquals("C", linksByUseCaseId.get(4).get(1));
	}

	// US7: otan anoigo “New Use Case”, blepo selida me thn forma dhmiourgias tou use case
	@Test
	public void showCreate_returnsFormInCreateMode() {
		Integer projectId = 7;
		ExtendedModelMap model = new ExtendedModelMap();

		String view = useCasesController.showCreate(projectId, model);

		assertEquals("usecase_form", view);
		assertEquals(projectId, model.get("projectId"));
		assertTrue(model.get("form") instanceof UseCaseForm);
		assertEquals("create", model.get("mode"));
		Mockito.verify(useCasesControllerProjectService).getById(projectId);
	}

	// US7: otan kano create me sosta stoixeia, me gurnaei sthn selida me thn lista ton use cases enhmeromenh
	@Test
	public void create_success_redirectsToList() {
		Integer projectId = 7;

		UseCaseForm form = new UseCaseForm();
		form.setTitle("T");
		form.setActors("A");
		form.setPreconditions("P");
		form.setMainFlow("F");
		form.setPostconditions("Post");
		form.setAlternativeFlow("Alt");
		form.setPostconditionsAlt("AltPost");

		ExtendedModelMap model = new ExtendedModelMap();

		String view = useCasesController.create(projectId, form, model);

		assertEquals("redirect:/projects/" + projectId + "/usecases", view);

		Mockito.verify(useCasesControllerProjectService).getById(projectId);
		Mockito.verify(useCasesControllerUseCaseService).create(projectId, "T", "A", "P", "F", "Post", "Alt", "AltPost");
	}

	// US7: an sto create uparxei problhma meno sthn forma dhmiourgias kai bganei mhnuma lathous
	@Test
	public void create_error_returnsFormWithError() {
		Integer projectId = 7;

		UseCaseForm form = new UseCaseForm();
		form.setTitle("   ");

		Mockito.doThrow(new IllegalArgumentException("Use case is required"))
				.when(useCasesControllerUseCaseService).create(projectId, "   ", null, null, null, null, null, null);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = useCasesController.create(projectId, form, model);

		assertEquals("usecase_form", view);
		assertEquals(projectId, model.get("projectId"));
		assertEquals(form, model.get("form"));
		assertEquals("create", model.get("mode"));
		assertEquals("Use case is required", model.get("error"));
	}

	// US8: otan patao na kano Edit pao sthn selida me thn forma ton uparxonton stoixeion tou use case
	@Test
	public void showEdit_returnsFormInEditMode() {
		Integer projectId = 7;
		Integer useCaseId = 4;

		UseCase uc = Mockito.mock(UseCase.class);
		Mockito.when(uc.getId()).thenReturn(useCaseId);
		Mockito.when(uc.getTitle()).thenReturn("Title");
		Mockito.when(uc.getActors()).thenReturn("Actors");
		Mockito.when(uc.getPreconditions()).thenReturn("Pre");
		Mockito.when(uc.getMainFlow()).thenReturn("Flow");
		Mockito.when(uc.getPostconditions()).thenReturn("Post");
		Mockito.when(uc.getAlternativeFlow()).thenReturn("Alt");
		Mockito.when(uc.getPostconditionsAlt()).thenReturn("AltPost");
		Mockito.when(uc.getUcNumber()).thenReturn(2);

		Mockito.when(useCasesControllerUseCaseService.get(projectId, useCaseId)).thenReturn(uc);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = useCasesController.showEdit(projectId, useCaseId, model);

		assertEquals("usecase_form", view);
		assertEquals(projectId, model.get("projectId"));
		assertEquals(useCaseId, model.get("useCaseId"));
		assertEquals(2, model.get("ucNumber"));
		assertEquals("edit", model.get("mode"));

		UseCaseForm outForm = (UseCaseForm) model.get("form");
		assertEquals("Title", outForm.getTitle());
		assertEquals("Actors", outForm.getActors());
		assertEquals("Flow", outForm.getMainFlow());

		Mockito.verify(useCasesControllerProjectService).getById(projectId);
		Mockito.verify(useCasesControllerUseCaseService).get(projectId, useCaseId);
	}

	// US8: otan kano update kai einai ola entajei me gurnaei sthn lista ton use cases
	@Test
	public void update_success_redirectsToList() {
		Integer projectId = 7;
		Integer useCaseId = 4;

		UseCaseForm form = new UseCaseForm();
		form.setTitle("T");
		form.setActors("A");
		form.setPreconditions("P");
		form.setMainFlow("F");
		form.setPostconditions("Post");
		form.setAlternativeFlow("Alt");
		form.setPostconditionsAlt("AltPost");

		ExtendedModelMap model = new ExtendedModelMap();

		String view = useCasesController.update(projectId, useCaseId, form, model);

		assertEquals("redirect:/projects/" + projectId + "/usecases", view);

		Mockito.verify(useCasesControllerProjectService).getById(projectId);
		Mockito.verify(useCasesControllerUseCaseService).update(projectId, useCaseId, "T", "A", "P", "F", "Post", "Alt", "AltPost");
	}

	// US8: an sto update uparxei lathos, meno sto edit kai blepo mhnuma lathous
	@Test
	public void update_error_returnsEditFormWithError() {
		Integer projectId = 7;
		Integer useCaseId = 4;

		UseCaseForm form = new UseCaseForm();
		form.setTitle("   ");

		Mockito.doThrow(new IllegalArgumentException("Use case is required"))
				.when(useCasesControllerUseCaseService).update(projectId, useCaseId, "   ", null, null, null, null, null, null);

		UseCase uc = Mockito.mock(UseCase.class);
		Mockito.when(uc.getUcNumber()).thenReturn(2);
		Mockito.when(useCasesControllerUseCaseService.get(projectId, useCaseId)).thenReturn(uc);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = useCasesController.update(projectId, useCaseId, form, model);

		assertEquals("usecase_form", view);
		assertEquals(projectId, model.get("projectId"));
		assertEquals(useCaseId, model.get("useCaseId"));
		assertEquals(2, model.get("ucNumber"));
		assertEquals(form, model.get("form"));
		assertEquals("edit", model.get("mode"));
		assertEquals("Use case is required", model.get("error"));
	}

	// US10: otan patao delete se use case, me gurnaei sthn lista me ta use cases
	@Test
	public void delete_redirectsToList() {
		Integer projectId = 7;
		Integer useCaseId = 4;

		String view = useCasesController.delete(projectId, useCaseId);

		assertEquals("redirect:/projects/" + projectId + "/usecases", view);
		Mockito.verify(useCasesControllerProjectService).getById(projectId);
		Mockito.verify(useCasesControllerUseCaseService).delete(projectId, useCaseId);
	}

	// US7:an den doso titlo sto use case den dhmiourgeitai
	@Test
	public void create_titleRequired() {
		try {
			useCaseService.create(1, "   ", "A", "P", "F", "Post", "", "");
		} catch (IllegalArgumentException ex) {
			assertEquals("Use case is required", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US7: otan dhmiourgo UC krataei trimmed title kai dinei sosto UC number
	@Test
	public void create_savesTrimmedTitleAndSetsUcNumber() {
		List<UseCase> existing = new ArrayList<UseCase>();

		UseCase uc1 = Mockito.mock(UseCase.class);
		Mockito.when(uc1.getUcNumber()).thenReturn(1);
		existing.add(uc1);

		UseCase uc2 = Mockito.mock(UseCase.class);
		Mockito.when(uc2.getUcNumber()).thenReturn(2);
		existing.add(uc2);

		Mockito.when(useCaseRepository.findByProjectIdOrderByUcNumberAsc(5)).thenReturn(existing);

		useCaseService.create(5, "  Title  ", null, "  Pre  ", "  Flow  ", null, null, " AltPost ");

		ArgumentCaptor<UseCase> captor = ArgumentCaptor.forClass(UseCase.class);
		Mockito.verify(useCaseRepository).save(captor.capture());

		UseCase saved = captor.getValue();
		assertEquals("Title", saved.getTitle());
		assertEquals("", saved.getActors());
		assertEquals("Pre", saved.getPreconditions());
		assertEquals("Flow", saved.getMainFlow());
		assertEquals("", saved.getPostconditions());
		assertEquals("", saved.getAlternativeFlow());
		assertEquals("AltPost", saved.getPostconditionsAlt());
		assertEquals(3, saved.getUcNumber());
	}

	// US7: An leipei kapoio endiameso noumero epeidh to kaname delete to epomeno pou tha dhmiourgisoume tha parei to noumero tou (π.χ. UC1, UC3), to epomeno ginetai UC2
	@Test
	public void create_reusesFirstMissingUcNumber() {
		List<UseCase> existing = new ArrayList<UseCase>();

		UseCase uc1 = Mockito.mock(UseCase.class);
		Mockito.when(uc1.getUcNumber()).thenReturn(1);
		existing.add(uc1);

		UseCase uc3 = Mockito.mock(UseCase.class);
		Mockito.when(uc3.getUcNumber()).thenReturn(3);
		existing.add(uc3);

		Mockito.when(useCaseRepository.findByProjectIdOrderByUcNumberAsc(9)).thenReturn(existing);

		useCaseService.create(9, "T", "A", "P", "F", "Post", "", "");

		ArgumentCaptor<UseCase> captor = ArgumentCaptor.forClass(UseCase.class);
		Mockito.verify(useCaseRepository).save(captor.capture());

		UseCase saved = captor.getValue();
		assertEquals(2, saved.getUcNumber());
	}

	// US8: an den doso titlo sto update, tote den ginetai enhmerosh
	@Test
	public void update_titleRequired() {
		try {
			useCaseService.update(1, 2, "   ", "A", "P", "F", "Post", "", "");
		} catch (IllegalArgumentException ex) {
			assertEquals("Use case is required", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US8: Update me pliri stoixeia apothikeuo kanonika ta stoixeia
	@Test
	public void update_complete_useCase_savesAndDoesNotUnlink() {
		UseCase uc = new UseCase(7, "Old", "OldA", "OldP", "OldF", "OldPost", "", "");
		uc.setUcNumber(1);

		Mockito.when(useCaseRepository.findByIdAndProjectId(4, 7)).thenReturn(Optional.of(uc));

		useCaseService.update(7, 4, "  New  ", "Actor", "Pre", "Flow", "Post", "Alt", "AltPost");

		Mockito.verify(useCaseRepository).save(Mockito.any(UseCase.class));
		Mockito.verify(useCaseServiceLinkService, Mockito.never()).deleteAllForUseCase(Mockito.anyInt(), Mockito.anyInt());

		assertEquals("New", uc.getTitle());
		assertEquals("Actor", uc.getActors());
		assertEquals("Flow", uc.getMainFlow());
	}

	// US8: an se update ta actors h main flow ginoun kena tote ginetai unlink ton CRC links
	@Test
	public void update_blankActorsOrMainFlow_unlinksCrcLinks() {
		UseCase uc = new UseCase(7, "Old", "OldA", "OldP", "OldF", "OldPost", "", "");
		uc.setUcNumber(1);

		Mockito.when(useCaseRepository.findByIdAndProjectId(4, 7)).thenReturn(Optional.of(uc));

		useCaseService.update(7, 4, "New", "   ", "Pre", "Flow", "Post", "", "");

		Mockito.verify(useCaseRepository).save(Mockito.any(UseCase.class));
		Mockito.verify(useCaseServiceLinkService).deleteAllForUseCase(7, 4);
	}

	// US9: epistrefei thn lista ton use cases tou project apo to repository
	@Test
	public void findByProject_returnsRepositoryList() {
		List<UseCase> list = new ArrayList<UseCase>();
		Mockito.when(useCaseRepository.findByProjectIdOrderByUcNumberAsc(3)).thenReturn(list);

		List<UseCase> out = useCaseService.findByProject(3);

		assertEquals(list, out);
	}

	// US10: diagrafh use case tote kaleitai h deleteByIdAndProjectId
	@Test
	public void delete_callsRepositoryDelete() {
		UseCase uc = new UseCase(7, "T", "A", "P", "F", "Post", "", "");
		Mockito.when(useCaseRepository.findByIdAndProjectId(4, 7)).thenReturn(Optional.of(uc));

		useCaseService.delete(7, 4);

		Mockito.verify(useCaseRepository).deleteByIdAndProjectId(4, 7);
	}

	// US13: otan anoigo thn selida me ta link CRC cards fortonontai ta use case, kartes kai linked ids
	@Test
	public void page_returnsUsecaseCrccardsWithData() {
		Integer projectId = 7;
		Integer useCaseId = 4;

		UseCase uc = Mockito.mock(UseCase.class);
		List<CrcCard> cards = new ArrayList<CrcCard>();
		List<Integer> linked = new ArrayList<Integer>();

		Mockito.when(linkControllerUseCaseService.get(projectId, useCaseId)).thenReturn(uc);
		Mockito.when(linkControllerCrcCardService.findByProject(projectId)).thenReturn(cards);
		Mockito.when(linkControllerLinkService.linkedCrcCardIds(projectId, useCaseId)).thenReturn(linked);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = linkController.page(projectId, useCaseId, model);

		assertEquals("usecase_crccards", view);
		assertEquals(projectId, model.get("projectId"));
		assertEquals(uc, model.get("useCase"));
		assertEquals(cards, model.get("cards"));
		assertEquals(linked, model.get("linkedIds"));
	}

	// US13:  epituxia se Link tote ginetai redirect sthn idia selida
	@Test
	public void link_success_redirects() {
		Integer projectId = 7;
		Integer useCaseId = 4;

		ExtendedModelMap model = new ExtendedModelMap();

		String view = linkController.link(projectId, useCaseId, 10, model);

		assertEquals("redirect:/projects/" + projectId + "/usecases/" + useCaseId + "/crccards", view);
		Mockito.verify(linkControllerProjectService).getById(projectId);
		Mockito.verify(linkControllerLinkService).link(projectId, useCaseId, 10);
	}

	// US13: apotuxia se link tote meno sthn idia selida kai blepo mhnuma lathous
	@Test
	public void link_error_returnsPageWithError() {
		Integer projectId = 7;
		Integer useCaseId = 4;

		Mockito.doThrow(new IllegalArgumentException("CRC card or Use Case is incompleted"))
				.when(linkControllerLinkService).link(projectId, useCaseId, 10);

		UseCase uc = Mockito.mock(UseCase.class);
		List<CrcCard> cards = new ArrayList<CrcCard>();
		List<Integer> linked = new ArrayList<Integer>();

		Mockito.when(linkControllerUseCaseService.get(projectId, useCaseId)).thenReturn(uc);
		Mockito.when(linkControllerCrcCardService.findByProject(projectId)).thenReturn(cards);
		Mockito.when(linkControllerLinkService.linkedCrcCardIds(projectId, useCaseId)).thenReturn(linked);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = linkController.link(projectId, useCaseId, 10, model);

		assertEquals("usecase_crccards", view);
		assertEquals("CRC card or Use Case is incompleted", model.get("error"));
	}

	// US13: kano unlink tote redirect sthn idia selida
	@Test
	public void unlink_redirects() {
		Integer projectId = 7;
		Integer useCaseId = 4;

		String view = linkController.unlink(projectId, useCaseId, 10);

		assertEquals("redirect:/projects/" + projectId + "/usecases/" + useCaseId + "/crccards", view);
		Mockito.verify(linkControllerProjectService).getById(projectId);
		Mockito.verify(linkControllerLinkService).unlink(projectId, useCaseId, 10);
	}

	// US13: elegxei gia ena Use Case pou exei sundethei me CRC cards h methodos na epistrefei mono ta ids ton sundedemenon crc cards gia to sugkekrimeno project kai use case.
	@Test
	public void linkedCrcCardIds_returnsIds() {
		UseCase uc = new UseCase(7, "T", "A", "P", "F", "Post", "", "");
		Mockito.when(linkUseCaseRepository.findByIdAndProjectId(4, 7)).thenReturn(Optional.of(uc));

		List<UseCaseCrcLink> links = new ArrayList<UseCaseCrcLink>();
		links.add(new UseCaseCrcLink(7, 4, 10));
		links.add(new UseCaseCrcLink(7, 4, 11));

		Mockito.when(linkRepository.findByProjectIdAndUseCaseId(7, 4)).thenReturn(links);

		List<Integer> out = linkService.linkedCrcCardIds(7, 4);

		assertEquals(2, out.size());
		assertEquals(10, out.get(0));
		assertEquals(11, out.get(1));
	}

	// US13: an to use case den uparxei den mporo na kano link
	@Test
	public void link_useCaseNotFound_throws() {
		Mockito.when(linkUseCaseRepository.findByIdAndProjectId(4, 7)).thenReturn(Optional.empty());

		try {
			linkService.link(7, 4, 10);
		} catch (IllegalArgumentException ex) {
			assertEquals("Use case not found", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US13: an to crc card den uparxei den mporo na kano link
	@Test
	public void link_crcNotFound_throws() {
		UseCase uc = new UseCase(7, "T", "A", "P", "F", "Post", "", "");
		Mockito.when(linkUseCaseRepository.findByIdAndProjectId(4, 7)).thenReturn(Optional.of(uc));
		Mockito.when(linkCrcCardRepository.findByIdAndProjectId(10, 7)).thenReturn(Optional.empty());

		try {
			linkService.link(7, 4, 10);
		} catch (IllegalArgumentException ex) {
			assertEquals("CRC card not found", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US13: an to use case h to crc card einai incomplete den ginetai link
	@Test
	public void link_incomplete_throws() {
		UseCase uc = new UseCase(7, "T", "   ", "P", "F", "Post", "", "");
		CrcCard card = new CrcCard(7, "C", "Resp", "");

		Mockito.when(linkUseCaseRepository.findByIdAndProjectId(4, 7)).thenReturn(Optional.of(uc));
		Mockito.when(linkCrcCardRepository.findByIdAndProjectId(10, 7)).thenReturn(Optional.of(card));

		try {
			linkService.link(7, 4, 10);
		} catch (IllegalArgumentException ex) {
			assertEquals("CRC card or Use Case is incompleted", ex.getMessage());
			Mockito.verify(linkRepository, Mockito.never()).save(Mockito.any(UseCaseCrcLink.class));
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US13: elegxei oti ginetai to link
	@Test
	public void link_notExists_saves() {
		UseCase uc = new UseCase(7, "T", "A", "P", "F", "Post", "", "");
		CrcCard card = new CrcCard(7, "C", "Resp", "");

		Mockito.when(linkUseCaseRepository.findByIdAndProjectId(4, 7)).thenReturn(Optional.of(uc));
		Mockito.when(linkCrcCardRepository.findByIdAndProjectId(10, 7)).thenReturn(Optional.of(card));
		Mockito.when(linkRepository.existsByProjectIdAndUseCaseIdAndCrcCardId(7, 4, 10)).thenReturn(false);

		linkService.link(7, 4, 10);

		ArgumentCaptor<UseCaseCrcLink> captor = ArgumentCaptor.forClass(UseCaseCrcLink.class);
		Mockito.verify(linkRepository).save(captor.capture());

		UseCaseCrcLink saved = captor.getValue();
		assertEquals(7, saved.getProjectId());
		assertEquals(4, saved.getUseCaseId());
		assertEquals(10, saved.getCrcCardId());
	}

	// US13: sbhnei to link
	@Test
	public void unlink_deletesLink() {
		UseCase uc = new UseCase(7, "T", "A", "P", "F", "Post", "", "");
		CrcCard card = new CrcCard(7, "C", "Resp", "");

		Mockito.when(linkUseCaseRepository.findByIdAndProjectId(4, 7)).thenReturn(Optional.of(uc));
		Mockito.when(linkCrcCardRepository.findByIdAndProjectId(10, 7)).thenReturn(Optional.of(card));

		linkService.unlink(7, 4, 10);

		Mockito.verify(linkRepository).deleteByProjectIdAndUseCaseIdAndCrcCardId(7, 4, 10);
	}
}
