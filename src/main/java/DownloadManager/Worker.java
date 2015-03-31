package DownloadManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

		// System.out.println(remoteFile.getName() + "(locals) size: " +
		// localFile.length());
		// System.out.println(remoteFile.getName() + "(remote) size: " +
		// remoteFile.getSize());

		// trebuie verificat daca e director sau nu. momentan nu mai merge sa
		// downloadezi director-ul
		// posibil sa trebuiasca sa renunti la metoda download(...)

		// de asemenea trebuie testat dupa daca mai merge sa downloadezi un
		// folder cu tot continutul
		// acestuia (un fisier de 2 gb de ex.
		
		download(downloadTo, remoteFile, "");

		/*
		 * File localFile = new File(downloadTo, remoteFile.getName()); if
		 * (!remoteFile.isDirectory()) { if((localFile.length() ==
		 * remoteFile.getSize())) { appendMessage("File " + remoteFile.getName()
		 * + " was already downloaded."); } else if ((localFile.length() <
		 * remoteFile.getSize()) && (localFile.length() != 0)) {
		 * appendMessage("Continuing download of file " + remoteFile.getName());
		 * resumeDownload(downloadTo, remoteFile, ""); } else {
		 * appendMessage("Starting to download file: " + remoteFile.getName());
		 * if (download(downloadTo, remoteFile, "")) {
		 * appendMessage("Succeded downloading file " + remoteFile.getName()); }
		 * else { appendMessage("File " + remoteFile.getName() +
		 * " can't be downloaded"); } } } else { download(downloadTo,
		 * remoteFile, ""); }
		 */

		return null;
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

	@Override
	protected void done() {
		String message = "Finished working with file " + remoteFile.getName();
		appendMessage(message);
	}

	public void appendMessage(String message) {
		textArea.append(message + "\n");
	}

	public boolean download(String to, FTPFile what, String name) {
		/*System.out.println("to: " + to);
		System.out.println("what: " + what.getName());
		System.out.println("name: " + name);*/
		if (ftpClient == null) {
			logger.info("ftpCliend not initialised");
			return false;
		} else {
			try {
				if (what.isDirectory()) {
					downloadDirectory(to, what, name);
				} else {
					File localFile = new File(to, what.getName());
					/*System.out.println("localFile: " + localFile.getName());
					System.out.println("what: " + what.getName());
					System.out.println("localfileLength: " + localFile.length());
					System.out.println("whatSize: " + what.getSize())*/;
					if ((localFile.length() == what.getSize())) {
						System.out.println("Fisierul " + what.getName()  + " din " + to + " deja era downloadat.");
						appendMessage("File " + what.getName() + " was already downloaded.");
					} else if ((localFile.length() < what.getSize()) && (localFile.length() != 0)) {
						appendMessage("Continuing download of file " + what.getName());
						System.out.println("Se continua downloadul lui " + what.getName() + " din " + to);
						resumeDownload(to, what, "");
					} else {
						System.out.println("localFile: " + localFile.getName() + " localfileLength: " + localFile.length());
						System.out.println("what: " + what.getName() + "whatSize: " + what.getSize());
						System.out.println("Se incepe downloadul fisierului " + what.getName()+ " din " + to);
						appendMessage("Starting to download file: "  + " din " + to + what.getName());
						downloadSingleFile(to, what);
					}
				}
			} catch (Exception e) {
			}
		}

		return true;
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
		boolean result = false;
		File theDir = new File(to + "\\" + what.getName());
		if (!theDir.exists()) {

			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException se) {
				logger.error("Error: " + se.getMessage());
			}
		}
		
		// maybe the directory isn't created yet
		//Thread.sleep(1000);
		//if (result) {
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
		//}

	}
}
