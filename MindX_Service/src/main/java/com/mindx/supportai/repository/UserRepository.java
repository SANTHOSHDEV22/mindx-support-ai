package com.mindx.supportai.repository;

import com.mindx.supportai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email); // 🔥 important for login
}