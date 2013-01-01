package eu.bseboy.tvrss.matching;

public class StringMatch implements Clause {

	// set to true if the result should be notted
	private boolean notFlag = false;
	private String checkString;
	private String compareTo;
	
	/**
	 * create a new string matcher
	 * if checkString begins with a '~', we make the clause a NOT clause
	 * @param checkString - the string to look for
	 * @param compareTo - the string in which we want to find it
	 */
	public StringMatch(String checkString, String compareTo)
	{
		if (checkString.charAt(0) == '~')
		{
			// NOT clause
			notFlag = true;
			this.checkString = checkString.substring(1);   // ignore first character (the ~)
		} else {
			notFlag = false;
			this.checkString = checkString;
		}
		this.compareTo = compareTo;
	}
	
	public boolean evaluate() {
		boolean result = false;
		
		// see if compareTo is found within checkString
		if (compareTo.toUpperCase().contains(checkString.toUpperCase()))
		{
			result = true;
		}

		// handle not flag ...
		if (notFlag) {
			result = !result;
		}
				
		return result;
	}

}
