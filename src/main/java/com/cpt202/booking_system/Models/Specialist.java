package com.cpt202.booking_system.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "specialists")
public class Specialist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "email")
    private String email;

    @Column(nullable = false, name = "password")
    private String password;

    @Column(name = "role")
    private String role = "Specialist";

    @Column(name = "status")
    private String status; // Pending Approval, Active, etc.

    @Column(name = "expertise")
    private String expertise;

    @Column(name = "cert_link")
    private String certLink;

    @Column(name = "level")
    private String level;

    @Column(name = "fee")
    private Double fee;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "bio")
    private String bio;

    @Column(name = "apply_status")
    private String applyStatus = "None";

    @Column(name = "apply_reason")
    private String applyReason;

    // Getter & Setter (篇幅限制，请自行生成)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getExpertise() { return expertise; }
    public void setExpertise(String expertise) { this.expertise = expertise; }
    public String getCertLink() { return certLink; }
    public void setCertLink(String certLink) { this.certLink = certLink; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Double getFee() { return fee; }
    public void setFee(Double fee) { this.fee = fee; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getApplyStatus() { return applyStatus; }
    public void setApplyStatus(String applyStatus) { this.applyStatus = applyStatus; }
    public String getApplyReason() { return applyReason; }
    public void setApplyReason(String applyReason) { this.applyReason = applyReason; }
}