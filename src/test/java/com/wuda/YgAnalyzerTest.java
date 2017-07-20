package com.wuda;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.YgAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import com.wuda.analysis.FileDictionaryHandler;

@SuppressWarnings("unused")
public class YgAnalyzerTest {

	private String indexDir = "e:/lucene"; // 索引目录
	private String dictDir = "e:/dict"; // 词典所在的目录

	public void index(Analyzer analyzer) throws IOException {
		Similarity similarity = new ClassicSimilarity();
		Path path = Paths.get(indexDir);
		Directory directory = NIOFSDirectory.open(path);
		Codec codec = new SimpleTextCodec();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setCodec(codec);
		config.setSimilarity(similarity);
		config.setUseCompoundFile(false);
		IndexWriter iwriter = new IndexWriter(directory, config);

		for (int i = 0; i < 1; i++) {
			Document doc = new Document();

			String title = "doc " + i + " java工程师  This is the text to be indexed for title field"; // 根据词典,自己重新设置内容,方便测试
			FieldType titleType = new FieldType();
			titleType.storeTermVectors();
			titleType.setStored(true);
			titleType.setTokenized(true);
			titleType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
			Field titleField = new Field("title", title, titleType);
			doc.add(titleField);

			String content = "doc " + i + " java工程师  This is the text to be indexed for content field";// 根据词典,自己重新设置内容,方便测试
			FieldType contentType = new FieldType();
			contentType.storeTermVectors();
			contentType.setStored(true);
			contentType.setTokenized(true);
			contentType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
			Field contentField = new Field("content", content, contentType);
			doc.add(contentField);

			iwriter.addDocument(doc);
		}
		iwriter.commit();
		iwriter.close();
	}

	public void search() throws IOException {
		Path path = Paths.get(indexDir);
		Directory directory = NIOFSDirectory.open(path);
		final DirectoryReader ireader = DirectoryReader.open(directory);
		IndexSearcher isearcher = new IndexSearcher(ireader);

		Query must = new TermQuery(new Term("title", "java工程师"));// 根据词典,自己重新设置内容,方便测试
		Query should = new TermQuery(new Term("content", "java工程师"));// 根据词典,自己重新设置内容,方便测试

		Builder builder = new Builder();
		builder.add(must, Occur.MUST);
		builder.add(should, Occur.SHOULD);
		BooleanQuery query = builder.build();
		TopDocs topDocs = isearcher.search(query, 10);

		ScoreDoc[] hits = topDocs.scoreDocs;
		// Iterate through the results:
		for (int i = 0; i < hits.length; i++) {
			Document hitDoc = isearcher.doc(hits[i].doc);
			System.out.println(hitDoc);
		}
	}

	public void showToken(YgAnalyzer analyzer, String input) {
		TokenStream stream = analyzer.tokenStream(null, input);
		CharTermAttribute charTermAttr = stream.addAttribute(CharTermAttribute.class);
		TypeAttribute typeAttr = stream.addAttribute(TypeAttribute.class);

		try {
			stream.reset();
			while (stream.incrementToken()) {
				System.out.println("token:" + charTermAttr.toString() + "\ttype:" + typeAttr.type());
			}
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		YgAnalyzerTest test = new YgAnalyzerTest();
		try {
			YgAnalyzer analyzer = new YgAnalyzer();// 使用yg分词
			analyzer.setEnumerateAll(false);
			FileDictionaryHandler handler = new FileDictionaryHandler();
			handler.setDirectory("e:/dict");
			handler.setIsAsynLoadDict(false);
			handler.loadAll(); // 从词典目录加载词典,同步加载词典
			// Analyzer analyzer=new StandardAnalyzer();
			// test.index(analyzer);
			// test.search();
			test.showToken(analyzer, "矿泉水500ml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
