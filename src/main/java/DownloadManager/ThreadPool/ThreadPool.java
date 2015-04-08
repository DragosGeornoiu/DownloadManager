package DownloadManager.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class ThreadPool {
	final static Logger logger = Logger.getLogger(ThreadPool.class);

	private BlockingQueue<Runnable> taskQueue = null;
	private List<Pool> threads;
	private List<Pool> reusableThreads;
	private boolean isStopped = false;
	private int noOfThreads;
	private int id = 0;

	public ThreadPool(int noOfThreads) {
		logger.info("Thread Pool initialised with " + noOfThreads + " threads.");
		taskQueue = new LinkedBlockingQueue<Runnable>();
		threads = new ArrayList<Pool>();
		this.noOfThreads = noOfThreads;
		// inca o structura pt. a le memora pe cele deja initializate
		reusableThreads = new ArrayList<Pool>();
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

		// verific daca exista thread initializat, dar care si-a finalizat
		// download-ul
		if (reusableThreads.isEmpty()) {
			// daca nu exista, verific daca pot initializa un thread nou sau nu.

			if (threads.size() < noOfThreads) {
				Pool pool = new Pool(taskQueue, id, this);
				threads.add(pool);
				pool.start();
				id++;
			}
		} else {
			// folosesc un thread din reusableThreads
			Pool pool = reusableThreads.get(0);
			reusableThreads.remove(0);
			threads.add(pool);

			synchronized (pool) {
				pool.notify();
			}

		}
		
		System.out.println("----------------------");
		System.out.println("ThreadListSize: " + threads.size());
		System.out.println("ReusableThreadsList: " + reusableThreads.size());

	}

	public void finishedRun(Pool runnable) {
		reusableThreads.add(runnable);
		for (int i = 0; i < threads.size(); i++) {
			Pool p = threads.get(i);
			if (p.getIdentificator() == runnable.getIdentificator()) {
				threads.remove(i);
				break;
			}
		}
	}

	public synchronized void stop() {
		logger.info("stop() method called.");
		this.isStopped = true;
		for (Pool thread : threads) {
			thread.doStop();
		}
	}

	public int getNoOfThreads() {
		return noOfThreads;
	}

	public void setNoOfThreads(int noOfThreads) {
		this.noOfThreads = noOfThreads;
	}

}
