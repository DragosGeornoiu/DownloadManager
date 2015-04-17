package downloadmanager.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import downloadmanager.Downloader;
import downloadmanager.FTPLogin;
import downloadmanager.ThreadManager;
import downloadmanager.constants.Constants;

/**
 * 
 * DownloadGUI represents the user interface where he can select what to
 * download, where to download it and on how many threads to download.
 *
 */

public class DownloadGUI extends JFrame implements ActionListener {
	final static Logger logger = Logger.getLogger(DownloadGUI.class);
	private static final long serialVersionUID = 1L;

	private JTextField hostnameText;
	private JTextField userText;
	private JPasswordField passwordText;
	private JTextField portText;
	private FTPLogin ftpLogin;
	private JButton connectButton;
	private JLabel loginErrorLabel;
	private JLabel notConectedLabel;

	private JScrollPane scroll;
	private JTextArea display;
	private JButton selectPathButton;
	private JLabel pathLabel;
	private String path;
	private JLabel errorLabel;
	// private JTextField noOfThreadsTextField;
	private JComboBox<Integer> noOfThreadsComboBox;
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

	public DownloadGUI() {
		super("DownloadGUI");
		this.setResizable(false);
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

		notConectedLabel = new JLabel("NOT CONNECTED TO ANY SERVER");
		notConectedLabel.setBounds(450, 145, 450, 500);
		panel.add(notConectedLabel);

		JLabel hostnameLabel = new JLabel("Host:");
		hostnameLabel.setBounds(10, 10, 160, 25);
		panel.add(hostnameLabel);

		hostnameText = new JTextField(20);
		hostnameText.setBounds(40, 10, 100, 25);
		panel.add(hostnameText);

		JLabel userLabel = new JLabel("Username:");
		userLabel.setBounds(150, 10, 160, 25);
		panel.add(userLabel);

		userText = new JTextField(20);
		userText.setBounds(215, 10, 100, 25);
		panel.add(userText);

		JLabel passwordLabel = new JLabel(Constants.PASSWORD_LABEL);
		passwordLabel.setBounds(325, 10, 160, 25);
		panel.add(passwordLabel);

		passwordText = new JPasswordField(20);
		passwordText.setBounds(390, 10, 100, 25);
		panel.add(passwordText);

		JLabel portLabel = new JLabel("Port:");
		portLabel.setBounds(500, 10, 160, 25);
		panel.add(portLabel);

		portText = new JTextField(20);
		portText.setBounds(530, 10, 100, 25);
		panel.add(portText);

		connectButton = new JButton(Constants.LOGIN_BUTTON);
		connectButton.setBounds(650, 10, 100, 25);
		panel.add(connectButton);
		connectButton.addActionListener(this);

		loginErrorLabel = new JLabel("");
		loginErrorLabel.setBounds(10, 50, 500, 25);
		panel.add(loginErrorLabel);

		selectPathButton = new JButton(Constants.PATH_LABEL);
		selectPathButton.addActionListener(this);
		selectPathButton.setBounds(10, 95, 100, 25);
		panel.add(selectPathButton);

		pathLabel = new JLabel(Constants.NO_PATH_SELECTED);
		pathLabel.setBounds(10, 135, 250, 25);
		panel.add(pathLabel);

		JLabel threads = new JLabel(Constants.NUMBER_OF_THREADS_LABEL);
		threads.setBounds(10, 175, 100, 25);
		panel.add(threads);

		// noOfThreadsTextField = new JTextField();
		noOfThreadsComboBox = new JComboBox<Integer>();
		noOfThreadsComboBox.setSize(100, 100);

		for (int i = 1; i <= 10; i++) {
			noOfThreadsComboBox.addItem(i);
		}
		noOfThreadsComboBox.setBounds(95, 175, 50, 30);
		noOfThreadsComboBox.setEditable(false);
		panel.add(noOfThreadsComboBox);

		errorLabel = new JLabel("");
		errorLabel.setBounds(10, 205, 160, 25);
		panel.add(errorLabel);

		runButton = new JButton(Constants.DOWNLOAD);
		runButton.addActionListener(this);
		runButton.setBounds(10, 270, 120, 25);
		runButton.setEnabled(false);
		panel.add(runButton);

		pauseButton = new JButton(Constants.PAUSE);
		pauseButton.addActionListener(this);
		pauseButton.setBounds(130, 270, 120, 25);
		panel.add(pauseButton);

		refreshButton = new JButton(Constants.REFRESH);
		refreshButton.addActionListener(this);
		refreshButton.setBounds(350, 55, 100, 25);
		// panel.add(refreshButton);

		clearButton = new JButton(Constants.CLEAR);
		clearButton.addActionListener(this);
		clearButton.setBounds(90, 545, 100, 25);
		panel.add(clearButton);

		display = new JTextArea();
		display.setEditable(false);
		scroll = new JScrollPane(display, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		scroll.setBounds(10, 305, 270, 230);
		panel.add(scroll);

		allCheckBox = new JCheckBox(Constants.SELECT_ALL);
		allCheckBox.setBounds(590, 65, 100, 25);
		allCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				customTable.setAllCheckBoxes(allCheckBox.isSelected());
			}
		});
		allCheckBox.setVisible(false);
		panel.add(allCheckBox);

		JRootPane rootPane = SwingUtilities.getRootPane(connectButton);
		rootPane.setDefaultButton(connectButton);

		// initialiseTable(ftpLogin);
	}

	/**
	 * Initializes the table with the files hosted in the given hostname from
	 * the LoginGUI. It gives some details about each one: name, type (directory
	 * or not), size and a column of checkBoxes to check if you want to download
	 * it or not.
	 */
	private void initialiseTable(FTPLogin ftpLogin) {
		logger.info("Initialising the table");

		files = new FTPFile[] {};
		try {
			files = getFiles(ftpLogin);
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
		customTable.setBounds(300, 90, 450, 500);
		customTable.setVisible(false);
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
	private FTPFile[] getFiles(FTPLogin ftpLogin) throws IOException {
		// ftpLogin.setFtpClient(new FTPClient());
		// ftpLogin.login(ftpLogin.getUser(), ftpLogin.getPassword());
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
				if(customTable != null) {
					customTable.setAllProgressesToZero();
				}
			}
		} else if (e.getSource() == runButton) {
			logger.info("Download button was pressed.");
			// The download of the selected files starts.

			Downloader.yesToAllOverwrite = false;
			Downloader.noToAllOverwrite = false;
			Downloader.yesToAllReconnect = false;
			Downloader.noToAllReconnect = false;
			
			if (runButton.getText().equals(Constants.DOWNLOAD)) {
				// if (noOfThreadsTextField.getText().trim().isEmpty()) {
				noOfThreads = (Integer) noOfThreadsComboBox.getSelectedItem();
				if (noOfThreads <= 0) {
					setErrorLabel();
				}
				/*
				 * } else { try { noOfThreads =
				 * Integer.parseInt(noOfThreadsTextField.getText());
				 * errorLabel.setText(""); } catch (Exception ex) {
				 * logger.error(ex.getMessage()); setErrorLabel(); } }
				 */

				if (noOfThreads <= 0) {
					setErrorLabel();
				}

				List<String> names = new ArrayList<String>();
				// List<JCheckBox> checkBoxes = customTable.getCheckBoxes();

				for (int i = 0; i < customTable.getSizeOfElements(); i++) {
					if (((Boolean) customTable.retVal(i, Constants.CHECK_COLUMN_POSITION) == true)) {
						names.add((String) customTable.retVal(i, 0));
					}
				}

				if (names.size() != 0) {
					noOfThreadsComboBox.setEnabled(false);
					runButton.setText(Constants.ADD_TO_QUEUE);
					pauseButton.setText(Constants.PAUSE);

					// nu ar trebui sa fac o noua instanta a threadManager la
					// fiecare download, ci sa o folosesc pe cea precedenta
					if (workerManager == null) {
						workerManager = new ThreadManager(noOfThreadsComboBox, runButton, customTable, display,
								noOfThreads, names, files, ftpLogin, path);
						workerManager.init();
					} else {
						workerManager.update(customTable, display, noOfThreads, names, files, ftpLogin, path);
					}
				}
			} else if (runButton.getText().equals(Constants.ADD_TO_QUEUE)) {
				List<String> names = new ArrayList<String>();
				// List<JCheckBox> checkBoxes = customTable.getCheckBoxes();

				for (int i = 0; i < customTable.getSizeOfElements(); i++) {
					if (((Boolean) customTable.retVal(i, Constants.CHECK_COLUMN_POSITION) == true)) {
						names.add((String) customTable.retVal(i, 0));
					}
				}

				workerManager.addToQueue(noOfThreads, names, files);
			}
		} else if (e.getSource() == refreshButton) {
			logger.info("Refesh button was pressed.");
			// refreshes the table of data to be downloaded, a new file might of
			// been added there and we shouldn't have to close the application
			// to see it.
			initialiseTable(ftpLogin);
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

				if (workerManager != null) {
					workerManager.pause();
				}

				// resumes the download.
			} else if (pauseButton.getText().equals(Constants.RESUME)) {
				logger.info("Resume button was pressed.");
				display.append(Constants.RESUME_MESSAGE + "\n");
				pauseButton.setText(Constants.PAUSE);
				// AAAAAAAAAAAAAAAAAAAAAAAAAA

				// try {
				// noOfThreads =
				// Integer.parseInt(noOfThreadsTextField.getText());
				// errorLabel.setText("");
				// } catch (Exception ex) {
				// logger.error(ex.getMessage());
				// setErrorLabel();
				// }
				// noOfThreads

				if (workerManager != null) {
					workerManager.resume();
				}
			}
		} else if (e.getSource() == connectButton) {
			logger.info("Login button was pressed");
			if (connectButton.getText().equals("Connect")) {
				try {
					ftpLogin = new FTPLogin(hostnameText.getText().trim(), Integer.parseInt(portText.getText().trim()));
					if (ftpLogin.login(userText.getText().trim(), new String(passwordText.getPassword()))) {
						loginErrorLabel.setText("<html><font color='green'>Connected...</font></html>");
						connectButton.setText("Disconnect");

						hostnameText.setEditable(false);
						userText.setEditable(false);
						passwordText.setEditable(false);
						portText.setEditable(false);

						initialiseTable(ftpLogin);
						notConectedLabel.setVisible(false);
						customTable.setVisible(true);
						allCheckBox.setVisible(true);
					} else {
						logger.error(ftpLogin.getFtpClient().getReplyString());
						if (ftpLogin.getFtpClient().getReplyString() != null) {
							loginErrorLabel.setText("<html><font color='red'>"
									+ ftpLogin.getFtpClient().getReplyString().replaceAll("\\d","") + "</font></html>");
						} else {
							loginErrorLabel.setText("<html><font color='red'>"
									+ "Problems with credentials. Probably host or port fields are not correct."
									+ "</font></html>");
						}
					}
					
				} catch (Exception ex) {
					loginErrorLabel
							.setText("<html><font color='red'>Problems logging in. Check again your credentials.</font></html>");
				}
			} else if (connectButton.getText().equals("Disconnect")) {
				try {
					ftpLogin.getFtpClient().logout();
					ftpLogin.getFtpClient().disconnect();

					
					loginErrorLabel.setText("<html><font color='green'>Disconnected...</font></html>");

					
					customTable.setAllCheckBoxes(false);
					
					
				} catch (IOException e1) {
					loginErrorLabel
							.setText("<html><font color='red'>Logged out. But, your connection was closed previously to logging out...</font></html>");
				} finally {
					connectButton.setText("Connect");
					hostnameText.setEditable(true);
					userText.setEditable(true);
					passwordText.setEditable(true);
					portText.setEditable(true);
					notConectedLabel.setVisible(true);
					customTable.setVisible(false);
					allCheckBox.setVisible(false);
				}
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
		// noOfThreadsTextField.setText(Integer.toString(noOfThreads));
		noOfThreadsComboBox.setSelectedIndex(Constants.DEFAULT_NUMBER_OF_THREADS - 1);
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
