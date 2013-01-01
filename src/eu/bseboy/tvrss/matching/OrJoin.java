package eu.bseboy.tvrss.matching;

import java.util.Iterator;
import java.util.List;

public class OrJoin implements Clause {

	private List<Clause> clauses;
	
	public OrJoin(List	<Clause> clauses) {
		this.clauses = clauses;
	}

	
	public boolean evaluate()
	{
		boolean result = false;
		Iterator<Clause> iter = clauses.iterator();
		// iterate through clauses until one becomes true, and then return true
		// if none are true, return false
		while (iter.hasNext() && (result == false))
		{
			Clause clause = iter.next();
			result = clause.evaluate();
		}
		return result;
	}

}
