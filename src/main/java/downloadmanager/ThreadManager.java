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

	/**
	 * Where the user is informed when the download of a file/directory started.
	 */
	private JTextArea display;
	/** The number of threads the user has selected. */
	private int noOfWorkers;
	/** Where the files the user has selected to be downloaded will be stored. */
	private List<Downloader> downloaderList;
	/** The files that the user has selected to be downloaded. */
	private FTPFile[] files;
	/**
	 * Represents all the files/directories on the server (also all that are in
	 * the table).
	 */
	private List<String> names;
	/** The FtpLogin with which the user connected and logged in to the host */
	private FTPLogin ftpLogin;
	/**
	 * The table with all the files/directories on the server and where the
	 * progress is updated.
	 */
	private CustomTable customTable;
	/** Where to download the file selected file on the server. */
	private String path;
	/** The ThreadPool where all the threads are stored */
	private ThreadPool threadPool;
	/**
	 * The button the user click to start downloading, or (if already
	 * downloaded) to add more files to the queue.
	 */
	private JButton downloadButton;
	/**
	 * The UI part where the user selects the number of threads, used for
	 * disabling it when the download start and enabling it when it finishes..
	 */
	private JComboBox<Integer> displayNoOfThreads;
	/** Used to check if the threads are paused or not */
	private boolean isPaused;

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
	 * The threadPool is given as many threads as possible (or as necessary).
	 * The maximum number being the one that the user set in the UI.
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

	/** Pauses the download of all the threads. */
	public void pause() {
		isPaused = true;
		for (int i = 0; i < downloaderList.size(); i++) {
			downloaderList.get(i).suspend();
		}
	}

	/** Resumes the download of all the threads. */
	public void resume() {
		isPaused = false;
		for (int i = 0; i < downloaderList.size(); i++) {
			downloaderList.get(i).resume();
		}

	}

	/**
	 * Updates the ThreadManager with the new settings.
	 * 
	 * @param customTable
	 *            the UI component where all the files, their type and the
	 *            progress is shown to the user.
	 * @param display where the user is notified about the start or end of a download.
	 * @param noOfThreads the number of threads selected by the user.
	 * @param names all the file names in the table
	 * @param files the files that the user selected to be downloaded.
	 * @param ftpLogin the ftpLogin with which the user connected and logged in.
	 * @param path where to download the files.
	 */
	public void update(CustomTable customTable, JTextArea display, int noOfThreads, List<String> names,
			FTPFile[] files, FTPLogin ftpLogin, String path) {
		this.customTable = customTable;
		this.display = display;
		this.noOfWorkers = noOfThreads;
		this.files = files;
		this.names = names;
		this.ftpLogin = ftpLogin;
		this.path = path;

		init();
	}

	/** Changes the role of the 'download button'. */
	public void setDownloadButton() {
		if (downloadButton.getText().equals(Constants.DOWNLOAD)) {
			downloadButton.setText(Constants.ADD_TO_QUEUE);
		} else {
			downloadButton.setText(Constants.DOWNLOAD);
			displayNoOfThreads.setEnabled(true);
		}

	}

	/**
	 * Adds a task to the current queue of downloading tasks.
	 * 
	 * @param noOfThreads
	 *            new noOfThreads (remains the same always in current version)
	 * @param names
	 *            all the files in the table.
	 * @param files
	 *            all the selected files.
	 */
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
				// if (downloaderList.size() < noOfThreads) {
				Task task = new Task(downloader);
				if (isPaused) {
					task.pause();
				}

				threadPool.execute(task);
			}
		}
	}

}
