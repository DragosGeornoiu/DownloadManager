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
		while (true && !isStopped()) {
			synchronized (this) {
				while (taskQueue.isEmpty()) {
					try {
						// aici probabil ar trebui sa ii spun threadPool-ului ca
						// thread-ul acesta si-a incheiat download-ul si astepta
						// sa fie refolosit.
						threadPool.finishedRun(this);
						System.out.println("FINISHED a thread: " + identificator);
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

			// If we don't catch RuntimeException,
			// the pool could leak threads
			try {
				System.out.println("EXECUTING RUN OF POOL");
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