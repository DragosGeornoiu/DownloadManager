package DownloadManager;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;

public class DownloadThread extends Thread {
	final static Logger logger = Logger.getLogger(DownloadThread.class);
	
	FTPClient ftpClient;
	String remoteFile;
	String downloadTo;
	
	DownloadThread(FTPClient ftpClient, String remoteFile, String downloadTo) {
		this.ftpClient = ftpClient;
		this.remoteFile = remoteFile;
		this.downloadTo = downloadTo;
	}
	
	@Override
	public void run() {
		super.run();
		download();
	}
	
	public boolean download() {
		if (ftpClient == null) {
			logger.info("ftpCliend not initialised");
			return false;
		} else {
			try {
				ftpClient.enterLocalPassiveMode();
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

				OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadTo + remoteFile));
				boolean success = ftpClient.retrieveFile(remoteFile, outputStream1);
				outputStream1.close();

				if (success) {
					logger.info("File #1 has been downloaded successfully.");
				}

			} catch (IOException ex) {
				logger.error("Error: " + ex.getMessage());
			}
			
			return true;
		}
	}
	

}
