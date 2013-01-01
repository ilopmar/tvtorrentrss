package eu.bseboy.tvrss.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SearchItemList {

	private List<SearchItem> itemList = new ArrayList<SearchItem>();
	
	public void addSearchItem(SearchItem newItem)
	{
		itemList.add(newItem);
	}
	
	public Iterator<SearchItem> iterator()
	{
		return itemList.iterator();
	}
}
