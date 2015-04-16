package downloadmanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
 * post in the textArea intermediate results.
 *
 */
public class Downloader {
	final static Logger logger = Logger.getLogger(Downloader.class);
	private boolean suspended = false;
	private FTPClient ftpClient;
	private FTPFile remoteFile;
	private String downloadTo;
	private int alreadyDownloaded = 0;
	private int remainingInDirectory = 0;
	private int indexofDirectory = -1;
	private ThreadToGUI displayer;

	Downloader(ThreadToGUI displayer, FTPClient ftpClient, FTPFile file, String downloadTo) {
		this.displayer = displayer;
		this.ftpClient = ftpClient;
		this.remoteFile = file;
		this.downloadTo = downloadTo;

	}

	public void execute() {
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

						OverwriteGUI overwrite = new OverwriteGUI(file.getName(), localFile.length(), file.getSize());
						while (!overwrite.isPressed()) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

						if (overwrite.getWhatButtonWasPressed() == 1) {
							downloadSingleFile(to, file, path);
						} else {
							int index = displayer.getIndexWhere(file.getName());
							if (index != -1) {
								displayer.appendToProgress("100%", index);
								displayer.appendToTextArea(file.getName() + " was not ovewritten.");
								ftpClient.logout();
								ftpClient.disconnect();
							}
						}
					} else {
						downloadSingleFile(to, file, path);
					}

					/*
					 * if ((localFile.length() == file.getSize())) { int index =
					 * displayer.getIndexWhere(file.getName()); if (index != -1)
					 * { displayer.appendToProgress("100%", index);
					 * displayer.appendToTextArea(file.getName() +
					 * Constants.FILE_ALREADY_DOWNLOADED); } } else {
					 * downloadSingleFile(to, file, path); }
					 */
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
	public void downloadSingleFile(String to, final FTPFile file, String path) {
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		File localFile = new File(to, file.getName());

		if (remainingInDirectory == 0) {
			displayer.appendToTextArea(Constants.STARTING_TO_DOWNLOAD + path + "/" + file.getName());
		}

		try {

			inputStream = new BufferedInputStream(ftpClient.retrieveFileStream(file.getName()));
			outputStream = new BufferedOutputStream(new FileOutputStream(localFile));

			int read;
			long size = 0;
			long whatSize = file.getSize();
			byte[] data = new byte[Constants.KBYTE];
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
					}
				} else {

					ReconnectGUI reconnect = new ReconnectGUI(file.getName(), size, whatSize);
					while (!reconnect.isPressed()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							logger.error(e.getMessage());
						}
					}

					if (reconnect.getWhatButtonWasPressed() == 1) {
						ftpClient.connect("localhost", 21);
						ftpClient.login("user", "password");
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
					} else {
						size = whatSize + 1;
						SwingUtilities.invokeLater(new Runnable() {
							final int index = displayer.getIndexWhere(file.getName());

							public void run() {
								displayer.appendToProgress("Failed", index);

							}
						});
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

			if (file.getSize() == localFile.length() && remainingInDirectory == 0) {
				displayer.appendToTextArea(Constants.FINISHED_DOWNLOADING + path + "/" + file.getName());
				ftpClient.logout();
				ftpClient.disconnect();
			}

			/*
			 * if (ftpClient.isConnected() &&
			 * !ftpClient.completePendingCommand()) { ftpClient.logout();
			 * ftpClient.disconnect();
			 * logger.error(Constants.FILE_TRANSFER_FAILED); }
			 */
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
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

	public boolean isSuspended() {
		return suspended;
	}

}
