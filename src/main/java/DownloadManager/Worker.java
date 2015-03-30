package DownloadManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

public class Worker extends SwingWorker {
	final static Logger logger = Logger.getLogger(DownloadThread.class);

	private JTextArea textArea;
	private FTPClient ftpClient;
	private FTPFile remoteFile;
	private String downloadTo;

	public Worker(JTextArea textArea, FTPClient ftpClient, FTPFile file, String downloadTo) {
		this.textArea = textArea;
		this.ftpClient = ftpClient;
		this.remoteFile = file;
		this.downloadTo = downloadTo;
	}

	@Override
	protected Object doInBackground() throws Exception {
		/*appendMessage(message);*/
        appendMessage("Trying to download file: " + remoteFile.getName());
		
		if (download(downloadTo, remoteFile, "")) {
			appendMessage("Started downloading file " + remoteFile.getName());
		} else {
			appendMessage("File " + remoteFile.getName() + " can't be downloaded"); 
		}

		return null;
	}

	@Override
	protected void done() {
		String message = "Finished downloading file " + remoteFile.getName();
		appendMessage(message);
	}

	public void appendMessage(String message) {
		textArea.append(message + "\n");
		System.out.println(message);
	}

	public boolean download(String to, FTPFile what, String name) {
		if (ftpClient == null) {
			logger.info("ftpCliend not initialised");
			return false;
		} else {
			try {
				if (what.isDirectory()) {
					downloadDirectory(to, what, name);
				} else {
					downloadSingleFile(to, what);
				}
			} catch (Exception ex) {
				logger.error("Error: " + ex.getMessage());
			}

			return true;
		}
	}

	public void downloadSingleFile(String to, FTPFile what) {
		try {
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(to + "\\" + what.getName()));
			boolean success = ftpClient.retrieveFile(what.getName(), outputStream1);
			outputStream1.close();

			if (success) {
				logger.info("File #1 has been downloaded successfully.");
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}

	public void downloadDirectory(String to, FTPFile what, String name) throws InterruptedException {
		File theDir = new File(to + "\\" + what.getName());
		if (!theDir.exists()) {
			boolean result = false;
			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException se) {
				logger.error("Error: " + se.getMessage());
			}

			Thread.sleep(1000);
			if (result) {
				try {
					boolean check = ftpClient.changeWorkingDirectory(name + "/" + what.getName());

					if (check) {
						FTPFile[] files = ftpClient.listFiles();
						for (int i = 0; i < files.length; i++) {
							download(to + "\\" + what.getName(), files[i], name + "/" + what.getName());
						}
					}

					ftpClient.changeToParentDirectory();
				} catch (IOException e) {
					logger.error("Error: " + e.getMessage());
				}
			}
		}

	}
}
