package com.example.gowork.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    private TaskTag tag;

    private LocalDate deadline;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    // Удобный метод для шаблонов
    public boolean isCompleted() {
        return status == TaskStatus.DONE;
    }

    public void setCompleted(boolean completed) {
        if (completed) this.status = TaskStatus.DONE;
    }

    public enum TaskStatus {
        TODO, IN_PROGRESS, DONE;

        public String getLabel() {
            return switch (this) {
                case TODO -> "Не начато";
                case IN_PROGRESS -> "В процессе";
                case DONE -> "Готово";
            };
        }

        public String getCssClass() {
            return switch (this) {
                case TODO -> "status-todo";
                case IN_PROGRESS -> "status-progress";
                case DONE -> "status-done";
            };
        }
    }

    public enum TaskTag {
        WORK, STUDY, HOME, HEALTH, OTHER;

        public String getLabel() {
            return switch (this) {
                case WORK -> "Работа";
                case STUDY -> "Учёба";
                case HOME -> "Дом";
                case HEALTH -> "Здоровье";
                case OTHER -> "Другое";
            };
        }

        public String getCssClass() {
            return switch (this) {
                case WORK -> "tag-work";
                case STUDY -> "tag-study";
                case HOME -> "tag-home";
                case HEALTH -> "tag-health";
                case OTHER -> "tag-other";
            };
        }
    }
}
