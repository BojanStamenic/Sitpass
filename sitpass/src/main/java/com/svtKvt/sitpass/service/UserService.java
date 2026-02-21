package com.svtKvt.sitpass.service;

import com.svtKvt.sitpass.model.User;
import com.svtKvt.sitpass.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void deleteUserWithDependencies(Long id) {
        jdbcTemplate.update("DELETE FROM comment WHERE user_id = ?", id);
        jdbcTemplate.update(
                "DELETE c FROM comment c INNER JOIN review r ON c.review_id = r.id WHERE r.user_id = ?",
                id
        );
        jdbcTemplate.update(
                "DELETE ra FROM rate ra INNER JOIN review r ON ra.review_id = r.id WHERE r.user_id = ?",
                id
        );
        jdbcTemplate.update("DELETE FROM review WHERE user_id = ?", id);
        jdbcTemplate.update("DELETE FROM exercise WHERE user_id = ?", id);
        userRepository.deleteById(id);
    }
}

