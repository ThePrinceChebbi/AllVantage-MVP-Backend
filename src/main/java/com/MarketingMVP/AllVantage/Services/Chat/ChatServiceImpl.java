package com.MarketingMVP.AllVantage.Services.Chat;

import com.MarketingMVP.AllVantage.DTOs.Chat.ChatSummaryDTO;
import com.MarketingMVP.AllVantage.Entities.Chat.Chat;
import com.MarketingMVP.AllVantage.Entities.Chat.Message;
import com.MarketingMVP.AllVantage.Entities.Chat.Response;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Chat.ChatRepository;
import com.MarketingMVP.AllVantage.Repositories.Chat.ResponseRepository;
import com.MarketingMVP.AllVantage.Services.FileData.FileServiceImpl;
import com.MarketingMVP.AllVantage.Services.UserEntity.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.key}")
    private String apiKey;

    private final HttpClient httpClient;
    private final UserService userService;
    private final ChatRepository chatRepository;
    private final ResponseRepository responseRepository;

    public ChatServiceImpl(HttpClient httpClient, UserService userService, ChatRepository chatRepository, ResponseRepository responseRepository) {
        this.httpClient = httpClient;
        this.userService = userService;
        this.chatRepository = chatRepository;
        this.responseRepository = responseRepository;
    }

    @Override
    public ResponseEntity<Object> createChat(UserDetails userDetails, String chatName) {
        try {
            UserEntity user = userService.getUserByUsername(userDetails.getUsername());
            Chat chat = new Chat(chatName, user, new ArrayList<>());
            return ResponseEntity.ok(chatRepository.save(chat));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating chat: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> getChatById(UserDetails userDetails, Long chatId) {
        try{
            UserEntity user = userService.getUserByUsername(userDetails.getUsername());
            Chat chat = chatRepository.findByIdAndUser(chatId, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chat not found with id: " + chatId));
            return ResponseEntity.ok(chat);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving chat: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> getAllChats(UserDetails userDetails) {
        try {
            UserEntity user = userService.getUserByUsername(userDetails.getUsername());
            List<ChatSummaryDTO> chats = chatRepository.findAllByUser(user.getId()).stream()
                    .map(chat -> new ChatSummaryDTO(chat.getId(), chat.getTitle()))
                    .toList();
            return ResponseEntity.ok(chats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving chats: " + e.getMessage());
        }
    }


    @Override
    public ResponseEntity<Object> deleteChat(UserDetails userDetails, Long chatId) {
        try{
            UserEntity user = userService.getUserByUsername(userDetails.getUsername());
            Chat chat = chatRepository.findByIdAndUser(chatId, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chat not found with id: " + chatId));
            chatRepository.delete(chat);
            return ResponseEntity.ok("Chat deleted successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting chat: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> MessageChat(UserDetails userDetails, Long chatId, String messageContent) {
        try {
            UserEntity user = userService.getUserByUsername(userDetails.getUsername());
            Chat chat = chatRepository.findByIdAndUser(chatId, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chat not found with id: " + chatId));

            // 1. Save user message
            Message userMessage = new Message(chat, "user", messageContent);
            chat.getMessages().add(userMessage);
            chatRepository.save(chat);

            // 2. Build OpenAI request payload
            List<Map<String, String>> messages = chat.getMessages().stream()
                    .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                    .toList();

            HttpPost request = new HttpPost(apiUrl);
            request.setHeader("Authorization", "Bearer " + apiKey);
            request.setHeader("Content-Type", "application/json");

            Map<String, Object> payload = Map.of(
                    "model", model,
                    "messages", messages
            );

            StringEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(payload), ContentType.APPLICATION_JSON);
            request.setEntity(entity);

            // 3. Execute HTTP request
            try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(request)) {
                String body = EntityUtils.toString(response.getEntity());

                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(body);

                if (json.has("error")) {
                    String errMsg = json.get("error").get("message").asText();
                    return ResponseEntity.status(502).body("OpenAI API Error: " + errMsg);
                }

                if (!json.has("choices") || !json.get("choices").isArray()) {
                    return ResponseEntity.status(502).body("Malformed OpenAI response: " + body);
                }

                String assistantReply = json.get("choices").get(0).get("message").get("content").asText();

                // 4. Save assistant message
                Message assistantMessage = new Message(chat, "assistant", assistantReply);
                chat.getMessages().add(assistantMessage);
                chatRepository.save(chat); // Save both messages

                // 5. Save Response metadata
                JsonNode usage = json.get("usage");
                Response messageResponse = new Response();
                messageResponse.setMessage(assistantMessage);
                messageResponse.setRawApiResponse(body);
                messageResponse.setReceivedAt(LocalDateTime.now());

                if (usage != null) {
                    messageResponse.setUsagePromptTokens(usage.get("prompt_tokens").asDouble());
                    messageResponse.setUsageCompletionTokens(usage.get("completion_tokens").asDouble());
                    messageResponse.setUsageTotalTokens(usage.get("total_tokens").asDouble());
                }

                responseRepository.save(messageResponse);

                // 6. Return assistant's message content
                return ResponseEntity.ok(assistantReply);
            }

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending message: " + e.getMessage());
        }
    }


}
