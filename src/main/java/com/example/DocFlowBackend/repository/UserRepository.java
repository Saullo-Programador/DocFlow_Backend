package com.example.DocFlowBackend.repository;

import com.example.DocFlowBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
