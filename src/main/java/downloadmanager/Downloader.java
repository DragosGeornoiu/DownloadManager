package downloadmanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;

import javax.swing.SwingUtilities;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import downloadmanager.constants.Constants;
import downloadmanager.gui.OverwriteGUI;
import downloadmanager.gui.ReconnectGUI;

/**
 * 
 * Downloader is responsible for downloading a file from the given ftpClient and
 * post in the textArea and table intermediate results.
 *
 */
public class Downloader {
	final static Logger logger = Logger.getLogger(Downloader.class);
	/**
	 * The static volatile boolean members are used when the user selects if he
	 * wants to overwrite the files written on disk of if he wants to reconnect
	 * when a connection failes.
	 */
	public static volatile boolean yesToAllOverwrite;
	public static volatile boolean noToAllOverwrite;
	public static volatile boolean yesToAllReconnect;
	public static volatile boolean noToAllReconnect;

	/**
	 * Used for checking if the thread on which the Downloader is running was
	 * paused or not.
	 */
	private boolean suspended = false;
	/** The ftpClient that manages the download. */
	private FTPClient ftpClient;
	/** The file selected to be downloaded */
	private FTPFile remoteFile;
	/** Where to download the selected file */
	private String downloadTo;
	/**
	 * Represents in what percentage has the file downloaded.
	 */
	private int alreadyDownloaded = 0;
	/**
	 * Used for checking if inside a directory, representing the number of files
	 * remaining to be downloaded in the current directory
	 */
	private int remainingInDirectory = 0;
	/** Used to check where in the table to update the progress */
	private int indexofDirectory = -1;
	/** Used for updating the UI */
	private ThreadToGUI displayer;
	/** The FtpLogin representing the succeded connection of the user */
	private FTPLogin loginFtp;
	/** Used for reading from the remoteFile */
	private BufferedInputStream inputStream = null;
	/** Used for writing in the file located on disk */
	private BufferedOutputStream outputStream = null;
	/** Represents the size of the file that has been downloaded. */
	private long size;

	Downloader(ThreadToGUI displayer, FTPLogin loginFtp, FTPFile file, String downloadTo) {
		this.displayer = displayer;
		this.ftpClient = loginFtp.getFtpClient();
		this.remoteFile = file;
		this.downloadTo = downloadTo;
		this.loginFtp = loginFtp;
	}

