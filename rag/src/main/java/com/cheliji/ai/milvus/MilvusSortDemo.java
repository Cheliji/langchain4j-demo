package com.cheliji.ai.milvus;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;

public class MilvusSortDemo {

    public static void main(String[] args) {

        String chunkText = "**7天无理由退货**：商品完好、配件齐全、不影响二次销售（特殊商品除外）" ;

        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl("https://api.siliconflow.cn/v1")
                .apiKey(System.getenv("SILICONFLOW_API_KEY"))
                .modelName("BAAI/bge-large-zh-v1.5")
                .build();


        // chunk 向量化
        Embedding embedding = embeddingModel.embed(chunkText).content();

        System.out.println("  === 向量化完成 ===  ");
        System.out.println("  === 准备连接 Milvus === ");

        MilvusEmbeddingStore embeddingStore = MilvusEmbeddingStore.builder()
                .host("localhost")
                .port(19530)
                .collectionName("knowledge")
                .collectionName("knowledge")
                .idFieldName("doc_id")
                .textFieldName("content")
                .metadataFieldName("metadata")
                .vectorFieldName("embedding")
                .dimension(4096)
                .build();


        String add = embeddingStore.add(embedding);

        System.out.println("===== 添加完成：" + add);


    }

}
