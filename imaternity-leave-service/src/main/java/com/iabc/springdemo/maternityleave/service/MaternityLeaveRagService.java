package com.iabc.springdemo.maternityleave.service;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import ai.z.openapi.service.model.ChatThinking;
import com.iabc.springdemo.maternityleave.entity.MaternityPolicy;
import com.iabc.springdemo.maternityleave.mapper.MaternityPolicyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaternityLeaveRagService {

    private final MaternityPolicyMapper policyMapper;
    private final ChatClient chatClient;
    private final ZhipuEmbeddingService embeddingService;

    private final ZhipuAiClient zhipuAiClient;


    public String queryMaternityPolicy(String question) {
        // 1. 生成问题的嵌入向量
        float[] questionEmbedding = embeddingService.generateEmbedding(question);

        // 2. 将嵌入向量转换为字符串格式用于查询
        String embeddingStr = toEmbeddingString(questionEmbedding);

        // 3. 检索最相关的政策
        List<MaternityPolicy> similarPolicies = policyMapper.findSimilarPolicies(embeddingStr, 1);
        String resp = calculateLeaveDays(similarPolicies.get(0).getPolicyText(), question);
        return resp;
    }


    public String calculateLeaveDays(String policy, String userQuery) {
        // 2. 构造 Prompt
        String prompt = """
                已知以下上海产假政策规则：
                %s
                
                用户问题：%s
                
                请严格根据规则，计算员工应享受的总产假天数，并给出推理过程和最终答案（只输出数字天数作为结论）。
                """.formatted(policy, userQuery);

        String prompt1 = """
                已知以下上海产假政策规则：
                %s
         
                请严格根据规则，计算员工应享受的总产假天数，只回答最后数字即可。
                """.formatted(policy);

//        OpenAiChatOptions options = OpenAiChatOptions.builder()
//                .model("glm-4.5")        // 指定智谱模型名（或其它兼容名称）
//                .temperature(0.0d)    // 可选：设置温度等
//                .build();
        ChatClient.CallResponseSpec response = chatClient.prompt(prompt)
                .call();
//        // 3. 调用 DeepSeek
//        ChatClient.CallResponseSpec response = chatClient.prompt(prompt).
//                .options(opt -> opt.("glm-4"))
//                .call();
        return callZhiPuBigModel(prompt1, userQuery);
    }


    public String callZhiPuBigModel(String prompt, String query) {
        // 创建聊天完成请求
        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model("glm-4.5")
                .messages(Arrays.asList(
                        ChatMessage.builder()
                                .role(ChatMessageRole.SYSTEM.value())
                                .content(prompt)
                                .build(),
                        ChatMessage.builder()
                                .role(ChatMessageRole.USER.value())
                                .content(query)
                                .build()
                ))
                .thinking(ChatThinking.builder().type("disabled").build())
                .maxTokens(4096)
                .temperature(0.6f)
                .build();

        // 发送请求
        ChatCompletionResponse response = zhipuAiClient.chat().createChatCompletion(request);

        // 获取回复
        String resp ="";
        if (response.isSuccess()) {
            Object reply = response.getData().getChoices().get(0).getMessage().getContent();
            System.out.println("AI 回复: " + reply);
            resp = reply.toString();
        } else {
            System.err.println("错误: " + response.getMsg());
            resp = response.getMsg();
        }
        return resp;
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
}