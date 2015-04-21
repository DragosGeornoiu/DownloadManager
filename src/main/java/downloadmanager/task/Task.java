package downloadmanager.task;

import downloadmanager.Downloader;

public class Task implements ITask {

	Downloader downloadThread;
	
	public Task(Downloader downloadThread) {
		this.downloadThread = downloadThread;
	}
	
	@Override
	public void execute() {
		downloadThread.startDownload();;
	}

	public Downloader getDownloadThread() {
		return downloadThread;
	}

	public void setDownloadThread(Downloader downloadThread) {
		this.downloadThread = downloadThread;
	}

	@Override
	public boolean isSuspended() {
		return downloadThread.isSuspended();
	}

	@Override
	public void resume() {
		downloadThread.resume();
	}

	@Override
	public void pause() {
		downloadThread.suspend();
	}
	
	

}
