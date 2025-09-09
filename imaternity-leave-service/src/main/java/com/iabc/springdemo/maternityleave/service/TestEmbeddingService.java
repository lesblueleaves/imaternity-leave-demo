package com.iabc.springdemo.maternityleave.service;//package com.iabc.springdemo.maternityleave.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.Arrays;
//
//@Service
//@RequiredArgsConstructor
//public class TestEmbeddingService {
//
//    private final OpenAiEmbeddingClient embeddingClient;
//
//    public String testEmbedding() {
//        String text = "员工正常分娩，享受98天产假，其中产前可休15天。";
//        float[] embedding = embeddingClient.createEmbedding(text);
//        System.out.println("Embedding 维度: " + embedding.length);
//        System.out.println("前10个值: " + Arrays.toString(Arrays.copyOf(embedding, 10)));
//        return Arrays.toString(Arrays.copyOf(embedding, 10));
//    }
//}
