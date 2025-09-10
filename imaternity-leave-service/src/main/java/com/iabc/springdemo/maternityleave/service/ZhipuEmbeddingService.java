package com.iabc.springdemo.maternityleave.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ZhipuEmbeddingService {

    @Value("${zhipuai.api-key}")
    private String apiKey;

    @Value("${zhipuai.embedding.url}")
    private String embeddingUrl;

    @Value("${zhipuai.embedding.model:embedding-2}")
    private String embeddingModel;

    private final WebClient webClient;

    public ZhipuEmbeddingService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }


    /**
     * 生成单个文本的嵌入向量
     */
    public float[] generateEmbedding(String text) {
        List<float[]> embeddings = generateEmbeddings(List.of(text));
        return embeddings.get(0);
    }

    /**
     * 批量生成多个文本的嵌入向量
     */
    public List<float[]> generateEmbeddings(List<String> texts) {
        // 准备请求体 - 智普AI的请求格式
        Map<String, Object> requestBody = new HashMap<>();
        embeddingModel = "embedding-2";
        requestBody.put("model", embeddingModel);
        requestBody.put("input", texts);

        // 发送请求并获取响应
        ZhipuEmbeddingResponse response = webClient.post()
                .uri(embeddingUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.error(new RuntimeException("智普AI API 请求失败: " + clientResponse.statusCode())))
                .bodyToMono(ZhipuEmbeddingResponse.class)
                .block();

        if (response == null || response.data == null || response.data.isEmpty()) {
            throw new RuntimeException("智普AI嵌入生成失败: 空响应或无效数据");
        }
        // 提取嵌入向量
        return response.data.stream()
                .map(item -> convertToFloatArray(item.embedding))
                .toList();
    }

    /**
     * 将 List<Double> 转换为 float[]
     */
    private float[] convertToFloatArray(List<Double> doubleList) {
        float[] floatArray = new float[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            floatArray[i] = doubleList.get(i).floatValue();
        }
        return floatArray;
    }

    /**
     * 计算两个文本的余弦相似度
     */
    public float calculateSimilarity(String text1, String text2) {
        float[] embedding1 = generateEmbedding(text1);
        float[] embedding2 = generateEmbedding(text2);
        return cosineSimilarity(embedding1, embedding2);
    }

    /**
     * 计算两个向量的余弦相似度
     */
    private float cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("向量必须具有相同的长度");
        }

        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        return (float) (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    /**
     * 智普AI API 响应数据结构
     */
    public static class ZhipuEmbeddingResponse {
        public String object;
        public List<EmbeddingData> data;
        public String model;
        public Usage usage;

        public static class EmbeddingData {
            public int index;
            public String object;
            public List<Double> embedding;
        }

        public static class Usage {
            public int prompt_tokens;
            public int total_tokens;
        }
    }
}