package eu.bseboy.tvrss.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import eu.bseboy.tvrss.ShowDetails;
import eu.bseboy.tvrss.config.SearchItem;


public class EpisodeMatcher {

	private static final String EZTV_PREFIX = "eztv_it: ";
	private static final String LINK_PREFIX = " http";
	
	// regular expression for a single episode
	private static final String singleEpRegExp = "([ ]|[\\.])(([S][0-9]+[E][0-9]+)|([0-9]+[x][0-9]+))([ ]|[\\.])";
	private Pattern pattern;
	
	private static final String notNumberRegExp = "([^0-9]+)";
	private Pattern numPattern;
	
	public EpisodeMatcher(String regExp) {
		super();
		pattern = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
		numPattern = Pattern.compile(notNumberRegExp);
	}
	
	public EpisodeMatcher()
	{
		this(singleEpRegExp);
	}

	private void debug(String message)
	{
		System.out.println(message);
	}
	@SuppressWarnings("unused")
	private void error(String message)
	{
		System.err.println(message);
	}
	
	public ShowDetails deduceShowDetails(String fullItemTitle)
	{
		// remove 'eztv_it: ' from start if present, etc
		String itemTitle = cleanupItemTitle(fullItemTitle);
		
		ShowDetails show = null;
		String name;
		String serEpStr;
		Integer series;
		Integer episode;
		String extra;
		
		String[] sections = pattern.split(itemTitle,2);
		if (sections.length == 2)
		{
			int itemLen = itemTitle.length();
			
			// correct number of sections ...
			name = sections[0];
			int nameLen = name.length();
			
			extra = sections[1];
			int extraLen = extra.length();
			
			serEpStr = itemTitle.substring(nameLen + 1, itemLen - (extraLen + 1) );  // gets the series + episode, eg S01E12, or 6x1
			
			// split out the number sections
			String[] numSections = numPattern.split(serEpStr, 3);
			if (numSections.length >= 2)
			{
				// last 2 sections are the series and episode numbers ...
				episode = new Integer(numSections[numSections.length - 1]);
				series = new Integer(numSections[numSections.length - 2]);
				
				// we have a full set of details ... create a show object
				show = new ShowDetails();
				show.setShowName(name);
				show.setExtraInfo(extra);
				show.setSeries(series);
				show.setEpisode(episode);
				show.setUrlFromTitle(extractURLFromTitle(fullItemTitle));
			}
		}

		return show;
	}
	
	/**
	 * if title contains a URL, extract it
	 * if not, return null
	 * @param fullItemTitle
	 * @return
	 */
	private String extractURLFromTitle(String fullItemTitle) {
		int i = fullItemTitle.indexOf(LINK_PREFIX);
		if (i > 0) {
			return fullItemTitle.substring(i+1);
		}
		else {
			return null;
		}
	}

	/**
	 * strip EZTV prefix off title
	 * @param fullItemTitle
	 * @return
	 */
	private String cleanupItemTitle(String fullItemTitle) {
		if (fullItemTitle.startsWith(EZTV_PREFIX)) {
			return fullItemTitle.substring(EZTV_PREFIX.length());
		}
		else {
			return fullItemTitle;
		}
	}

	/**
	 * take a search string, split into OR'ed sections, each with a StringMatch clause ...
	 * @param sections
	 * @return
	 */
	private Clause buildOrClause(String andSection, String searchIn) {
		
		List<Clause> stringClauseList = new ArrayList<Clause>();
		
		String[] orSections = andSection.split("\\|");
		for (int i = 0; i < orSections.length; i++) {
			String toMatch = orSections[i];
			// dont create clauses for empty sections
			if (!toMatch.equals("")) {
				stringClauseList.add(new StringMatch(toMatch, searchIn));
			}
		}
		
		return new OrJoin(stringClauseList);
	}
	
	/**
	 * 
	 * @param searchItem
	 * @return
	 */
	private Clause buildAndClause(String searchItem, String searchIn) {
			
		List<Clause> orClauseList = new ArrayList<Clause>();

		// split the search item into and section
		String[] andSections = searchItem.split("&");
		// for each section split by & ....
		for (int i = 0; i < andSections.length; i++) {
			// split the & section into | sections, returning a list of them
			Clause orClause = buildOrClause(andSections[i], searchIn);
			orClauseList.add(orClause);
		}
		
		return new AndJoin(orClauseList);
	}
	
