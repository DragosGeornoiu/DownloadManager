package DownloadManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
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

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

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
	private JButton refreshButton;
	private JButton clearButton;
	private JButton stopButton;
	private int noOfThreads;
	private CustomCheckBoxGroup checkboxgroup;
	// private String[] names;
	private FTPFile[] files;
	private JPanel panel;
	private ExecutorService executor;
	List<Worker> workerList;

	public DownloadGUI(FTPLogin downloader) {
		super("DownloadGUI");
		this.setResizable(false);
		this.downloader = downloader;
		this.setSize(600, 580);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new JPanel();
		this.add(panel);
		placeComponents();

		this.setVisible(true);

	}

	private void placeComponents() {
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
		runButton.setBounds(10, 225, 100, 25);
		runButton.setEnabled(false);
		panel.add(runButton);

		stopButton = new JButton("Stop");
		stopButton.addActionListener(this);
		stopButton.setBounds(130, 225, 100, 25);
		panel.add(stopButton);

		refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(this);
		refreshButton.setBounds(350, 10, 100, 25);
		panel.add(refreshButton);

		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		clearButton.setBounds(90, 500, 100, 25);
		panel.add(clearButton);

		display = new JTextArea();
		display.setEditable(false);
		scroll = new JScrollPane(display, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		scroll.setBounds(10, 260, 270, 230);
		panel.add(scroll);

		initialiseCheckGroup();
	}

	private void initialiseCheckGroup() {
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

		if (checkboxgroup != null) {
			panel.remove(checkboxgroup);
		}

		checkboxgroup = new CustomCheckBoxGroup(names);
		checkboxgroup.setBounds(300, 50, 250, 500);

		panel.revalidate();
		panel.repaint();
		panel.add(checkboxgroup);

	}

	private FTPFile[] getFiles() throws IOException {
		downloader.setFtpClient(new FTPClient());
		downloader.login(downloader.getUser(), downloader.getPassword());
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
			stopButton.setText("Stop");
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
				workerList = new ArrayList<Worker>();

				List<String> names = new ArrayList<String>();
				List<JCheckBox> checkBoxes = checkboxgroup.getCheckBoxes();
				for (int i = 0; i < checkBoxes.size(); i++) {
					if (checkBoxes.get(i).isSelected()) {
						names.add(checkBoxes.get(i).getText());
					}
				}

				executor = Executors.newFixedThreadPool(noOfThreads);
				for (int i = 0; i < files.length; i++) {
					if (names.contains(files[i].getName())) {
						FTPLogin ftpLogin = new FTPLogin(downloader.getServer(), 21);
						ftpLogin.login(downloader.getUser(), downloader.getPassword());

						// DownloadThread task = new DownloadThread(display,
						// ftpLogin.getFtpClient(), files[i], path);
						Worker task = new Worker(display, ftpLogin.getFtpClient(), files[i], path);
						// collection.add(task);

						// poti vedea si cu future, dar cred ca asta inseamna un
						// singur
						// rezultat, nu multiple, insa atunci nu ar mai trebui
						// sa trimiti
						// display ca parametru.
						// executor.submit(task);
						workerList.add(task);
					}
				}

				for (int i = 0; i < workerList.size(); i++) {
					executor.submit(workerList.get(i));
				}

				/*
				 * try { // display.setText(display.getText() +
				 * 
				 * executor.invokeAll(collection); executor.shutdown(); } catch
				 * (Exception ex) { ex.printStackTrace(); }
				 */
			}
		} else if (e.getSource() == refreshButton) {
			initialiseCheckGroup();
		} else if (e.getSource() == clearButton) {
			display.setText("");
		} else if (e.getSource() == stopButton) {
			if (stopButton.getText().equals("Stop")) {
				display.append("---PAUSE---" + "\n");
				stopButton.setText("Resume");
				
				for (int i = 0; i < workerList.size(); i++) {
					workerList.get(i).pause();
				}
			} else if (stopButton.getText().equals("Resume")) {
				display.append("---RESUME---" + "\n");
				stopButton.setText("Stop");
				
				for (int i = 0; i < workerList.size(); i++) {
					workerList.get(i).resume();
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
