import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class SwingWorkerExecutorTest
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new SwingWorkerExecutorTest();
            }
        });
    }


    public SwingWorkerExecutorTest()
    {
        JFrame frame = new JFrame("Frame");

        int numberOfThreads = 2; //1 so they are executed one after the other.
        final ExecutorService threadPool = Executors.newFixedThreadPool(numberOfThreads);

        JButton button1 = new JButton("Submit SwingWorker 1");
        button1.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String workerName = "Worker 1";
                appendMessage("Submited " + workerName);
                SwingWorker worker = new TestWorker(workerName);
                threadPool.submit(worker);
            }
        });

        JButton button2 = new JButton("Submit SwingWorker 2");
        button2.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String workerName = "Worker 2";
                appendMessage("Submited " + workerName);
                SwingWorker worker = new TestWorker(workerName);
                threadPool.submit(worker);
            }
        });

        JButton button3 = new JButton("Submit SwingWorker 3");
        button3.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String workerName = "Worker 3";
                appendMessage("Submited " + workerName);
                SwingWorker worker = new TestWorker(workerName);
                threadPool.submit(worker);
            }
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(button1);
        buttonsPanel.add(button2);
        buttonsPanel.add(button3);
        frame.add(buttonsPanel, BorderLayout.PAGE_END);

        _textArea = new JTextArea("Submit some workers:\n");
        _textArea.setEditable(false);
        frame.add(new JScrollPane(_textArea));

        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    private class TestWorker extends SwingWorker
    {
        public TestWorker(String name)
        {
            _name = name;
        }

        @Override
        protected Object doInBackground() throws Exception
        {
            String message = "A " + _name + " has started!";
            appendMessage(message);
            doHardWork();
            return null;
        }

        @Override
        protected void done()
        {
            String message = "A " + _name + " has finished!";
            appendMessage(message);
        }

        private void doHardWork()
        {
            try
            {
                Thread.sleep(2000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        private String  _name;
    }

    private static void appendMessage(String message)
    {
        _textArea.append(message + "\n");
        System.out.println(message);
    }

    private static JTextArea    _textArea;
}