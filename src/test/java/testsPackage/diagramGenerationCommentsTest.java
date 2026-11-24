package testsPackage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;

import oi.lebentes.controllers.CommentForm;
import oi.lebentes.controllers.CommentsController;
import oi.lebentes.controllers.DiagramsController;
import oi.lebentes.diagrams.ClassDiagramGenerator;
import oi.lebentes.diagrams.DiagramGeneratorFactory;
import oi.lebentes.diagrams.NomnomlClassDiagramGenerator;
import oi.lebentes.diagrams.NomnomlUseCaseDiagramGenerator;
import oi.lebentes.diagrams.PlantUmlClassDiagramGenerator;
import oi.lebentes.diagrams.PlantUmlUseCaseDiagramGenerator;
import oi.lebentes.diagrams.UseCaseDiagramGenerator;
import oi.lebentes.domain.Comment;
import oi.lebentes.domain.CrcCard;
import oi.lebentes.domain.UseCase;
import oi.lebentes.domain.User;
import oi.lebentes.repositories.CommentRepository;
import oi.lebentes.services.CommentService;
import oi.lebentes.services.CrcCardService;
import oi.lebentes.services.DiagramService;
import oi.lebentes.services.ProjectService;
import oi.lebentes.services.UseCaseService;
import oi.lebentes.services.UserService;

class diagramGenerationCommentsTest {

	private DiagramService diagramsControllerDiagramService;
	private DiagramsController diagramsController;

	private ProjectService diagramProjectService;
	private UseCaseService diagramUseCaseService;
	private CrcCardService diagramCrcCardService;
	private DiagramGeneratorFactory diagramFactory;
	private DiagramService diagramService;

	private CommentService commentsControllerCommentService;
	private ProjectService commentsControllerProjectService;
	private UserService commentsControllerUserService;
	private UseCaseService commentsControllerUseCaseService;
	private CrcCardService commentsControllerCrcCardService;
	private CommentsController commentsController;

	private CommentRepository commentRepository;
	private CommentService commentService;

	@BeforeEach
	public void setup() {
		diagramsControllerDiagramService = Mockito.mock(DiagramService.class);
		diagramsController = new DiagramsController(diagramsControllerDiagramService);

		diagramProjectService = Mockito.mock(ProjectService.class);
		diagramUseCaseService = Mockito.mock(UseCaseService.class);
		diagramCrcCardService = Mockito.mock(CrcCardService.class);
		diagramFactory = Mockito.mock(DiagramGeneratorFactory.class);

		diagramService = new DiagramService(
				diagramProjectService,
				diagramUseCaseService,
				diagramCrcCardService,
				diagramFactory
		);

		commentsControllerCommentService = Mockito.mock(CommentService.class);
		commentsControllerProjectService = Mockito.mock(ProjectService.class);
		commentsControllerUserService = Mockito.mock(UserService.class);
		commentsControllerUseCaseService = Mockito.mock(UseCaseService.class);
		commentsControllerCrcCardService = Mockito.mock(CrcCardService.class);

		commentsController = new CommentsController(
				commentsControllerCommentService,
				commentsControllerProjectService,
				commentsControllerUserService,
				commentsControllerUseCaseService,
				commentsControllerCrcCardService
		);

		commentRepository = Mockito.mock(CommentRepository.class);
		commentService = new CommentService(commentRepository);
	}

	// US15/US16: otan anoigo thn selida Diagrams prepei na gemizei me 4 scripts (UseCase PlantUML/Nomnoml kai Class PlantUML/Nomnoml)
	@Test
	public void page_returnsDiagramsWithScripts() {
		Mockito.when(diagramsControllerDiagramService.useCasePlantUml(7)).thenReturn("UCP");
		Mockito.when(diagramsControllerDiagramService.useCaseNomnoml(7)).thenReturn("UCN");
		Mockito.when(diagramsControllerDiagramService.classPlantUml(7)).thenReturn("CLP");
		Mockito.when(diagramsControllerDiagramService.classNomnoml(7)).thenReturn("CLN");

		ExtendedModelMap model = new ExtendedModelMap();

		String view = diagramsController.page(7, model);

		assertEquals("diagrams", view);
		assertEquals(7, model.get("projectId"));
		assertEquals("UCP", model.get("usecasePlantUml"));
		assertEquals("UCN", model.get("usecaseNomnoml"));
		assertEquals("CLP", model.get("classPlantUml"));
		assertEquals("CLN", model.get("classNomnoml"));
	}

