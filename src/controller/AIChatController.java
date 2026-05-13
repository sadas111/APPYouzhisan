package com.chenghua.controller;

import com.chenghua.dto.ChatRequest;
import com.chenghua.dto.ChatResponse;
import com.chenghua.entity.ChatMessage;
import com.chenghua.service.ChatMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
@Slf4j
public class AIChatController {

    @Value("${bigmodel.api.key:}")
    private String apiKeyFromConfig;
	
	// 首先我定义一个静态常量API_URL，存储大模型API的请求地址，此URL用于调用生成式AI的聊天补全接口
	// 我们所调用的大模型为国产智普AI大模型，智普 AI 基于 GLM  架构，是国产大模型中的佼佼者。
	// 相比于西方模型，它在中文语境理解、中国文化常识（特别是本项目涉及的非遗文化/油纸伞）方面表现更为出色。
    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatMessageService chatMessageService;

    // Cache token to avoid frequent generation
    private String cachedToken = null;
    private long tokenExpirationTime = 0;

    @GetMapping("/history")
    public List<ChatMessage> getHistory(@RequestParam(defaultValue = "1") Long userId) {
        return chatMessageService.getHistory(userId);
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            Long userId = request.getUserId() != null ? request.getUserId() : 1L;

            // Save user message
            chatMessageService.saveMessage(userId, "user", request.getMessage());

            String token = getApiToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
			
			//  构建API请求体参数Map
            Map<String, Object> body = new HashMap<>();
			// 设置模型标识参数
			// "model"字段指定要调用的生成式AI模型版本
			// "glm-4"表示使用智普AI第四代大语言模型（GLM-4），
			// GLM-4 模型在提供媲美 GPT-4 智能水平的同时，调用成本更低，非常适合需要频繁交互的应用场景。
            body.put("model", "glm-4");
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            // Add System Prompt
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你叫撑花AI，是一个专业的油纸伞非遗文化助手。请用亲切、专业的语气回答用户关于油纸伞制作、历史、文化等方面的问题。如果用户问你是谁，请回答你是撑花AI。");
            messages.add(systemMessage);

            // Add history context (optional, but good for conversation continuity)
            // For now, let's just send the current message to save tokens, or maybe last few messages
            // But user just asked to SAVE history, not necessarily context window. 
            // Zhipu needs context to "remember", but let's stick to basic requirement first: save/load history.
            // If we want context, we should append previous messages here.
            // Let's add last 5 messages for context
            List<ChatMessage> history = chatMessageService.getHistory(userId);
            // Limit to last 10 messages (excluding the one we just saved? No, include it but handle carefully)
            // The one we just saved IS in history now.
            
            int start = Math.max(0, history.size() - 6); // Last 5 + current one
            for (int i = start; i < history.size(); i++) {
                ChatMessage histMsg = history.get(i);
                Map<String, String> msgMap = new HashMap<>();
                msgMap.put("role", "ai".equals(histMsg.getRole()) ? "assistant" : "user");
                msgMap.put("content", histMsg.getContent());
                messages.add(msgMap);
            }

            // If we just saved the user message, it's already in history loop above.
            // So we don't need to add it explicitly again unless we skipped it.
            // Wait, if history fetch includes the just-saved message, we are good.
            
            body.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            // Call Zhipu API
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    
                    // Save AI response
                    chatMessageService.saveMessage(userId, "ai", content);
                    
                    return ResponseEntity.ok(new ChatResponse(content));
                }
            }
            
            return ResponseEntity.status(500).body(new ChatResponse("AI响应异常"));
            
        } catch (Exception e) {
            log.error("AI Chat Error", e);
            return ResponseEntity.status(500).body(new ChatResponse("AI服务暂时不可用: " + e.getMessage()));
        }
    }

    private synchronized String getApiToken() {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpirationTime) {
            return cachedToken;
        }

        try {
            String apiKey = (apiKeyFromConfig != null && !apiKeyFromConfig.isBlank())
                    ? apiKeyFromConfig
                    : System.getenv("BIGMODEL_API_KEY");
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("Missing BigModel API Key. Set spring property 'bigmodel.api.key' or env 'BIGMODEL_API_KEY'.");
            }
            String[] parts = apiKey.split("\\.");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid API Key format");
            }
            String id = parts[0];
            String secret = parts[1];

            long now = System.currentTimeMillis();
            long exp = now + 3600 * 1000; // 1 hour

            // Header
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("sign_type", "SIGN");
            String headerEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(header));

            // Payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("api_key", id);
            payload.put("exp", exp);
            payload.put("timestamp", now);
            String payloadEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(payload));

            // Signature
            String dataToSign = headerEncoded + "." + payloadEncoded;
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] signatureBytes = sha256_HMAC.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));
            String signatureEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);

            cachedToken = headerEncoded + "." + payloadEncoded + "." + signatureEncoded;
            tokenExpirationTime = exp - 60000; // Refresh 1 minute before expiry
            
            return cachedToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }
}
