package com.MarketingMVP.AllVantage.Entities.Chat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Message {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Chat chat;

    private String role; // user | assistant | system

    @Column(length = 4000)
    private String content;

    private LocalDateTime timestamp;

    public Message(Chat chat, String role, String content) {
        this.chat = chat;
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
}
