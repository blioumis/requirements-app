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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.ui.ExtendedModelMap;

import oi.lebentes.config.SecurityConfig;
import oi.lebentes.controllers.AuthController;
import oi.lebentes.controllers.RegisterForm;
import oi.lebentes.domain.Project;
import oi.lebentes.domain.User;
import oi.lebentes.repositories.ProjectMemberRepository;
import oi.lebentes.repositories.UserRepository;
import oi.lebentes.services.ProjectService;
import oi.lebentes.services.UserService;

class registerLoginProfileLogoutTest {

	private UserService authUserService;
	private UserService userService;
	private AuthController authController;
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private ProjectService projectService;
	private ProjectMemberRepository projectMemberRepository;
	

	@BeforeEach
	public void setup() {
		authUserService = Mockito.mock(UserService.class);
		authController = new AuthController(authUserService);
		userRepository = Mockito.mock(UserRepository.class);
		passwordEncoder = Mockito.mock(PasswordEncoder.class);
		projectService = Mockito.mock(ProjectService.class);
		projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
		userService = new UserService(userRepository, passwordEncoder, projectService, projectMemberRepository);
	}

	// Elegxei an anoigei h sosth selida gia na kano register kai epistrefei kena plaisia gia na balo ta stoixeia mou
	@Test
	public void showRegister_returnsViewAndForm() {
		ExtendedModelMap model = new ExtendedModelMap();

		String view = authController.showRegister(model);

		assertEquals("register", view);
		assertTrue(model.get("form") instanceof RegisterForm);
	}

	// Elegxei gia otan patao na to koumpi sign up an na stelnontai oi plhrofories mou 
	@Test
	public void doRegister_success_redirectsToLogin() {
		RegisterForm form = new RegisterForm();
		form.setName("John");
		form.setEmail("a@b.com");
		form.setPassword("pass");

		ExtendedModelMap model = new ExtendedModelMap();

		String view = authController.doRegister(form, model);

		assertEquals("redirect:/login", view);
		Mockito.verify(authUserService).register("John", "a@b.com", "pass");
	}

	// Elegxei gia to an o xrhsths ebale lathos mail na menei sthn selida kai na erxetai mhnuma lathous
	@Test
	public void doRegister_error_returnsRegisterWithError() {
		RegisterForm form = new RegisterForm();
		form.setName("John");
		form.setEmail("a@b.com");
		form.setPassword("pass");

		Mockito.doThrow(new IllegalArgumentException("Email already exists"))
				.when(authUserService).register("John", "a@b.com", "pass");

		ExtendedModelMap model = new ExtendedModelMap();

		String view = authController.doRegister(form, model);

		assertEquals("register", view);
		assertEquals("Email already exists", model.get("error"));
	}

	// Elegxei gia otan o xrhsths anoigei thn sosti selida login otan patame ton sundesmo.
	@Test
	public void showLogin_returnsView() {
		String view = authController.showLogin();
		assertEquals("login", view);
	}
	
	// Elegxos gia to register lathos an leipei onoma
		@Test
		public void register_nameRequired() {
			try {
				userService.register("   ", "a@b.com", "pass");
			} catch (IllegalArgumentException ex) {
				assertEquals("Name is required", ex.getMessage());
				return;
			}
			throw new AssertionError("Expected IllegalArgumentException");
		}

		// Elegxos gia to register lathos an leipei email
		@Test
		public void register_emailRequired() {
			try {
				userService.register("John", "   ", "pass");
			} catch (IllegalArgumentException ex) {
				assertEquals("Email is required", ex.getMessage());
				return;
			}
			throw new AssertionError("Expected IllegalArgumentException");
		}

		// Elegxos gia to register lathos an leipei password
		@Test
		public void register_passwordRequired() {
			try {
				userService.register("John", "a@b.com", "   ");
			} catch (IllegalArgumentException ex) {
				assertEquals("Password is required", ex.getMessage());
				return;
			}
			throw new AssertionError("Expected IllegalArgumentException");
		}

		// Elegxos gia to register lathos an uparxei hdh to email 
		@Test
		public void register_emailAlreadyExists() {
			User existing = new User("X", "a@b.com", "p");
			Mockito.when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(existing));

