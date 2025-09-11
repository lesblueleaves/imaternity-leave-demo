package com.iabc.springdemo.maternityleave.service;

import com.iabc.springdemo.maternityleave.entity.MaternityPolicy;
import com.iabc.springdemo.maternityleave.mapper.MaternityPolicyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaternityLeaveRagService {

    private final MaternityPolicyMapper policyMapper;
    private final ZhipuEmbeddingService embeddingService;
    private final BigModelService bigModelService;


    public List<MaternityPolicy> queryMaternityPolicy(String question) {
        // 1. 生成问题的嵌入向量
        float[] questionEmbedding = embeddingService.generateEmbedding(question);

        // 2. 将嵌入向量转换为字符串格式用于查询
        String embeddingStr = toEmbeddingString(questionEmbedding);

        // 3. 检索最相关的政策
        List<MaternityPolicy> similarPolicies = policyMapper.findSimilarPolicies(embeddingStr, 1);
        return similarPolicies;
//        String resp = calculateLeaveDays(similarPolicies.get(0).getPolicyText(), question);
//        return resp;
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