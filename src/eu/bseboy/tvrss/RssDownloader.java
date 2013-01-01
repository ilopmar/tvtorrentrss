package eu.bseboy.tvrss;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import eu.bseboy.tvemail.EmailDownloader;
import eu.bseboy.tvrss.config.Configuration;
import eu.bseboy.tvrss.config.SearchItem;
import eu.bseboy.tvrss.dao.DownloadedDAO;
import eu.bseboy.tvrss.dao.DownloadedDAOImpl;
import eu.bseboy.tvrss.dao.DownloadedDAOImpl2;
import eu.bseboy.tvrss.io.Downloader;
import eu.bseboy.tvrss.matching.EpisodeMatcher;

public class RssDownloader {

	private static final String defaultConfig = "./tvrss.properties";
	
	Configuration conf;
	
	public RssDownloader(String configFilename)
	{
		conf = loadConfiguration(configFilename);
	}
	
	/**
	 * load configuration from file name
	 * @param file
	 * @return
	 */
	private Configuration loadConfiguration(String file)
	{
		Configuration config = new Configuration();
		
		config.loadConfiguration(file);
		
		return config;
	}

	/**
	 * Generate an item that uniqiely identifies the show we matched
	 * based on the criteria entered PLUS the specific episode matched 
	 * @param show
	 * @param item
	 * @return 
	 */
	private ShowDetails createMatchedDetails(ShowDetails show, SearchItem item)
	{
		ShowDetails matchedDetails = new ShowDetails();
		// use the name / extra info from the search
		matchedDetails.setShowName(item.getShowName());
		matchedDetails.setExtraInfo(item.getExtraMatch());
		// set episode / series from the actual show 
		matchedDetails.setEpisode(show.getEpisode());
		matchedDetails.setSeries(show.getSeries());
		
		return matchedDetails;
	}
	
	private void debug(String message)
	{
		System.out.println(message);
	}
	private void error(String message)
	{
		System.err.println(message);
	}	
	
	@SuppressWarnings("unchecked")
	public void runProcess() throws Exception
	{
		EpisodeMatcher matcher = new EpisodeMatcher();
		DownloadedDAO dao = new DownloadedDAOImpl2();
				
		Downloader downloader = new Downloader(conf);
		
		Iterator<String> feeds = conf.getFeedIterator();
		
		// for each feed in the configuration ...
		while (feeds.hasNext())
		{
			String feedURL = feeds.next();
			debug("Feed URL - " + feedURL);

			try {
						
				SyndFeedInput feedIn = new SyndFeedInput();
				SyndFeed feed = feedIn.build(new XmlReader(new URL(feedURL)));
				debug("Feed Type : " + feed.getFeedType());
		
				// for each item in the current feed ....
				List<SyndEntry> entries = (List<SyndEntry>)feed.getEntries();
				if (entries != null) {
					Iterator<SyndEntry> iter = entries.iterator();
					while (iter.hasNext())
					{
						// get item details ...
						SyndEntry entry = iter.next();
						
						debug("======================================================================");
						debug("Title: " + entry.getTitle());
						
//						ShowDetails show = matcher.deduceShowDetails(entry.getTitle());
//						
//						// for each search item in the configuration ...
//						Iterator<SearchItem> items = conf.getSearchItemIterator();				
//						while (items.hasNext())
//						{
//							SearchItem searchItem = items.next();
							
							//boolean matches = matcher.matchesSearch(searchItem, show);
							//if (matches) {
								
								// the item matches the episode / show filter
								// now check if it has been downloaded before
								//ShowDetails matchedDetails = createMatchedDetails(show, searchItem);
							
								List<SyndEnclosure> enclosures = (List<SyndEnclosure>)entry.getEnclosures();
								SyndEnclosure enclosure = enclosures.get(0);
								String url = enclosure.getUrl();
								
								boolean downloaded = dao.previouslyDownloaded(entry.getTitle(), url);
								
								if (downloaded)
								{
									debug("PREVIOUSLY DOWNLOADED");
								}
								else 
								{
									// we may have URL in show object if eztv item ...
									//String url = show.getUrlFromTitle();
									//if (url == null) {
									
									/**
										// we now need to get the download link
										List<SyndEnclosure> enclosures = (List<SyndEnclosure>)entry.getEnclosures();
										SyndEnclosure enclosure = enclosures.get(0);
										String url = enclosure.getUrl();
										
										**/
									//}
									
									debug("DOWNLOADING : " + url);
									downloaded = downloader.downloadItem(url);
									
									if (downloaded)
									{
										// record this download ...
										dao.recordDownload(entry.getTitle(), url);
										debug("SUCCESS - FLAGGED AS DOWNLOADED");
									}
									else
									{
										error("FAILED TO DOWNLOAD ITEM");
									}
								} // not downloaded before
							//} //  if (matches)
						//} // search item iterator
					} // rss item iterator
				} // rss entries not null
			} // try block
			catch (Exception e)
			{
				error("Failed to load feed " + feedURL + " : " + e.getMessage());
			}
		} // feed iterator
		
		dao.shutdown();
		
	}
	
	/**
	 * main entry point for running the application
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String filename = defaultConfig;
		
		if (args.length > 0) {
			filename = args[0];
		}
		
		RssDownloader downloader = new RssDownloader(filename);
		downloader.runProcess();
		
//		EmailDownloader eDownloader = new EmailDownloader(filename);
//		eDownloader.runProcess();
	}
}
