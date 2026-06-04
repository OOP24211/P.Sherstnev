package com.example.gowork.service;

import com.example.gowork.model.Task;
import com.example.gowork.model.User;
import com.example.gowork.repository.TaskRepository;
import com.example.gowork.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository; // Исправлено: используем репозиторий, не getAll()

    public Task createTaskForUser(Long userId, Task task) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        task.setUser(user);
        return taskRepository.save(task);
    }
}
