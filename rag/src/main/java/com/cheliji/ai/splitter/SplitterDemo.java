package com.cheliji.ai.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.segment.TextSegment;

import java.nio.file.Paths;
import java.util.List;

public class SplitterDemo {

    public static void main(String[] args) {

        // 解析文档
        Document document = FileSystemDocumentLoader.loadDocument(
                Paths.get("D:\\\\Users\\\\Administrator\\\\Desktop\\\\产品手册.md"),
                //new ApachePdfBoxDocumentParser()
                new TextDocumentParser()
        );
        // 元数据会继承
        document.metadata().put("type","产品文档") ;
        document.metadata().put("section","产品部") ;
        // 配置分块策略
        DocumentSplitter splitter = new DocumentByCharacterSplitter(
                100,
                30);

        List<TextSegment> split = splitter.split(document);

        System.out.println("=== 文档分块完成 ===");
        System.out.println("最终分块数量：" + split.size());

        for (int i = 0; i < split.size(); i++) {

            System.out.println("=== 第" + (i + 1) + "块内容："+ split.get(i).text());
            System.out.println("元数据：" + split.get(i).metadata());

        }
    }

}
