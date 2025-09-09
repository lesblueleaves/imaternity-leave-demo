package com.iabc.springdemo.maternityleave.service;


import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

//@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    @Autowired
    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public float[] embedText(String text) {
//        float[] embedding = embeddingModel.embed(text);
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(text));
        return embeddingResponse.getResult().getOutput();
//        List<Double> doubleList = new ArrayList<>();
//        for (float f : floatArray) {
//            doubleList.add((double) f);
//        }
//        return doubleList;
    }

    public float[] embedMultipleTexts(List<String> texts) {
        EmbeddingResponse response = embeddingModel.embedForResponse(texts);
        return response.getResult().getOutput();
    }
}