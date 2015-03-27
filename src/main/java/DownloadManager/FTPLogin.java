package DownloadManager;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

public class FTPLogin {
	final static Logger logger = Logger.getLogger(FTPLogin.class);

	FTPClient ftpClient;
	private String server;
	private int port;

	public FTPLogin(String server, int port) {
		this.server = server;
		this.port = port;
	}

	public boolean login(String user, String password) {
		ftpClient = new FTPClient();
		try {
			ftpClient.connect(server, port);
			int replyCode = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				logger.info("Operation failed. Server reply code: " + replyCode);
				return false;
			}

			boolean success = ftpClient.login(user, password);
			if (!success) {
				logger.info("Could not login to the server");
				return false;
			} else {
				logger.info("LOGGED IN SERVER with user: " + user + " and password: " + password);
				return true;
			}
		} catch (IOException ex) {
			logger.error(ex);
		}

		return false;
	}

	public FTPClient getFtpClient() {
		return ftpClient;
	}

	public void setFtpClient(FTPClient ftpClient) {
		this.ftpClient = ftpClient;
	}
}
