import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JFrame;

import java.awt.BorderLayout;


public class UI extends JFrame{
	private static final long serialVersionUID = 8679358624834624663L;
	private Game gameRenderer;
	private World world;
	private Util util;
	
	public static void main(String[] args) {
		new UI();
	}
	
	public UI() {
		util = new Util();
		
		util.Log("Minecraft 2D - Reloaded. Initializing...");
		
		setTitle("Minecraft 2D - Reloaded");
		Rectangle window = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		int WIDTH = window.width;
		int HEIGHT = window.height;
		
		this.setUndecorated(true);
		this.setLocation(new Point(window.x, window.y));
		this.setSize(WIDTH, HEIGHT);
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		this.setVisible(true);
		
		gameRenderer = new Game(world = new World(100, 6, 64, 64, util), this);
		getContentPane().add(gameRenderer, BorderLayout.CENTER);
		
		this.validate();
		this.repaint();
	}
}
