package com.example.emms.interfaces.controller;

import com.example.emms.domain.model.User;
import com.example.emms.domain.repository.UserRepository;
import com.example.emms.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "用户管理")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "获取所有用户")
    public ApiResponse<List<User>> getAllUsers() {
        return ApiResponse.success(userRepository.findAll());
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public ApiResponse<User> createUser(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ApiResponse.error("用户名已存在");
        }
        return ApiResponse.success(userRepository.save(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return ApiResponse.success(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public ApiResponse<String> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ApiResponse.success("删除成功");
    }
}
