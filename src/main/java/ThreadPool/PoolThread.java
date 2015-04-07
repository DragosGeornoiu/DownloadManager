package ThreadPool;

import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

public class PoolThread extends Thread {
	final static Logger logger = Logger.getLogger(PoolThread.class);

	private BlockingQueue<Runnable> taskQueue = null;
	private boolean isStopped = false;

	public PoolThread(BlockingQueue<Runnable> queue) {
		taskQueue = queue;
	}

	public void run() {
		logger.info("Run method called");
		while (!isStopped()) {
			try {
				Runnable runnable = (Runnable) taskQueue.take();
				runnable.run();
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
	}

	public synchronized void doStop() {
		logger.info("doStop() method called");
		isStopped = true;
		this.interrupt(); // break pool thread out of dequeue() call.
	}

	public synchronized boolean isStopped() {
		return isStopped;
	}
}