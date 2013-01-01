package eu.bseboy.tvrss.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import eu.bseboy.tvrss.ShowDetails;
import eu.bseboy.tvrss.config.Configuration;
import eu.bseboy.tvrss.io.email.EmailOutputStream;

public class DownloadStreamFactory {

	private static void debug(String message)
	{
		System.out.println(message);
	}
	@SuppressWarnings("unused")
	private static void error(String message)
	{
		System.err.println(message);
	}
	
	private static OutputStream createFileOutputStream(String location) throws IOException
	{
		File dir = new File(location);
		File tempFile = File.createTempFile("download_", ".torrent", dir);
		
		debug("Created output file : " + tempFile.getAbsolutePath());
		
		FileOutputStream fos = new FileOutputStream(tempFile);
		
		return fos;
	}
	
	private static OutputStream createEmailOutputStream(String emailAddress, ShowDetails show) throws IOException
	{
		debug("Creating email stream handler ...");
		OutputStream os = new EmailOutputStream(emailAddress, show);
		return os;
	}
	
	private static OutputStream createOutputStream(String location) throws IOException
	{
		// only handle files for now
		OutputStream os = null;

//		if (location.startsWith("email:"))
//		{
//			// email prefix, send the file to the specified email address
//			os = createEmailOutputStream(location.substring(6), show);
//		}
//		else {
			// default is to use a file location (no prefix)
			os = createFileOutputStream(location);			
//		}
		
		return os;
	}
	
	public static OutputStream[] createDownloadOutputStreams(Configuration conf) throws IOException
	{
		OutputStream[] osArray = null;
		int numLocs = 0;
		
		Iterator<String> locs = conf.getDownloadLocations();
		
		// count number of locations ...
		while(locs.hasNext())
		{
			numLocs++;
			locs.next();
		}
		osArray = new OutputStream[numLocs];
		
		numLocs = 0;
		locs = conf.getDownloadLocations();
		while(locs.hasNext())
		{
			osArray[numLocs] = createOutputStream(locs.next());
			numLocs++;
		}		
		
		return osArray;
	}
}
