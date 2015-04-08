package DownloadManager.ThreadPool;

import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

public class Pool extends Thread {
	final static Logger logger = Logger.getLogger(Pool.class);

	private int identificator;
	private BlockingQueue<Runnable> taskQueue = null;
	private boolean isStopped = false;
	private ThreadPool threadPool;

	public Pool(BlockingQueue<Runnable> queue, int id, ThreadPool threadPool) {
		taskQueue = queue;
		this.identificator = id;
		this.threadPool = threadPool;
	}

	public void run() {
		logger.info("Run method called");
		Runnable r = null;
		
		while (!isStopped()) {
			synchronized (this) {
				while (taskQueue.isEmpty()) {
					try {
						// aici ii spun threadPool-ului ca thread-ul acesta si-a
						// incheiat download-ul si astepta sa fie refolosit.
						threadPool.finishedRun(this);
						wait();
					} catch (InterruptedException ignored) {
						logger.error(ignored.getMessage());
					}
				}

				try {
					r = (Runnable) taskQueue.take();
				} catch (Exception e) {
					logger.error(e.getMessage());
				}

			}

			try {
				r.run();
			} catch (RuntimeException e) {
				logger.error(e.getMessage());
			}
		}
	}

	public synchronized void doStop() {
		logger.info("doStop() method called");
		isStopped = true;
		this.interrupt(); // break pool thread out of dequeue() call.
	}

	public int getIdentificator() {
		return identificator;
	}

	public synchronized boolean isStopped() {
		return isStopped;
	}

	public void set() {
		notify();
	}

}