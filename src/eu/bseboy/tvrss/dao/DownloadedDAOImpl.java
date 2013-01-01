package eu.bseboy.tvrss.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import eu.bseboy.tvrss.ShowDetails;

public class DownloadedDAOImpl implements DownloadedDAO {

	private static final String sync = "syncToken";
	private static boolean initialised = false;
	
	private static final String dbURL = "jdbc:hsqldb:file:downloaded";
	private static final String dbUser = "sa";
	private static final String dbPwd = "";
	
	private static final String NULL_EXTRA = "NULL";
	
	private static final String CREATE_TABLE_1 = "create table downloaded_episodes (showname varchar(100), extrainfo varchar(100), series integer, episode integer) ";
	private static final String CHECK_TABLE_1 = "select count(*) from downloaded_episodes";
	
	private static final String CHECK_DOWNLOADED_SQL = "select count(*) from downloaded_episodes where showname = ? and extrainfo = ? and series = ? and episode = ? ";
	
	private static final String RECORD_DOWNLOAD_SQL = "insert into downloaded_episodes (showname, extrainfo, series, episode) values (?,?,?,?)";
	
	private String nvl(String value, String valueIfNull)
	{
		if (value != null) {
			return value;
		} else 
		{
			return valueIfNull;
		}
	}

	private void debug(String message)
	{
		System.out.println(message);
	}
	private void error(String message)
	{
		System.err.println(message);
	}
	
	public boolean previouslyDownloaded(ShowDetails matchedDetails) {
		
		boolean downloaded = false;
		
		Connection c = null;
		
		try {
			c = getConnection();
			PreparedStatement stmt = c.prepareStatement(CHECK_DOWNLOADED_SQL);
			stmt.setString(1, matchedDetails.getShowName());
			stmt.setString(2, nvl(matchedDetails.getExtraInfo(), NULL_EXTRA));
			stmt.setInt(3, matchedDetails.getSeries().intValue());
			stmt.setInt(4, matchedDetails.getEpisode().intValue());
			
			ResultSet rs = stmt.executeQuery();

			debug("Checked for downloads of : " + matchedDetails.getShowName() + " : " + matchedDetails.getExtraInfo() + " : " + matchedDetails.getSeries().intValue() + " : " + matchedDetails.getEpisode().intValue());

			int matches = 0;
			if (rs.next())
			{
				matches = rs.getInt(1);
			}
			
			if (matches > 0)
			{
				debug("Downloaded " + matches + " times before");
				downloaded = true;
			}
			
			rs.close();
			stmt.close();
			c.close();
		} 
		catch (SQLException e)
		{
			error(e.getMessage());
		}
		
		return downloaded;
	}

	/**
	 * shutdown database on exit
	 */
	public void shutdown() {
		try {
			Connection c = getConnection();
			CallableStatement stmt = c.prepareCall("SHUTDOWN");
			stmt.execute();
			c.close();
			debug("Database shutdown called");
		} 
		catch (Exception e)
		{
			error(e.getMessage());
		}
	}

	/**
	 * record the fact that we have downloaded a specific matching episode
	 */
	public void recordDownload(ShowDetails matchedDetails) {
		
		Connection c = null;
		
		try {
			c = getConnection();
			CallableStatement stmt = c.prepareCall(RECORD_DOWNLOAD_SQL);
			stmt.setString(1, matchedDetails.getShowName());
			stmt.setString(2, nvl(matchedDetails.getExtraInfo(), NULL_EXTRA));
			stmt.setInt(3, matchedDetails.getSeries().intValue());
			stmt.setInt(4, matchedDetails.getEpisode().intValue());
			
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			
			debug("Recorded download of : " + matchedDetails.getShowName() + " : " + matchedDetails.getExtraInfo() + " : " + matchedDetails.getSeries().intValue() + " : " + matchedDetails.getEpisode().intValue());
		}
		catch (SQLException e1)
		{
			error(e1.getMessage());
		}
		finally {
			try { c.close();} catch (SQLException e2) { error(e2.getMessage()); }
		}

	}
	
	private Connection getConnection() throws SQLException
	{
		Connection c = DriverManager.getConnection(dbURL, dbUser, dbPwd);
		return c;
	}
	
	/**
	 * creates the DB tables on first run
	 */
	private void initialiseSchema()
	{
		synchronized(sync)
		{
			if (initialised == false)
			{
			    try {
			        Class.forName("org.hsqldb.jdbcDriver" );
			    } catch (Exception e) {
			        error("ERROR: failed to load HSQLDB JDBC driver.");
			        error(e.getMessage());
			        return;
			    }

				// only try and initialise once 
				initialised = true;
				// create previous downloads table
				Connection c = null;
				try {
					c = getConnection();
					try {
						PreparedStatement pstmt = c.prepareStatement(CHECK_TABLE_1);
						pstmt.executeQuery();
						debug("Check passed - table already exists");
					} catch (SQLException e1)
					{
						error("Exception checking downloads table " + e1.getMessage());
						CallableStatement stmt = c.prepareCall(CREATE_TABLE_1);
						stmt.execute();
						debug("Created table with SQL " + CREATE_TABLE_1);
					}
				} catch (SQLException e)
				{
					error("Exception trying to create downloads table " + e.getMessage());
				}
				finally
				{
					try { c.close(); } catch (SQLException e) {}
				}
			}
		}
	}
	
	public DownloadedDAOImpl()
	{
		super();
		initialiseSchema();
	}

}
