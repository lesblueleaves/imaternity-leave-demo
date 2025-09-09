package com.iabc.springdemo.maternityleave.service;

import com.iabc.springdemo.maternityleave.entity.MaternityPolicy;
import com.iabc.springdemo.maternityleave.mapper.MaternityPolicyMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MaternityLeaveRagService {

    private final MaternityPolicyMapper policyMapper;
    private final ChatClient chatClient;
    private final ZhipuEmbeddingService embeddingService;

    @Autowired
    public MaternityLeaveRagService(MaternityPolicyMapper policyMapper,
                                    ChatClient chatClient,
                                    ZhipuEmbeddingService embeddingService) {
        this.policyMapper = policyMapper;
        this.chatClient = chatClient;
        this.embeddingService = embeddingService;
    }

    public String queryMaternityPolicy(String question) {
        // 1. 生成问题的嵌入向量
        float[] questionEmbedding = embeddingService.generateEmbedding(question);

        // 2. 将嵌入向量转换为字符串格式用于查询
        String embeddingStr = toEmbeddingString(questionEmbedding);

        // 3. 检索最相关的政策
        List<MaternityPolicy> similarPolicies = policyMapper.findSimilarPolicies(embeddingStr, 5);
        int days =similarPolicies.stream()
                .map(item -> item.getDays())
                .reduce(Integer::sum).orElse(0);
        return days + "";
//        // 5. 构建上下文
//        String context = buildContext(similarPolicies);
//
//        // 6. 使用 Spring AI 1.0 的 ChatClient 构建对话
//        return chatClient.prompt()
//                .system(s -> s.text("""
//                        你是一个专业的HR助手，请根据知识库信息回答关于产假天数的问题。
//
//                        请以结构化方式回答，包括:
//                        1. 产假类型
//                        2. 适用天数
//                        3. 适用条件
//                        4. 所需证明材料(如果有)
//
//                        如果问题涉及多种情况，请分别说明。
//
//                        回答时请使用中文。
//                        """))
//                .user(u -> u.text("知识库信息:\n" + context + "\n\n问题: " + question))
//                .call()
//                .content();
    }

    private String buildContext(List<MaternityPolicy> policies) {
        if (policies.isEmpty()) {
            return "没有找到相关的产假政策信息。";
        }

        StringBuilder context = new StringBuilder();
        for (MaternityPolicy policy : policies) {
            context.append(String.format("""
                            城市: %s
                            产假类型: %s
                            条件类型: %s
                            条件值: %s
                            天数: %d %s
                            是否顺延节假日: %s
                            描述: %s
                            
                            """,
                    policy.getCity(),
                    policy.getLeaveType(),
                    policy.getConditionKey(),
                    policy.getConditionValue(),
                    policy.getDays(),
                    policy.getExtendHoliday() ? "是" : "否",
                    policy.getPolicyText()));
        }

        return context.toString();
    }

    private String toEmbeddingString(float[] embedding) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 产假计算结果类
     */
    public static class MaternityLeaveCalculation {
        private final String explanation;
        private final int totalDays;
        private final Map<String, Integer> breakdown;

        public MaternityLeaveCalculation(String explanation, int totalDays, Map<String, Integer> breakdown) {
            this.explanation = explanation;
            this.totalDays = totalDays;
            this.breakdown = breakdown;
        }

        public String getExplanation() {
            return explanation;
        }

        public int getTotalDays() {
            return totalDays;
        }

        public Map<String, Integer> getBreakdown() {
            return breakdown;
        }
    }
}