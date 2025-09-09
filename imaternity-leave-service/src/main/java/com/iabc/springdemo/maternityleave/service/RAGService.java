package com.iabc.springdemo.maternityleave.service;

import com.iabc.springdemo.maternityleave.entity.MaternityPolicy;
import com.iabc.springdemo.maternityleave.mapper.MaternityPolicyMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Service
public class RAGService {

    private final MaternityPolicyMapper policyMapper;
    private final ChatClient chatClient;
    private final EmbeddingService embeddingService;

    @Autowired
    public RAGService(MaternityPolicyMapper policyMapper,
                                    ChatClient chatClient,
                                    EmbeddingService embeddingService) {
        this.policyMapper = policyMapper;
        this.chatClient = chatClient;
        this.embeddingService = embeddingService;
    }

    /**
     * 查询产假政策 - 使用 RAG 模式
     */
    public String queryMaternityPolicy(String question, String region) {
        // 1. 生成问题的嵌入向量
        float[] questionEmbedding = embeddingService.embedText(question);

        // 2. 将嵌入向量转换为字符串格式用于查询
        String embeddingStr = toEmbeddingString(questionEmbedding);

        // 3. 检索最相关的政策
        List<MaternityPolicy> similarPolicies = policyMapper.findSimilarPolicies(embeddingStr, 5);

        // 4. 如果有地区筛选，进一步过滤
        if (region != null && !region.trim().isEmpty()) {
            similarPolicies = similarPolicies.stream()
                    .filter(policy -> region.equalsIgnoreCase(policy.getCity()))
                    .collect(Collectors.toList());
        }

        // 5. 构建上下文
        String context = buildContext(similarPolicies);

        // 6. 使用 Spring AI 1.0 的 ChatClient 构建对话
        return chatClient.prompt()
                .system(s -> s.text("""
                    你是一个专业的HR助手，请根据知识库信息回答关于产假天数的问题。
                    
                    请以结构化方式回答，包括:
                    1. 产假类型
                    2. 适用天数
                    3. 适用条件
                    4. 所需证明材料(如果有)
                    
                    如果问题涉及多种情况，请分别说明。
                    
                    回答时请使用中文。
                    """))
                .user(u -> u.text("知识库信息:\n" + context + "\n\n问题: " + question))
                .call()
                .content();
    }

    /**
     * 计算产假天数 - 结构化方法
     */
//    public MaternityLeaveCalculation calculateMaternityLeave(String region, String leaveType,
//                                                             String conditionValue, boolean hasDystocia,
//                                                             int numberOfBabies, boolean isLateBirth) {
//        // 1. 查找相关策略
//        List<MaternityPolicy> policies = policyMapper.findSimilarPolicies(region, 5);
//
//        // 2. 根据条件筛选
//        MaternityPolicy matchedPolicy = policies.stream()
//                .filter(policy -> conditionValue.equals(policy.getConditionValue()))
//                .findFirst()
//                .orElse(null);
//
//        if (matchedPolicy == null) {
//            return new MaternityLeaveCalculation("未找到匹配的产假政策", 0, Map.of());
//        }
//
//        // 3. 计算基础天数
//        int totalDays = matchedPolicy.getDays();
//        Map<String, Integer> breakdown = new java.util.HashMap<>();
//        breakdown.put(leaveType, matchedPolicy.getDays());
//
//        // 4. 应用额外条件
//        if (hasDystocia && "法定产假".equals(leaveType)) {
//            // 查找难产假政策
//            List<MaternityPolicy> dystociaPolicies = policyMapper.findByRegionAndLeaveType(region, "难产假");
//            MaternityPolicy dystociaPolicy = dystociaPolicies.stream().findFirst().orElse(null);
//            if (dystociaPolicy != null) {
//                totalDays += dystociaPolicy.getDays();
//                breakdown.put("难产假", dystociaPolicy.getDays());
//            }
//        }
//
//        if (numberOfBabies > 1 && "法定产假".equals(leaveType)) {
//            // 查找多胞胎假政策
//            List<MaternityPolicy> multipleBirthPolicies = policyMapper.findByRegionAndLeaveType(region, "多胞胎假");
//            MaternityPolicy multipleBirthPolicy = multipleBirthPolicies.stream().findFirst().orElse(null);
//            if (multipleBirthPolicy != null) {
//                int additionalDays = multipleBirthPolicy.getDays() * (numberOfBabies - 1);
//                totalDays += additionalDays;
//                breakdown.put("多胞胎假", additionalDays);
//            }
//        }
//
//        if (isLateBirth && "法定产假".equals(leaveType)) {
//            // 查找晚育假政策
//            List<MaternityPolicy> lateBirthPolicies = policyMapper.findByRegionAndLeaveType(region, "晚育假");
//            MaternityPolicy lateBirthPolicy = lateBirthPolicies.stream().findFirst().orElse(null);
//            if (lateBirthPolicy != null) {
//                totalDays += lateBirthPolicy.getDays();
//                breakdown.put("晚育假", lateBirthPolicy.getDays());
//            }
//        }
//
//        // 5. 使用大模型生成自然语言解释
//        String explanation = generateExplanation(region, leaveType, conditionValue,
//                hasDystocia, numberOfBabies, isLateBirth,
//                matchedPolicy.getDays(), totalDays, breakdown);
//
//        return new MaternityLeaveCalculation(explanation, totalDays, breakdown);
//    }

