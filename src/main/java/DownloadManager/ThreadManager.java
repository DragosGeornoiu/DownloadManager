package DownloadManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JTextArea;

import org.apache.commons.net.ftp.FTPFile;

/**
 *
 * Manages the initialization, pause and resume of all the Threads.
 *
 */
public class ThreadManager {
	private JTextArea display;
	private int noOfWorkers;
	private List<DownloadThread> threadList;
	private ExecutorService executor;
	private FTPFile[] files;
	private List<String> names;
	private FTPLogin downloader;
	private String path;

	public ThreadManager(JTextArea display, int noOfThreads, List<String> names, FTPFile[] files, FTPLogin downloader,
			String path) {
		this.display = display;
		this.noOfWorkers = noOfThreads;
		this.files = files;
		this.names = names;
		this.downloader = downloader;
		this.path = path;
	}

	/**
	 * The executor is given a fixed thread pool with the noOfWorkers inserted
	 * in the user interface and starts the download of each file selected to be
	 * downloaded on a thread (if the thread pool allows it)
	 */
	public void init() {
		threadList = new ArrayList<DownloadThread>();

		executor = Executors.newFixedThreadPool(noOfWorkers);

		for (int i = 0; i < files.length; i++) {
			if (names.contains(files[i].getName())) {
				FTPLogin ftpLogin = new FTPLogin(downloader.getServer(), 21);
				ftpLogin.login(downloader.getUser(), downloader.getPassword());

				threadList.add(new DownloadThread(display, ftpLogin.getFtpClient(), files[i], path));
			}
		}

		for (int i = 0; i < threadList.size(); i++) {
			executor.execute(threadList.get(i));
		}

	}

	/**
	 * Pauses the download of all the threads.
	 */
	public void pause() {
		for (int i = 0; i < threadList.size(); i++) {
			threadList.get(i).suspend();
		}
	}

	/**
	 * Resumes the download of all the threads.
	 */
	public void resume() {
		for (int i = 0; i < threadList.size(); i++) {
			threadList.get(i).resume();
		}
	}

}