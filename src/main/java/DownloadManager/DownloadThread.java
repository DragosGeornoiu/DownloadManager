package DownloadManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
	private FTPClient ftpClient;
	private FTPFile remoteFile;
	private String downloadTo;
	private int alreadyDownloaded = 0;
	private int remainingInDirectory = 0;
	private int indexofDirectory = -1;
	private ThreadToGUI displayer;

	DownloadThread(ThreadToGUI displayer, FTPClient ftpClient, FTPFile file, String downloadTo) {
		this.displayer = displayer;
		this.ftpClient = ftpClient;
		this.remoteFile = file;
		this.downloadTo = downloadTo;
	}

	public void run() {
		download(downloadTo, remoteFile, "");
	}


	/**
	 * Starts the download of a file. At this point it isn't known if the file
	 * is a directory or not.
	 * 
	 * @param to
	 *            where to download the file.
	 * @param file
	 *            what to download.
	 * @param path
	 *            the path to that file on the server.
	 * 
	 * @return true if the download is successful, false otherwise.
	 */
	public boolean download(String to, FTPFile file, String path) {
		logger.info("Starting to download file " +file.getName() + " to " + to);
		if (ftpClient == null) {
			logger.error(Constants.FTPCLIENT_NOT_INIT);
			return false;
		} else {
			try {
				if (file.isDirectory()) {
					downloadDirectory(to, file, path);
				} else {
					File localFile = new File(to, file.getName());

					if ((localFile.length() == file.getSize())) {
						int index = displayer.getIndexWhere(file.getName());
						if (index != -1) {
							displayer.appendToProgress("100% already", index);
							displayer.appendToTextArea( file.getName() + Constants.FILE_ALREADY_DOWNLOADED);
						}
					} else {
						displayer.appendToTextArea(Constants.STARTING_TO_DOWNLOAD + path + "/" + file.getName());
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
	 * @param to
	 *            where to download the file.
	 * @param file
	 *            what to download
	 * @param path
	 *            the path to that file on the server.
	 */
	public void downloadSingleFile(String to, FTPFile file, String path) {
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		File localFile = new File(to, file.getName());
		try {

			inputStream = new BufferedInputStream(ftpClient.retrieveFileStream(file.getName()));
			outputStream = new BufferedOutputStream(new FileOutputStream(localFile));
			// citesti 1024
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
						int index = displayer.getIndexWhere(file.getName());
						if (index != -1) {
							displayer.appendToProgress(alreadyDownloaded + "%", index);
						}
						//Thread.sleep(100);
					}
				}

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
				displayer.appendToTextArea(Constants.FINISHED_DOWNLOADING + path + "//" + file.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Downloads a file that was established to be a directory.
	 * 
	 * @param to
	 *            where to download the file.
	 * @param file
	 *            what to download
	 * @param path
	 *            the path to that file on the server.
	 */
	public void downloadDirectory(String to, FTPFile file, String path) throws InterruptedException {
		long whatSize = directorySizeFTP(file);
		long size = 0;
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
					long directoryAlreadyDownloaded = 0;
					if (indexofDirectory == -1 || ftpClient.printWorkingDirectory().equals("/")) {
						indexofDirectory = displayer.getIndexWhere(file.getName());
					}
					download(to + "\\" + file.getName(), ftpFile, path + "/" + file.getName());
					remainingInDirectory--;
					size = folderSize(theDir);
					if (size * 100 / whatSize > directoryAlreadyDownloaded) {
						directoryAlreadyDownloaded = (int) (size * 100 / whatSize);

						if (indexofDirectory != -1) {
							displayer.appendToProgress(directoryAlreadyDownloaded + "%", indexofDirectory);
						}
					}
				}
			}
			ftpClient.changeToParentDirectory();

			if (ftpClient.printWorkingDirectory().equals("/")) {
				displayer.appendToTextArea(Constants.FINISHED_DOWNLOADING_DIRECTORY + path + "/" + file.getName());
			}
		} catch (IOException e) {
			logger.error("Error: " + e.getMessage());
		}
	}

	/**
	 * Calculates the size of a directory.
	 * 
	 * @param files2
	 *            the file representing a directory. It has been checked with
	 *            isDirectory() already.
	 * @return the size of the directory.
	 */
	public long directorySizeFTP(FTPFile files2) {
		long length = 0;

		try {
			ftpClient.changeWorkingDirectory(files2.getName());
			for (FTPFile file : ftpClient.listFiles()) {
				if (file.isFile()) {
					length += file.getSize();
				} else {
					ftpClient.changeWorkingDirectory(file.getName());
					length += directorySizeFTP(file);
					ftpClient.changeToParentDirectory();
				}
			}
		} catch (Exception e) {
			logger.error("Error");
		}
		return length;
	}

	public long folderSize(File directory) {
		long length = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile())
				length += file.length();
			else
				length += folderSize(file);
		}
		return length;
	}

	/**
	 * Pauses the thread, suspending the download.
	 */
	public void suspend() {
		suspended = true;
	}

	/**
	 * Resumes the download.
	 */
	public synchronized void resume() {
		suspended = false;
		notify();
	}

}
