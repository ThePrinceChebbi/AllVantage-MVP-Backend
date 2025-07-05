package com.MarketingMVP.AllVantage.Entities.Chat;

import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Chat {
    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @ManyToOne
    private UserEntity user;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();


    private LocalDateTime createdAt;

    public Chat(String title, UserEntity user, List<Message> messages) {
        this.title = title;
        this.user = user;
        this.messages = messages;
        this.createdAt = LocalDateTime.now();
    }
}