	// US15: an kapoio use case den exei actors den paragetai script kai bgainei mhnuma lathous
	@Test
	public void useCasePlantUml_incomplete_returnsMessage() {
		List<UseCase> ucs = new ArrayList<UseCase>();
		UseCase uc = new UseCase(7, "T", "   ", "P", "F", "Post", "", "");
		uc.setUcNumber(1);
		ucs.add(uc);

		Mockito.when(diagramUseCaseService.findByProject(7)).thenReturn(ucs);

		String out = diagramService.useCasePlantUml(7);

		assertEquals("Some Use Cases are incompleted", out);
		Mockito.verify(diagramFactory, Mockito.never()).useCaseGenerator(Mockito.anyString());
	}

	// US15: otan ola ta use cases exoun actors kalei PlantUML generator kai epistrefei to script
	@Test
	public void useCasePlantUml_complete_callsGenerator() {
		List<UseCase> ucs = new ArrayList<UseCase>();
		UseCase uc = new UseCase(7, "Login", "User", "P", "F", "Post", "", "");
		uc.setUcNumber(1);
		ucs.add(uc);

		Mockito.when(diagramUseCaseService.findByProject(7)).thenReturn(ucs);

		UseCaseDiagramGenerator gen = Mockito.mock(UseCaseDiagramGenerator.class);
		Mockito.when(diagramFactory.useCaseGenerator("plantuml")).thenReturn(gen);
		Mockito.when(gen.generate(ucs)).thenReturn("PLANT_SCRIPT");

		String out = diagramService.useCasePlantUml(7);

		assertEquals("PLANT_SCRIPT", out);
		Mockito.verify(diagramFactory).useCaseGenerator("plantuml");
	}

	// US15: otan ola ta use cases exoun actors kalei Nomnoml generator kai epistrefei to script
	@Test
	public void useCaseNomnoml_complete_callsGenerator() {
		List<UseCase> ucs = new ArrayList<UseCase>();
		UseCase uc = new UseCase(7, "Register", "User", "P", "F", "Post", "", "");
		uc.setUcNumber(2);
		ucs.add(uc);

		Mockito.when(diagramUseCaseService.findByProject(7)).thenReturn(ucs);

		UseCaseDiagramGenerator gen = Mockito.mock(UseCaseDiagramGenerator.class);
		Mockito.when(diagramFactory.useCaseGenerator("nomnoml")).thenReturn(gen);
		Mockito.when(gen.generate(ucs)).thenReturn("NOM_SCRIPT");

		String out = diagramService.useCaseNomnoml(7);

		assertEquals("NOM_SCRIPT", out);
		Mockito.verify(diagramFactory).useCaseGenerator("nomnoml");
	}

	// US16:an kapoio crc card den exei responsibilities den paragetai script kai mhnuma lathous
	@Test
	public void classPlantUml_blankResponsibilities_returnsMessage() {
		List<CrcCard> cards = new ArrayList<CrcCard>();
		cards.add(new CrcCard(7, "A", "   ", ""));
		cards.add(new CrcCard(7, "B", "R", "dependency:A"));

		Mockito.when(diagramCrcCardService.findByProject(7)).thenReturn(cards);

		String out = diagramService.classPlantUml(7);

		assertEquals("Some CRC cards are incompleted", out);
		Mockito.verify(diagramFactory, Mockito.never()).classGenerator(Mockito.anyString());
	}

	// US16: an uparxei isolated(dhladh pou den exei alles sxeseis/sundeseis me alles crc cards) crc card epistrefei mhnuma incompleted crc card
	@Test
	public void classPlantUml_isolatedCard_returnsMessage() {
		List<CrcCard> cards = new ArrayList<CrcCard>();
		cards.add(new CrcCard(7, "A", "R", "association:B"));
		cards.add(new CrcCard(7, "B", "R", ""));
		cards.add(new CrcCard(7, "C", "R", ""));

		Mockito.when(diagramCrcCardService.findByProject(7)).thenReturn(cards);

		String out = diagramService.classPlantUml(7);

		assertEquals("Some CRC cards are incompleted", out);
		Mockito.verify(diagramFactory, Mockito.never()).classGenerator(Mockito.anyString());
	}

