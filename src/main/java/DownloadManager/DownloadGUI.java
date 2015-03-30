package DownloadManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import sun.net.ftp.FtpClient;

public class DownloadGUI extends JFrame implements ActionListener {
	final static Logger logger = Logger.getLogger(DownloadGUI.class);
	private static final long serialVersionUID = 1L;

	private FTPLogin downloader;

	private JScrollPane scroll;
	private JTextArea display;
	private JButton selectPathButton;
	private JLabel pathLabel;
	private String path;
	private JLabel errorLabel;
	private JTextField noOfThreadsTextField;
	private JButton runButton;
	private int noOfThreads;
	private CustomCheckBoxGroup checkboxgroup;
	private String[] names;
	private FTPFile[] files;
	boolean pathWasChosen;

	public DownloadGUI(FTPLogin downloader) {
		super("DownloadGUI");
		this.setResizable(false);
		this.downloader = downloader;
		this.setSize(600, 550);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		this.add(panel);
		placeComponents(panel);

		this.setVisible(true);

	}

	private void placeComponents(JPanel panel) {
		panel.setLayout(null);

		JLabel hostnameLabel = new JLabel("You logged in succesfull");
		hostnameLabel.setBounds(10, 10, 160, 25);
		panel.add(hostnameLabel);

		selectPathButton = new JButton("Path");
		selectPathButton.addActionListener(this);
		selectPathButton.setBounds(10, 50, 100, 25);
		panel.add(selectPathButton);

		pathLabel = new JLabel("Path: no path selected");
		pathLabel.setBounds(10, 90, 250, 25);
		panel.add(pathLabel);

		JLabel threads = new JLabel("no. of threads: ");
		threads.setBounds(10, 130, 100, 25);
		panel.add(threads);

		noOfThreadsTextField = new JTextField();
		noOfThreadsTextField.setBounds(150, 130, 30, 26);
		panel.add(noOfThreadsTextField);

		errorLabel = new JLabel("");
		errorLabel.setBounds(10, 160, 160, 25);
		panel.add(errorLabel);

		runButton = new JButton("Donwload");
		runButton.addActionListener(this);
		runButton.setBounds(70, 220, 100, 25);
		runButton.setEnabled(false);
		panel.add(runButton);

		display = new JTextArea();
		display.setEditable(false);
		scroll = new JScrollPane(display, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		scroll.setBounds(10, 260, 270, 230);
		panel.add(scroll);

		files = new FTPFile[] {};
		try {
			files = getFiles();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		String[] names = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			names[i] = files[i].getName();
		}
		checkboxgroup = new CustomCheckBoxGroup(names);
		checkboxgroup.setBounds(300, 10, 250, 500);
		panel.add(checkboxgroup);
	}

	private FTPFile[] getFiles() throws IOException {
		// return downloader.getFtpClient().listNames();
		return downloader.getFtpClient().listFiles();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == selectPathButton) {
			path = choosePath();
			if (path == null) {
				pathLabel.setText("<html><font color='red'>Path: Invalid path</font></html>");
			} else {
				pathLabel.setText("Path: " + path);
				runButton.setEnabled(true);
			}
		} else if (e.getSource() == runButton) {

			if (noOfThreadsTextField.getText().trim().isEmpty()) {
				noOfThreads = 5;
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
				Collection<DownloadThread> collection = new ArrayList<DownloadThread>();

				// trebuie sa fac o lista numai cu fisierele selectate si sa
				// verific
				// sa downloadez numai ce e selectat

				List<String> names = new ArrayList<String>();
				List<JCheckBox> checkBoxes = checkboxgroup.getCheckBoxes();
				for (int i = 0; i < checkBoxes.size(); i++) {
					if (checkBoxes.get(i).isSelected()) {
						names.add(checkBoxes.get(i).getText());
					}
				}

				int end = 0;
				// trebuie sa fac sa ruleze pe 5 threaduri si sa downloadeze
				// toate fisierele, deci o verificare cu files.size si dupa...
				
				
				ExecutorService executor = Executors.newFixedThreadPool(noOfThreads);
				for (int i = 0; i < files.length; i++) {
					if (names.contains(files[i].getName())) {
						FTPLogin ftpLogin = new FTPLogin(downloader.getServer(), 21);
						ftpLogin.login(downloader.getUser(), downloader.getPassword());

						DownloadThread task = new DownloadThread(ftpLogin.getFtpClient(), files[i], path);
						collection.add(task);
					}
				}

				try {
					// display.setText(display.getText() +

					executor.invokeAll(collection);
					executor.shutdown();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

	}

	private void setErrorLabel() {
		noOfThreads = 5;
		errorLabel.setText("<html><font color='red'>Invalid number of threads. " + "Selcted default 5.</font></html>");

	}

	public String choosePath() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("choosertitle");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().toString();
		} else {
			return null;
		}
	}
}
