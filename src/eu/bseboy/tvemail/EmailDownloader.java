package eu.bseboy.tvemail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import eu.bseboy.tvrss.io.Downloader;

public class EmailDownloader {
	
	public static String LOGGER = "EmailDownloader";

	private static Logger logger = Logger.getLogger(EmailDownloader.LOGGER);
	
	private EmailConfiguration config = null;

	private static void error(String message) {
		logger.log(Level.SEVERE, message);
		System.out.println("ERROR : " + message);
	}
	private static void debug(String message) {
		logger.log(Level.FINE, message);
		System.out.println("DEBUG : " + message);
	}
	
	private Properties genJavaMailProps(EmailAccount account) {
		// list all the properties
		Enumeration<Object> keys = account.getProperties().keys();
		while (keys.hasMoreElements())
		{
			String key = (String)keys.nextElement();
			String value = account.getProperty(key);
			debug(key.toString() + " = " + value);
		}
		return account.getProperties();
	}
	
	private void downloadPart(BodyPart part) {
		
		boolean success = false;
		
		try {
			// get the filename
			String filename = part.getFileName();
			// get an input stream from the part
			InputStream inS = part.getInputStream();
			// create a downloader from our configuration
			Downloader dl = new Downloader(config);
			// try and download the attachment
			success = dl.downloadItem(inS, filename); 
			
			if (success) {
				debug("Successfully downloaded " + filename);
			}
		} catch (Exception e) {
			error("Exception in downloadPart()");
			error(e.getMessage());
		}
	}
	
	private void processMultipart(MimeMultipart parts) throws IOException, MessagingException {
		
		int numParts = parts.getCount();
		
		// for each part
		for (int p = 0; p < numParts; p++) {
			BodyPart part = parts.getBodyPart(p);
			
			// if the part we have found is itself a multi-part item, recurse ...
			if ( part.isMimeType("multipart/*") ) {
				Object content = part.getContent();
				if (content instanceof MimeMultipart) {
					processMultipart((MimeMultipart)content);
				}
			} else {
				// not a multi-part item ... and not a text item
				if (!part.isMimeType("text/*")) {
					String filename = part.getFileName();
					String mimeType = part.getContentType();
					debug("Item " + filename + " of type " + mimeType);
					
					// is this attachment a torrent ?
					if ( (filename != null) && (filename.toLowerCase().endsWith(".torrent"))) {
						debug("Torrent file detected - now downloading");
						downloadPart(part);
					}
				}
			}
		}
	}
	
	private void processMessage(Message msg) throws IOException, MessagingException {
		Object content = msg.getContent();
		if ( (content != null) && (content instanceof MimeMultipart) ) {
			debug("Message is a multipart item ...");
			processMultipart((MimeMultipart)content);
		} else {
			debug("Message is not multipart ...");
		}
		
		
	}
	
	private void processAccount(EmailAccount account) {
		// now we have an account, we need to create a session
		// and download all messages
		// for each .torrent attachment, we save this to 1+ outputstreams
		// defined in the config file
		
		// 1. setup mail properties from account object
		Properties props = genJavaMailProps(account);
		
		// 2. create a session
		Session sess = Session.getInstance(props);
		
		if (sess != null) {
			// proceed if we have a mail session ...
			try {
				// connect to the message store + folder ...
				Store store = sess.getStore();
				store.connect(account.getProperty("username"), account.getProperty("password"));
				
				Folder folder = store.getFolder("INBOX");
				folder.open(Folder.READ_ONLY);
				
				if (folder.isOpen()) {
					debug("Folder name : " + folder.getFullName() + "  Count : " + folder.getMessageCount());
					
					// retrieve the messages
					Message[] messages = folder.getMessages();
					
					// loop through all messages ...
					for (int m = 0; (messages != null) && (m < messages.length); m++) {
						processMessage(messages[m]);
					}
				} // folder open
				
				folder.close(false);  // don't delete read messages
				// close when done
				store.close();
			} catch (NoSuchProviderException e) {
				error(e.getMessage());
				e.printStackTrace();
			} catch (MessagingException e) {
				error(e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				error(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public void runProcess() {
		
		if ( (config != null) && (config.getAccounts() != null) ) {
			// loop through all configured accounts ...
			Iterator<Entry<String, EmailAccount>> accountIter = config.getAccounts().entrySet().iterator();
			while (accountIter.hasNext()) {
				EmailAccount account = accountIter.next().getValue();
				
				processAccount(account);
			}
		}
	}
	
	
	public EmailDownloader(String configFilename) {
		EmailConfiguration config = new EmailConfiguration();
		config.loadConfiguration(configFilename);
		this.config = config;
		debug("Loaded configuration from file " + configFilename);		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		logger.setLevel(Level.ALL);
		
		EmailDownloader dl = new EmailDownloader("tvrss.properties.home");
		dl.runProcess();
		debug("Completed");
	}

}
