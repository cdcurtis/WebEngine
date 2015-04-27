import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;




public class Indexer {
	
	public static void main(String[] args) throws IOException, ParseException 
	{
		//Create IndexWriter
		IndexWriter indexWriter = CreateIndexWriter("indexDirectory"); 
		
		//Find HTML Files
		File HTMLDirectory = FindSavedHTMLFiles("../output");
		
		//Add each HTML to Index writer.
		File [] html=HTMLDirectory.listFiles();
		
		for(int i=0;i<html.length;++i){
			String fileName= html[i].toString();
			System.out.print("Starting: "+ fileName + " ");
			if(!fileName.matches("index.txt")){
				String doc=getfile(fileName);
				String content=stripFile(doc);
				content= cleanContent(content);
				AddDocument(indexWriter, fileName, content, "");
			}
			System.out.println("Finished: "+ fileName);
			
		}
		
		
		//Close IndexWriter
		CloseIndexWriter(indexWriter);
		
		System.out.println("Finsihed Indexing");
	}
	
	static File FindSavedHTMLFiles(String filePath)
	{
		File htmlFiles=new File(filePath);
		
		if (!htmlFiles.isDirectory())
		{
			System.err.println(filePath + ": is not a path to the directory of HTML docs.");
		}
		
		return htmlFiles;
	}
	private static String getfile(String fileName) {

		String doc="";
		BufferedReader br = null;

		String sCurrentLine;
		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((sCurrentLine = br.readLine()) != null) {
				doc+=sCurrentLine;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doc;
	}
	
	public static String cleanString(String s){
		String save="";
		while(s.indexOf('<')!=-1)
		{
			int index= s.indexOf('<');
			save+= s.substring(0,index)+ " ";
			index= s.indexOf('>');
			if(index==-1){
				System.out.println("there was a problem with: "+ s);
				return s;
			}
			s=s.substring(index+1);
		}
		return save +" "+s;
	}
	

	private static String cleanContent(String content) {
		content = content.replace('&', ' ');
		content = content.replace(':', ' ');
		content = content.replace('.', ' ');
		content = content.replace(',', ' ');
		content = content.replace(';', ' ');
		content = content.replace('!', ' ');
		content = content.replace('?', ' ');
		content = content.replace('(', ' ');
		content = content.replace(')', ' ');
		content = content.replace('[', ' ');
		content = content.replace(']', ' ');
		content = content.replace('-', ' ');
		content = content.replace('\'', ' ');
		content = content.replace('\"', ' ');
		content = content.toLowerCase();
		return content;
	}
	
	public static String stripFile(String File) {
		String collection="";
		org.jsoup.nodes.Document doc = Jsoup.parse(File);

//		Elements links = doc.select("a[href]");
		Elements head1 = doc.select("h1");
		Elements head2 = doc.select("h2");
		Elements head3 = doc.select("h3");
		Elements head4 = doc.select("h4");
		Elements paragraph = doc.select("p");
		
		for (Element head :head1){
			collection+=cleanString(head.html())+ " ";
			//System.out.println("H1 " + cleanString(head.html()) +" -> " + head.html());
		}
		for (Element head :head2){
			collection+=cleanString(head.html())+ " ";
			//System.out.println("H2 " + cleanString(head.html()) +" -> " + head.html());
		}
		for (Element head :head3){
			collection+=cleanString(head.html())+ " ";
			//System.out.println("H3 " + cleanString(head.html()) +" -> " + head.html());
		}
		for (Element head :head4){
			collection+=cleanString(head.html())+ " ";
			//System.out.println("H4 " + cleanString(head.html()) +" -> " + head.html());
		}
		for (Element p :paragraph){
			collection+=cleanString(p.html())+ " ";
			//System.out.println("P " + cleanString(p.html()) +" -> " + p.html());
		}		
	return collection;
}

	static IndexWriter CreateIndexWriter(String indexDirectory) throws IOException
	{
		File f = new File(indexDirectory);
		Directory indexDir = FSDirectory.open(f.toPath());
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriter indexWriter = new IndexWriter(indexDir, config);
		return indexWriter;
	}
	
	static void CloseIndexWriter(IndexWriter indexWriter) throws IOException
	{
		if(indexWriter.isOpen())
			indexWriter.close();
	}
	
	static void AddDocument(IndexWriter indexWriter, String URL, String Content, String snipit) throws IOException
	{
		Document doc = new Document();
		doc.add(new StringField("URL", URL, Field.Store.YES));
		doc.add(new StringField("Snipit", snipit, Field.Store.YES));
		doc.add(new TextField("content", Content ,Field.Store.NO));
		indexWriter.addDocument(doc);
	}
	
	

}
