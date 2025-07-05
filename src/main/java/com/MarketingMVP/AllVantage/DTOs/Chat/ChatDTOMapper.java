package com.MarketingMVP.AllVantage.DTOs.Chat;

import com.MarketingMVP.AllVantage.Entities.Chat.Chat;

import java.util.function.Function;

public class ChatDTOMapper implements Function<Chat, ChatDTO> {

    @Override
    public ChatDTO apply(Chat chat) {
        return new ChatDTO(
                chat.getId(),
                chat.getTitle(),
                chat.getUser().getUsername(),
                chat.getCreatedAt().toString(),
                chat.getMessages()
        );
    }
}
