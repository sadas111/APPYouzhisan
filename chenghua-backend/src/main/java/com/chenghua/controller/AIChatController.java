package com.chenghua.controller;

import com.chenghua.dto.ChatRequest;
import com.chenghua.dto.ChatResponse;
import com.chenghua.entity.ChatMessage;
import com.chenghua.security.AuthenticatedUserService;
import com.chenghua.service.ChatMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

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

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    // Cache token to avoid frequent generation
    private String cachedToken = null;
    private long tokenExpirationTime = 0;

    @GetMapping("/history")
    public List<ChatMessage> getHistory(@RequestParam(required = false) Long userId) {
        Long currentUserId = authenticatedUserService.requireCurrentUserId();
        if (userId != null && !authenticatedUserService.isAdmin() && !userId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        Long finalUserId = (userId != null && authenticatedUserService.isAdmin()) ? userId : currentUserId;
        return chatMessageService.getHistory(finalUserId);
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            Long currentUserId = authenticatedUserService.requireCurrentUserId();
            if (request.getUserId() != null && !authenticatedUserService.isAdmin() && !request.getUserId().equals(currentUserId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
            }
            Long userId = (request.getUserId() != null && authenticatedUserService.isAdmin()) ? request.getUserId() : currentUserId;

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
            
            // Add System Prompt with oil-paper umbrella knowledge (油纸伞历史、制作流程、制作步骤)
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", buildOilPaperUmbrellaSystemPrompt());
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

    /**
     * 构建油纸伞专项知识系统提示词，使 AI 能准确回答：油纸伞历史、制作流程、制作步骤等。
     */
    private String buildOilPaperUmbrellaSystemPrompt() {
        return "你是撑花AI，专业的油纸伞非遗文化助手。请用亲切、专业的语气回答用户关于油纸伞的问题。若用户问你是谁，请回答你是撑花AI。\n\n" +
                "【油纸伞历史】\n" +
                "油纸伞历史可追溯至东汉时期，相传由蔡伦弟子左伯改良造纸术后，结合传统制伞工艺创制。早期以竹为骨、以纸为面，涂桐油防水，轻便耐用，成为民间出行必备。唐宋时期工艺成熟，融入礼仪文化：婚嫁用红色油纸伞遮轿，寓意圆满喜庆、辟邪祈福；文人雅士在伞面绘山水、题墨宝。明清形成地域流派：四川泸州、浙江余杭、福建连城等地各具特色。泸州油纸伞因工艺最复杂、寿命最长被誉为“中国油纸伞之乡”，其制作技艺列入国家级非物质文化遗产名录。\n\n" +
                "【油纸伞制作流程与步骤】\n" +
                "传统油纸伞需经八十余道纯手工工序，主要流程与步骤包括：\n" +
                "1. 选竹（号竹）：选用三年以上、阳坡生长的楠竹（如泸州纳溪一带），韧性与弹力好。\n" +
                "2. 制骨：将竹锯成约50厘米，刮青、画墨、削伞骨，钻孔、拼架、穿线，串联伞柄伞头制成骨架。\n" +
                "3. 裱纸：在伞骨上刷白胶，将手工皮纸（楮树皮等）浮贴于伞架，修边、定型、曝晒。\n" +
                "4. 绘花/题字：在伞面绘制花鸟、山水、吉祥纹样或题字，使油纸伞成为可移动的民间艺术品。\n" +
                "5. 上油：在伞面刷熟桐油，晾干。晴天晾干后伞面油亮，阴天呈哑光。\n" +
                "6. 装柄与收尾：装伞柄、缠柄、穿内线等。成品可抗约5级大风，使用多年不破损、不漏水。\n" +
                "民间有“工序七十二道半，搬进搬出不肖算”之说，可见工序繁复。\n\n" +
                "重要：当用户问「制作步骤」或「步骤」时，只回复简洁的步骤列表，每步一行、一句话概括，不要展开解释或重复上述长段落。其他问题可正常展开回答。回答时优先依据上述知识；若问题超出油纸伞范围，可简要说明你主要擅长油纸伞与非遗文化。";
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
