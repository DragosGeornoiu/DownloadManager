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
		String message = "Finished working with file " + remoteFile.getName();
		appendMessage(message);

	}

	public void appendMessage(String message) {
		textArea.append(message + "\n");
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
					File localFile = new File(to, what.getName());

					if ((localFile.length() == what.getSize())) {
						appendMessage("File " + what.getName() + " was already downloaded.");
					/*} else if ((localFile.length() < what.getSize()) && (localFile.length() != 0)) {
						appendMessage("Continuing download of file " + what.getName());
						resumeDownload(to, what, "");*/
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

	/*
	 * public void downloadSingleFile(String to, FTPFile what) { try {
	 * ftpClient.enterLocalPassiveMode();
	 * ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	 * 
	 * OutputStream outputStream1 = new BufferedOutputStream(new
	 * FileOutputStream(to + "\\" + what.getName())); boolean success =
	 * ftpClient.retrieveFile(what.getName(), outputStream1);
	 * outputStream1.close();
	 * 
	 * if (success) { logger.info("File #1 has been downloaded successfully.");
	 * } } catch (Exception ex) { logger.error(ex.getMessage()); } }
	 */

	public void downloadSingleFile(String to, FTPFile what) {
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		InputStream in = null;
		File localFile = new File(to, what.getName());
		try {
			in = ftpClient.retrieveFileStream(what.getName());
			inputStream = new BufferedInputStream(in);
			outputStream = new BufferedOutputStream(new FileOutputStream(localFile));

			int read = inputStream.read();
			while(!isPaused() && read != -1) {
				outputStream.write(read);
				read = inputStream.read();
			}

			outputStream.flush();

			System.out.println("remainingInDir: " + remainingInDirectory);
			System.out.println("whatSize: " + what.getSize());
			System.out.println("localSize: " + localFile.length());
			if (what.getSize() ==  localFile.length() && remainingInDirectory == 0 ) {
				this.cancel(true);
			}
		} catch (Exception e) {

		}
	}

	public void downloadDirectory(String to, FTPFile what, String name) throws InterruptedException {
		// boolean result = false;
		File theDir = new File(to + "\\" + what.getName());
		if (!theDir.exists()) {

			try {
				theDir.mkdir();
				// result = true;
			} catch (SecurityException se) {
				logger.error("Error: " + se.getMessage());
			}
		}

		// maybe the directory isn't created yet
		// Thread.sleep(1000);
		// if (result) {
		try {
			boolean check = ftpClient.changeWorkingDirectory(name + "/" + what.getName());

			if (check) {
				FTPFile[] files = ftpClient.listFiles();
				remainingInDirectory +=files.length;
				/*for (int i = 0; i < files.length; i++) {
					download(to + "\\" + what.getName(), files[i], name + "/" + what.getName());
				}*/
				int i=0;
				while(i<files.length && !isPaused()) {
					System.out.println("to: " +to + "\\" + what.getName());
					System.out.println("what: " + files[i].getName());
					download(to + "\\" + what.getName(), files[i], name + "/" + what.getName());
					i++;
					remainingInDirectory--;
				}
			}
			ftpClient.changeToParentDirectory();
		} catch (IOException e) {
			logger.error("Error: " + e.getMessage());
		}
		// }

	}

	private void resumeDownload(String downloadTo, FTPFile remoteFile, String string) {
		try {
			File localFile = new File(downloadTo, remoteFile.getName());
			ftpClient.setRestartOffset(localFile.length());

			BufferedInputStream inputStream = null;
			BufferedOutputStream outputStream = null;
			InputStream in = null;
			in = ftpClient.retrieveFileStream(remoteFile.getName());
			inputStream = new BufferedInputStream(in);
			outputStream = new BufferedOutputStream(new FileOutputStream(localFile, true));
			for (int read = inputStream.read(); read != -1; read = inputStream.read()) {
				outputStream.write(read);
			}
			outputStream.flush();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isPaused() {
		return pause;
	}

	public void pause() {
		if (!isPaused() && !isDone()) {
			pause = true;
		}
	}

	public void resume() {
		if (isPaused() && !isDone()) {
			pause = false;
		}
	}
}
