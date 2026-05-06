package com.iyunxin.jxkh.infra.ai;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyunxin.jxkh.config.AiConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 智能总结服务（使用阿里云通义千问）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AISummaryService {
    
    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 生成周报智能总结
     *
     * @param content 周报内容
     * @return AI 总结结果
     */
    public AISummaryResult generateWeeklySummary(String content) {
        if (!aiConfig.isEnabled()) {
            log.debug("AI 服务未启用，返回空结果");
            return AISummaryResult.builder()
                    .keyAchievements(List.of())
                    .risks(List.of())
                    .suggestions(List.of())
                    .build();
        }
        
        // 数据脱敏
        String sanitizedContent = sanitizeContent(content);
        
        String prompt = buildWeeklySummaryPrompt(sanitizedContent);
        
        try {
            String response = callAI(prompt);
            return parseAIResponse(response);
        } catch (Exception e) {
            log.error("AI 总结生成失败", e);
            return AISummaryResult.builder()
                    .keyAchievements(List.of())
                    .risks(List.of())
                    .suggestions(List.of())
                    .error(e.getMessage())
                    .build();
        }
    }
    
    /**
     * 数据脱敏
     */
    private String sanitizeContent(String content) {
        if (content == null) {
            return "";
        }
        
        // 隐藏手机号（11位数字）
        content = content.replaceAll("\\b1[3-9]\\d{9}\\b", "***隐藏手机号***");
        
        // 隐藏身份证号（18位）
        content = content.replaceAll("\\b\\d{17}[\\dXx]\\b", "***隐藏身份证号***");
        
        // 隐藏工号（假设格式：EMP+数字）
        content = content.replaceAll("\\bEMP\\d+\\b", "***隐藏工号***");
        
        return content;
    }
    
    /**
     * 构建 Prompt
     */
    private String buildWeeklySummaryPrompt(String content) {
        return """
                你是一个专业的绩效考核助手。请对以下周报内容进行智能分析并生成结构化总结。
                
                【分析要求】
                1. 提取本周关键成果（3-5条，简洁明了）
                2. 识别潜在风险点（如有）
                3. 给出下周工作建议（2-3条）
                
                【周报内容】
                %s
                
                【输出格式】
                请严格按照以下JSON格式输出，不要包含其他文字：
                {
                  "keyAchievements": ["成果1", "成果2"],
                  "risks": ["风险1", "风险2"],
                  "suggestions": ["建议1", "建议2"]
                }
                
                如果没有风险或建议，对应数组可以为空。
                """.formatted(content);
    }
    
    /**
     * 调用 AI API
     */
    private String callAI(String prompt) throws ApiException, NoApiKeyException, InputRequiredException {
        AiConfig.AliyunConfig config = aiConfig.getAliyun();
        
        Generation gen = new Generation();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(prompt)
                .build();
        
        GenerationParam param = GenerationParam.builder()
                .apiKey(config.getApiKey())
                .model(config.getModel())
                .messages(Arrays.asList(userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        
        GenerationResult result = gen.call(param);
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }
    
    /**
     * 解析 AI 响应
     */
    private AISummaryResult parseAIResponse(String response) {
        try {
            // 提取 JSON 部分（可能包含 markdown 代码块）
            String jsonStr = extractJSON(response);
            
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            
            List<String> achievements = extractArray(jsonNode, "keyAchievements");
            List<String> risks = extractArray(jsonNode, "risks");
            List<String> suggestions = extractArray(jsonNode, "suggestions");
            
            return AISummaryResult.builder()
                    .keyAchievements(achievements)
                    .risks(risks)
                    .suggestions(suggestions)
                    .build();
                    
        } catch (JsonProcessingException e) {
            log.error("解析 AI 响应失败: {}", response, e);
            return AISummaryResult.builder()
                    .keyAchievements(List.of())
                    .risks(List.of())
                    .suggestions(List.of())
                    .error("解析失败: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 从响应中提取 JSON
     */
    private String extractJSON(String response) {
        // 尝试提取 ```json ... ``` 中的内容
        Pattern pattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```");
        Matcher matcher = pattern.matcher(response);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // 如果没有 markdown 标记，尝试直接解析
        int startIndex = response.indexOf('{');
        int endIndex = response.lastIndexOf('}');
        
        if (startIndex >= 0 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        
        return response;
    }
    
    /**
     * 提取 JSON 数组
     */
    private List<String> extractArray(JsonNode jsonNode, String fieldName) {
        JsonNode arrayNode = jsonNode.get(fieldName);
        if (arrayNode != null && arrayNode.isArray()) {
            return objectMapper.convertValue(arrayNode, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        }
        return List.of();
    }
    
    /**
     * AI 总结结果
     */
    @Data
    @lombok.Builder
    public static class AISummaryResult {
        private List<String> keyAchievements;
        private List<String> risks;
        private List<String> suggestions;
        private String error;
    }
}