    /**
     * 使用大模型生成解释
     */
    private String generateExplanation(String region, String leaveType, String conditionValue,
                                       boolean hasDystocia, int numberOfBabies, boolean isLateBirth,
                                       int baseDays, int totalDays, Map<String, Integer> breakdown) {
        return chatClient.prompt()
                .system(s -> s.text("""
                    你是一个专业的HR助手，请根据提供的产假计算信息生成一个友好、专业的解释。
                    
                    请解释计算过程，包括基础天数和各项额外天数。
                    回答时请使用中文，语气友好专业。
                    """))
                .user(u -> u.text(String.format("""
                    请根据以下信息生成一个关于产假天数的自然语言解释：
                    
                    地区: %s
                    产假类型: %s
                    条件: %s
                    是否难产: %s
                    婴儿数量: %d个
                    是否晚育: %s
                    
                    基础天数: %d天
                    总天数: %d天
                    明细: %s
                    """,
                        region, leaveType, conditionValue,
                        hasDystocia ? "是" : "否", numberOfBabies, isLateBirth ? "是" : "否",
                        baseDays, totalDays, breakdown)))
                .call()
                .content();
    }

    /**
     * 流式响应 - 适用于实时对话场景
     */
//    public void streamMaternityPolicyResponse(String question, String region,
//                                              java.util.function.Consumer<String> contentConsumer) {
//        // 1. 生成问题的嵌入向量
//        float[] questionEmbedding = embeddingService.embedText(question);
//
//        // 2. 将嵌入向量转换为字符串格式用于查询
//        String embeddingStr = toEmbeddingString(questionEmbedding);
//
//        // 3. 检索最相关的政策
//        List<MaternityPolicy> similarPolicies = policyMapper.findSimilarPolicies(embeddingStr, 5);
//
//        // 4. 如果有地区筛选，进一步过滤
//        if (region != null && !region.trim().isEmpty()) {
//            similarPolicies = similarPolicies.stream()
//                    .filter(policy -> region.equalsIgnoreCase(policy.getCity()))
//                    .collect(Collectors.toList());
//        }
//
//        // 5. 构建上下文
//        String context = buildContext(similarPolicies);
//
//        // 6. 使用流式API生成响应
//        chatClient.prompt()
//                .system(s -> s.text("""
//                    你是一个专业的HR助手，请根据知识库信息回答关于产假天数的问题。
//
//                    请以结构化方式回答，包括:
//                    1. 产假类型
//                    2. 适用天数
//                    3. 适用条件
//                    4. 所需证明材料(如果有)
//
//                    如果问题涉及多种情况，请分别说明。
//
//                    回答时请使用中文。
//                    """))
//                .user(u -> u.text("知识库信息:\n" + context + "\n\n问题: " + question))
//                .stream()
//                .(contentConsumer);
//    }

    /**
     * 构建政策上下文
     */
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

    /**
     * 将嵌入向量转换为字符串表示
     */
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