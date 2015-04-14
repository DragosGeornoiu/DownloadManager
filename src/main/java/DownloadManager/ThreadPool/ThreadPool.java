package downloadmanager.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import downloadmanager.ThreadManager;
import downloadmanager.task.ITask;

public class ThreadPool {
	final static Logger logger = Logger.getLogger(ThreadPool.class);

	private BlockingQueue<ITask> taskQueue = null;
	private List<Pool> threads;
	private List<Pool> reusableThreads;
	private boolean isStopped = false;
	private int noOfThreads;	
	ThreadManager threadManager;

	public ThreadPool(int noOfThreads, ThreadManager threadManager) {
		logger.info("Thread Pool initialised with " + noOfThreads + " threads.");
		taskQueue = new LinkedBlockingQueue<ITask>();
		threads = new ArrayList<Pool>();
		this.noOfThreads = noOfThreads;
		// inca o structura pt. a le memora pe cele deja initializate
		reusableThreads = new ArrayList<Pool>();
		
		this.threadManager = threadManager;
	}

	public synchronized void execute(ITask task) {
		logger.info("execute(ITask) method called.");
		if (this.isStopped) {
			throw new IllegalStateException("ThreadPool is stopped");
		}
		System.out.println("----------------------");
		System.out.println("ThreadListSize: " + threads.size());
		System.out.println("ReusableThreadsList: " + reusableThreads.size());

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
				Pool pool = new Pool(taskQueue, this);
				threads.add(pool);
				pool.start();
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

		System.out.println("ThreadListSize: " + threads.size());
		System.out.println("ReusableThreadsList: " + reusableThreads.size());
		System.out.println("----------------------");

	}

	public void finishedRun(Pool ITask) {
		reusableThreads.add(ITask);
		for (int i = 0; i < threads.size(); i++) {
			Pool p = threads.get(i);
			if (p.getIdentificator() == ITask.getIdentificator()) {
				threads.remove(i);
				break;
			}
		}
		
		if(threads.size() == 0) {
			//poti sa downloadezi din nou
			threadManager.setDownloadButton();			
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
		if (this.noOfThreads > noOfThreads) {
			if (reusableThreads.size() + threads.size() < noOfThreads) {
				// in cazul in care nu au fost folosite cate thread-uri aveam
				// limita. Atunci, cand
				// shimba nr-ul limita, listele reusableThreads si threads nu
				// sunt afectate.
				System.out.println("reusableThreads + threads > noOfThreads");
				System.out.println("reusableThreads: " + reusableThreads.size());
				System.out.println("threads: " + threads.size());
				System.out.println("oldNoOfThreads: " + this.noOfThreads);
				System.out.println("newNoOfThreads: " + noOfThreads);
				System.out.println("taskQueue: " + taskQueue.size());
				this.noOfThreads = noOfThreads;
			} else {
				// trebuie sa eliminam din liste.
				System.out.println("reusableThreads + threads <= noOfThreads");
				System.out.println("reusableThreads: " + reusableThreads.size());
				System.out.println("threads: " + threads.size());
				System.out.println("oldNoOfThreads: " + this.noOfThreads);
				System.out.println("newNoOfThreads: " + noOfThreads);
				System.out.println("taskQueue: " + taskQueue.size());
				if (reusableThreads.size() > this.noOfThreads - noOfThreads) {
					// este suficient sa eliminam numai din reusableThreads,
					// fara sa fie afectate
					// thread-urile active in acest moment.
					System.out.println("reusableThreads > oldNoOfThreads - newNoOfThreads");
					System.out.println("size before: " + reusableThreads.size());
					for (int i = 0; (i < this.noOfThreads - noOfThreads); i++) {
						reusableThreads.remove(0);
					}
					System.out.println("size after: " + reusableThreads.size());
				} else {
					System.out.println("reusableThreads <= oldNoOfThreads - newNoOfThreads");
					// trebuie sa eliminam toate thread-urile din
					// reusableThreads, dar tot nu este suficient,
					// trebuie sa eliminam si din threads.
					int reusSize = reusableThreads.size();
					while (reusableThreads.size() != 0) {
						reusableThreads.remove(0);
					}

					int i = 0;
					while (this.noOfThreads - noOfThreads - reusSize > i) {
						System.out.println("before taskQueue: " + taskQueue.size());
						Pool pool = threads.get(0);
						taskQueue.add(pool.getTask());
						threads.remove(0);
						System.out.println("after taskQueue: " + taskQueue.size());
						i++;
					}
				}

			}

		}

		this.noOfThreads = noOfThreads;
	}

	public BlockingQueue<ITask> getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(BlockingQueue<ITask> taskQueue) {
		this.taskQueue = taskQueue;
	}
	
	public void addToTaskQueue(ITask task) {
		try {
			this.taskQueue.put(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
