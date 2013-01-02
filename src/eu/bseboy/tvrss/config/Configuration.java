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

	private void debug(String message) {
		System.out.println(message);
	}
	
	private void error(String message) {
		System.err.println(message);
	}
	
	private void loadFeedProperty(String value) {
		feedList.addFeed(value);
		debug("Configured feed : " + value);
	}
	
	private void loadLocationProperty(String value) {
		downloadLocations.add(value);
		debug("Configured location : " + value);
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
	
	public boolean loadConfiguration(InputStream stream) {
		boolean success = false;
		
		Properties props = new Properties();
		
		// load properties file containing configuration
		try {
			props.load(stream);
			
			Enumeration<Object> keys = props.keys();
			
			// for each item in the properties file
			while (keys.hasMoreElements()) {
				String key = keys.nextElement().toString();
				String value = props.getProperty(key);
				
				if (key.startsWith("feed")) {
					loadFeedProperty(value);
				}
				
				if (key.startsWith("location")) {
					loadLocationProperty(value);
				}
			}
			
			success = true;
		} catch (IOException e) {
			error(e.getMessage());
		}
		
		return success;
	}
	
	public Iterator<String> getFeedIterator() {
		return feedList.iterator();
	}
	
	public Iterator<String> getDownloadLocations() {
		return downloadLocations.iterator();
	}
}
