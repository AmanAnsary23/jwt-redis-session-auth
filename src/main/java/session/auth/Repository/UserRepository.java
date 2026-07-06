package session.auth.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import session.auth.Entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User , Long> {
    Optional<User> findByEmail(String email);
}
