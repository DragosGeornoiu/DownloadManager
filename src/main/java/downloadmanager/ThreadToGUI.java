package downloadmanager;

import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import downloadmanager.gui.CustomTable;

/**
 * Used to append to the table and textArea of the user interface from the
 * runing thread.
 *
 */
public class ThreadToGUI {
	final static Logger logger = Logger.getLogger(ThreadToGUI.class);

	/**
	 * The component of the UI where the user is informed when a download start
	 * and finishes.
	 */
	private JTextArea textArea;
	/**
	 * The component of the UI where the user is shown the files stored where he
	 * connected and the progress of their download.
	 */
	private CustomTable customTable;

	public ThreadToGUI(JTextArea textArea, CustomTable customTable) {
		this.textArea = textArea;
		this.customTable = customTable;
	}

	/**
	 * Appends the messsage to the textArea.
	 * 
	 * @param message
	 *            the text to be appended
	 */
	public void appendToTextArea(String message) {
		logger.info("Appending to textArea message: " + message);
		textArea.append(message + "\n");
	}

	/**
	 * Appends the progress to the table.
	 * 
	 * @param progress
	 *            the progress to be appended.
	 * @param index
	 *            the value to be appended.
	 * 
	 * @return the name of the file where it appends.
	 */
	public String appendToProgress(String progress, int index) {
		logger.info("Appending to table at index: " + index + " progress: " + progress);
		customTable.setTextAt(progress, index, 4);

		return getNameWhereIndex(index);
	}

	/**
	 * Returns the index where the name of the file is equal to the String given
	 * as parameter.
	 * 
	 * @param name
	 *            the String to check for.
	 * 
	 * @return the index where the name matches.
	 */
	public int getIndexWhere(String name) {
		return customTable.getIndexWhere(name);
	}

	/**
	 * Return the name where the index of row is equal to the int given as
	 * parameter.
	 * 
	 * @param index
	 *            the index of the row.
	 * @return the String representing the name where the index of the table is
	 *         equal to the one given as parameter.
	 */
	public String getNameWhereIndex(int index) {
		return (String) customTable.retVal(index, 0);

	}
}
