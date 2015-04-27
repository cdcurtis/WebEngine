package edu.ucr.files;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.handler.Handler;

import org.ccil.cowan.tagsoup.jaxp.SAXParserImpl;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.safety.Whitelist;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ParseFiles {

	class Handler extends DefaultHandler {
	    private StringBuilder sb = new StringBuilder();
	    private boolean keep = true;
	    public void characters(char[] ch, int start, int length)
	            throws SAXException {
	        if (keep) {
	        	String line = new String(ch);
	        	//System.out.println("line = " + line);
	        	if (!line.contains("CDATA")) {
	        		sb.append(ch, start, length);
	        	}
	            
	        }
	    }
	    public String getText() {
	        return sb.toString();
	    }
	    public void startElement(String uri, String localName, String qName,
	            Attributes atts) throws SAXException {
	        if (localName.equalsIgnoreCase("script")) {
	            keep = false;
	        } 
	    }
	    public void endElement(String uri, String localName, String qName)
	            throws SAXException {
	        keep = true;
	    }
	}
	
	private long desiredFileSize;
	
	/**
	 * 
	 * @param desiredFileSize - in MB
	 */
	public ParseFiles(int desiredFileSize) {
		this.desiredFileSize = desiredFileSize;
	}
	
	public void readFile(String path) {
		
		try {
			
			File file = new File(path);
			

			SAXParserImpl parser = SAXParserImpl.newInstance(null);
			Handler h = new Handler();
			parser.parse(file, h);
			System.out.println(h.getText());
			
			/*
			TagNode tagNod = new HtmlCleaner().clean(file);
			DomSerializer ser = new DomSerializer(new CleanerProperties());
			Document doc = ser.createDOM(tagNod);
			System.out.println(doc);
			*/
			/*
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			Document document = db.parse(file);
			
			System.out.println(document.toString());
			*/
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
	
	public String stripCDATA(File path) {
		String result = "";
		try {
			File file = path;

			SAXParserImpl parser = SAXParserImpl.newInstance(null);
			Handler h = new Handler();
			parser.parse(file, h);
			return h.getText();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	
	
	private long getFileSizeInMB(File f) {
		return f.length()/1024;
	}
	
	private boolean isFileUnderSizeLimit(File f) {
		return getFileSizeInMB(f) < this.desiredFileSize;
	}
}
