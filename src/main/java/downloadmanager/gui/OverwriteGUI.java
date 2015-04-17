package downloadmanager.gui;

import downloadmanager.constants.Constants;

public class OverwriteGUI extends PopupGUI{
	private static final long serialVersionUID = 1L;
	
	public OverwriteGUI(String name, long sourceFileSize, long targetFileSize) {
		super(name, sourceFileSize, targetFileSize, Constants.FILE_EXISTS);
		
		textLabel.setText("<html>Do you want to overwrite file " + name + "? <br> <br> Source file: "
				+ sourceFileSize + " bytes<br> <br>" + "Target file: " + targetFileSize + " bytes</html>");
	}
	
}

