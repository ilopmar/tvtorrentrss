package eu.bseboy.tvrss.matching;

import java.util.Iterator;
import java.util.List;

public class AndJoin implements Clause {
	
	private List<Clause> clauses;
	
	public AndJoin(List	<Clause> clauses) {
		this.clauses = clauses;
	}
	
	public boolean evaluate()
	{
		boolean result = true;
		Iterator<Clause> iter = clauses.iterator();
		// iterate through clauses until one becomes false, and then return false
		// if none are false, return true
		while (iter.hasNext() && (result == true))
		{
			Clause clause = iter.next();
			result = clause.evaluate();
		}
		return result;
	}


}
