package DownloadManager.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import DownloadManager.FTPLogin;
import DownloadManager.ThreadManager;
import DownloadManager.Constants.Constants;

/**
 * 
 * DownloadGUI represents the user interface where he can select what to
 * download, where to download it and on how many threads to download.
 *
 */

public class DownloadGUI extends JFrame implements ActionListener {
	final static Logger logger = Logger.getLogger(DownloadGUI.class);
	private static final long serialVersionUID = 1L;

	private FTPLogin ftpLogin;
	private JScrollPane scroll;
	private JTextArea display;
	private JButton selectPathButton;
	private JLabel pathLabel;
	private String path;
	private JLabel errorLabel;
	private JTextField noOfThreadsTextField;
	private JButton runButton;
	private JButton refreshButton;
	private JButton clearButton;
	private JButton pauseButton;
	private JCheckBox allCheckBox;
	private int noOfThreads;
	private CustomTable customTable;
	private FTPFile[] files;
	private JPanel panel;
	private ThreadManager workerManager;

	public DownloadGUI(FTPLogin downloader) {
		super("DownloadGUI");
		this.setResizable(false);
		this.ftpLogin = downloader;
		this.setSize(820, 620);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new JPanel();
		this.add(panel);
		placeComponents();
		this.setVisible(true);
	}

