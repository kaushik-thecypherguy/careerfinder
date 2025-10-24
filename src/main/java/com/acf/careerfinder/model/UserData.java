package com.acf.careerfinder.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserData {

    @Id
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "username", nullable = false, length = 120)
    private String username;

    @Column(name = "userpassword", nullable = false, length = 120)
    private String userpassword;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "ui_lang", length = 5)
    private String uiLang; // "en", "hi", "mr" or null (not chosen yet)

    @Column(name = "login_id_shown_at")
    private LocalDateTime loginIdShownAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserData() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserpassword() { return userpassword; }
    public void setUserpassword(String userpassword) { this.userpassword = userpassword; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getUiLang() { return uiLang; }
    public void setUiLang(String uiLang) { this.uiLang = uiLang; }

    public LocalDateTime getLoginIdShownAt() { return loginIdShownAt; }
    public void setLoginIdShownAt(LocalDateTime loginIdShownAt) { this.loginIdShownAt = loginIdShownAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}