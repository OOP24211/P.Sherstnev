package com.example.gowork.controller;

import com.example.gowork.model.Task;
import com.example.gowork.model.User;
import com.example.gowork.repository.TaskRepository;
import com.example.gowork.service.TaskService;
import com.example.gowork.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping
    public List<User> getUsers() {
        return userService.getAll();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }

    @PostMapping("/{userId}/tasks")
    public Task addTask(@PathVariable Long userId, @RequestBody Task task) {
        return taskService.createTaskForUser(userId, task);
    }

    @GetMapping("/{userId}/tasks")
    public List<Task> getUserTasks(@PathVariable Long userId) {
        return taskRepository.findByUserIdOrderByIdDesc(userId);
    }
}
