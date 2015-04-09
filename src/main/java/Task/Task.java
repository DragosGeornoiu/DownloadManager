package Task;

import DownloadManager.Downloader;

public class Task implements ITask {

	Downloader downloadThread;
	
	public Task(Downloader downloadThread) {
		this.downloadThread = downloadThread;
	}
	
	@Override
	public void execute() {
		downloadThread.execute();;
	}

}
