package com.svtKvt.sitpass.controller;


import com.svtKvt.sitpass.model.User;
import com.svtKvt.sitpass.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.findByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            User updatedUser = user.get();
            // Ažuriraj polja prema userDetails
            updatedUser.setName(userDetails.getName());
            updatedUser.setSurname(userDetails.getSurname());
            updatedUser.setEmail(userDetails.getEmail());
            updatedUser.setPhoneNumber(userDetails.getPhoneNumber());
            updatedUser.setAddress(userDetails.getAddress());
            updatedUser.setCity(userDetails.getCity());
            updatedUser.setZipCode(userDetails.getZipCode());
            updatedUser.setBirthday(userDetails.getBirthday());
            // Dodatna ažuriranja prema potrebi

            return ResponseEntity.ok(userService.saveUser(updatedUser));
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/{id}/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@PathVariable Long id, @RequestBody Map<String, String> passwordDetails) {
        Optional<User> userOptional = userService.getUserById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String oldPassword = passwordDetails.get("oldPassword");
            String newPassword = passwordDetails.get("newPassword");

            // Proverite staru lozinku i postavite novu lozinku
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Incorrect old password"));
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userService.saveUser(user);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
