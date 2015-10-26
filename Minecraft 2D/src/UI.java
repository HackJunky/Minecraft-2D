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
	
	private Client client;
	private Util util;
	
	public UI(Client c) {
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
		
		client = c;
		gameRenderer = new Game(this, client);

		getContentPane().add(gameRenderer, BorderLayout.CENTER);
		
		this.validate();
		this.repaint();
	}

	public Game getGame() {
		return gameRenderer;
	}
}
