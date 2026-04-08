package com.mindx.supportai.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 RELATION WITH TICKET
    @ManyToOne
    @JoinColumn(name = "ticket_id")
    @JsonBackReference
    private Ticket ticket;

    // SENDER (USER / AI)
    @Enumerated(EnumType.STRING)
    private Sender sender;

    // MESSAGE TEXT
    @Column(columnDefinition = "TEXT")
    private String message;

    // TIME
    private LocalDateTime timestamp;
}