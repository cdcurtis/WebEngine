//Should probably make error messages better. Stack trace necessary?
//Also need to make terminal output more meaningful, multiple threads kinda break that ------->Done (or good enough, at least)
//Errors:
//java.net.UnknownHostException
//org.jsoup.UnsupportedMimeTypeException
//java.net.SocketTimeoutException
//org.jsoup.HttpStatusException
//Error messages occasionally gets cut in two
//Gets 100 to threadno pages, due to threads. Easy fix, but does it matter? ------->Fixed
//I assume we can leave timeout alone... same with the others?
//To do:
//Robots.txt
//Extra duplicate check? e.g. sim hash?
//General optimizations... maybe
//Change threads to user input? Trivial and kinda nice, but probably a step back in optimization
import java.io.*;
import java.net.*;
import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.util.concurrent.*;
public class crawler {

	//Simple node class for use with BlockingQueue
	static public class Node{
		public String link;
		public Integer hops;
		Node(String s, Integer i){
			link=s;
			hops=i;
		}
	}

	//This function was pulled from the discussion slides and is not my own work.
	public static String downloadFile(Node n, BlockingQueue< Node > q, File out, int pageMax) {
		String url=n.link;
		try{
			Document doc = Jsoup.connect(url).get();

			Elements links = doc.select("a[href]");
			System.out.println(url + "\nLinks: " + links.size() + "\tHops: " + n.hops + "\n");

			for (Element link : links) {
				try {//things we dont want to pick up
					if(link.attr("abs:href").contains(".edu") && !
							( link.attr("abs:href").contains("xml")
							|| link.attr("abs:href").contains("mailto:")
							|| link.attr("abs:href").contains("jpg")
							|| link.attr("abs:href").contains("pdf")
							|| link.attr("abs:href").contains("tiff"))){
						Node temp = new Node(link.attr("abs:href"), n.hops+1);
						q.put(temp);
					}
					
				} catch (InterruptedException e) {
					System.out.println("Caught " + e );
					e.printStackTrace(); 
				}
			}

			//	 System.out.println("Done with links now saving file\n"); //Necessary? Threads don't like this
			int tempFN = fileno++;
			if( tempFN < pageMax ) { //Secondary check to see if pages have reached the max
				String filename = "file" + tempFN + ".dld";

				BufferedWriter fos= new BufferedWriter( new FileWriter(out.getAbsolutePath() + "/" + filename));
				fos.write(doc.toString() + "\n");
				fos.close();

				return filename;
			}
		}
		catch(MalformedURLException e){
			System.out.println("Caught bad URL at"+ url+" " + e);
			e.printStackTrace();
		
		} 
		catch (IOException e) {
			System.out.println("Caught exception on"+ url+" " + e);
			e.printStackTrace();
			
		}
		

		return null;
	}

	//This function tests if the input string is an integer
	public static boolean isInt(String s) {
		try { Integer.parseInt(s); }
		catch(NumberFormatException err) { return false; }
		return true;
	}

	//Simple error message function
	public static void perr(String s) {
		System.out.println(s);
		System.exit(0);
	}

	public static void main(String [] args) throws MalformedURLException, IOException
	{
		double sTime = System.nanoTime();
		
		if( args.length != 4 ) //Check for correct number of arguments
			perr("Correct format: java crawler <seed-File> <num-pages> <hops-away> <output-dir>");

		if( !isInt(args[1]) || !isInt(args[2]) ) //Check that args 2 and 3 are ints
			perr("num-pages and hops-away must be integers");

		//Parse arguments
		String seed = args[0];
		final int pageMax = Integer.parseInt(args[1]);
		final int hopMax = Integer.parseInt(args[2]);
		String out = args[3];

		File input = new File (seed);
		input.createNewFile();
		if(!input.canRead()) //Check that file exists and can be opened
			perr("Could not open " + seed);

		//Check if output already exists, create if not
		final File outDir = new File(out);
		if (!outDir.exists())
			if(!outDir.mkdir())
				perr("Could not create directory");

		String fPath = outDir.getAbsolutePath() + "/index.txt";
		final BufferedWriter output = new BufferedWriter( new FileWriter( fPath ) );
		Scanner s = null;

		final ConcurrentMap< String, Integer > map = new ConcurrentHashMap< String, Integer >();	//<link, #hops>
		final BlockingQueue< Node > q = new LinkedBlockingQueue< Node >();

		class CrawlThread extends Thread {
			public int tNum;	//Thread Number, for debugging (previously for sleep time)

			CrawlThread(int i) {
				tNum = i;
			}

			public void run() {	//Main thread function
				while(q.isEmpty() && currentPages < pageMax) { //Sleep and try again if queue is empty at start, end at when pageMax is reached
					try {
						sleep(100);	//.1s
					} catch (InterruptedException e) {
						System.out.println("Thread was interrupted");
						e.printStackTrace();
					}
				}

				try {
					while(!q.isEmpty() && currentPages < pageMax) //Crawl
					{
						Node readURL = q.take();
						if(RobotExclusionUtil.robotsShouldFollow(readURL.link)){
							if(map.putIfAbsent(readURL.link, readURL.hops) == null){
								if( map.get(readURL.link) <= hopMax ) {
									String newFile= downloadFile(readURL, q, outDir, pageMax);

									if( newFile == null )
										break;

									output.write(readURL.link + " -> " + newFile + "\n");
									//	 output.write(readURL.link + " -> " + newFile + "\tThread#" + tNum + "\n"); //Debugging version
									output.flush();
									++currentPages;
								}
							}
						}
					}
				} catch (FileNotFoundException e) {
					System.out.println("File "+ " Not Found!");
				} catch (InterruptedException e) {
					System.out.println("Caught " + e );
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Caught1 " + e );
					e.printStackTrace();
				}
			}
		}

		try { //Parse seed links
			s = new Scanner(input);

			while(s.hasNext()) {
				String temp = s.next();
				Node n = new Node(temp, 0);
				try { q.put(n); }
				catch(InterruptedException e) { 
					System.out.println("Caught " + e ); 
					e.printStackTrace();
				}
			}
		}
		finally { //Close seed file
			if (s != null)
				s.close();
		}

		List< CrawlThread > t = new ArrayList< CrawlThread >();

		for(int i = 0; i < threadno; ++i) {	//Start threads
			t.add( new CrawlThread( i ) );
			t.get(i).start();
		}

		for(int i = 0; i < threadno; ++i) {	//Wait for threads to finish
			try {
				t.get(i).join();
			} catch (InterruptedException e) {
				System.out.println("Thread was interrupted");
				e.printStackTrace();
			}
		}

		output.close();
		System.out.println("DONE!");

		double eTime = System.nanoTime();
		double seconds = (eTime - sTime) / 1000000000;
		System.out.println("Run time: " + seconds + " seconds");
	}

	static int fileno = 0;
	static int currentPages = 0;
	static int threadno = 20;
}