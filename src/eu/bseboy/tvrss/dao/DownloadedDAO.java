package eu.bseboy.tvrss.dao;

import eu.bseboy.tvrss.ShowDetails;

public interface DownloadedDAO {

	public void recordDownload(ShowDetails matchedDetails);
	public boolean previouslyDownloaded(ShowDetails matchedDetails);
	public void shutdown();
	
}
