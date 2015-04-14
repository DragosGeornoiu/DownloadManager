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

		if (threads.size() == 0) {
			// poti sa downloadezi din nou
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
				System.out.println("reusableThreads + threads < noOfThreads");
				System.out.println("reusableThreads: " + reusableThreads.size());
				System.out.println("threads: " + threads.size());
				System.out.println("oldNoOfThreads: " + this.noOfThreads);
				System.out.println("newNoOfThreads: " + noOfThreads);
				System.out.println("taskQueue: " + taskQueue.size());
				this.noOfThreads = noOfThreads;
			} else {
				// trebuie sa eliminam din liste.
				// cred ca trebuie sa distrugi thread-urile, nu numai sa le
				// scoti din lista.
				System.out.println("reusableThreads + threads >= noOfThreads");
				System.out.println("reusableThreads: " + reusableThreads.size());
				System.out.println("threads: " + threads.size());
				System.out.println("oldNoOfThreads: " + this.noOfThreads);
				System.out.println("newNoOfThreads: " + noOfThreads);
				System.out.println("taskQueue: " + taskQueue.size());
				if (threads.isEmpty() && reusableThreads.size() > noOfThreads) {
					System.out.println("threads is empty");
					System.out.println("reusableThreads > noOfThreads");
					System.out.println("size before: " + reusableThreads.size());

					int size = reusableThreads.size();
					for (int i = 0; i < size - noOfThreads; i++) {
						System.out.println("i: " + i);
						System.out.println("Trying to remove: " + reusableThreads.size());
						Pool p = reusableThreads.remove(0); //reusableThreads.get(0); reusableThreads.remove(0);
						try {
						
						} catch (Exception e){
							e.printStackTrace();
						}
						System.out.println("The item that we tried to remove: " + p);
						// p.kill();
						// synchronized (p) {
						// p.notify();
						// }
						// p.doStop();

						System.out.println("reusableSize: " + reusableThreads.size());
					}
					System.out.println("size after: " + reusableThreads.size());
				} else {
					System.out.println("reusableThreads <= oldNoOfThreads - newNoOfThreads");
					// trebuie sa eliminam toate thread-urile din
					// reusableThreads, dar tot nu este suficient,
					// trebuie sa eliminam si din threads.
					int reusSize = reusableThreads.size();
					while (reusableThreads.size() != 0) {
						Pool p = reusableThreads.get(0);
						reusableThreads.remove(0);
						/*
						 * p.kill(); synchronized (p) { p.notify(); }
						 */
						//p.doStop();
					}

					int i = 0;
					System.out.println("this.noOfThreads: " + this.noOfThreads);
					System.out.println("noOfThreads: " + noOfThreads);
					System.out.println("reusSize: " + reusSize);
					System.out.println("this.noOfThreads - noOfThreads - reusSize: "
							+ (this.noOfThreads - noOfThreads - reusSize));
					while (this.noOfThreads - noOfThreads - reusSize >= i) {
						System.out.println("i: " + i);
						System.out.println("before taskQueue: " + taskQueue.size());
						Pool pool = threads.get(0);
						taskQueue.add(pool.getTask());
						threads.remove(0);
						System.out.println("after taskQueue: " + taskQueue.size());
						// pool.kill();
						// synchronized (pool) {
						// pool.notify();
						// }
						//pool.doStop();
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
