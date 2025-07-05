package com.MarketingMVP.AllVantage.Controllers.Chat;

import com.MarketingMVP.AllVantage.Services.Chat.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createChat(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestParam String chatName) {
        return chatService.createChat(userDetails, chatName);
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<Object> getChatById(@AuthenticationPrincipal UserDetails userDetails,
                                              @PathVariable Long chatId) {
        return chatService.getChatById(userDetails, chatId);
    }

    @GetMapping("/")
    public ResponseEntity<Object> getAllChats(@AuthenticationPrincipal UserDetails userDetails) {
        return chatService.getAllChats(userDetails);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Object> deleteChat(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Long chatId) {
        return chatService.deleteChat(userDetails, chatId);
    }

    @PostMapping("/{chatId}/message")
    public ResponseEntity<Object> sendMessage(@AuthenticationPrincipal UserDetails userDetails,
                                              @PathVariable Long chatId,
                                              @RequestParam String messageContent) {
        return chatService.MessageChat(userDetails, chatId, messageContent);
    }

}
