package eu.bseboy.tvrss.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DownloadedDAOImpl implements DownloadedDAO {

	protected static final Log log = LogFactory.getLog(DownloadedDAOImpl.class);
	
	private static final String sync = "syncToken";
	private static boolean initialised = false;

	private static final String dbURL = "jdbc:hsqldb:file:downloaded";
	private static final String dbUser = "sa";
	private static final String dbPwd = "";

	private static final String CREATE_TABLE = "create table downloaded_episodes (title varchar(255), url varchar(255)) ";
	private static final String CHECK_TABLE = "select count(*) from downloaded_episodes";

	private static final String CHECK_DOWNLOADED_SQL = "select count(*) from downloaded_episodes where title = ? and url = ? ";

	private static final String RECORD_DOWNLOAD_SQL = "insert into downloaded_episodes (title, url) values (?, ?)";

	public boolean previouslyDownloaded(String title, String url) {

		boolean downloaded = false;

		Connection c = null;

		try {
			c = getConnection();
			PreparedStatement stmt = c.prepareStatement(CHECK_DOWNLOADED_SQL);
			stmt.setString(1, title);
			stmt.setString(2, url);

			ResultSet rs = stmt.executeQuery();

			log.debug("Checked for downloads of: " + title + " with url: " + url);

			int matches = 0;
			if (rs.next()) {
				matches = rs.getInt(1);
			}

			if (matches > 0) {
				log.debug("Downloaded " + matches + " times before");
				downloaded = true;
			}

			rs.close();
			stmt.close();
			c.close();
		} catch (SQLException e) {
			log.error(e.getMessage());
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
			log.debug("Database shutdown called");
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * record the fact that we have downloaded a specific matching episode
	 */
	public void recordDownload(String title, String url) {

		Connection c = null;

		try {
			c = getConnection();
			CallableStatement stmt = c.prepareCall(RECORD_DOWNLOAD_SQL);
			stmt.setString(1, title);
			stmt.setString(2, url);

			stmt.executeUpdate();
			stmt.close();
			c.commit();

			log.debug("Recorded download of: " + title + " with url: " + url);
		} catch (SQLException e1) {
			log.error(e1.getMessage());
		}
		finally {
			try { 
				c.close();
			} catch (SQLException e2) { 
				log.error(e2.getMessage());
			}
		}

	}

	private Connection getConnection() throws SQLException {
		Connection c = DriverManager.getConnection(dbURL, dbUser, dbPwd);
		return c;
	}

	/**
	 * creates the DB tables on first run
	 */
	private void initialiseSchema() {
		synchronized(sync) {
			if (initialised == false) {
				try {
					Class.forName("org.hsqldb.jdbcDriver" );
				} catch (Exception e) {
					log.error("ERROR: failed to load HSQLDB JDBC driver.");
					log.error(e.getMessage());
					return;
				}

				// only try and initialise once 
				initialised = true;
				// create previous downloads table
				Connection c = null;
				try {
					c = getConnection();
					try {
						PreparedStatement pstmt = c.prepareStatement(CHECK_TABLE);
						pstmt.executeQuery();
						log.debug("Check passed - table already exists");
					} catch (SQLException e1) {
						log.error("Exception checking downloads table " + e1.getMessage());
						CallableStatement stmt = c.prepareCall(CREATE_TABLE);
						stmt.execute();
						log.debug("Created table with SQL " + CREATE_TABLE);
					}
				} catch (SQLException e) {
					log.error("Exception trying to create downloads table " + e.getMessage());
				} finally {
					try { 
						c.close();
					} catch (SQLException e) {

					}
				}
			}
		}
	}

	public DownloadedDAOImpl() {
		super();
		initialiseSchema();
	}
}
