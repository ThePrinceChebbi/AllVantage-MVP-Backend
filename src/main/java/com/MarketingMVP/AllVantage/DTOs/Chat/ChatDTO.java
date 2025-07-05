package com.MarketingMVP.AllVantage.DTOs.Chat;

import com.MarketingMVP.AllVantage.Entities.Chat.Message;

import java.util.List;

public record ChatDTO(
        Long id,

        String title,

        String employeeUsername,

        String createdAt,

        List<Message> messages
) {
}
