package com.cheliji.ai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;


public class DocumentDemo {

    public static void main(String[] args) throws FileNotFoundException {

        Document document = toAnalyzeFile(new File("D:\\\\Users\\\\Administrator\\\\Desktop\\\\产品手册.pdf")) ;

        System.out.println("解析文件的文本内容：" + document.text());
        System.out.println("解析文本元数据：" + document.metadata());


    }

    private static Document toAnalyzeFile(File file) throws FileNotFoundException {

        DocumentParser parser = new ApachePdfBoxDocumentParser();
        Document parse = parser.parse(new FileInputStream(file));

        String text = parse.text() ;

        // 文档的元数据一般是要自己创建
        Metadata metadata = new Metadata();

        metadata.put("fileName",file.getName());
        metadata.put("type","PDF") ;

        metadata.put("section","产品部") ;
        metadata.put("createTime", String.valueOf(new Date(System.currentTimeMillis())));

        return Document.from(text,metadata) ;


    }

}
