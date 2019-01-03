package com.ants.luncene;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * luncene入门，创建索引 查询索引
 */
public class ForstLuncene {

    //创建索引
    @Test
    public void testName() throws Exception {
        //1 创建IndexWrite对象
        //2.1 指定索引库Directory对象
        Directory directory = FSDirectory.open(Paths.get("F:/search/index"));//文件系统  硬盘索引
        //2.2 指定一个分析器，对文件进行分析  标准分析其
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);
        //3 创建Document对象
        Document document= new Document();
        //4 创建Field对象，将Field对象添加到Document对象中
        File file = new File("E:\\workspace\\stu\\net\\src\\main\\java\\pub\\ants\\net\\chat\\demo04");
        File[] files = file.listFiles();
        for (File f: files){
            //分析 存储 拆分 StringField
            //文件名称
            document.add(new TextField("fileName",f.getName(), Field.Store.YES));
            document.add(new StringField("fileSize",FileUtils.sizeOf(f)+"", Field.Store.YES));
            document.add(new StoredField("filePath",f.getPath()));
            document.add(new TextField("fileContent",FileUtils.readFileToString(f, "utf-8"), Field.Store.YES));
            //5 使用ndexWrite对象将将document对象写到索引库，此过程需要建立索引，并将索引的document存到索引库中
            indexWriter.addDocument(document);
        }
        //6 关闭IndexWrite对象
        indexWriter.close();
    }

    public void testSearch() throws IOException {
        //1 创建一个Directory，索引库中的位置
        Directory directory = FSDirectory.open(Paths.get("F:/search/index"));//文件系统
        //2 创建IndexReader对象，需要指定Directory对象
        IndexReader indexReader = directory.

    }
}
