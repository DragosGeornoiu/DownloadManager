package downloadmanager.threadpool;

import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import downloadmanager.task.ITask;

public class Pool extends Thread {
	final static Logger logger = Logger.getLogger(Pool.class);

	private int identificator;
	private BlockingQueue<ITask> taskQueue = null;
	private volatile boolean isStopped = false;
	private ThreadPool threadPool;
	private static int mCount;
	private ITask task;

	public Pool(BlockingQueue<ITask> queue, ThreadPool threadPool) {
		taskQueue = queue;
		this.identificator = ++mCount;
		this.threadPool = threadPool;
	}

	public void run() {
		logger.info("Run method called");

		while (!isStopped()) {
		//while(!Thread.currentThread().isInterrupted()) {
			try {
				task = (ITask) taskQueue.take();
				task.execute();
			} catch (RuntimeException e) {
				logger.error(e.getMessage());
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

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

			}

		}

		System.out.println("Thread " + identificator + " finished his run...");
	}

	public synchronized void doStop() {
		logger.info("doStop() method called");
		isStopped = true;
		try {
			this.interrupt(); // break pool thread out of dequeue() call.
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		
		notify();
	}

	public int getIdentificator() {
		return identificator;
	}

	public synchronized boolean isStopped() {
		return isStopped;
	}

	public ITask getTask() {
		return task;
	}

	public void kill() {
		isStopped = true;
	}

	public void setTask(ITask task) {
		this.task = task;
	}

}