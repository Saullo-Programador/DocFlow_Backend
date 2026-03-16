package com.example.DocFlowBackend.service;

import com.example.DocFlowBackend.entity.User;
import com.example.DocFlowBackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String name, String email, String password){
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        return userRepository.save(user);
    }
    public List<User> listUsers(){
        return userRepository.findAll();
    }
}
