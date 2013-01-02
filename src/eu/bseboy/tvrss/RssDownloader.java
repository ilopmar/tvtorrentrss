package eu.bseboy.tvrss;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import eu.bseboy.tvrss.config.Configuration;
import eu.bseboy.tvrss.dao.DownloadedDAO;
import eu.bseboy.tvrss.dao.DownloadedDAOImpl;
import eu.bseboy.tvrss.io.Downloader;

public class RssDownloader {
	
	protected static final Log log = LogFactory.getLog(RssDownloader.class);

	private static final String defaultConfig = "./tvrss.properties";

	Configuration conf;

	public RssDownloader(String configFilename) {
		conf = loadConfiguration(configFilename);
	}

	/**
	 * load configuration from file name
	 * @param file
	 * @return
	 */
	private Configuration loadConfiguration(String file) {
		Configuration config = new Configuration();

		config.loadConfiguration(file);

		return config;
	}

	@SuppressWarnings("unchecked")
	public void runProcess() throws Exception {
		DownloadedDAO dao = new DownloadedDAOImpl();

		Downloader downloader = new Downloader(conf);

		Iterator<String> feeds = conf.getFeedIterator();

		// for each feed in the configuration ...
		while (feeds.hasNext()) {
			String feedURL = feeds.next();
			log.debug("Feed URL - " + feedURL);

			try {
				SyndFeedInput feedIn = new SyndFeedInput();
				SyndFeed feed = feedIn.build(new XmlReader(new URL(feedURL)));
				log.debug("Feed Type : " + feed.getFeedType());

				// for each item in the current feed ....
				List<SyndEntry> entries = (List<SyndEntry>)feed.getEntries();
				if (entries != null) {
					Iterator<SyndEntry> iter = entries.iterator();
					while (iter.hasNext()) {
						// get item details ...
						SyndEntry entry = iter.next();

						log.debug("======================================================================");
						log.debug("Title: " + entry.getTitle());

						List<SyndEnclosure> enclosures = (List<SyndEnclosure>)entry.getEnclosures();
						SyndEnclosure enclosure = enclosures.get(0);
						String url = enclosure.getUrl();

						boolean downloaded = dao.previouslyDownloaded(entry.getTitle(), url);

						if (downloaded) {
							log.debug("PREVIOUSLY DOWNLOADED");
						} else {
							log.debug("DOWNLOADING : " + url);
							downloaded = downloader.downloadItem(url);

							if (downloaded) {
								// record this download ...
								dao.recordDownload(entry.getTitle(), url);
								log.debug("SUCCESS - FLAGGED AS DOWNLOADED");
							} else {
								log.error("FAILED TO DOWNLOAD ITEM");
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("Failed to load feed " + feedURL + " : " + e.getMessage());
			}
		}

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
	}
}
