package testsPackage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;

import oi.lebentes.controllers.ProjectForm;
import oi.lebentes.controllers.ProjectShareController;
import oi.lebentes.controllers.ProjectShareForm;
import oi.lebentes.controllers.ProjectsController;
import oi.lebentes.domain.Project;
import oi.lebentes.domain.ProjectMember;
import oi.lebentes.domain.User;
import oi.lebentes.repositories.CommentRepository;
import oi.lebentes.repositories.ProjectMemberRepository;
import oi.lebentes.repositories.ProjectRepository;
import oi.lebentes.repositories.UserRepository;
import oi.lebentes.services.CrcCardService;
import oi.lebentes.services.ProjectService;
import oi.lebentes.services.UseCaseCrcLinkService;
import oi.lebentes.services.UseCaseService;
import oi.lebentes.services.UserService;

class projectCreateDeleteShareLeaveTest {

	private ProjectService projectsControllerProjectService;
	private UserService projectsControllerUserService;
	private ProjectsController projectsController;

	private ProjectRepository projectRepository;
	private ProjectMemberRepository projectMemberRepository;
	private UseCaseService useCaseService;
	private CrcCardService crcCardService;
	private UseCaseCrcLinkService useCaseCrcLinkService;
	private CommentRepository commentRepository;
	private ProjectService projectService;

	private ProjectService shareControllerProjectService;
	private UserService shareControllerUserService;
	private UserRepository shareControllerUserRepository;
	private ProjectShareController projectShareController;

	@BeforeEach
	public void setup() {
		projectsControllerProjectService = Mockito.mock(ProjectService.class);
		projectsControllerUserService = Mockito.mock(UserService.class);
		projectsController = new ProjectsController(projectsControllerProjectService, projectsControllerUserService);

		projectRepository = Mockito.mock(ProjectRepository.class);
		projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
		useCaseService = Mockito.mock(UseCaseService.class);
		crcCardService = Mockito.mock(CrcCardService.class);
		useCaseCrcLinkService = Mockito.mock(UseCaseCrcLinkService.class);
		commentRepository = Mockito.mock(CommentRepository.class);

		projectService = new ProjectService(
				projectRepository,
				projectMemberRepository,
				useCaseService,
				crcCardService,
				useCaseCrcLinkService,
				commentRepository
		);

		shareControllerProjectService = Mockito.mock(ProjectService.class);
		shareControllerUserService = Mockito.mock(UserService.class);
		shareControllerUserRepository = Mockito.mock(UserRepository.class);

		projectShareController = new ProjectShareController(
				shareControllerProjectService,
				shareControllerUserService,
				shareControllerUserRepository
		);
	}

	// US4/US18: otan anoigo thn selida Projects, emfanizontai ksexorista ta dika mou kai ta shared projects
	@Test
	public void showProjects_returnsProjectsPageWithOwnedAndSharedLists() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("a@b.com");

		User user = Mockito.mock(User.class);
		Mockito.when(user.getId()).thenReturn(10);
		Mockito.when(user.getName()).thenReturn("leb");
		Mockito.when(projectsControllerUserService.getByEmail("a@b.com")).thenReturn(user);

		List<Project> ownedProjects = new ArrayList<Project>();
		List<Project> sharedProjects = new ArrayList<Project>();

		Mockito.when(projectsControllerProjectService.findOwnedForUser(10)).thenReturn(ownedProjects);
		Mockito.when(projectsControllerProjectService.findSharedForUser(10)).thenReturn(sharedProjects);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = projectsController.showProjects(model, auth);