	// US16: otan ola ta CRC cards einai ok kalei Nomnoml class generator kai epistrefei script
	@Test
	public void classNomnoml_complete_callsGenerator() {
		List<CrcCard> cards = new ArrayList<CrcCard>();
		cards.add(new CrcCard(7, "A", "R", "dependency:B"));
		cards.add(new CrcCard(7, "B", "R", ""));

		Mockito.when(diagramCrcCardService.findByProject(7)).thenReturn(cards);

		ClassDiagramGenerator gen = Mockito.mock(ClassDiagramGenerator.class);
		Mockito.when(diagramFactory.classGenerator("nomnoml")).thenReturn(gen);
		Mockito.when(gen.generate(cards)).thenReturn("CLASS_NOM");

		String out = diagramService.classNomnoml(7);

		assertEquals("CLASS_NOM", out);
		Mockito.verify(diagramFactory).classGenerator("nomnoml");
	}

	// otan to susthma zhta PlantUML gia use case diagrams prepei na epistrefetai PlantUML generator
	@Test
	public void useCaseGenerator_plantuml_returnsPlantUml() {
		DiagramGeneratorFactory f = new DiagramGeneratorFactory();
		assertEquals(PlantUmlUseCaseDiagramGenerator.class, f.useCaseGenerator("plantuml").getClass());
	}

	// otan to susthma zhta Nomnoml gia use case diagrams prepei na epistrefetai Nomnoml generator
	@Test
	public void useCaseGenerator_nomnoml_returnsNomnoml() {
		DiagramGeneratorFactory f = new DiagramGeneratorFactory();
		assertEquals(NomnomlUseCaseDiagramGenerator.class, f.useCaseGenerator("nomnoml").getClass());
	}

	// otan to susthma zhta PlantUML gia class diagrams prepei na epistrefetai PlantUML generator
	@Test
	public void classGenerator_plantuml_returnsPlantUml() {
		DiagramGeneratorFactory f = new DiagramGeneratorFactory();
		assertEquals(PlantUmlClassDiagramGenerator.class, f.classGenerator("plantuml").getClass());
	}

	// otan to susthma zhta Nomnoml gia class diagrams prepei na epistrefetai Nomnoml generator
	@Test
	public void classGenerator_nomnoml_returnsNomnoml() {
		DiagramGeneratorFactory f = new DiagramGeneratorFactory();
		assertEquals(NomnomlClassDiagramGenerator.class, f.classGenerator("nomnoml").getClass());
	}

	// US15: paragei plantuml script gia use cases
	@Test
	public void generate_containsActorsUseCasesAndLinks() {
		List<UseCase> ucs = new ArrayList<UseCase>();

		UseCase uc1 = new UseCase(7, "Login", "User", "", "", "", "", "");
		uc1.setUcNumber(1);

		UseCase uc2 = new UseCase(7, "Register", "User, Admin", "", "", "", "", "");
		uc2.setUcNumber(2);

		ucs.add(uc1);
		ucs.add(uc2);

		PlantUmlUseCaseDiagramGenerator gen = new PlantUmlUseCaseDiagramGenerator();
		String out = gen.generate(ucs);

		assertTrue(out.contains("@startuml"));
		assertTrue(out.contains("actor \"User\" as A1"));
		assertTrue(out.contains("actor \"Admin\" as A2"));
		assertTrue(out.contains("usecase \"UC1: Login\" as UC1"));
		assertTrue(out.contains("usecase \"UC2: Register\" as UC2"));
		assertTrue(out.contains("A1 --> UC1"));
		assertTrue(out.contains("A1 --> UC2"));
		assertTrue(out.contains("A2 --> UC2"));
		assertTrue(out.contains("@enduml"));
	}

	// US15: paragei nomnoml script gia use cases
	@Test
	public void generate_containsNodesAndLinks() {
		List<UseCase> ucs = new ArrayList<UseCase>();

		UseCase uc1 = new UseCase(7, "Login", "User", "", "", "", "", "");
		uc1.setUcNumber(1);
		ucs.add(uc1);

		NomnomlUseCaseDiagramGenerator gen = new NomnomlUseCaseDiagramGenerator();
		String out = gen.generate(ucs);

		assertTrue(out.contains("#direction: right"));
		assertTrue(out.contains("[User] - [UC1: Login]"));
	}

