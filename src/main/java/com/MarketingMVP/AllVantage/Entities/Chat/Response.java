package com.MarketingMVP.AllVantage.Entities.Chat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Response {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private Message message;

    @Column(length = 10000)
    private String rawApiResponse;

    private Double usagePromptTokens;
    private Double usageCompletionTokens;
    private Double usageTotalTokens;

    private LocalDateTime receivedAt;
}