	/**
	 * returns TRUE if the show matches the search parameters
	 * @param searchItem
	 * @param show
	 * @return
	 */
	public boolean matchesSearch(SearchItem searchItem, ShowDetails show)
	{
		boolean matches = false;
		
		if ( (show != null) && (searchItem != null) )
		{
			boolean showMatch = false;
			boolean extraMatch = false;
			boolean fromMatch = false;
			boolean uptoMatch = false;

			// 1. match the show name (case insensitive)
			// check that the show has a name and the search params have a show name ...
			if ( (show.getShowName() != null) && (searchItem.getShowName() != null) )
			{
				String showName = show.getShowName().toUpperCase();
				String srchShowName = searchItem.getShowName().toUpperCase();
				
				Clause showClause = buildAndClause(srchShowName, showName);
				if ( (showClause != null) && (showClause.evaluate() == true) )
				{
					// show name contains search string
					showMatch = true;
				}
			}
			
			// 2. extra details match
			//    - return true if no search on extra details
			//    - return true if search extra string is in show extra string (case insensitive)
			if ( searchItem.getExtraMatch() == null )
			{
				// not searching on extra
				extraMatch = true;
			} else {
				if (show.getExtraInfo() != null)
				{
					// we are searching by extra, and the show has extra details
					String showExtra = show.getExtraInfo().toUpperCase();
					String srchExtra = searchItem.getExtraMatch().toUpperCase();

					Clause xtraClause = buildAndClause(srchExtra, showExtra);
					if ( (xtraClause != null) && (xtraClause.evaluate() == true) )
					{
						extraMatch = true;
					}
				}
			}
			
			// 3. match from series and episodes
			//    - return true if seriesFrom and epispodeFrom search items are null
			//    - true if seriesFrom < showSeries
			//    - true if (seriesFrom = showSeries) AND (episodeFrom <= showEpisode)
			//    - true if (seriesFrom is null) AND (episodeFrom <= showEpisode)
			//    - true if (seriesFrom = showSeries) AND (episodeFrom is null)
			Integer seriesFrom = searchItem.getSeriesFrom();
			Integer episodeFrom = searchItem.getEpisodeFrom();
			Integer showSeries = show.getSeries();
			Integer showEpisode = show.getEpisode();
			
			if ( (seriesFrom == null) && (episodeFrom == null) )
			{
				// no search on series or episode
				fromMatch = true;
			} else {
				// one or more of episode and series searched for
				// is the show series > search from criteria
				if ( (seriesFrom != null) && ( showSeries.compareTo(seriesFrom) > 0 ) ) 
				{
					fromMatch = true;
				} else {
					if  ( ( (seriesFrom == null) || (seriesFrom.equals(showSeries)) ) && ( (episodeFrom == null) || (showEpisode.compareTo(episodeFrom) >= 0) ) )
					{
						// final 3 match scenarios
						fromMatch = true;
					}
				}
			}

			// 4. match UPTO series and episodes
			//    - return true if seriesUpto and epispodeUpto search items are null
			//    - true if showSeries < seriesUpto 
			//    - true if (seriesFrom = showSeries) AND (showEpisode <= episodeFrom)
			//    - true if (seriesFrom is null) AND (showEpisode <= episodeFrom)
			//    - true if (seriesFrom = showSeries) AND (episodeFrom is null)
			Integer seriesUpto = searchItem.getSeriesUpto();
			Integer episodeUpto = searchItem.getEpisodeUpto();
			
			if ( (seriesUpto == null) && (episodeUpto == null) )
			{
				// no search on series or episode
				uptoMatch = true;
			} else {
				// one or more of episode and series searched for
				// is the show series > search from criteria
				if ( (seriesUpto != null) && ( showSeries.compareTo(seriesUpto) < 0 ) ) 
				{
					uptoMatch = true;
				} else {
					if  ( ( (seriesUpto == null) || (seriesUpto.equals(showSeries)) ) && ( (episodeUpto == null) || (showEpisode.compareTo(episodeUpto) <= 0) ) )
					{
						// final 3 match scenarios
						uptoMatch = true;
					}
				}
			}
			
			matches = (showMatch & extraMatch & fromMatch & uptoMatch);
		}
		
		return matches;
	}
	
	public static void mainOld(String[] args) {
		EpisodeMatcher m = new EpisodeMatcher();
		Clause c1a = m.buildAndClause("house&~desperate", "Desperate Housewives");
		Clause c1b = m.buildAndClause("house&desperate", "Desperate Housewives");
		Clause c1c = m.buildAndClause("house&desperate", "Desperate Wousehives");
		Clause c1d = m.buildAndClause("house|desperate", "Housewives");
		Clause c2 = m.buildAndClause("dext|exter&mouse", "dexter");
		Clause c3 = m.buildAndClause("dext|exter&mouse", "mouse");
		Clause c4 = m.buildAndClause("dext|exter&mouse", "mousedext");
		Clause c5 = m.buildAndClause("simple", "some simple text");
		m.debug("c1a : " + c1a.evaluate());
		m.debug("c1b : " + c1b.evaluate());		
		m.debug("c1c : " + c1c.evaluate());
		m.debug("c1d : " + c1d.evaluate());
		m.debug("c2 : " + c2.evaluate());
		m.debug("c3 : " + c3.evaluate());
		m.debug("c4 : " + c4.evaluate());
		m.debug("c5 : " + c5.evaluate());
	}
}
