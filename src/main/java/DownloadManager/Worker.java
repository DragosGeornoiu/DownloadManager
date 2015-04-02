package DownloadManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	private boolean pause;
	private int remainingInDirectory = 0;

	public Worker(JTextArea textArea, FTPClient ftpClient, FTPFile file, String downloadTo) {
		this.textArea = textArea;
		this.ftpClient = ftpClient;
		this.remoteFile = file;
		this.downloadTo = downloadTo;
	}

	@Override
	protected Object doInBackground() throws Exception {
		System.out.println("a");
		while (!isCancelled() && !isDone()) {
			System.out.println("b");
			System.out.println("isPaused:" + isPaused());
			if (!isPaused()) {
				download(downloadTo, remoteFile, "");
			} else {
				Thread.sleep(200);
			}
		}

		return null;
	}

	@Override
	protected void done() {
		String message = "Finished working with file " + remoteFile.getName();
		appendMessage(message);

	}

	public void appendMessage(String message) {
		textArea.append(message + "\n");
	}

	public boolean download(String to, FTPFile what, String name) {
		System.out.println("TRYING TO DOWNLOAD: " + what.getName());
		if (ftpClient == null) {
			System.out.println("ftpClient is null");
			logger.info("ftpCliend not initialised");
			return false;
		} else {
			try {
				if (what.isDirectory()) {
					downloadDirectory(to, what, name);
				} else {
					File localFile = new File(to, what.getName());

					if ((localFile.length() == what.getSize())) {
						appendMessage("File " + what.getName() + " was already downloaded.");
						/*
						 * } else if ((localFile.length() < what.getSize()) &&
						 * (localFile.length() != 0)) {
						 * appendMessage("Continuing download of file " +
						 * what.getName()); resumeDownload(to, what, "");
						 */
					} else {
						appendMessage("Starting to download file: " + what.getName());
						downloadSingleFile(to, what);
					}
				}
			} catch (Exception e) {
			}
		}

		return true;
	}

	public void downloadSingleFile(String to, FTPFile what) {
		System.out.println("DOWNLOADING SINGLE FILE: " + what.getName());
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		InputStream in = null;
		File localFile = new File(to, what.getName());
		try {
			// in = ftpClient.retrieveFileStream(what.getName());
			inputStream = new BufferedInputStream(ftpClient.retrieveFileStream(what.getName()));
			outputStream = new BufferedOutputStream(new FileOutputStream(localFile));

			int read = inputStream.read();
			while (isPaused() || read != -1) {
				outputStream.write(read);
				read = inputStream.read();
			}

			// in.close();
			outputStream.flush();
			outputStream.close();

			if (!ftpClient.completePendingCommand()) {
				ftpClient.logout();
				ftpClient.disconnect();
				System.err.println("File transfer failed.");
			}

			if (what.getSize() == localFile.length() && remainingInDirectory == 0) {
				this.cancel(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void downloadDirectory(String to, FTPFile what, String name) throws InterruptedException {
		File theDir = new File(to + "\\" + what.getName());
		if (!theDir.exists()) {

			try {
				theDir.mkdir();
				// result = true;
			} catch (SecurityException se) {
				logger.error("Error: " + se.getMessage());
			}
		}

		try {
			System.out.println("BEFORE: " + ftpClient.printWorkingDirectory());
			boolean check = ftpClient.changeWorkingDirectory(name + "/" + what.getName());
			System.out.println("BEFORE: " + ftpClient.printWorkingDirectory());

			if (check) {
				FTPFile[] files = ftpClient.listFiles();
				remainingInDirectory += files.length;
				/*
				 * for (int i = 0; i < files.length; i++) { download(to + "\\" +
				 * what.getName(), files[i], name + "/" + what.getName()); }
				 */
				int i = 0;
				while (i < files.length && !isPaused()) {
					System.out.println("to: " + to + "\\" + what.getName());
					System.out.println("what: " + files[i].getName());
					System.out.println("Iteration of i: " + i);
					FTPFile file = files[i];
					i++;
					download(to + "\\" + what.getName(), file, name + "/" + what.getName());
					// Thread.sleep(1000);
					remainingInDirectory--;
				}
			}
			ftpClient.changeToParentDirectory();

			if (remainingInDirectory == 0 || ftpClient.printWorkingDirectory().equals("/")) {
				this.cancel(true);
			}
		} catch (IOException e) {
			logger.error("Error: " + e.getMessage());
		}
		// }

	}

	public boolean isPaused() {
		return pause;
	}

	public void pause() {
		pause = true;
	}

	public void resume() {
		pause = false;
		/*
		 * if (!(isCancelled() || !isDone())) { download(downloadTo, remoteFile,
		 * ""); }
		 */
	}
}