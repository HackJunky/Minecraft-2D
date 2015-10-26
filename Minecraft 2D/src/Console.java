import javax.swing.JFrame;
import java.awt.Window.Type;
import javax.swing.JList;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.ListSelectionModel;

public class Console extends JFrame {
	
	public Console (Util u) {
		setTitle("Minecraft 2D - Console");
		setType(Type.UTILITY);
		this.setSize(512, 256);
		
		JList<String> list = new JList<String>();
		list.setEnabled(false);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setForeground(Color.GREEN);
		list.setBackground(Color.BLACK);
		getContentPane().add(list, BorderLayout.CENTER);
		
		list.setModel(u.getModel());
		
		this.setVisible(true);
	}
}
