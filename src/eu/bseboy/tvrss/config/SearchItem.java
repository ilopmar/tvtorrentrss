package eu.bseboy.tvrss.config;

/**
 * represents an item the user wants to search for
 * 
 * @author rnewton
 *
 */
public class SearchItem {

	private String showName;  // matched to the show name
	private String extraMatch; // matched to the extra info
	private Integer seriesFrom; 
	private Integer seriesUpto;
	private Integer episodeFrom;
	private Integer episodeUpto;
	
	
	public SearchItem()
	{
		super();
	}
	
	public SearchItem(String showName, String extraMatch, Integer seriesFrom, Integer seriesUpto, Integer episodeFrom, Integer episodeUpto)
	{
		this();
		this.showName = showName;
		this.extraMatch = extraMatch;
		this.seriesFrom = seriesFrom;
		this.seriesUpto = seriesUpto;
		this.episodeFrom = episodeFrom;
		this.episodeUpto = episodeUpto;
	}
	
	public String getShowName() {
		return showName;
	}
	public void setShowName(String showName) {
		this.showName = showName;
	}
	public String getExtraMatch() {
		return extraMatch;
	}
	public void setExtraMatch(String extraMatch) {
		this.extraMatch = extraMatch;
	}
	public Integer getSeriesFrom() {
		return seriesFrom;
	}
	public void setSeriesFrom(Integer seriesFrom) {
		this.seriesFrom = seriesFrom;
	}
	public Integer getSeriesUpto() {
		return seriesUpto;
	}
	public void setSeriesUpto(Integer seriesUpto) {
		this.seriesUpto = seriesUpto;
	}
	public Integer getEpisodeFrom() {
		return episodeFrom;
	}
	public void setEpisodeFrom(Integer episodeFrom) {
		this.episodeFrom = episodeFrom;
	}
	public Integer getEpisodeUpto() {
		return episodeUpto;
	}
	public void setEpisodeUpto(Integer episodeUpto) {
		this.episodeUpto = episodeUpto;
	}
	
	
}
