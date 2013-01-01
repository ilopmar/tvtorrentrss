package eu.bseboy.tvrss;


public class ShowDetails {

	private String showName;
	private Integer series;
	private Integer episode;
	private String extraInfo;
	private String urlFromTitle;
	
	
	public String getUrlFromTitle() {
		return urlFromTitle;
	}
	public void setUrlFromTitle(String urlFromTitle) {
		this.urlFromTitle = urlFromTitle;
	}
	public String getShowName() {
		return showName;
	}
	public void setShowName(String showName) {
		this.showName = showName;
	}
	public Integer getSeries() {
		return series;
	}
	public void setSeries(Integer series) {
		this.series = series;
	}
	public Integer getEpisode() {
		return episode;
	}
	public void setEpisode(Integer episode) {
		this.episode = episode;
	}
	public String getExtraInfo() {
		return extraInfo;
	}
	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
}
