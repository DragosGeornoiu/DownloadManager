package DownloadManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import DownloadManager.Constants.Constants;

public class Worker extends SwingWorker<Object, Object> {
	final static Logger logger = Logger.getLogger(Worker.class);

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
		while (!isCancelled() && !isDone()) {
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
		String message = Constants.FINISHED_DOWNLOADING + remoteFile.getName();
		appendMessage(message);

	}

	public void appendMessage(String message) {
		textArea.append(message + "\n");
	}

	public boolean download(String to, FTPFile what, String name) {
		if (ftpClient == null) {
			logger.info(Constants.FTPCLIENT_NOT_INIT);
			return false;
		} else {
			try {
				if (what.isDirectory()) {
					downloadDirectory(to, what, name);
				} else {
					File localFile = new File(to, what.getName());

					if ((localFile.length() == what.getSize())) {
						appendMessage("File " + what.getName() + " was already downloaded.");
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
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		File localFile = new File(to, what.getName());
		try {

			inputStream = new BufferedInputStream(ftpClient.retrieveFileStream(what.getName()));
			outputStream = new BufferedOutputStream(new FileOutputStream(localFile));

			int read = inputStream.read();
			while (isPaused() || read != -1) {
				outputStream.write(read);
				read = inputStream.read();
			}

			outputStream.flush();
			outputStream.close();

			if (!ftpClient.completePendingCommand()) {
				ftpClient.logout();
				ftpClient.disconnect();
				logger.error(Constants.FILE_TRANSFER_FILE);
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
			} catch (SecurityException se) {
				logger.error("Error: " + se.getMessage());
			}
		}

		try {
			boolean check = ftpClient.changeWorkingDirectory(name + "/" + what.getName());

			if (check) {
				FTPFile[] files = ftpClient.listFiles();
				remainingInDirectory += files.length;

				int i = 0;
				while (i < files.length && !isPaused()) {
					FTPFile file = files[i];
					i++;
					download(to + "\\" + what.getName(), file, name + "/" + what.getName());
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

	}

	public boolean isPaused() {
		return pause;
	}

	public void pause() {
		pause = true;
	}

	public void resume() {
		pause = false;
	}
}