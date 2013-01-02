package eu.bseboy.tvrss.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.bseboy.tvrss.config.Configuration;

public class DownloadStreamFactory {
	
	protected static final Log log = LogFactory.getLog(DownloadStreamFactory.class);

	private static OutputStream createFileOutputStream(String location) throws IOException {
		File dir = new File(location);
		File tempFile = File.createTempFile("download_", ".torrent", dir);
		
		log.debug("Created output file : " + tempFile.getAbsolutePath());
		
		FileOutputStream fos = new FileOutputStream(tempFile);
		
		return fos;
	}
	
	private static OutputStream createOutputStream(String location) throws IOException {
		// only handle files for now
		OutputStream os = null;

		os = createFileOutputStream(location);			
	
		return os;
	}
	
	public static OutputStream[] createDownloadOutputStreams(Configuration conf) throws IOException {
		OutputStream[] osArray = null;
		int numLocs = 0;
		
		Iterator<String> locs = conf.getDownloadLocations();
		
		// count number of locations ...
		while(locs.hasNext()) {
			numLocs++;
			locs.next();
		}
		osArray = new OutputStream[numLocs];
		
		numLocs = 0;
		locs = conf.getDownloadLocations();
		while(locs.hasNext()) {
			osArray[numLocs] = createOutputStream(locs.next());
			numLocs++;
		}		
		
		return osArray;
	}
}
