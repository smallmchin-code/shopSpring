package com.example.springtest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springtest.model.User;
import com.example.springtest.service.UserService;

import jakarta.servlet.http.HttpServletRequest; // 💡 新增依賴
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable int id, @RequestBody User updatedUser) {
        return userService.updateUser(id, updatedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user, HttpServletRequest request) {
        User authenticatedUser = userService.login(user.getUsername(), user.getPassword());
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("userId", authenticatedUser.getId());
        session.setAttribute("username", authenticatedUser.getUsername());

        // ⚠️ 返回前端時，請務必移除密碼
        authenticatedUser.setPassword(null);
        return ResponseEntity.ok(authenticatedUser);

    }

    // 2. 獲取當前用戶資訊：用來保持登入狀態
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // 不建立新 Session

        if (session != null && session.getAttribute("userId") != null) {
            int userId = (Integer) session.getAttribute("userId");
            User user = userService.getUserById(userId);

            if (user != null) {
                user.setPassword(null); // 避免密碼洩漏
                return ResponseEntity.ok(user);
            }
        }
        // Session 無效或不存在
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // 使 Session 失效，瀏覽器的 Cookie 也就無效了
        }
        return ResponseEntity.ok().build();
    }
}
