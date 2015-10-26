import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.BorderLayout;
import javax.swing.JPanel;

public class UI extends JFrame{
	private static final long serialVersionUID = 8679358624834624663L;
	private Game gameRenderer;
	private World world;

	private Client client;
	private Util util;

	private JPanel loadingPanel;
	private JLabel icon;

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

		util.Log("Loading...");
		
		client = c;
		gameRenderer = new Game(this, client);

		loadingPanel = new JPanel();
		getContentPane().add(loadingPanel, BorderLayout.CENTER);
		loadingPanel.setLayout(null);

		
		ImageIcon loading = new ImageIcon("data/UI/loading.gif");
		icon = new JLabel("", loading, JLabel.CENTER);
		icon.setLocation(WIDTH / 2 - 32, HEIGHT / 2 + (HEIGHT / 4) - 32);
		icon.setSize(64, 64);
		loadingPanel.add(icon);
		
		this.setVisible(true);
		
		try {
			loadingPanel.getGraphics().drawImage(ImageIO.read(new File("data/UI/wallpaper.jpg")), 0, 0, WIDTH, HEIGHT, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.validate();
		this.repaint();
	}

	public Game getGame() {
		return gameRenderer;
	}

	public void callback() {
		getContentPane().remove(loadingPanel);
		getContentPane().remove(icon);
		getContentPane().add(gameRenderer, BorderLayout.CENTER);
		util.Log("Graphical Sync completed. Ready to go.");
		this.validate();
		this.repaint();
	}
}
