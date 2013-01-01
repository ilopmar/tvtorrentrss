package eu.bseboy.tvrss.dao;

public interface DownloadedDAO {

//	public void recordDownload(ShowDetails matchedDetails);
//	public boolean previouslyDownloaded(ShowDetails matchedDetails);
	public void recordDownload(String title, String url);
	public boolean previouslyDownloaded(String title, String url);
	public void shutdown();
	
}
