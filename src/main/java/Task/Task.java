package Task;

import DownloadManager.DownloadThread;

public class Task implements ITask {

	DownloadThread downloadThread;
	
	public Task(DownloadThread downloadThread) {
		this.downloadThread = downloadThread;
	}
	
	@Override
	public void execute() {
		downloadThread.run();
	}

}
