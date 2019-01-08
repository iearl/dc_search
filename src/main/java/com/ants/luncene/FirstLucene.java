package com.ants.luncene;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;


/**
 * luncene入门，创建索引 查询索引
 */
public class FirstLucene {
    //索引库位置
    private String directoryPath = "F:\\search\\index";
    //文件位置
    private String srcFilePath ="E:\\workspace\\stu\\dc_designpattern\\src\\main\\java\\com\\ants\\behavioral\\mediator";


    //创建索引
    @Test
    public void testName() throws Exception {
        //1 创建IndexWrite对象
        //2.1 指定索引库Directory对象
        Directory directory = FSDirectory.open(Paths.get(directoryPath));//文件系统  硬盘索引
        //2.2 指定一个分析器，对文件进行分析  标准分析其
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);
        //3 创建Document对象
        Document document=null;
        //4 创建Field对象，将Field对象添加到Document对象中
        File file = new File(srcFilePath);
        File[] files = file.listFiles();
        for (File f: files){
            document = new Document();
            //分析 存储 拆分 StringField
            //文件名称
            document.add(new TextField("fileName",f.getName(), Field.Store.YES));
            System.out.println(f.getName());
            document.add(new StringField("fileSize",FileUtils.sizeOf(f)+"", Field.Store.YES));
            document.add(new StoredField("filePath",f.getPath()));
            document.add(new TextField("fileContent",FileUtils.readFileToString(f, "utf-8"), Field.Store.YES));
            //5 使用ndexWrite对象将将document对象写到索引库，此过程需要建立索引，并将索引的document存到索引库中
            indexWriter.addDocument(document);
        }
        //6 关闭IndexWrite对象
        indexWriter.close();
    }

    @Test
    public void testSearch() throws IOException {
        //1 创建一个Directory，索引库中的位置
        Directory directory = FSDirectory.open(Paths.get(directoryPath));//文件系统
        //2 创建IndexReader对象，需要指定Directory对象
        IndexReader indexReader = DirectoryReader.open(directory);
        //3 创建indexSearch对象，需要指定IndexReader对像
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //4 创建TermQuery对象，指定查询域和查询的关键词
        Query query =new TermQuery(new Term("fileContent","erfg"));
        //5 执行查询
        TopDocs topDocs = indexSearcher.search(query,1);//查处评分最高的的n条记录
        //6 返回查询结果，遍历并输出
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc s:scoreDocs){//文档类型
            int doc = s.doc;
            Document doc1 = indexSearcher.doc(doc);
            System.out.println(doc1.get("fileName"));
            System.out.println(doc1.get("fileSize"));
            System.out.println(doc1.get("filePath"));
            System.out.println(doc1.get("fileContent"));
            System.out.println("-------------------------");
        }
        //7 关闭流
        indexReader.close();
    }
    //分词效果，标准分词器对于汉字只是单个拆分
    @Test
    public void testTokenStream() throws Exception{
        //创建一个标准分词对象
        //--Analyzer analyzer = new StandardAnalyzer();//标准分词器
        //Analyzer analyzer = new CJKAnalyzer();//中日韩二分分词器，两个单词分
        //Analyzer analyzer = new SmartChineseAnalyzer();//中文较号，扩展性差
        Analyzer analyzer = new IKAnalyzer();//IK分词器，扩展性高
        //获得TokenStream对象 (域名-随便,分析文档的内容)
        TokenStream tokenStream = analyzer.tokenStream("test","马高伟开始学习分词器");
        //添加引用，获得每个关键词
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        //添加引用，记录关键词的开始和结束位置
        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        //指针调整列表头部
        tokenStream.reset();
        //遍历关键词列表，通过incrementToken方法列表是否结束
        while (tokenStream.incrementToken()){
            //关键词的起始位置
            System.out.println("start-->"+offsetAttribute.startOffset());
            //取关键词
            System.out.println(charTermAttribute);
            //关键词的结束位置
            System.out.println("end-->"+offsetAttribute.endOffset());
        }
        //关闭流
        tokenStream.close();
    }
}
