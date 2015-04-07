package ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class ThreadPool {
	final static Logger logger = Logger.getLogger(ThreadPool.class);
	private BlockingQueue<Runnable> taskQueue = null;
	private List<PoolThread> threads = new ArrayList<PoolThread>();
	private boolean isStopped = false;

	public ThreadPool(int noOfThreads) {
		logger.info("Thread Pool initialised with " + noOfThreads + " threads.");
		taskQueue = new LinkedBlockingQueue<Runnable>();

		for (int i = 0; i < noOfThreads; i++) {
			threads.add(new PoolThread(taskQueue));
		}
		for (PoolThread thread : threads) {
			thread.start();
		}
	}

	public synchronized void execute(Runnable task) {
		logger.info("execute(Runnable) method called.");
		if (this.isStopped) {
			throw new IllegalStateException("ThreadPool is stopped");
		}

		try {
			this.taskQueue.put(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void stop() {
		logger.info("stop() method called.");
		this.isStopped = true;
		for (PoolThread thread : threads) {
			thread.doStop();
		}
	}

}
