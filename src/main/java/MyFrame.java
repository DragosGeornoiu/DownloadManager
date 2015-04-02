import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

public class MyFrame extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	CheckBoxGroup checkboxgroup1;
	CheckBoxGroup checkboxgroup2;
	JButton button;

	MyFrame() {

		super("Testing");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridLayout(0, 3));
		checkboxgroup1 = new CheckBoxGroup(new String[] { "Bananas",
				"Oranages", "Apples", "Pears" });
		checkboxgroup2 = new CheckBoxGroup(new String[] { "Learn Archery",
				"Float in the dead sea", "Swing with a whale shark",
				"Sail the greek islands", "Go skydiving", "Dance in the rain",
				"Cycle through the Netherlands" });
		add(checkboxgroup1);
		add(checkboxgroup2);
		button = new JButton("Submit");
		button.addActionListener(this);
		add(button);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		List<JCheckBox> list = checkboxgroup1.getCheckBoxes();
		for(int i=0; i<list.size(); i++) {
			if(list.get(i).isSelected()) {
				System.out.println(list.get(i).getText());
			}
		}

	}

}
