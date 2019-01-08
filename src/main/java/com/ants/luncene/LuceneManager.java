package com.ants.luncene;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * lucene 索引管理 增删改查
 */
public class LuceneManager {
    //索引库位置
    private String directoryPath = "F:\\search\\index";
    //文件位置
    private String srcFilePath ="E:\\workspace\\stu\\dc_designpattern\\src\\main\\java\\com\\ants\\behavioral\\mediator";

    public IndexWriter getIndexWriter() throws IOException {
        Directory directory = FSDirectory.open(Paths.get(directoryPath));//文件系统
        Analyzer analyzer = new IKAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);
        return indexWriter;
    }

    public IndexSearcher getIndexSearcher() throws IOException {
        //1 创建一个Directory，索引库中的位置
        Directory directory = FSDirectory.open(Paths.get(directoryPath));//文件系统
        //2 创建IndexReader对象，需要指定Directory对象
        IndexReader indexReader = DirectoryReader.open(directory);
        //3 创建indexSearch对象，需要指定IndexReader对像
        return new IndexSearcher(indexReader);
    }
    //新增
    @Test
    public void add() throws IOException {
        IndexWriter indexWriter = getIndexWriter();
        Document document=null;
        //4 创建Field对象，将Field对象添加到Document对象中
        File file = new File(srcFilePath);
        File[] files = file.listFiles();
        for (File f: files){
            document = new Document();
            document.add(new TextField("fileName",f.getName(), Field.Store.YES));
            document.add(new LongPoint("fileSize", file.length()));
            // 即 IntPoint,DoublePoint等
            document.add(new LongPoint("fileSize", file.length()));
            //大小
            document.add(new StoredField("fileSize", file.length()));
            //同时添加排序支持
            document.add(new NumericDocValuesField("fileSize",file.length()));
            document.add(new StringField("filePath",f.getPath(), Field.Store.YES));
            document.add(new TextField("fileContent",FileUtils.readFileToString(f, "utf-8"), Field.Store.YES));
            //5 使用ndexWrite对象将将document对象写到索引库，此过程需要建立索引，并将索引的document存到索引库中
            indexWriter.addDocument(document);
        }
        //6 关闭IndexWrite对象
        indexWriter.close();
    }
    //删除
    @Test
    public void deleteAll() throws IOException {
        IndexWriter indexWriter = getIndexWriter();
        indexWriter.deleteAll();//删除全部索引
        indexWriter.close();
    }
    //删除
    @Test
    public void delete() throws IOException {
        IndexWriter indexWriter = getIndexWriter();
        Query query =new TermQuery(new Term("fileContent","public"));
        System.out.println(indexWriter.deleteDocuments(query));
        indexWriter.close();
    }
    //修改
    @Test
    public void update() throws IOException {
        IndexWriter indexWriter = getIndexWriter();
        Document document = new Document();
        document.add(new TextField("fileContent","erfg", Field.Store.YES));
        indexWriter.updateDocument(new Term("fileContent","public"),document);
        indexWriter.close();
    }


    //查询所有
    @Test
    public void query() throws IOException {
        IndexSearcher indexSearcher = getIndexSearcher();
        Query query =new MatchAllDocsQuery();
        printfContent(indexSearcher,query);
        indexSearcher.getIndexReader().close();
    }
    //组合条件查询
    @Test
    public void queryBoolean() throws IOException {
        IndexSearcher indexSearcher = getIndexSearcher();
        BooleanQuery.Builder  builder=new BooleanQuery.Builder();
        builder.add(new TermQuery(new Term("fileContent","class")), BooleanClause.Occur.MUST);// 文件名不包含词语,但是内容必须包含姚振
        builder.add(new TermQuery(new Term("fileContent","department")), BooleanClause.Occur.MUST);
        BooleanQuery  query=builder.build();
        System.out.println(query);
        printfContent(indexSearcher,query);
        indexSearcher.getIndexReader().close();
    }
    //查询数值    lucene支持数字不支持字符串返回
    @Test
    public void queryNumber() throws IOException {
        IndexSearcher indexSearcher = getIndexSearcher();
        Query query = LongPoint.newRangeQuery("fileSize", 4097, 500000);
        System.out.println(query);
        printfContent(indexSearcher,query);
        indexSearcher.getIndexReader().close();
    }
    //条件解释得对象查询 一个域
    public void testQueryParser() throws IOException, ParseException {
        IndexSearcher indexSearcher = getIndexSearcher();
        String[] strArr = {"fileContent","fileSize"};
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(strArr, new IKAnalyzer());
        //Query parse = queryParser.parse("*:*");//*域：*值
        Query parse = queryParser.parse("Department class");//使用IK分词器将输入得单词转换成语汇单元，进行搜索
        printfContent(indexSearcher,parse);
        indexSearcher.getIndexReader().close();
    }
    //条件解释得对象查询  多个域
    public void testQueryMutilParser() throws IOException, ParseException {
        IndexSearcher indexSearcher = getIndexSearcher();
        QueryParser queryParser = new QueryParser("fileContent", new IKAnalyzer());
        //Query parse = queryParser.parse("*:*");//*域：*值
        Query parse = queryParser.parse("Department class");//使用IK分词器将输入得单词转换成语汇单元，进行搜索
        printfContent(indexSearcher,parse);
        indexSearcher.getIndexReader().close();
    }
    //条件解释得对象查询 lucene不支持数值，因为拼接字符串会被拆分
    public void testQueryParserStr() throws IOException, ParseException {
        IndexSearcher indexSearcher = getIndexSearcher();
        QueryParser queryParser = new QueryParser("fileSize", new IKAnalyzer());
        //Query parse = queryParser.parse("*:*");//*域：*值
        Query parse = queryParser.parse("fileSize:[4097 TO 500000]");//使用IK分词器将输入得单词转换成语汇单元，进行搜索
        printfContent(indexSearcher,parse);
        indexSearcher.getIndexReader().close();
    }
    @Test
    public void queryBooleanParse() throws IOException, ParseException {
        IndexSearcher indexSearcher = getIndexSearcher();
        QueryParser queryParser = new QueryParser("fileContent", new IKAnalyzer());
        //Query parse = queryParser.parse("*:*");//*域：*值  可以使用OR AND
        Query parse = queryParser.parse("+fileContent:class -fileContent:despartment");//使用IK分词器将输入得单词转换成语汇单元，进行搜索
        printfContent(indexSearcher,parse);
        indexSearcher.getIndexReader().close();
    }

    private void printfContent(IndexSearcher indexSearcher,Query query ) throws IOException {
        TopDocs topDocs = indexSearcher.search(query, 2);

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
    }
}
