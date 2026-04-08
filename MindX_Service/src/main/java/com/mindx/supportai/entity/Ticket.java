package com.mindx.supportai.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String query;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // ✅ MESSAGES
    @JsonManagedReference
    @OneToMany(
            mappedBy = "ticket",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER
    )
    private List<Message> messages;
}