	// US16: paragei plantuml class diagram apo crc cards
	@Test
	public void generate_containsClassesResponsibilitiesAndRelations() {
		List<CrcCard> cards = new ArrayList<CrcCard>();

		CrcCard a = new CrcCard(7, "A", "do a\ndo b", "dependency:B, dependency:B");
		CrcCard b = new CrcCard(7, "B", "do c", "");
		cards.add(a);
		cards.add(b);

		PlantUmlClassDiagramGenerator gen = new PlantUmlClassDiagramGenerator();
		String out = gen.generate(cards);

		assertTrue(out.contains("@startuml"));
		assertTrue(out.contains("class A"));
		assertTrue(out.contains("..Responsibilities.."));
		assertTrue(out.contains("do a"));
		assertTrue(out.contains("do b"));
		assertTrue(out.contains("class B"));
		assertTrue(out.contains("A ..> B"));
		assertEquals(1, count(out, "A ..> B"));
		assertTrue(out.contains("@enduml"));
	}

	// US16: paragei nomnoml class diagram apo ta crc cards
	// den metriountai oi diplotimes sxeseis
	@Test
	public void generate_containsNodesAndRelations() {
		List<CrcCard> cards = new ArrayList<CrcCard>();

		CrcCard a = new CrcCard(7, "A", "do a\ndo b", "dependency:B, dependency:B");
		CrcCard b = new CrcCard(7, "B", "do c", "");
		cards.add(a);
		cards.add(b);

		NomnomlClassDiagramGenerator gen = new NomnomlClassDiagramGenerator();
		String out = gen.generate(cards);

		assertTrue(out.contains("#direction: right"));
		assertTrue(out.contains("[A|do a;do b]"));
		assertTrue(out.contains("[B|do c]"));
		assertTrue(out.contains("[A] --> [B]"));
		assertEquals(1, count(out, "[A] --> [B]"));
	}

	// US19: an den exo prosbash sto project, den anoigei h selida me ta sxolia(mporo na mpo sthn selida xrhsimopoiontas to url:(http://localhost:8080/projects/(projectid)/usecases/(usecaseid)/comments) kai oxi mono me ton klasiko tropo)
	@Test
	public void useCaseComments_noAccess_throws() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("me@a.com");

		User me = Mockito.mock(User.class);
		Mockito.when(me.getId()).thenReturn(10);
		Mockito.when(commentsControllerUserService.getByEmail("me@a.com")).thenReturn(me);

		Mockito.when(commentsControllerProjectService.canAccess(7, 10)).thenReturn(false);

		try {
			commentsController.useCaseComments(7, 4, new ExtendedModelMap(), auth);
		} catch (IllegalArgumentException ex) {
			assertEquals("No access to project", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US19: otan exo prosbash sto project blepo thn selida me ta sxolia ton use case kai pernietai to id tou sundedemenou xrhsth
	@Test
	public void useCaseComments_success_returnsPage() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("me@a.com");

		User me = Mockito.mock(User.class);
		Mockito.when(me.getId()).thenReturn(10);
		Mockito.when(commentsControllerUserService.getByEmail("me@a.com")).thenReturn(me);

		Mockito.when(commentsControllerProjectService.canAccess(7, 10)).thenReturn(true);

		UseCase uc = Mockito.mock(UseCase.class);
		Mockito.when(commentsControllerUseCaseService.get(7, 4)).thenReturn(uc);

		List<Comment> comments = new ArrayList<Comment>();
		Mockito.when(commentsControllerCommentService.list(7, "USECASE", 4)).thenReturn(comments);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = commentsController.useCaseComments(7, 4, model, auth);

		assertEquals("usecase_comments", view);
		assertEquals(7, model.get("projectId"));
		assertEquals(uc, model.get("useCase"));
		assertEquals(10, model.get("currentUserId"));
		assertEquals(comments, model.get("comments"));
	}

	// US19: otan prosteto sxolio se use case apothikeuetai mazi me to id tou suntakth kai kanei redirect piso sthn selida me ta sxolia
	@Test
	public void addUseCaseComment_redirects() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("me@a.com");

		User me = Mockito.mock(User.class);
		Mockito.when(me.getId()).thenReturn(10);
		Mockito.when(me.getName()).thenReturn("Me");
		Mockito.when(commentsControllerUserService.getByEmail("me@a.com")).thenReturn(me);

		Mockito.when(commentsControllerProjectService.canAccess(7, 10)).thenReturn(true);

		CommentForm form = new CommentForm();
		form.setText("hello");

		String view = commentsController.addUseCaseComment(7, 4, form, auth);

		assertEquals("redirect:/projects/7/usecases/4/comments", view);
		Mockito.verify(commentsControllerCommentService).add(7, "USECASE", 4, 10, "Me", "hello");
	}

