package edu.nu.owaspapivulnlab.repo;

import edu.nu.owaspapivulnlab.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    // ✅ Add this back so UserController.search() compiles
    @Query("select u from AppUser u where u.username like %?1% or u.email like %?1%")
    List<AppUser> search(String q);
}
