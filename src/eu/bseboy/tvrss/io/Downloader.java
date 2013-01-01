package eu.bseboy.tvrss.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import eu.bseboy.tvrss.ShowDetails;
import eu.bseboy.tvrss.config.Configuration;

public class Downloader {

	private Configuration config;

	private void debug(String message)
	{
		System.out.println(message);
	}
	private void error(String message)
	{
		System.err.println(message);
	}
	
	private void moveProgressDisplay()
	{
		System.out.print(".");
	}
	
	private void endProgressDisplay()
	{
		System.out.println("]");
	}

	private void startProgressDisplay()
	{
		System.out.print("[");
	}
	
	public Downloader(Configuration configuration)
	{
		this.config = configuration;
	}
	
	
	/**
	 * Download from an input stream using the filename supplied
	 * @param inS
	 * @param filename
	 * @return
	 */
	public boolean downloadItem(InputStream inS, String filename) {
		// create dummy show object
		ShowDetails show = new ShowDetails();
		show.setShowName(filename);
		show.setEpisode(new Integer(0));
		show.setExtraInfo("");
		show.setSeries(new Integer(0));
		
		boolean success = false;
		
		try {
			success = download(inS, show);
		} catch (Exception e) {
			error("Unable to download file : " + filename);
			error(e.getMessage());
		}
		
		return success;
	}
	
	private boolean download(InputStream inS, ShowDetails matchedShow) throws Exception {
		boolean success = false;
		
		long totalBytes = 0;
		byte[] chunk = new byte[1024];  // read 1K at a time
		int bytesRead = 0;
		
		OutputStream[] outS = DownloadStreamFactory.createDownloadOutputStreams(config, matchedShow);
		
		// create flag to record error per stream
		boolean[] streamError = new boolean[outS.length];
		for (int i = 0; i < outS.length; i++) { streamError[i] = false; }
		
		startProgressDisplay();
		
		// read data in blocks, into array 'chunk'
		do
		{
			bytesRead = inS.read(chunk);
			// write each chunk to each output stream ...
			for (int i = 0; (bytesRead > 0) && (i < outS.length); i++)
			{
				try {
					// only write bytes if no error on this stream
					if (streamError[i] == false) {
						outS[i].write(chunk, 0, bytesRead);
					}
				}
				catch (IOException ioe)
				{
					// record that the output stream encountered an error
					streamError[i] = true;
					error("Error writing to stream " + ioe.getMessage());
				}
			}
			totalBytes += bytesRead;
			moveProgressDisplay();
		}
		while (bytesRead > 0);
		
		endProgressDisplay();
		
		debug("TOTAL BYTES READ : " + totalBytes);
		
		// close all output streams
		for (int i = 0; i < outS.length; i++)
		{
			try
			{
				outS[i].flush();
				outS[i].close();
			}
			catch (IOException ioe)
			{
				// record that the output stream encountered an error
				streamError[i] = true;
				error("Error closing stream " + ioe.getMessage());
			}
		}
		
		inS.close();
		
		// indicate it worked if any stream had no errors ...
		for (int i = 0; i < outS.length; i++)
		{
			if (streamError[i] == false) { success = true; }
		} 
		
		return success;
	}
	
	/**
	 * we will return true if ONE of the downloaders succeeded (no exceptions raised) ...
	 * @param itemURL
	 * @param matchedShow
	 * @return true if at least ONE of the downloads succeeded
	 */
	public boolean downloadItem(String itemURL, ShowDetails matchedShow)
	{
		boolean success = false;
		
		try {
			URL url = new URL(itemURL);
			
			// create the input stream
			InputStream inS = url.openStream();
			// and download from it
			success = download(inS, matchedShow);
			
		} catch (Exception e)
		{
			error("Failed to download item : " + itemURL);
			error(e.getMessage());
		}
		
		return success;
	}
	
}
