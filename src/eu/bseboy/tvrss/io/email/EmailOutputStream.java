package eu.bseboy.tvrss.io.email;

import java.io.ByteArrayOutputStream;

import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.MultiPartEmail;

import eu.bseboy.tvrss.ShowDetails;
import eu.bseboy.tvrss.io.DownloadException;

/**
 * stream that send an e-mail with the data in the stream attached when it is closed
 * @author rnewton
 */
public class EmailOutputStream extends ByteArrayOutputStream {

	private String emailAddress;
	private String server;
	private String fromAddress;
	private String user;
	private String password;
	
	private ShowDetails show;
	
	public EmailOutputStream(String emailLocation, ShowDetails show) {
		super(); // create superclass
		
		// split up the config data into fields
		String[] emailConfig = emailLocation.split(",");
		if (emailConfig.length == 5)
		{
			this.show = show;
			emailAddress = emailConfig[0];
			fromAddress = emailConfig[1];
			server = emailConfig[2];
			user = emailConfig[3];
			password = emailConfig[4];
		}
		else {
			throw new RuntimeException("Not enough parameters in for e-mail : " + emailLocation);
		}
	}
	
	private static void debug(String message)
	{
		System.out.println(message);
	}
	private static void error(String message)
	{
		System.err.println(message);
	}
		
	@Override
	public void close() throws DownloadException {
		try {
			super.close();
			debug("Sending email to " + emailAddress + " now stream is closed. Bytes in buffer : " + this.count );
			sendEmail();
		}
		catch (Exception e)
		{
			error("Unable to send e-mail : " + e.getMessage());
			error(e.getCause().toString());
			throw(new DownloadException("Unable to send e-mail", e));
		}
	}
	
	private void sendEmail() throws Exception
	{
		// send an email with the buffer contents as an attachment

		  // Create the email message
		  MultiPartEmail email = new MultiPartEmail();
		  email.setHostName(server);
		  email.addTo(emailAddress);
		  email.setFrom(fromAddress);
		  email.setSubject("Downloaded torrent for " + show.getShowName());
		  email.setMsg("Find the torrent file attached.");
		  email.setAuthentication(user, password);

		  // create datasource for the data from the byte array
		  ByteArrayDataSource dataSource = new ByteArrayDataSource(this.toByteArray(), "application/octet-stream");
		  
		  String name = show.getShowName() + "_" + show.getExtraInfo() + "_" + show.getSeries() + "x" + show.getEpisode() + ".torrent";
		  String description = name;
		  
		  // add the datasource as an attachment
		  email.attach(dataSource, name, description);

		  // send the email
		  email.send();		
	}

	
	public static void main(String[] args) throws Exception {
		
		ShowDetails show = new ShowDetails();
		show.setShowName("test");
		show.setExtraInfo("extra");
		show.setEpisode(new Integer(1));
		show.setSeries(new Integer(2));
		
		String email = "email:to@to.com,from@from.com,serveraddress,username,password";
		
		EmailOutputStream os = new EmailOutputStream(email, show);
		os.write(1);
		os.close();
	}
}
