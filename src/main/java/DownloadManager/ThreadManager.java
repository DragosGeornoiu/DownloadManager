package DownloadManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.swing.JTextArea;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import DownloadManager.Constants.Constants;
import DownloadManager.GUI.CustomTable;
import DownloadManager.ThreadPool.ThreadPool;

/**
 *
 * Manages the initialization, pause and resume of all the Threads.
 *
 */
public class ThreadManager {
	final static Logger logger = Logger.getLogger(ThreadManager.class);

	private JTextArea display;
	private int noOfWorkers;
	private List<DownloadThread> threadList;
	// private ExecutorService executor;
	private FTPFile[] files;
	private List<String> names;
	private FTPLogin downloader;
	private CustomTable customTable;
	private String path;
	private ThreadPool threadPool;

	public ThreadManager(CustomTable customTable, JTextArea display, int noOfThreads, List<String> names,
			FTPFile[] files, FTPLogin downloader, String path) {
		this.customTable = customTable;
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
		logger.info(Constants.STARTING_DOWNLOAD);
		threadList = new ArrayList<DownloadThread>();

		// executor = Executors.newFixedThreadPool(noOfWorkers);

		for (int i = 0; i < files.length; i++) {
			if (names.contains(files[i].getName())) {
				FTPLogin ftpLogin = new FTPLogin(downloader.getServer(), 21);
				ftpLogin.login(downloader.getUser(), downloader.getPassword());

				ThreadToGUI displayer = new ThreadToGUI(display, customTable);
				threadList.add(new DownloadThread(displayer, ftpLogin.getFtpClient(), files[i], path));
			}
		}

		if (threadPool == null) {
			threadPool = new ThreadPool(noOfWorkers);
		} else {
			threadPool.setNoOfThreads(noOfWorkers);
		}

		for (int i = 0; i < threadList.size(); i++) {
			try {
				threadPool.execute(threadList.get(i));
			} catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			// executor.execute(threadList.get(i));
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

	public void update(CustomTable customTable, JTextArea display, int noOfThreads, List<String> names,
			FTPFile[] files, FTPLogin downloader, String path) {
		this.customTable = customTable;
		this.display = display;
		this.noOfWorkers = noOfThreads;
		this.files = files;
		this.names = names;
		this.downloader = downloader;
		this.path = path;
		
		init();

	}

}