	// US19: otan exo prosbash sto project blepo thn selida me ta sxolia kai to onoma ths CRC card kai pernietai to id tou sundedemenou xrhsth
	@Test
	public void crcCardComments_success_returnsPage() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("me@a.com");

		User me = Mockito.mock(User.class);
		Mockito.when(me.getId()).thenReturn(10);
		Mockito.when(commentsControllerUserService.getByEmail("me@a.com")).thenReturn(me);

		Mockito.when(commentsControllerProjectService.canAccess(7, 10)).thenReturn(true);

		CrcCard card = new CrcCard(7, "ShoppingCart", "Manage cart", "");
		Mockito.when(commentsControllerCrcCardService.get(7, 3)).thenReturn(card);

		List<Comment> comments = new ArrayList<Comment>();
		Mockito.when(commentsControllerCommentService.list(7, "CRC", 3)).thenReturn(comments);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = commentsController.crcCardComments(7, 3, model, auth);

		assertEquals("crccard_comments", view);
		assertEquals(7, model.get("projectId"));
		assertEquals(3, model.get("crcCardId"));
		assertEquals(card, model.get("crcCard"));
		assertEquals("ShoppingCart", ((CrcCard) model.get("crcCard")).getClassName());
		assertEquals(10, model.get("currentUserId"));
		assertEquals(comments, model.get("comments"));
	}

	// US19: otan prosteto sxolio se CRC card, apothikeuetai mazi me to id tou suntakth kai ginetai redirect piso sthn selida me ta sxolia
	@Test
	public void addCrcCardComment_redirects() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("me@a.com");

		User me = Mockito.mock(User.class);
		Mockito.when(me.getId()).thenReturn(10);
		Mockito.when(me.getName()).thenReturn("Me");
		Mockito.when(commentsControllerUserService.getByEmail("me@a.com")).thenReturn(me);

		Mockito.when(commentsControllerProjectService.canAccess(7, 10)).thenReturn(true);

		CommentForm form = new CommentForm();
		form.setText("hello");

		String view = commentsController.addCrcCardComment(7, 3, form, auth);

		assertEquals("redirect:/projects/7/crccards/3/comments", view);
		Mockito.verify(commentsControllerCommentService).add(7, "CRC", 3, 10, "Me", "hello");
	}

	// US19: den epitrepetai keno comment
	@Test
	public void add_textRequired() {
		try {
			commentService.add(7, "USECASE", 4, 10, "me", "   ");
		} catch (IllegalArgumentException ex) {
			assertEquals("Comment text is required", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US19: apothikeuontai trimmed texts dhladh kobontai ta kena apo thn arxi kai to telos tou sxoliou, mazi me to id tou suntakth
	@Test
	public void add_savesTrimmedText() {
		commentService.add(7, "USECASE", 4, 10, "me", "  hello  ");

		ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
		Mockito.verify(commentRepository).save(captor.capture());

		Comment saved = captor.getValue();
		assertEquals(7, saved.getProjectId());
		assertEquals("USECASE", saved.getTargetType());
		assertEquals(4, saved.getTargetId());
		assertEquals(10, saved.getAuthorId());
		assertEquals("me", saved.getAuthorName());
		assertEquals("hello", saved.getText());
	}

	// otan o xrhsths einai o suntakths tou sxoliou mporei na to kanei edit kai delete, enw enas allos xrhsths den mporei
	@Test
	public void editAndDeleteComment_checksOwnership() {
		Comment comment = new Comment(7, "USECASE", 4, 10, "Me", "Old comment");

		Mockito.when(commentRepository.findById(3)).thenReturn(Optional.of(comment));

		assertThrows(AccessDeniedException.class,
				() -> commentService.update(7, 3, 11, "New comment"));

		assertThrows(AccessDeniedException.class,
				() -> commentService.delete(7, 3, 11));

		assertEquals("Old comment", comment.getText());
		Mockito.verify(commentRepository, Mockito.never()).save(comment);
		Mockito.verify(commentRepository, Mockito.never()).delete(comment);

		commentService.update(7, 3, 10, "New comment");

		assertEquals("New comment", comment.getText());
		Mockito.verify(commentRepository).save(comment);

		commentService.delete(7, 3, 10);

		Mockito.verify(commentRepository).delete(comment);
	}

	private int count(String text, String part) {
		int i = 0;
		int c = 0;
		while (true) {
			int idx = text.indexOf(part, i);
			if (idx < 0) {
				return c;
			}
			c = c + 1;
			i = idx + part.length();
		}
	}
}
