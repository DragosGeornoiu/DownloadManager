package DownloadManager;

import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import DownloadManager.GUI.CustomTable;

/**
 * Used to append to the table and textArea of the user interface from the
 * runing thread.
 *
 */
public class ThreadToGUI {
	final static Logger logger = Logger.getLogger(ThreadToGUI.class);

	private JTextArea textArea;
	private CustomTable customTable;

	public ThreadToGUI(JTextArea textArea, CustomTable customTable) {
		this.textArea = textArea;
		this.customTable = customTable;
	}

	public void appendToTextArea(String message) {
		logger.info("Appending to textArea message: " + message);
		textArea.append(message + "\n");
	}

	public void appendToProgress(String progress, int index) {
		logger.info("Appending to table at index: " + index + " progress: " + progress);
		customTable.setTextAt(progress, index, 4);
	}

	public int getIndexWhere(String name) {
		return customTable.getIndexWhere(name);
	}
}
