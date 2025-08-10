package com.spring.springbootapplication.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;



@Entity
@Table(name = "larning_data")
@EntityListeners(AuditingEntityListener.class)
public class LearningData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // 学習記録ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // ユーザー情報(外部キー)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // カテゴリー情報(外部キー)

    @NotBlank(message = "項目名は必ず入力してください")
    @Size(max = 50, message = "項目名は50文字以内で入力してください")
    @Column(nullable = false)
    private String name; // 項目名

    @Column(name = "study_month", nullable = false)
    private LocalDate studyMonth; // 学習実施月

    @Column(name = "study_time", nullable = false)
    private LocalDate studyTime; // 学習時間

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 登録日時

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, updatable = false)
    private LocalDateTime updatedAt; // 更新日時

    // --- getter・setter ---
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStudyTime() {
        return studyTime;
    }
    public void setStudyTime(LocalDate studyTime) {
        this.studyTime = studyTime;
    }

    public LocalDate getStudyMonth() {
        return studyMonth;
    }
    public void setStudyMonth(LocalDate studyMonth) {
        this.studyMonth = studyMonth;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

