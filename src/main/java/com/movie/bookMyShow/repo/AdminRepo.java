package com.movie.bookMyShow.repo;
import com.movie.bookMyShow.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminRepo extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
}