			try {
				userService.register("John", "a@b.com", "pass");
			} catch (IllegalArgumentException ex) {
				assertEquals("Email already exists", ex.getMessage());
				Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
				return;
			}
			throw new AssertionError("Expected IllegalArgumentException");
		}

		// Elegxos gia to register ginetai trim kai apothikeuei kodiko encoded
		@Test
		public void register_success_savesTrimmedAndEncoded() {
			Mockito.when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
			Mockito.when(passwordEncoder.encode("pass")).thenReturn("ENC");

			userService.register("  John  ", "  a@b.com  ", "pass");

			Mockito.verify(passwordEncoder).encode("pass");

			ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
			Mockito.verify(userRepository).save(captor.capture());

			User saved = captor.getValue();
			assertEquals("John", saved.getName());
			assertEquals("a@b.com", saved.getEmail());
			assertEquals("ENC", saved.getPassword());
		}

		// Elegxos gia to login lathos an den brethei user me to email
		@Test
		public void loadUserByUsername_userNotFound() {
			Mockito.when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());

			try {
				userService.loadUserByUsername("a@b.com");
			} catch (UsernameNotFoundException ex) {
				assertEquals("User not found", ex.getMessage());
				return;
			}
			throw new AssertionError("Expected UsernameNotFoundException");
		}

		// Elegxei otan uparxei user, epistrefetai o idios ws UserDetails
		@Test
		public void loadUserByUsername_returnsUser() {
			User u = new User("John", "a@b.com", "ENC");
			Mockito.when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(u));

			Object details = userService.loadUserByUsername("a@b.com");

			assertEquals(u, details);
		}

		// Elegxei gia to updateProfile na allazoun name, email, password kai epistrefei true otan allaksei email
		@Test
		public void updateProfile_updatesNameEmailPassword_andReturnsTrueWhenEmailChanged() {
			User u = new User("Old", "old@e.com", "OLD");
			Mockito.when(userRepository.findByEmail("old@e.com")).thenReturn(Optional.of(u));
			Mockito.when(userRepository.findByEmail("new@e.com")).thenReturn(Optional.empty());
			Mockito.when(passwordEncoder.encode("newpass")).thenReturn("NEWENC");

			boolean changed = userService.updateProfile("old@e.com", "  New Name  ", "  new@e.com  ", "newpass");

			assertTrue(changed);

			ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
			Mockito.verify(userRepository).save(captor.capture());

			User saved = captor.getValue();
			assertEquals("New Name", saved.getName());
			assertEquals("new@e.com", saved.getEmail());
			assertEquals("NEWENC", saved.getPassword());
		}

		// Elegxei an to email den allaksei na mhn se kanei logout apo to susthma
		@Test
		public void updateProfile_sameEmail_returnsFalse() {
			User u = new User("Old", "me@a.com", "OLD");
			Mockito.when(userRepository.findByEmail("me@a.com")).thenReturn(Optional.of(u));

			boolean changed = userService.updateProfile("me@a.com", "New", "  me@a.com  ", "");

			assertFalse(changed);
			Mockito.verify(userRepository).save(Mockito.any(User.class));
		}

		// Elegxei gia to email na min einai idio me email allou xrhsth
		@Test
		public void updateProfile_newEmailAlreadyExists() {
			User u = new User("Old", "old@e.com", "OLD");
			User other = new User("Other", "new@e.com", "X");

			Mockito.when(userRepository.findByEmail("old@e.com")).thenReturn(Optional.of(u));
			Mockito.when(userRepository.findByEmail("new@e.com")).thenReturn(Optional.of(other));

			try {
				userService.updateProfile("old@e.com", null, "new@e.com", null);
			} catch (IllegalArgumentException ex) {
				assertEquals("Email already exists", ex.getMessage());
				Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
				return;
			}
			throw new AssertionError("Expected IllegalArgumentException");
		}

		// delete account: sbhnei ta owned projects, ta memberships kai ton idio ton xrhsth
		@Test
		public void deleteAccount_deletesProjectsMembershipsAndUser() {
			User u = Mockito.mock(User.class);
			Mockito.when(u.getId()).thenReturn(10);

			Project p1 = Mockito.mock(Project.class);
			Mockito.when(p1.getId()).thenReturn(1);

			Project p2 = Mockito.mock(Project.class);
			Mockito.when(p2.getId()).thenReturn(2);

			List<Project> owned = new ArrayList<Project>();
			owned.add(p1);
			owned.add(p2);

			Mockito.when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(u));
			Mockito.when(projectService.findOwnedForUser(10)).thenReturn(owned);

			userService.deleteAccount("a@b.com");

			Mockito.verify(projectService).deleteProjectForUser(1, 10);
			Mockito.verify(projectService).deleteProjectForUser(2, 10);
			Mockito.verify(projectMemberRepository).deleteByUserId(10);
			Mockito.verify(userRepository).delete(u);
		}
		
		@Test
		public void logoutSuccessUrl_isLoginLogout() throws Exception {
			SecurityConfig config = new SecurityConfig();

			HttpSecurity http = Mockito.mock(HttpSecurity.class);

			Mockito.when(http.authorizeHttpRequests(Mockito.any(Customizer.class))).thenReturn(http);
			Mockito.when(http.formLogin(Mockito.any(Customizer.class))).thenReturn(http);

			ArgumentCaptor<Customizer> logoutCaptor = ArgumentCaptor.forClass(Customizer.class);
			Mockito.when(http.logout(logoutCaptor.capture())).thenReturn(http);

			Mockito.when(http.build()).thenReturn(Mockito.mock(DefaultSecurityFilterChain.class));

			config.filterChain(http);

			Customizer logoutCustomizer = logoutCaptor.getValue();

			LogoutConfigurer logoutConfigurer = Mockito.mock(LogoutConfigurer.class);
			Mockito.when(logoutConfigurer.logoutSuccessUrl("/login?logout")).thenReturn(logoutConfigurer);
			Mockito.when(logoutConfigurer.permitAll()).thenReturn(logoutConfigurer);

			logoutCustomizer.customize(logoutConfigurer);

			Mockito.verify(logoutConfigurer).logoutSuccessUrl("/login?logout");
			Mockito.verify(logoutConfigurer).permitAll();
		}

}
