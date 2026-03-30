package com.cheliji.ai.retriever;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;

import java.util.List;

public class ContentRetrieverDemo {

    public static void main(String[] args) {

        String chunkText = "我前几天在你们平台买了一件东西，现在想要退货" ;

        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl("https://api.siliconflow.cn/v1")
                .apiKey(System.getenv("SILICONFLOW_API_KEY"))
                .modelName("Qwen/Qwen3-Embedding-8B")
                .build();


        // chunk 向量化
        // Embedding embedding = embeddingModel.embed(chunkText).content();

        // System.out.println("  === 向量化完成 ===  " + embedding.dimension());
        System.out.println("  === 准备链接 Milvus === ");

        MilvusEmbeddingStore milvusEmbeddingStore = MilvusEmbeddingStore.builder()
                .host("localhost")
                .port(19530)
                .dimension(4096)
                .collectionName("knowledge")
                .idFieldName("doc_id")
                .textFieldName("content")
                .metadataFieldName("metadata")
                .vectorFieldName("embedding")
                .build();

        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(milvusEmbeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.5)
                .build();

        List<Content> retrieve = contentRetriever.retrieve(new Query(chunkText));


        System.out.println("  === 检索完成 ===  ");
        for (int i = 0; i < retrieve.size(); i++) {

            Content content = retrieve.get(i);

            System.out.println("   ==== 第" + (i + 1) + "块检索内容： ==== ");
            System.out.println("内容：" + content.textSegment().text());
            System.out.println("元数据： " + content.metadata());
            System.out.println("得分：" + content.metadata().get(ContentMetadata.SCORE));


        }


    }

}
