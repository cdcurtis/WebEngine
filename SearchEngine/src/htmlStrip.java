import java.io.*;
import java.net.*;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class htmlStrip {
	
	
	static public class Node{
		public String link;
		public Integer termF;
		Node(String s, Integer i){
			link=s;
			termF=i;
		}
		Node(){
			link=null;
			termF=null;
		}
	}
	
	public static void main(String [] args) throws MalformedURLException, IOException
	{
		
		File htmlFiles=new File("htmlFiles");
		File [] html=htmlFiles.listFiles();
		for(int i=0;i<html.length;++i){
			
			String fileName= html[i].toString();
			System.out.print("Starting: "+ fileName + " ");
			if(!fileName.matches("index.txt")){
				String doc=getfile(fileName);
				String content=stripFile(doc);
				content= cleanContent(content);
				addToMap(fileName, content,termFeqMap);
			}
			System.out.println("Finished: "+ fileName);
			
		}

		printMap(termFeqMap);



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
	

	
	
	public static String stripFile(String File) {
			String collection="";
			Document doc = Jsoup.parse(File);

//			Elements links = doc.select("a[href]");
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

	private static void printMap(HashMap<String, ArrayList<Node>> map) {
		for(Map.Entry<String,ArrayList<Node>> entry : map.entrySet()){
			System.out.print(entry.getKey());
			for(int i=0;i<entry.getValue().size();++i){
				System.out.print("( "+ entry.getValue().get(i).link + ", "+ entry.getValue().get(i).termF + ") ");	
			}
			System.out.print("\n");
		}
		
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
	
	public static void addToMap(String fileName, String Content, HashMap <String,ArrayList<Node>> map){
		String[] words = Content.split("\\s+");
		Boolean flag=false;
		for(int i=0;i<words.length;++i){
			//System.out.println(i+ ": "+ words[i]);
			if(map.containsKey(words[i])){//if term frequency exists see if doc exists
				ArrayList<Node> temp=map.get(words[i]);
				for(int j=0;j<temp.size();++j){
					flag=false;
					if(temp.get(j).link==fileName){
						flag=true;
						temp.get(j).termF++;
						map.remove(words[i]);
						map.put(words[i],temp);
					}
				}
				if(flag==false){
					temp.add(new Node(fileName,1));
					map.remove(words[i]);
					map.put(words[i],temp);
				}
			}
			else{//creates new term Frequency list for new word found.
				ArrayList<Node> indexList =new ArrayList<Node>();
				indexList.add(new Node(fileName,1));
				map.put(words[i], indexList);
			}
		}
	}
	
	
	

	static HashMap <String,ArrayList<Node>> termFeqMap= new HashMap <String,ArrayList<Node>> ();
}
//map <String ,Pair<String,Integer>> tfMap=new HashMap <String, Pair <String,Integer>>();