		assertEquals("projects", view);
		assertEquals(ownedProjects, model.get("ownedProjects"));
		assertEquals(sharedProjects, model.get("sharedProjects"));
		assertTrue(model.get("form") instanceof ProjectForm);
		assertEquals(10, model.get("currentUserId"));
		assertEquals("leb", model.get("currentUsername"));
	}

	// US5: otan patao create project me swsto onoma meno sthn idia selida
	@Test
	public void createProject_success_redirectsToProjects() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("a@b.com");

		User user = Mockito.mock(User.class);
		Mockito.when(user.getId()).thenReturn(10);
		Mockito.when(projectsControllerUserService.getByEmail("a@b.com")).thenReturn(user);

		ProjectForm form = new ProjectForm();
		form.setName("My Project");

		ExtendedModelMap model = new ExtendedModelMap();

		String view = projectsController.createProject(form, model, auth);

		assertEquals("redirect:/projects", view);
		Mockito.verify(projectsControllerProjectService).createProject("My Project", 10);
	}

	// US5: an pathso create project me keno onoma, meno sthn idia selida kai blepo mhnuma lathous
	@Test
	public void createProject_error_returnsProjectsWithError() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("a@b.com");

		User user = Mockito.mock(User.class);
		Mockito.when(user.getId()).thenReturn(10);
		Mockito.when(user.getName()).thenReturn("leb");
		Mockito.when(projectsControllerUserService.getByEmail("a@b.com")).thenReturn(user);

		ProjectForm form = new ProjectForm();
		form.setName("   ");

		Mockito.doThrow(new IllegalArgumentException("Project name is required"))
				.when(projectsControllerProjectService).createProject("   ", 10);

		List<Project> ownedProjects = new ArrayList<Project>();
		List<Project> sharedProjects = new ArrayList<Project>();

		Mockito.when(projectsControllerProjectService.findOwnedForUser(10)).thenReturn(ownedProjects);
		Mockito.when(projectsControllerProjectService.findSharedForUser(10)).thenReturn(sharedProjects);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = projectsController.createProject(form, model, auth);

		assertEquals("projects", view);
		assertEquals(ownedProjects, model.get("ownedProjects"));
		assertEquals(sharedProjects, model.get("sharedProjects"));
		assertEquals(form, model.get("form"));
		assertEquals(10, model.get("currentUserId"));
		assertEquals("leb", model.get("currentUsername"));
		assertEquals("Project name is required", model.get("error"));
	}

	// US6: otan patao delete se project pou mou anhkei meno sthn idia selida
	@Test
	public void deleteProject_success_redirectsToProjects() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("a@b.com");

		User user = Mockito.mock(User.class);
		Mockito.when(user.getId()).thenReturn(10);
		Mockito.when(projectsControllerUserService.getByEmail("a@b.com")).thenReturn(user);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = projectsController.deleteProject(7, model, auth);

		assertEquals("redirect:/projects", view);
		Mockito.verify(projectsControllerProjectService).deleteProjectForUser(7, 10);
	}

	// leave: otan patao leave se shared project meno sthn idia selida
	@Test
	public void leaveProject_success_redirectsToProjects() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("a@b.com");

		User user = Mockito.mock(User.class);
		Mockito.when(user.getId()).thenReturn(10);
		Mockito.when(projectsControllerUserService.getByEmail("a@b.com")).thenReturn(user);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = projectsController.leaveProject(7, model, auth);

		assertEquals("redirect:/projects", view);
		Mockito.verify(projectsControllerProjectService).leaveProject(7, 10);
	}

	// US4: an den uparxei userId, den mporei na fortosei h lista me ta dika mou projects
	@Test
	public void findOwnedForUser_userIdRequired() {
		try {
			projectService.findOwnedForUser(null);
		} catch (IllegalArgumentException ex) {
			assertEquals("User id is required", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US4: epistrefei mono ta projects pou anhkoun ston sugkekrimeno xrhsth
	@Test
	public void findOwnedForUser_returnsOwnedProjects() {
		Integer userId = 10;

		List<Project> owned = new ArrayList<Project>();
		Project p1 = Mockito.mock(Project.class);
		Project p2 = Mockito.mock(Project.class);
		owned.add(p1);
		owned.add(p2);

		Mockito.when(projectRepository.findByOwnerIdOrderByNameAsc(userId)).thenReturn(owned);

		List<Project> out = projectService.findOwnedForUser(userId);

		assertEquals(2, out.size());
		assertEquals(p1, out.get(0));
		assertEquals(p2, out.get(1));
	}

	// US18: an den uparxei userId, den mporei na fortosei h lista me ta shared projects
	@Test
	public void findSharedForUser_userIdRequired() {
		try {
			projectService.findSharedForUser(null);
		} catch (IllegalArgumentException ex) {
			assertEquals("User id is required", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US18: epistrefei mono ta projects pou exoun ginei share ston sugkekrimeno xrhsth
	@Test
	public void findSharedForUser_returnsSharedProjects() {
		Integer userId = 10;

		Project p2 = Mockito.mock(Project.class);
		Project p3 = Mockito.mock(Project.class);

		List<ProjectMember> memberships = new ArrayList<ProjectMember>();
		memberships.add(new ProjectMember(2, userId));
		memberships.add(new ProjectMember(3, userId));

		Mockito.when(projectMemberRepository.findByUserId(userId)).thenReturn(memberships);
		Mockito.when(projectRepository.findById(2)).thenReturn(Optional.of(p2));
		Mockito.when(projectRepository.findById(3)).thenReturn(Optional.of(p3));

		List<Project> out = projectService.findSharedForUser(userId);

		assertEquals(2, out.size());
		assertEquals(p2, out.get(0));
		assertEquals(p3, out.get(1));
	}

	// US5: an to onoma einai keno, den ginetai dhmiourgia project
	@Test
	public void createProject_nameRequired() {
		try {
			projectService.createProject("   ", 5);
		} catch (IllegalArgumentException ex) {
			assertEquals("Project name is required", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US5: otan dhmiourgo project, apothikeuei trimmed name kai ownerId
	@Test
	public void createProject_savesTrimmedNameAndOwner() {
		projectService.createProject("  My Project  ", 5);

		ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
		Mockito.verify(projectRepository).save(captor.capture());

		Project saved = captor.getValue();
		assertEquals("My Project", saved.getName());
		assertEquals(5, saved.getOwnerId());
	}

	// US6: an den dothei projectId, den mporei na ginei delete
	@Test
	public void deleteProjectForUser_projectIdRequired() {
		try {
			projectService.deleteProjectForUser(null, 5);
		} catch (IllegalArgumentException ex) {
			assertEquals("Project id is required", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US6: mono o owner mporei na sbhsei ena project
	@Test
	public void deleteProjectForUser_onlyOwnerCanDelete() {
		Integer projectId = 7;

		Project p = Mockito.mock(Project.class);
		Mockito.when(p.getOwnerId()).thenReturn(5);
		Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(p));

		try {
			projectService.deleteProjectForUser(projectId, 6);
		} catch (IllegalArgumentException ex) {
			assertEquals("Only the owner can delete the project", ex.getMessage());

			Mockito.verify(projectRepository, Mockito.never()).deleteById(Mockito.anyInt());
			Mockito.verify(projectMemberRepository, Mockito.never()).deleteByProjectId(Mockito.anyInt());
			Mockito.verify(useCaseService, Mockito.never()).deleteAllForProject(Mockito.anyInt());
			Mockito.verify(crcCardService, Mockito.never()).deleteAllForProject(Mockito.anyInt());
			Mockito.verify(useCaseCrcLinkService, Mockito.never()).deleteAllForProject(Mockito.anyInt());
			Mockito.verify(commentRepository, Mockito.never()).deleteByProjectId(Mockito.anyInt());

			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US6: otan o owner sbhnei ena project, sbhnei kai oti eixe mesa to project auto
	@Test
	public void deleteProjectForUser_ownerDeletes_cascadesAndDeletesProject() {
		Integer projectId = 7;

		Project p = Mockito.mock(Project.class);
		Mockito.when(p.getOwnerId()).thenReturn(5);
		Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(p));

		projectService.deleteProjectForUser(projectId, 5);

		Mockito.verify(useCaseCrcLinkService).deleteAllForProject(projectId);
		Mockito.verify(commentRepository).deleteByProjectId(projectId);
		Mockito.verify(useCaseService).deleteAllForProject(projectId);
		Mockito.verify(crcCardService).deleteAllForProject(projectId);
		Mockito.verify(projectMemberRepository).deleteByProjectId(projectId);
		Mockito.verify(projectRepository).deleteById(projectId);
	}

	// US18: den ginetai share an leipei projectId
	@Test
	public void shareProject_projectIdRequired() {
		try {
			projectService.shareProject(null, 1, 2);
		} catch (IllegalArgumentException ex) {
			assertEquals("Project id is required", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US18: den ginetai share an leipei teammateUserId
	@Test
	public void shareProject_teammateIdRequired() {
		try {
			projectService.shareProject(7, 1, null);
		} catch (IllegalArgumentException ex) {
			assertEquals("Teammate user id is required", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US18: den ginetai share me ton eauto sou
	@Test
	public void shareProject_cannotShareWithYourself() {
		try {
			projectService.shareProject(7, 5, 5);
		} catch (IllegalArgumentException ex) {
			assertEquals("You cannot share with yourself", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US18: mono o owner mporei na kanei share
	@Test
	public void shareProject_onlyOwnerCanShare() {
		Project p = new Project("P", 10);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));

		try {
			projectService.shareProject(7, 11, 20);
		} catch (IllegalArgumentException ex) {
			assertEquals("Only the owner can share the project", ex.getMessage());
			Mockito.verify(projectMemberRepository, Mockito.never()).save(Mockito.any(ProjectMember.class));
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US18: an einai hdh member bgazei mhnuma lathous kai den kanei save
	@Test
	public void shareProject_alreadyMember_throwsErrorAndNoSave() {
		Project p = new Project("P", 10);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));
		Mockito.when(projectMemberRepository.existsByProjectIdAndUserId(7, 20)).thenReturn(true);

		try {
			projectService.shareProject(7, 10, 20);
		} catch (IllegalArgumentException ex) {
			assertEquals("The user is already a teamate", ex.getMessage());
			Mockito.verify(projectMemberRepository, Mockito.never()).save(Mockito.any(ProjectMember.class));
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US18: an den einai member kanei save ProjectMember
	@Test
	public void shareProject_savesMember() {
		Project p = new Project("P", 10);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));
		Mockito.when(projectMemberRepository.existsByProjectIdAndUserId(7, 20)).thenReturn(false);

		projectService.shareProject(7, 10, 20);

		ArgumentCaptor<ProjectMember> captor = ArgumentCaptor.forClass(ProjectMember.class);
		Mockito.verify(projectMemberRepository).save(captor.capture());

		ProjectMember saved = captor.getValue();
		assertEquals(7, saved.getProjectId());
		assertEquals(20, saved.getUserId());
	}

	// US18: mono o owner mporei na dei ta members
	@Test
	public void listMembers_onlyOwnerCanView() {
		Project p = new Project("P", 10);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));

		try {
			projectService.listMembers(7, 11);
		} catch (IllegalArgumentException ex) {
			assertEquals("Only the owner can view members", ex.getMessage());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// US18: listMembers epistrefei thn lista apo to repository
	@Test
	public void listMembers_returnsRepositoryList() {
		Project p = new Project("P", 10);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));

		List<ProjectMember> members = new ArrayList<ProjectMember>();
		members.add(new ProjectMember(7, 20));

		Mockito.when(projectMemberRepository.findByProjectId(7)).thenReturn(members);

		List<ProjectMember> out = projectService.listMembers(7, 10);

		assertEquals(members, out);
	}

	// leave: an owner pathsei na fygei apo diko tou project pairnei lathos
	@Test
	public void leaveProject_ownerCannotLeaveOwnProject() {
		Project p = new Project("P", 10);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));

		try {
			projectService.leaveProject(7, 10);
		} catch (IllegalArgumentException ex) {
			assertEquals("Owner cannot leave own project", ex.getMessage());
			Mockito.verify(projectMemberRepository, Mockito.never()).deleteByProjectIdAndUserId(Mockito.anyInt(), Mockito.anyInt());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// leave: an o xrhsths den einai member den mporei na kanei leave
	@Test
	public void leaveProject_notMember_throws() {
		Project p = new Project("P", 99);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));
		Mockito.when(projectMemberRepository.existsByProjectIdAndUserId(7, 20)).thenReturn(false);

		try {
			projectService.leaveProject(7, 20);
		} catch (IllegalArgumentException ex) {
			assertEquals("You are not a member of this project", ex.getMessage());
			Mockito.verify(projectMemberRepository, Mockito.never()).deleteByProjectIdAndUserId(Mockito.anyInt(), Mockito.anyInt());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// leave: an o xrhsths einai member ginetai delete to membership
	@Test
	public void leaveProject_memberLeaves_deletesMembership() {
		Project p = new Project("P", 99);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));
		Mockito.when(projectMemberRepository.existsByProjectIdAndUserId(7, 20)).thenReturn(true);

		projectService.leaveProject(7, 20);

		Mockito.verify(projectMemberRepository).deleteByProjectIdAndUserId(7, 20);
	}

	// remove: mono o owner mporei na kanei remove teammate
	@Test
	public void removeMember_onlyOwnerCanRemove() {
		Project p = new Project("P", 10);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));

		try {
			projectService.removeMember(7, 11, 20);
		} catch (IllegalArgumentException ex) {
			assertEquals("Only the owner can remove teammates", ex.getMessage());
			Mockito.verify(projectMemberRepository, Mockito.never()).deleteByProjectIdAndUserId(Mockito.anyInt(), Mockito.anyInt());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// remove: den mporei na ginei remove o idios o owner
	@Test
	public void removeMember_ownerCannotBeRemoved() {
		Project p = new Project("P", 10);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));

		try {
			projectService.removeMember(7, 10, 10);
		} catch (IllegalArgumentException ex) {
			assertEquals("Owner cannot be removed", ex.getMessage());
			Mockito.verify(projectMemberRepository, Mockito.never()).deleteByProjectIdAndUserId(Mockito.anyInt(), Mockito.anyInt());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// remove: an o teammate den einai member pairnei lathos
	@Test
	public void removeMember_teammateNotMember_throws() {
		Project p = new Project("P", 10);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));
		Mockito.when(projectMemberRepository.existsByProjectIdAndUserId(7, 20)).thenReturn(false);

		try {
			projectService.removeMember(7, 10, 20);
		} catch (IllegalArgumentException ex) {
			assertEquals("Teammate is not a member of this project", ex.getMessage());
			Mockito.verify(projectMemberRepository, Mockito.never()).deleteByProjectIdAndUserId(Mockito.anyInt(), Mockito.anyInt());
			return;
		}
		throw new AssertionError("Expected IllegalArgumentException");
	}

	// remove: an o owner kanei remove enan member, sbhnetai to membership
	@Test
	public void removeMember_success_deletesMembership() {
		Project p = new Project("P", 10);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));
		Mockito.when(projectMemberRepository.existsByProjectIdAndUserId(7, 20)).thenReturn(true);

		projectService.removeMember(7, 10, 20);

		Mockito.verify(projectMemberRepository).deleteByProjectIdAndUserId(7, 20);
	}

	// US19: owner exei prosbash sto project
	@Test
	public void canAccess_ownerTrue() {
		Project p = new Project("P", 10);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));

		assertTrue(projectService.canAccess(7, 10));
	}

	// US19: member exei prosbash sto project
	@Test
	public void canAccess_memberTrue() {
		Project p = new Project("P", 10);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));
		Mockito.when(projectMemberRepository.existsByProjectIdAndUserId(7, 20)).thenReturn(true);

		assertTrue(projectService.canAccess(7, 20));
	}

	// US19: allos user xoris na einai teammate den exei prosbash sto project
	@Test
	public void canAccess_otherFalse() {
		Project p = new Project("P", 10);
		Mockito.when(projectRepository.findById(7)).thenReturn(Optional.of(p));
		Mockito.when(projectMemberRepository.existsByProjectIdAndUserId(7, 30)).thenReturn(false);

		assertFalse(projectService.canAccess(7, 30));
	}

	// show share: fortwnei to project kai tous teammates ws users
	@Test
	public void showShare_returnsPageWithTeammates() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("me@a.com");

		User me = Mockito.mock(User.class);
		Mockito.when(me.getId()).thenReturn(10);
		Mockito.when(shareControllerUserService.getByEmail("me@a.com")).thenReturn(me);

		Project project = Mockito.mock(Project.class);
		Mockito.when(shareControllerProjectService.getById(7)).thenReturn(project);

		List<ProjectMember> members = new ArrayList<ProjectMember>();
		members.add(new ProjectMember(7, 20));
		Mockito.when(shareControllerProjectService.listMembers(7, 10)).thenReturn(members);

		User teammate = Mockito.mock(User.class);
		Mockito.when(shareControllerUserRepository.findById(20)).thenReturn(Optional.of(teammate));

		ExtendedModelMap model = new ExtendedModelMap();

		String view = projectShareController.showShare(7, model, auth);

		assertEquals("project_share", view);
		assertEquals(project, model.get("project"));
		assertTrue(model.get("form") instanceof ProjectShareForm);

		@SuppressWarnings("unchecked")
		List<User> teammates = (List<User>) model.get("teammates");
		assertEquals(1, teammates.size());
		assertEquals(teammate, teammates.get(0));
	}

	// add share: me swsto email ginetai redirect
	@Test
	public void addShare_success_redirects() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("me@a.com");

		User me = Mockito.mock(User.class);
		Mockito.when(me.getId()).thenReturn(10);
		Mockito.when(shareControllerUserService.getByEmail("me@a.com")).thenReturn(me);

		Project project = Mockito.mock(Project.class);
		Mockito.when(shareControllerProjectService.getById(7)).thenReturn(project);

		User teammate = Mockito.mock(User.class);
		Mockito.when(teammate.getId()).thenReturn(20);
		Mockito.when(shareControllerUserService.getByEmail("aaa@a.com")).thenReturn(teammate);

		ProjectShareForm form = new ProjectShareForm();
		form.setEmail("  aaa@a.com  ");

		ExtendedModelMap model = new ExtendedModelMap();

		String view = projectShareController.addShare(7, form, model, auth);

		assertEquals("redirect:/projects/7/share", view);
		Mockito.verify(shareControllerProjectService).shareProject(7, 10, 20);
	}

	// add share: an to email einai keno, meno sth share page kai blepo lathos
	@Test
	public void addShare_blankEmail_returnsPageWithError() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("me@a.com");

		User me = Mockito.mock(User.class);
		Mockito.when(me.getId()).thenReturn(10);
		Mockito.when(shareControllerUserService.getByEmail("me@a.com")).thenReturn(me);

		Project project = Mockito.mock(Project.class);
		Mockito.when(shareControllerProjectService.getById(7)).thenReturn(project);
		Mockito.when(shareControllerProjectService.listMembers(7, 10)).thenReturn(new ArrayList<ProjectMember>());

		ProjectShareForm form = new ProjectShareForm();
		form.setEmail("   ");

		ExtendedModelMap model = new ExtendedModelMap();

		String view = projectShareController.addShare(7, form, model, auth);

		assertEquals("project_share", view);
		assertEquals("Email is required", model.get("error"));
	}

	// add share: an o teammate einai hdh mesa, emfanizetai to antistoixo mhnuma
	@Test
	public void addShare_duplicateTeammate_returnsPageWithError() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("me@a.com");

		User me = Mockito.mock(User.class);
		Mockito.when(me.getId()).thenReturn(10);
		Mockito.when(shareControllerUserService.getByEmail("me@a.com")).thenReturn(me);

		Project project = Mockito.mock(Project.class);
		Mockito.when(shareControllerProjectService.getById(7)).thenReturn(project);
		Mockito.when(shareControllerProjectService.listMembers(7, 10)).thenReturn(new ArrayList<ProjectMember>());

		User teammate = Mockito.mock(User.class);
		Mockito.when(teammate.getId()).thenReturn(20);
		Mockito.when(shareControllerUserService.getByEmail("aaa@a.com")).thenReturn(teammate);

		Mockito.doThrow(new IllegalArgumentException("The user is already a teamate"))
				.when(shareControllerProjectService).shareProject(7, 10, 20);

		ProjectShareForm form = new ProjectShareForm();
		form.setEmail("aaa@a.com");

		ExtendedModelMap model = new ExtendedModelMap();

		String view = projectShareController.addShare(7, form, model, auth);

		assertEquals("project_share", view);
		assertEquals("The user is already a teamate", model.get("error"));
	}

	// remove share: otan o owner kanei remove teammate ginetai redirect
	@Test
	public void removeShare_success_redirects() {
		Authentication auth = Mockito.mock(Authentication.class);
		Mockito.when(auth.getName()).thenReturn("me@a.com");

		User me = Mockito.mock(User.class);
		Mockito.when(me.getId()).thenReturn(10);
		Mockito.when(shareControllerUserService.getByEmail("me@a.com")).thenReturn(me);

		Project project = Mockito.mock(Project.class);
		Mockito.when(shareControllerProjectService.getById(7)).thenReturn(project);

		ExtendedModelMap model = new ExtendedModelMap();

		String view = projectShareController.removeShare(7, 20, model, auth);

		assertEquals("redirect:/projects/7/share", view);
		Mockito.verify(shareControllerProjectService).removeMember(7, 10, 20);
	}
}