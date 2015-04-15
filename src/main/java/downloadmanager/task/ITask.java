package downloadmanager.task;

public interface ITask {
	public void execute();
	public boolean isSuspended();
	public void resume();
	public void pause();
}
