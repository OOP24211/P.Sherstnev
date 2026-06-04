package com.example.gowork.controller;

import com.example.gowork.model.Task;
import com.example.gowork.model.Task.TaskStatus;
import com.example.gowork.model.Task.TaskTag;
import com.example.gowork.model.User;
import com.example.gowork.repository.TaskRepository;
import com.example.gowork.repository.UserRepository;
import com.example.gowork.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserService userService;

    // ─── Главная / Вход ───────────────────────────────────────────────────────

    @GetMapping("/")
    public String showLoginPage() {
        return "index";
    }

    @PostMapping("/login")
    public String login(@RequestParam Long userId, RedirectAttributes ra) {
        if (userRepository.existsById(userId)) {
            return "redirect:/dashboard/" + userId;
        }
        ra.addFlashAttribute("error", "Пользователь с таким ID не найден");
        return "redirect:/";
    }

    // ─── Регистрация ──────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String name,
                               @RequestParam String email,
                               RedirectAttributes ra) {
        if (userService.emailExists(email)) {
            ra.addFlashAttribute("error", "Этот email уже зарегистрирован");
            return "redirect:/register";
        }
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser = userRepository.save(newUser);
        return "redirect:/?registeredId=" + newUser.getId();
    }

    // ─── Дашборд ──────────────────────────────────────────────────────────────

    @GetMapping("/dashboard/{id:[0-9]+}")
    public String dashboard(@PathVariable Long id,
                            @RequestParam(required = false) String tag,
                            @RequestParam(required = false) String status,
                            Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return "redirect:/";

        List<Task> tasks = taskRepository.findByUserIdOrderByIdDesc(id);

        // Фильтрация по тегу
        if (tag != null && !tag.isBlank()) {
            try {
                TaskTag tagEnum = TaskTag.valueOf(tag);
                tasks = tasks.stream().filter(t -> tagEnum.equals(t.getTag())).toList();
            } catch (IllegalArgumentException ignored) {}
        }

        // Фильтрация по статусу
        if (status != null && !status.isBlank()) {
            try {
                TaskStatus statusEnum = TaskStatus.valueOf(status);
                tasks = tasks.stream().filter(t -> statusEnum.equals(t.getStatus())).toList();
            } catch (IllegalArgumentException ignored) {}
        }

        long totalTasks = taskRepository.findByUserIdOrderByIdDesc(id).size();
        long doneTasks = taskRepository.findByUserIdOrderByIdDesc(id).stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE).count();

        model.addAttribute("user", user);
        model.addAttribute("tasks", tasks);
        model.addAttribute("tags", TaskTag.values());
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("doneTasks", doneTasks);
        model.addAttribute("activeTag", tag);
        model.addAttribute("activeStatus", status);
        model.addAttribute("today", LocalDate.now());
        return "user_info";
    }

    // ─── Добавить задачу ──────────────────────────────────────────────────────

    @PostMapping("/dashboard/{id}/add-task")
    public String addTask(@PathVariable Long id,
                          @RequestParam String title,
                          @RequestParam(required = false) String description,
                          @RequestParam(required = false) String tag,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            Task newTask = new Task();
            newTask.setTitle(title);
            newTask.setDescription(description);
            newTask.setStatus(TaskStatus.TODO);
            newTask.setUser(user);
            if (tag != null && !tag.isBlank()) {
                try { newTask.setTag(TaskTag.valueOf(tag)); } catch (IllegalArgumentException ignored) {}
            }
            newTask.setDeadline(deadline);
            taskRepository.save(newTask);
        }
        return "redirect:/dashboard/" + id;
    }

    // ─── Изменить статус задачи ───────────────────────────────────────────────

    @PostMapping("/dashboard/{userId}/set-status/{taskId}")
    public String setStatus(@PathVariable Long userId,
                            @PathVariable Long taskId,
                            @RequestParam String status) {
        taskRepository.findById(taskId).ifPresent(task -> {
            try {
                task.setStatus(TaskStatus.valueOf(status));
                taskRepository.save(task);
            } catch (IllegalArgumentException ignored) {}
        });
        return "redirect:/dashboard/" + userId;
    }

    // ─── Удалить задачу ───────────────────────────────────────────────────────

    @PostMapping("/dashboard/{userId}/delete-task/{taskId}")
    public String deleteTask(@PathVariable Long userId, @PathVariable Long taskId) {
        taskRepository.deleteById(taskId);
        return "redirect:/dashboard/" + userId;
    }
}
