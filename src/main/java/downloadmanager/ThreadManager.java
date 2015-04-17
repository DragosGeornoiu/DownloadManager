package downloadmanager;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextArea;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import downloadmanager.constants.Constants;
import downloadmanager.gui.CustomTable;
import downloadmanager.task.Task;
import downloadmanager.threadpool.ThreadPool;

/**
 *
 * Manages the initialization, pause and resume of all the Threads.
 *
 */
public class ThreadManager {
	final static Logger logger = Logger.getLogger(ThreadManager.class);

	private JTextArea display;
	private int noOfWorkers;
	private List<Downloader> downloaderList;
	private FTPFile[] files;
	private List<String> names;
	private FTPLogin ftpLogin;
	private CustomTable customTable;
	private String path;
	private ThreadPool threadPool;
	private JButton downloadButton;
	private JComboBox<Integer> displayNoOfThreads;
	boolean isPaused;

	public ThreadManager(JComboBox<Integer> noOfThreadsTextField, JButton downloadButton, CustomTable customTable,
			JTextArea display, int noOfThreads, List<String> names, FTPFile[] files, FTPLogin ftpLogin, String path) {
		this.customTable = customTable;
		this.display = display;
		this.noOfWorkers = noOfThreads;
		this.files = files;
		this.names = names;
		this.ftpLogin = ftpLogin;
		this.path = path;
		this.downloadButton = downloadButton;
		this.displayNoOfThreads = noOfThreadsTextField;
	}

	/**
	 * The executor is given a fixed thread pool with the noOfWorkers inserted
	 * in the user interface and starts the download of each file selected to be
	 * downloaded on a thread (if the thread pool allows it)
	 */
	public void init() {
		logger.info(Constants.STARTING_DOWNLOAD);
		downloaderList = new ArrayList<Downloader>();

		for (int i = 0; i < files.length; i++) {
			if (names.contains(files[i].getName())) {
				FTPLogin loginFtp = new FTPLogin(ftpLogin.getServer(), ftpLogin.getPort());
				loginFtp.login(ftpLogin.getUser(), ftpLogin.getPassword());

				ThreadToGUI displayer = new ThreadToGUI(display, customTable);
				downloaderList.add(new Downloader(displayer, loginFtp, files[i], path));
			}
		}

		if (threadPool == null) {
			threadPool = new ThreadPool(noOfWorkers, this);
		} else {
			threadPool.setNoOfThreads(noOfWorkers);
		}

		for (int i = 0; i < downloaderList.size(); i++) {
			try {
				threadPool.execute(new Task(downloaderList.get(i)));
			} catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}

	}

	/**
	 * Pauses the download of all the threads.
	 */
	public void pause() {
		isPaused = true;
		for (int i = 0; i < downloaderList.size(); i++) {
			downloaderList.get(i).suspend();
		}
	}

	/**
	 * Resumes the download of all the threads.
	 */
	public void resume() {
		isPaused = false;
		for (int i = 0; i < downloaderList.size(); i++) {
			downloaderList.get(i).resume();
		}

	}

	public void update(CustomTable customTable, JTextArea display, int noOfThreads, List<String> names,
			FTPFile[] files, FTPLogin downloader, String path) {
		this.customTable = customTable;
		this.display = display;
		this.noOfWorkers = noOfThreads;
		this.files = files;
		this.names = names;
		this.ftpLogin = downloader;
		this.path = path;

		init();
	}

	public void setDownloadButton() {
		if (downloadButton.getText().equals(Constants.DOWNLOAD)) {
			downloadButton.setText(Constants.ADD_TO_QUEUE);
		} else {
			downloadButton.setText(Constants.DOWNLOAD);
			displayNoOfThreads.setEnabled(true);
		}

	}

	public void addToQueue(int noOfThreads, List<String> names, FTPFile[] files) {
		// vezi ce se adauga in queue

		this.names = names;
		this.noOfWorkers = noOfThreads;
		this.files = files;

		for (int i = 0; i < files.length; i++) {
			if (names.contains(files[i].getName())) {
				FTPLogin loginFtp = new FTPLogin(ftpLogin.getServer(), 21);
				loginFtp.login(ftpLogin.getUser(), ftpLogin.getPassword());

				ThreadToGUI displayer = new ThreadToGUI(display, customTable);
				Downloader downloader = new Downloader(displayer, loginFtp, files[i], path);
				downloaderList.add(downloader);
				//if (downloaderList.size() < noOfThreads) {
					Task task = new Task(downloader);
					if (isPaused) {
						task.pause();
					}
					
					threadPool.execute(task);
			}
		}
	}

}
