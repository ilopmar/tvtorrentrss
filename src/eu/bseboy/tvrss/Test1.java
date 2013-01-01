package eu.bseboy.tvrss;

public class Test1 {
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("Starting test1");

		RssDownloader downloader = new RssDownloader("tvrss.properties");
		downloader.runProcess();
		
		System.out.println("Finished test1");
	}
}
