package eu.bseboy.tvrss.dao;

public interface DownloadedDAO {

	public void recordDownload(String title, String url);
	public boolean previouslyDownloaded(String title, String url);
	public void shutdown();
	
}
