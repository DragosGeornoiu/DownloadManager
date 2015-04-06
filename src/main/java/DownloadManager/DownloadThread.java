package DownloadManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JTextArea;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import DownloadManager.Constants.Constants;

/**
 * 
 * A thread responsible for downloading a file from the given ftpClient and post
 * in the textArea intermediate results.
 *
 */
public class DownloadThread implements Runnable {
	final static Logger logger = Logger.getLogger(DownloadThread.class);
	private boolean suspended = false;
	private JTextArea textArea;
	private FTPClient ftpClient;
	private FTPFile remoteFile;
	private String downloadTo;
	private int alreadyDownloaded = 0;
	private int remainingInDirectory = 0;

	DownloadThread(JTextArea textArea, FTPClient ftpClient, FTPFile file, String downloadTo) {
		this.textArea = textArea;
		this.ftpClient = ftpClient;
		this.remoteFile = file;
		this.downloadTo = downloadTo;
	}

	public void run() {
		download(downloadTo, remoteFile, "");
	}

	/**
	 * Appends the given message to the textArea.
	 * 
	 * @param message the message to be appended.
	 */
	public void appendMessage(String message) {
		textArea.append(message + "\n");
	}

	/**
	 * Starts the download of a file. At this point it isn't known if the file is a directory or not.
	 * 
	 * @param to where to download the file.
	 * @param file what to download.
	 * @param path the path to that file on the server.
	 * 
	 * @return true if the download is successful, false otherwise.
	 */
	public boolean download(String to, FTPFile file, String path) {
		if (ftpClient == null) {
			logger.info(Constants.FTPCLIENT_NOT_INIT);
			return false;
		} else {
			try {
				if (file.isDirectory()) {
					downloadDirectory(to, file, path);
				} else {
					File localFile = new File(to, file.getName());

					if ((localFile.length() == file.getSize())) {
						appendMessage("File " + file.getName() + " was already downloaded.");
					} else {
						appendMessage(Constants.STARTING_TO_DOWNLOAD + file.getName());
						downloadSingleFile(to, file, path);
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}

		return true;
	}

	/**
	 * Downloads a file that was established not to be a directory.
	 * 
	 * @param to where to download the file.
	 * @param file what to download
	 * @param path the path to that file on the server.
	 */
	public void downloadSingleFile(String to, FTPFile file, String path) {
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		File localFile = new File(to, file.getName());
		try {

			inputStream = new BufferedInputStream(ftpClient.retrieveFileStream(file.getName()));
			outputStream = new BufferedOutputStream(new FileOutputStream(localFile));

			int read = inputStream.read();
			long size = 0;
			long whatSize = file.getSize();

			while (size < whatSize) {
				if (read != -1) {
					size++;
					outputStream.write(read);
					read = inputStream.read();

					if (size * 100 / whatSize > alreadyDownloaded) {
						alreadyDownloaded = (int) (size * 100 / whatSize);
						appendMessage("Downloaded " + alreadyDownloaded + "% of file " + path + "/" + file.getName());
						Thread.sleep(100);
					}
				}

				// Thread.sleep(300);
				// Thread.sleep(100);
				synchronized (this) {
					while (suspended) {
						wait();
					}
				}
			}

			outputStream.flush();
			outputStream.close();

			if (!ftpClient.completePendingCommand()) {
				ftpClient.logout();
				ftpClient.disconnect();
				logger.error(Constants.FILE_TRANSFER_FAILED);
			}

			if (file.getSize() == localFile.length() && remainingInDirectory == 0) {
				appendMessage("Finished downloading " + path + "\\" + file.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Downloads a file that was established to be a directory.
	 * 
	 * @param to where to download the file.
	 * @param file what to download
	 * @param path the path to that file on the server.
	 */
	public void downloadDirectory(String to, FTPFile file, String path) throws InterruptedException {
		File theDir = new File(to + "\\" + file.getName());
		if (!theDir.exists()) {
			try {
				theDir.mkdir();
			} catch (SecurityException se) {
				logger.error("Error: " + se.getMessage());
			}
		}
		try {
			boolean check = ftpClient.changeWorkingDirectory(path + "/" + file.getName());

			if (check) {
				FTPFile[] files = ftpClient.listFiles();
				remainingInDirectory += files.length;

				int i = 0;
				while (i < files.length) {
					FTPFile ftpFile = files[i];
					i++;
					alreadyDownloaded = 0;
					download(to + "\\" + file.getName(), ftpFile, path + "/" + file.getName());
					remainingInDirectory--;
				}
			}
			ftpClient.changeToParentDirectory();

			if (ftpClient.printWorkingDirectory().equals("/")) {
				appendMessage("Finished downloading folder " + file.getName());
			}
		} catch (IOException e) {
			logger.error("Error: " + e.getMessage());
		}
	}

	/**
	 * Pauses the thread, suspending the download.
	 */
	void suspend() {
		suspended = true;
	}

	/**
	 * Resumes the download.
	 */
	synchronized void resume() {
		suspended = false;
		notify();
	}

}
