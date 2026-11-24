package oi.lebentes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth
	        .requestMatchers("/", "/register", "/register/**").permitAll()
	        .requestMatchers("/login", "/error").permitAll()
	        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
	        .anyRequest().authenticated()
	    );

	    http.formLogin(form -> form
	        .loginPage("/login")
	        .usernameParameter("email")
	        .failureUrl("/login?error")
	        .permitAll()
	    );

	    http.logout(logout -> logout
	        .logoutSuccessUrl("/login?logout")
	        .permitAll()
	    );
	    
	    return http.build();
	}
}