	/**
	 * Places all the components of the DownloadGUI in the panel.
	 */
	private void placeComponents() {
		logger.info("Placing components on the panel.");
		panel.setLayout(null);

		JLabel hostnameLabel = new JLabel();
		hostnameLabel.setBounds(10, 10, 160, 25);
		panel.add(hostnameLabel);

		selectPathButton = new JButton(Constants.PATH_LABEL);
		selectPathButton.addActionListener(this);
		selectPathButton.setBounds(10, 50, 100, 25);
		panel.add(selectPathButton);

		pathLabel = new JLabel(Constants.NO_PATH_SELECTED);
		pathLabel.setBounds(10, 90, 250, 25);
		panel.add(pathLabel);

		JLabel threads = new JLabel(Constants.NUMBER_OF_THREADS_LABEL);
		threads.setBounds(10, 130, 100, 25);
		panel.add(threads);

		noOfThreadsTextField = new JTextField();
		noOfThreadsTextField.setBounds(150, 130, 30, 26);
		panel.add(noOfThreadsTextField);

		errorLabel = new JLabel("");
		errorLabel.setBounds(10, 160, 160, 25);
		panel.add(errorLabel);

		runButton = new JButton(Constants.DOWNLOAD);
		runButton.addActionListener(this);
		runButton.setBounds(10, 225, 100, 25);
		runButton.setEnabled(false);
		panel.add(runButton);

		pauseButton = new JButton(Constants.PAUSE);
		pauseButton.addActionListener(this);
		pauseButton.setBounds(130, 225, 100, 25);
		panel.add(pauseButton);

		refreshButton = new JButton(Constants.REFRESH);
		refreshButton.addActionListener(this);
		refreshButton.setBounds(350, 10, 100, 25);
		panel.add(refreshButton);

		allCheckBox = new JCheckBox(Constants.SELECT_ALL);
		allCheckBox.setBounds(590, 25, 100, 25);
		allCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				customTable.setAllCheckBoxes(allCheckBox.isSelected());
			}
		});
		panel.add(allCheckBox);

		clearButton = new JButton(Constants.CLEAR);
		clearButton.addActionListener(this);
		clearButton.setBounds(90, 500, 100, 25);
		panel.add(clearButton);

		display = new JTextArea();
		display.setEditable(false);
		scroll = new JScrollPane(display, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		scroll.setBounds(10, 260, 270, 230);
		panel.add(scroll);

		initialiseTable();
	}

	/**
	 * Initializes the table with the files hosted in the given hostname from
	 * the LoginGUI. It gives some details about each one: name, type (directory
	 * or not), size and a column of checkBoxes to check if you want to download
	 * it or not.
	 */
	private void initialiseTable() {
		logger.info("Initialising the table");
		files = new FTPFile[] {};
		try {
			files = getFiles();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		String[] names = new String[files.length];
		String[] types = new String[files.length];
		String[] sizes = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			names[i] = files[i].getName();
			if (files[i].isDirectory()) {
				types[i] = Constants.DIRECTORY;
				sizes[i] = FileUtils.byteCountToDisplaySize(directorySize(files[i]));
			} else {
				types[i] = " - ";
				sizes[i] = FileUtils.byteCountToDisplaySize(files[i].getSize());
			}
		}

		if (customTable != null) {
			panel.remove(customTable);
		}

		Object[] columns = { Constants.TABLE_COLUMN_NAME, Constants.TABLE_COLUMN_TYPE, Constants.TABLE_COLUMN_SIZE,
				Constants.TABLE_COLUMN_CHECK, Constants.TABLE_COLUMN_PROGRESS };

		customTable = new CustomTable(columns, names, types, sizes);
		customTable.setBounds(300, 50, 450, 500);

		panel.revalidate();
		panel.repaint();
		panel.add(customTable);

	}

	/**
	 * Returns all files from the hostname where the user has access.
	 * 
	 * @return the files as a FTPFile array.
	 * @throws IOException
	 *             if an I/O error occurs while either sending a command to the
	 *             server or receiving a reply from the server.
	 */
	private FTPFile[] getFiles() throws IOException {
		ftpLogin.setFtpClient(new FTPClient());
		ftpLogin.login(ftpLogin.getUser(), ftpLogin.getPassword());
		return ftpLogin.getFtpClient().listFiles();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == selectPathButton) {
			logger.info("Path button was pressed");
			// allows the user to select the path
			path = choosePath();
			if (path == null) {
				pathLabel.setText(Constants.INVALID_PATH_COLOR_RED);
			} else {
				pathLabel.setText(Constants.PATH_LABEL + ": " + path);
				runButton.setEnabled(true);
			}
		} else if (e.getSource() == runButton) {
			logger.info("Download button was pressed.");
			// The download of the selected files starts.
			pauseButton.setText(Constants.PAUSE);
			if (noOfThreadsTextField.getText().trim().isEmpty()) {
				setErrorLabel();
			} else {
				try {
					noOfThreads = Integer.parseInt(noOfThreadsTextField.getText());
					errorLabel.setText("");
				} catch (Exception ex) {
					logger.error(ex.getMessage());
					setErrorLabel();
				}
			}

			if (noOfThreads <= 0) {
				setErrorLabel();
			} else {

				List<String> names = new ArrayList<String>();
				// List<JCheckBox> checkBoxes = customTable.getCheckBoxes();

				for (int i = 0; i < customTable.getSizeOfElements(); i++) {
					if ((Boolean) customTable.retVal(i, 3) == true) {
						names.add((String) customTable.retVal(i, 0));
					}
				}
				
				//nu ar trebui sa fac o noua instanta a threadManager la fiecare download, ci sa o folosesc pe cea precedenta
				if (workerManager == null) {
					workerManager = new ThreadManager(customTable, display, noOfThreads, names, files, ftpLogin, path);
					workerManager.init();
				} else {
					workerManager.update(customTable, display, noOfThreads, names, files, ftpLogin, path);
				}
			}
		} else if (e.getSource() == refreshButton) {
			logger.info("Refesh button was pressed.");
			// refreshes the table of data to be downloaded, a new file might of
			// been added there and we shouldn't have to close the application
			// to see it.
			initialiseTable();
			allCheckBox.setSelected(false);
		} else if (e.getSource() == clearButton) {
			logger.info("Clear button was pressed.");
			// cleares the display textArea.
			display.setText("");
		} else if (e.getSource() == pauseButton) {

			// pauses the download.
			if (pauseButton.getText().equals(Constants.PAUSE)) {
				logger.info("Pause button was pressed.");
				display.append(Constants.PAUSE_MESSAGE + "\n");
				pauseButton.setText(Constants.RESUME);
				workerManager.pause();

				// resumes the download.
			} else if (pauseButton.getText().equals(Constants.RESUME)) {
				logger.info("Resume button was pressed.");
				display.append(Constants.RESUME_MESSAGE + "\n");
				pauseButton.setText(Constants.PAUSE);
				workerManager.resume();
			}
		}

	}

	/**
	 * The user hasn't picked or inserted an invalid number of threads to be
	 * used, so the number of threads is set to a default value of 5 and using
	 * the error label, the user is informed of that.
	 */
	private void setErrorLabel() {
		noOfThreads = Constants.DEFAULT_NUMBER_OF_THREADS;
		noOfThreadsTextField.setText(Integer.toString(noOfThreads));
		errorLabel.setText(Constants.INVALID_NUMBER_OF_THREADS_RED);
	}

	/**
	 * The file chooser is set up for the user to select the directory to
	 * download to.
	 * 
	 * @return the path where to download, selected by the user.
	 */
	public String choosePath() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Select a directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().toString();
		} else {
			return null;
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
	public long directorySize(FTPFile files2) {
		long length = 0;

		try {
			ftpLogin.getFtpClient().changeWorkingDirectory(files2.getName());
			for (FTPFile file : ftpLogin.getFtpClient().listFiles()) {
				if (file.isFile()) {
					length += file.getSize();
				} else {
					ftpLogin.getFtpClient().changeWorkingDirectory(file.getName());
					length += directorySize(file);
					ftpLogin.getFtpClient().changeToParentDirectory();
				}
			}
		} catch (Exception e) {
			logger.error("Error");
		}
		return length;
	}

}
