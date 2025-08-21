package com.spring.springbootapplication.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Transient;
import jakarta.persistence.Basic;
import jakarta.persistence.FetchType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;


@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ユーザーID

    @NotBlank(message = "氏名は必ず入力してください")
    @Size(max = 255, message = "氏名は255文字以内で入力してください")
    @Column(nullable = false)
    private String name; // 氏名

    @NotBlank(message = "メールアドレスは必ず入力してください")
    @Email(message = "メールアドレスが正しい形式ではありません")
    @Column(nullable = false, unique = true, length = 255)
    private String email; // メールアドレス

    @NotBlank(message = "パスワードは必ず入力してください")
    @Column(nullable = false, length = 255)
    private String password; // パスワード

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 登録日時

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 更新日時

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction; // 自己紹介 // DB用（バリデーションなし）

    @Column(name = "avatar_path", length = 255)
    private String avatarPath; // プロフィール画像のファイルパス

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "avatar_data")
    private byte[] avatarData;

    @Column(name = "avatar_mime", length = 100)
    private String avatarMime;


    // --- getter・setter ---
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
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

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    // 互換：/uploads/ を先頭に付けて返す（旧ファイル保存方式用）
    @Transient
    public String getAvatarUrl() {
        if (this.avatarPath == null || this.avatarPath.isBlank()) return null;
        return this.avatarPath.startsWith("/uploads/")
                ? this.avatarPath
                : "/uploads/" + this.avatarPath;
    }

    // 互換：/uploads/ が付いていたら剥がしてDBにはファイル名のみ保存
    public void setAvatarPath(String avatarPath) {
        if (avatarPath != null && avatarPath.startsWith("/uploads/")) {
            this.avatarPath = avatarPath.substring("/uploads/".length());
        } else {
            this.avatarPath = avatarPath;
        }
    }

    // 追加（User.java のクラス末尾あたりに）
public byte[] getAvatarData() { return avatarData; }
public void setAvatarData(byte[] avatarData) { this.avatarData = avatarData; }

public String getAvatarMime() { return avatarMime; }
public void setAvatarMime(String avatarMime) { this.avatarMime = avatarMime; }

}


