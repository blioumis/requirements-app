package oi.lebentes.domain;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="users")
public class User implements UserDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable=false)
	private String name;
	
	@Column(nullable=false,unique=true)
	private String email;
	
	@Column(nullable=false)
	private String password;
	
	public User() {
		
	}
	
	public User(String name,String email,String password) {
		this.name=name;
		this.email=email;
		this.password=password;
	}
	
	public Integer getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setPassword(String password) {
		this.password=password;
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public void setEmail(String email) {
		this.email=email;
	}
	
	@Override
	public Collection<org.springframework.security.core.GrantedAuthority> getAuthorities(){
		return Collections.emptyList();
	}
	
	@Override
	public String getPassword() {
		return password;
	}
	
	@Override
	public String getUsername() {
		return email;
	}
	
	@Override 
	public boolean isAccountNonExpired() {
		return true;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
}
