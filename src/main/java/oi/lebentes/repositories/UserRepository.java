package oi.lebentes.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import oi.lebentes.domain.User;

public interface UserRepository extends JpaRepository<User,Integer>{
	Optional<User> findByEmail(String email);
}
