package com.example.gowork.repository;

import com.example.gowork.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdOrderByIdDesc(Long userId);
}
