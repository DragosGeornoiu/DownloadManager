package DownloadManager;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

public class FTPLogin {
	final static Logger logger = Logger.getLogger(FTPLogin.class);

	private FTPClient ftpClient;
	private String server;
	private int port;
	private String user;
	private String password;
	

	public FTPLogin(String server, int port) {
		this.server = server;
		this.port = port;
	}

	public boolean login(String user, String password) {
		this.user = user;
		this.password = password;
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

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
