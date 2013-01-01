package eu.bseboy.tvemail;

import java.util.Properties;

public class EmailAccount {

	private String accountId = null;
	private Properties props = new Properties();
	
	public EmailAccount(String accountId) {
		this.setAccountId(accountId); 
	}
	
	public void setProperty(String name, String value)
	{
		props.setProperty(name, value);
	}
	
	public String getProperty(String name) {
		return props.getProperty(name);
	}
	
	public Properties getProperties() {
		return props;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getAccountId() {
		return accountId;
	}
}
