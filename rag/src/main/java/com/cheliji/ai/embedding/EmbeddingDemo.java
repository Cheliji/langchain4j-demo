package com.cheliji.ai.embedding;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

public class EmbeddingDemo {

    public static void main(String[] args) {

        String text1 = "取消订单：未发货订单可在“我的订单”页面自主取消" ;
        String text2 = "7天无理由退货：商品完好、配件齐全、不影响二次销售（特殊商品除外）" ;
        String text3 = "质量问题退换：签收后15天内可申请，平台承担退货运费" ;

        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl("https://api.siliconflow.cn/v1")
                .apiKey(System.getenv("SILICONFLOW_API_KEY"))
                .modelName("BAAI/bge-large-zh-v1.5")
                .build();

        Embedding embedding = embeddingModel.embed("我要退货，有什么要求吗？").content();
        Embedding embedding1 = embeddingModel.embed(text1).content();
        Embedding embedding2 = embeddingModel.embed(text2).content();
        Embedding embedding3 = embeddingModel.embed(text3).content();

        System.out.println("向量为度：" + embedding.dimension());


        System.out.println("问题向量化：[") ;
        for(float e : embedding.vector()) {
            System.out.print(" " + e);
        }

        System.out.println("]");

        System.out.println("text1 向量化：[") ;
        for(float e : embedding1.vector()) {
            System.out.print(" " + e);
        }

        System.out.println("]");

        System.out.println("text2 向量化：[") ;
        for(float e : embedding2.vector()) {
            System.out.print(" " + e);
        }

        System.out.println("]");

        System.out.println("text3 向量化：[") ;
        for(float e : embedding3.vector()) {
            System.out.print(" " + e);
        }

        System.out.println("]");


        System.out.println("与 text1 的余弦相似度：" +
                cosineSimilarity(embedding.vector(),embedding1.vector()));;
        System.out.println("与 text2 的余弦相似度：" +
                cosineSimilarity(embedding.vector(),embedding2.vector()));;
        System.out.println("与 text3 的余弦相似度：" +
                cosineSimilarity(embedding.vector(),embedding3.vector()));;



    }

    private static double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        // 修正：这里应该是乘法，不是除法！
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

}
