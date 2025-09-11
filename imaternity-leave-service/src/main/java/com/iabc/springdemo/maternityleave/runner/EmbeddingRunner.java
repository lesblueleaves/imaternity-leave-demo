package com.iabc.springdemo.maternityleave.runner;

import com.iabc.springdemo.maternityleave.service.ZhipuEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingRunner implements CommandLineRunner {

//    private final ZhiPuAiEmbeddingModel embeddingModel;

//    public EmbeddingRunner(ZhiPuAiEmbeddingModel embeddingModel) {
//        this.embeddingModel = embeddingModel;
//    }

    @Autowired
    ZhipuEmbeddingService zhipuEmbeddingService;

    @Override
    public void run(String... args) throws Exception {
//        // 单文本嵌入
//        float[] embedding = zhipuEmbeddingService.generateEmbedding("张三小姐请产假");
//        System.out.println(embedding);
//        System.out.println("嵌入维度: " + embedding.length);
    }
}