	/**
	 * Starts the download to the downloadTo path of the remoteFile starts.
	 */
	public void startDownload() {
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
		logger.info("Starting to download file " + file.getName() + " to " + to);
		if (ftpClient == null) {
			logger.error(Constants.FTPCLIENT_NOT_INIT);
			return false;
		} else {
			try {
				if (file.isDirectory()) {
					downloadDirectory(to, file, path);
				} else {
					File localFile = new File(to, file.getName());

					if (localFile.exists()) {
						checkIfOverwrite(to, file, path, localFile);
					} else {
						return downloadSingleFile(to, file, path);
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
	 * @param where
	 *            to download the file.
	 * @param file
	 *            what to download
	 * @param path
	 *            the path to that file on the server.
	 */
	public boolean downloadSingleFile(String to, final FTPFile file, String path) {
		File localFile = new File(to, file.getName());
		if (remainingInDirectory == 0) {
			displayer.appendToTextArea(Constants.STARTING_TO_DOWNLOAD + path + "/" + file.getName());
		}

		try {
			try {
				inputStream = new BufferedInputStream(ftpClient.retrieveFileStream(file.getName()));
			} catch (Exception ex) {
				if (remainingInDirectory == 0) {
					checkIfReconnect(file, localFile, file.getSize());
				} else {
					failedDownload(file, file.getSize());
				}
			}
			outputStream = new BufferedOutputStream(new FileOutputStream(localFile));

			int read;
			size = 0;
			long whatSize = file.getSize();
			byte[] data = new byte[Constants.KBYTE];
			boolean finished = false;
			while (size < whatSize) {
				read = inputStream.read(data);
				if (read != -1) {
					size += read;
					outputStream.write(data, 0, read);
					if (size * 100 / whatSize > alreadyDownloaded) {
						alreadyDownloaded = (int) (size * 100 / whatSize);
						final int index = displayer.getIndexWhere(file.getName());
						if (index != -1) {

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									displayer.appendToProgress(alreadyDownloaded + "%", index);
								}
							});

						}

						if (alreadyDownloaded == 100) {
							finished = true;
						}
					}
				} else if (!finished && remainingInDirectory == 0) {
					checkIfReconnect(file, localFile, whatSize);
				} else {
					return false;
				}
				synchronized (this) {
					while (suspended) {
						wait();
					}
				}
			}

			outputStream.flush();
			outputStream.close();

			if (file.getSize() == localFile.length() && remainingInDirectory == 0) {
				displayer.appendToTextArea(Constants.FINISHED_DOWNLOADING + path + "/" + file.getName());
				ftpClient.logout();
				ftpClient.disconnect();
			}

			if (ftpClient != null && !ftpClient.completePendingCommand()) {
				ftpClient.logout();
				ftpClient.disconnect();
				logger.error(Constants.FILE_TRANSFER_FAILED);
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return true;
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

		try {
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

			if (ftpClient.printWorkingDirectory().equals("/" + file.getName())) {
				displayer.appendToTextArea(Constants.STARTING_DOWNLOAD_OF_DIRECTORY + path + "/" + file.getName());
			}
			boolean check = ftpClient.changeWorkingDirectory(path + "/" + file.getName());

			if (check) {
				FTPFile[] files = ftpClient.listFiles();
				remainingInDirectory += files.length;

				int i = 0;
				while (i < files.length) {
					FTPFile ftpFile = files[i];
					i++;
					long directoryAlreadyDownloaded = 0;

					if (download(to + "\\" + file.getName(), ftpFile, path + "/" + file.getName())) {
						remainingInDirectory--;
						size = folderSize(theDir);

						if (indexofDirectory == -1 || ftpClient.printWorkingDirectory().equals("/")) {
							indexofDirectory = displayer.getIndexWhere(file.getName());
						}

						if (size * 100 / whatSize > directoryAlreadyDownloaded) {
							directoryAlreadyDownloaded = (int) (size * 100 / whatSize);

							if (indexofDirectory != -1) {
								displayer.appendToProgress(directoryAlreadyDownloaded + "%", indexofDirectory);
							}
						}
					} else {
						i = files.length;
						String name = displayer.appendToProgress(Constants.FAILED, indexofDirectory);
						displayer.appendToTextArea(Constants.FAILED_DOWNLOADING_DIRECTORY + name);

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
	 * Calculates the size of a FTP directory.
	 * 
	 * @param ftpFile
	 *            the file representing a directory. It has been checked with
	 *            isDirectory() already.
	 * @return the size of the directory.
	 */
	public long directorySizeFTP(FTPFile ftpFile) {
		long length = 0;

		try {
			ftpClient.changeWorkingDirectory(ftpFile.getName());
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

	/**
	 * Calculates the size of a local directory.
	 * 
	 * @param directory
	 *            the file representing a directory. It has been checked with
	 *            isDirectory() already.
	 * @return the size of the directory.
	 */
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

	/** Pauses the thread, suspending the download. */
	public void suspend() {
		suspended = true;
	}

	/** Resumes the download. */
	public synchronized void resume() {
		suspended = false;
		notify();
	}

	
	public boolean isSuspended() {
		return suspended;
	}

	/**
	 * Asks the user if he wants to reconnect and try to download again the file
	 * for which the connection failed.
	 * 
	 * @param file
	 *            FtpFile that the user is trying to download.
	 * @param localFile
	 *            File where the FtpFile is downloaded.
	 * @param whatSize
	 *            size of ftpFile.
	 * 
	 * @throws SocketException
	 * @throws IOException
	 */
	private void checkIfReconnect(FTPFile file, File localFile, long whatSize) throws SocketException, IOException {
		if (yesToAllReconnect == true) {
			reconnect(file, localFile);
		} else if (noToAllReconnect == true) {
			failedDownload(file, whatSize);
		} else {
			ReconnectGUI reconnect = new ReconnectGUI(file.getName(), size, whatSize);
			while (!reconnect.isPressed() && (yesToAllReconnect != true && noToAllReconnect != true)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
				}
			}

			if (yesToAllReconnect == true) {
				reconnect.dispose();
				reconnect(file, localFile);
			} else if (noToAllReconnect == true) {
				reconnect.dispose();
				failedDownload(file, whatSize);
			} else {
				if (reconnect.getWhatButtonWasPressed() == Constants.PRESSED_YES_BUTTON) {
					reconnect.dispose();
					reconnect(file, localFile);
				} else if (reconnect.getWhatButtonWasPressed() == Constants.PRESSED_YES_TO_ALL_BUTTON) {
					yesToAllReconnect = true;
					reconnect(file, localFile);
				} else if (reconnect.getWhatButtonWasPressed() == Constants.PRESSED_NO_TO_ALL_BUTTON) {
					noToAllReconnect = true;
					failedDownload(file, whatSize);
				} else {
					failedDownload(file, whatSize);
				}
			}
		}
	}

	/**
	 * The user has accepted to try to reconnect and download again the file for
	 * which the connection failed.
	 * 
	 * @param file
	 *            FtpFile that the user is trying to download.
	 * @param localFile
	 *            File where the FtpFile is downloaded.
	 * 
	 * @throws SocketException
	 * @throws IOException
	 */
	private void reconnect(final FTPFile file, File localFile) throws SocketException, IOException {
		ftpClient.connect(loginFtp.getServer(), loginFtp.getPort());
		ftpClient.login(loginFtp.getUser(), loginFtp.getPassword());
		inputStream = new BufferedInputStream(ftpClient.retrieveFileStream(file.getName()));
		outputStream = new BufferedOutputStream(new FileOutputStream(localFile));
		size = 0;

		alreadyDownloaded = 0;
		SwingUtilities.invokeLater(new Runnable() {
			final int index = displayer.getIndexWhere(file.getName());

			public void run() {
				displayer.appendToProgress("", index);

			}
		});
	}

	/**
	 * The user has rejected to try to reconnect and downloag again the file for
	 * which the connection failed.
	 * 
	 * @param file
	 *            FtpFile that the user is trying to download.
	 * @param whatSize
	 *            size of the ftpFile.
	 */
	private void failedDownload(final FTPFile file, long whatSize) {
		size = whatSize + 1;
		SwingUtilities.invokeLater(new Runnable() {
			final int index = displayer.getIndexWhere(file.getName());

			public void run() {
				displayer.appendToProgress(Constants.FAILED, index);

			}
		});
	}

	/**
	 * Asks the user if he want to overwrite or not the file written on the
	 * disk.
	 * 
	 * @param to
	 *            where the ftpFile is located.
	 * @param file
	 *            the FtpFile.
	 * @param path
	 *            the path to the file.
	 * @param localFile
	 *            the file on the local disk.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void checkIfOverwrite(String to, FTPFile file, String path, File localFile) throws IOException,
			InterruptedException {
		if (yesToAllOverwrite == true) {
			downloadSingleFile(to, file, path);
		} else if (noToAllOverwrite == true) {
			doNotOverwrite(file);
		} else {
			OverwriteGUI overwrite = new OverwriteGUI(file.getName(), localFile.length(), file.getSize());
			while (!overwrite.isPressed() && (yesToAllOverwrite != true && noToAllOverwrite != true)) {
				Thread.sleep(100);
			}

			if (yesToAllOverwrite == true) {
				overwrite.dispose();
				downloadSingleFile(to, file, path);
			} else if (noToAllOverwrite == true) {
				overwrite.dispose();
				doNotOverwrite(file);
			} else {
				if (overwrite.getWhatButtonWasPressed() == 1) {
					downloadSingleFile(to, file, path);
				} else if (overwrite.getWhatButtonWasPressed() == 3) {
					yesToAllOverwrite = true;
					downloadSingleFile(to, file, path);
				} else if (overwrite.getWhatButtonWasPressed() == 4) {
					noToAllOverwrite = true;
					doNotOverwrite(file);
				} else {
					doNotOverwrite(file);
				}
			}
		}
	}

	/**
	 * The user refused to overwrite the file on his local disk..
	 * 
	 * @param file
	 *            the ftpFile that wasn't downloaded to local disk.
	 * 
	 * 
	 * @throws IOException
	 */
	private void doNotOverwrite(FTPFile file) throws IOException {
		int index = displayer.getIndexWhere(file.getName());
		if (index != -1) {
			displayer.appendToProgress("100%", index);
			displayer.appendToTextArea(file.getName() + " was not ovewritten.");
			ftpClient.logout();
			ftpClient.disconnect();
		}
	}
}