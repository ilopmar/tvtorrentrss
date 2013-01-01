package eu.bseboy.tvemail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


import eu.bseboy.tvrss.config.Configuration;

public class EmailConfiguration extends Configuration {

	private static Logger logger = Logger.getLogger(EmailDownloader.LOGGER);
	
	private Map<String, EmailAccount> accounts = new HashMap<String, EmailAccount>();
	
	
	public Map<String, EmailAccount> getAccounts() {
		return accounts;
	}
	
	public void setAccounts(Map<String, EmailAccount> accounts) {
		this.accounts = accounts;
	}
	
	private static void error(String message) {
		logger.log(Level.SEVERE, message);
	}
	@SuppressWarnings("unused")
	private static void debug(String message) {
		logger.log(Level.FINE, message);
	}
	
	private static String getAccountId(String key) {
		String[] parts = key.split("\\.",2);
		return parts[0];
	}
	
	private static String getAccountProperty(String key) {
		String[] parts = key.split("\\.",2);
		return parts[1];		
	}
	
	private EmailAccount getOrCreateAccount(String accountId) {
		if (!accounts.containsKey(accountId)) {
			accounts.put(accountId, new EmailAccount(accountId));
		}
		return accounts.get(accountId);
	}
	
	private void loadEmailProperty(String key, String value) {
		
		String accountId = null;
		if (key != null) {
			accountId = getAccountId(key);
		}
		
		// we get get id from the key
		if (accountId != null) {
			// get matching account from the map, or create a new one
			EmailAccount account = getOrCreateAccount(accountId);
			
			String accountProp = getAccountProperty(key);
			
			if (accountProp != null) {
				// we have a property
				account.setProperty(accountProp, value);
			}
		}
	}
	
	private boolean loadEmailConfiguration(InputStream stream) {
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
				
				if (key.startsWith("email"))
				{
					loadEmailProperty(key, value);
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
	
	private boolean loadEmailConfiguration(String filename) {
		
		boolean success = false;
		
		try {
			InputStream inS;
			inS = new FileInputStream(filename);
			success = loadEmailConfiguration(inS);
			inS.close();
		} 
		catch (IOException e) {
			error("Error loading configuration file " + e.getMessage());
		}
		
		return success;		
	}
	
	@Override
	public boolean loadConfiguration(String filename) {
		boolean success = false;
		
		// load most of config using parent class
		success = super.loadConfiguration(filename);
		
		// load additional config details for e-mail retrieval
		if (success) {
			success = loadEmailConfiguration(filename);
		}
		
		return success;
	}

}
