package eu.bseboy.tvrss.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class Configuration {

	private List<String> downloadLocations = new ArrayList<String>();
	private FeedList feedList = new FeedList();
	private SearchItemList itemList = new SearchItemList();

	private void debug(String message)
	{
		System.out.println(message);
	}
	private void error(String message)
	{
		System.err.println(message);
	}
	
	private void loadFeedProperty(String value)
	{
		feedList.addFeed(value);
		debug("Configured feed : " + value);
	}
	
	private void loadLocationProperty(String value)
	{
		downloadLocations.add(value);
		debug("Configured location : " + value);
	}
	
	private void loadSearchProperty(String value)
	{
		// split the property by comma ...
		String[] splitVals = value.split(",");
		
		if (splitVals.length == 6)
		{
			SearchItem item = new SearchItem();
			String show = splitVals[0];
			String extra = splitVals[1];
			String fSeries = splitVals[2];
			String fEp = splitVals[3];
			String uSeries = splitVals[4];
			String uEp = splitVals[5];
			
			if ((show != null) && (!show.equals("")) )
			{
				item.setShowName(splitVals[0]);
			}
			
			if ( (extra != null) && (!extra.equals("")) && (!extra.equals("-")) )
			{
				item.setExtraMatch(extra);
			}

			if ( (fSeries != null) && (!fSeries.equals("")) && (!fSeries.equals("-")) )
			{
				item.setSeriesFrom(Integer.valueOf(fSeries));
			}

			if ( (fEp != null) && (!fEp.equals("")) && (!fEp.equals("-")) )
			{
				item.setEpisodeFrom(Integer.valueOf(fEp));
			}

			if ( (uSeries != null) && (!uSeries.equals("")) && (!uSeries.equals("-")) )
			{
				item.setSeriesUpto(Integer.valueOf(uSeries));
			}

			if ( (uEp != null) && (!uEp.equals("")) && (!uEp.equals("-")) )
			{
				item.setEpisodeUpto(Integer.valueOf(uEp));
			}
			
			// add the new search item to the config
			itemList.addSearchItem(item);
			
			debug("Added search item : " + show + ", " + extra + ", " + fSeries + ", " + fEp + ", " + uSeries + ", " + uEp);
		}
		else
		{
			error("Search item should have 6 elements : " + value);
		}
	}
	
	public boolean loadConfiguration(String filename)
	{
		boolean success = false;
		
		try {
			InputStream inS;
			inS = new FileInputStream(filename);
			success = loadConfiguration(inS);
			inS.close();
		} 
		catch (IOException e) {
			error("Error loading configuration file " + e.getMessage());
		}
		
		return success;
	}
	
	public boolean loadConfiguration(InputStream stream)
	{
		boolean success = false;
		
		Properties props = new Properties();
		
		// load properties file containing configuration
		try
		{
			props.load(stream);
			
			Enumeration<Object> keys = props.keys();
			
			// for each item in the properties file
			while (keys.hasMoreElements())
			{
				String key = keys.nextElement().toString();
				String value = props.getProperty(key);
				
				if (key.startsWith("feed"))
				{
					loadFeedProperty(value);
				}
				
				if (key.startsWith("location"))
				{
					loadLocationProperty(value);
				}
				
				if (key.startsWith("search"))
				{
					loadSearchProperty(value);
				}
			}
			
			success = true;
		}
		catch (IOException e)
		{
			error(e.getMessage());
		}
		
		return success;
	}
	
	public boolean loadTestConfiguration()
	{
		downloadLocations.add("./");
		
		SearchItem searchItem = new SearchItem();
		
		searchItem.setShowName("family guy");
		//searchItem.setExtraMatch("hdtv");
		searchItem.setEpisodeFrom(new Integer(1));
		searchItem.setSeriesFrom(new Integer(4));
		//searchItem.setEpisodeUpto(new Integer(15));
		//searchItem.setSeriesUpto(new Integer(10));	
		itemList.addSearchItem(searchItem);
		
		searchItem = new SearchItem();
		searchItem.setShowName("heroes");
		searchItem.setExtraMatch("hdtv");
		searchItem.setEpisodeFrom(new Integer(1));
		searchItem.setSeriesFrom(new Integer(4));
		//searchItem.setEpisodeUpto(new Integer(15));
		//searchItem.setSeriesUpto(new Integer(10));	
		itemList.addSearchItem(searchItem);
		
		feedList.addFeed("http://www.mininova.org/rss.xml?cat=8");
		
		return true;
	}
	
	public Iterator<String> getFeedIterator()
	{
		return feedList.iterator();
	}
	
	public Iterator<SearchItem> getSearchItemIterator()
	{
		return itemList.iterator();
	}
	
	public Iterator<String> getDownloadLocations()
	{
		return downloadLocations.iterator();
	}
}
