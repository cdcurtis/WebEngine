package edu.ucr.files;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ccil.cowan.tagsoup.jaxp.SAXParserImpl;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ParseFiles {

	
	/**
	 * 
	 * @param desiredFileSize - in MB
	 */
	public ParseFiles(int desiredFileSize) {
		
	}
	
	public void readFile(String path) {
		
		try {
			
			File file = new File(path);
			

			SAXParserImpl parser = SAXParserImpl.newInstance(null);
			parser.parse(file, new DefaultHandler() );
			parser.
			
			TagNode tagNod = new HtmlCleaner().clean(file);
			DomSerializer ser = new DomSerializer(new CleanerProperties());
			Document doc = ser.createDOM(tagNod);
			System.out.println(doc);
			
			/*
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			Document document = db.parse(file);
			
			System.out.println(document.toString());
			*/
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void joinFiles() {
		
	}
	
	public String htmlToRawString(String html) {
		String text = Jsoup.clean(html, Whitelist.basic());
		return text;
		
	}
	
	
	
	private long getFileSizeInMB(File f) {
		return f.length()/1024;
	}
}
