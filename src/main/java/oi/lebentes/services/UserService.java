package oi.lebentes.services;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import oi.lebentes.domain.Project;
import oi.lebentes.domain.User;
import oi.lebentes.repositories.ProjectMemberRepository;
import oi.lebentes.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private ProjectService projectService;
	private ProjectMemberRepository projectMemberRepository;
	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			ProjectService projectService, ProjectMemberRepository projectMemberRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.projectService = projectService;
		this.projectMemberRepository = projectMemberRepository;
	}
	
	public void register(String name, String email, String rawPassword) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Name is required");
		}
		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("Email is required");
		}
		if (rawPassword == null || rawPassword.trim().isEmpty()) {
			throw new IllegalArgumentException("Password is required");
		}
		if (userRepository.findByEmail(email.trim()).isPresent()) {
			throw new IllegalArgumentException("Email already exists");
		}
		String encodedPassword = passwordEncoder.encode(rawPassword);
		userRepository.save(new User(name.trim(), email.trim(), encodedPassword));
	}
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<User> opt = userRepository.findByEmail(email.trim());
		if (opt.isEmpty()) {
			throw new UsernameNotFoundException("User not found");
		}
		return opt.get();
	}
	
	public User getByEmail(String email) {
		Optional<User> opt = userRepository.findByEmail(email.trim());
		if (opt.isEmpty()) {
			throw new IllegalArgumentException("User not found");
		}
		return opt.get();
	}
	
	public boolean updateProfile(String currentEmail, String newName, String newEmail, String newPassword) {
		User user = getByEmail(currentEmail);
		
		if (newName != null && !newName.trim().isEmpty()) {
			user.setName(newName.trim());
		}
		
		boolean emailChanged = false;
		if (newEmail != null && !newEmail.trim().isEmpty()) {
			String emailTrim = newEmail.trim();
			if (!emailTrim.equals(currentEmail)) {
				if (userRepository.findByEmail(emailTrim).isPresent()) {
					throw new IllegalArgumentException("Email already exists");
				}
				user.setEmail(emailTrim);
				emailChanged = true;
			}
		}
		
		if (newPassword != null && !newPassword.trim().isEmpty()) {
			String encoded = passwordEncoder.encode(newPassword);
			user.setPassword(encoded);
		}
		
		userRepository.save(user);
		return emailChanged;
	}

	@Transactional
	public void deleteAccount(String currentEmail) {
		User user = getByEmail(currentEmail);

		List<Project> ownedProjects = projectService.findOwnedForUser(user.getId());
		for (int i = 0; i < ownedProjects.size(); i++) {
			Project p = ownedProjects.get(i);
			projectService.deleteProjectForUser(p.getId(), user.getId());
		}

		projectMemberRepository.deleteByUserId(user.getId());
		userRepository.delete(user);
	}
}