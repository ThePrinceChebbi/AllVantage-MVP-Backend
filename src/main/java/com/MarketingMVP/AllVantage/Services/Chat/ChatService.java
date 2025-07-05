package com.MarketingMVP.AllVantage.Services.Chat;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

public interface ChatService {

    ResponseEntity<Object> createChat(UserDetails userDetails, String chatName);

    ResponseEntity<Object> getChatById(UserDetails userDetails, Long chatId);

    ResponseEntity<Object> getAllChats(UserDetails userDetails);

    ResponseEntity<Object> deleteChat(UserDetails userDetails, Long chatId);

    ResponseEntity<Object> MessageChat(UserDetails userDetails, Long chatId, String messageContent);

}
