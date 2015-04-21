package downloadmanager.gui;

import downloadmanager.constants.Constants;

/**
 * OverwriteGUI extends the PopupGUI and has almost the same functionality,
 * except that it provides some information to the user about what he is going
 * to overwrite.
 * 
 * At the moment, no actual need for OverwriteGUI to be a class, the label can
 * be set in PopupGUI, but added for possible future needs.
 *
 */
public class OverwriteGUI extends PopupGUI {
	private static final long serialVersionUID = 1L;

	public OverwriteGUI(String name, long sourceFileSize, long targetFileSize) {
		super(name, sourceFileSize, targetFileSize, Constants.FILE_EXISTS);

		textLabel.setText("<html>Do you want to overwrite file " + name + "? <br> <br> Source file: " + sourceFileSize
				+ " bytes<br> <br>" + "Target file: " + targetFileSize + " bytes</html>");
	}

}
