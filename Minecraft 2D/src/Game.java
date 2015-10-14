import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Game extends JPanel {
	private static final long serialVersionUID = -8395759457708163217L;
	private Rectangle viewport;

	private World world;
	private UI ui;

	private boolean drawDebug = false;

	private Block[][] drawData;
	private Rectangle previousViewport;
	
	private ArrayList<Image> textures;
	
	private static class VisualDefinitions {
		private static int BLOCK_WIDTH = 24;
		private static int BLOCK_HEIGHT = 24;
		private static String TEXTURE_PATH_PREFIX = "textures/";
		private static int CAM_PAN_SPEED = 5;
	}

	static {
		System.setProperty("sun.java2d.transaccel", "True");
		// System.setProperty("sun.java2d.trace", "timestamp,log,count");
		// System.setProperty("sun.java2d.opengl", "True");
		System.setProperty("sun.java2d.d3d", "True");
		System.setProperty("sun.java2d.ddforcevram", "True");
	}

	public Game(World world, UI ui) {
		this.world = world;
		this.ui = ui;
		loadImages();

		new Thread(new Renderer()).start();

		this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		this.setVisible(true);
	}

	public void loadImages() {
		world.getUtil().Log("Pre-loading images into memory space...");
		long startTime = System.currentTimeMillis();
		textures = new ArrayList<Image>();
		Block.BlockID[] blocks = Block.BlockID.values();
		for (int i = 0; i < blocks.length; i++) {
			try {
				textures.add(blocks[i].getID(), 
						Toolkit.getDefaultToolkit().getImage(VisualDefinitions.TEXTURE_PATH_PREFIX + blocks[i].getTextureName()));
			} catch (Exception e) {
				textures.add(blocks[i].getID(), null);
				world.getUtil().Log("Could not load " + blocks[i].getTextureName() + "!");
				//e.printStackTrace();
			}
		}
		world.getUtil().Log("Pre-load completed in " + (System.currentTimeMillis() - startTime) + " ms.");
	}

	public void viewLeft() {
		viewport = new Rectangle(viewport.x + VisualDefinitions.CAM_PAN_SPEED, viewport.y, viewport.width + VisualDefinitions.CAM_PAN_SPEED, viewport.height);
	}

	public void viewRight() {
		viewport = new Rectangle(viewport.x - VisualDefinitions.CAM_PAN_SPEED, viewport.y, viewport.width - VisualDefinitions.CAM_PAN_SPEED, viewport.height);
	}

	public void viewUp() {
		viewport = new Rectangle(viewport.x, viewport.y + VisualDefinitions.CAM_PAN_SPEED, viewport.width, viewport.height + VisualDefinitions.CAM_PAN_SPEED);
	}

	public void viewDown() {
		viewport = new Rectangle(viewport.x, viewport.y - VisualDefinitions.CAM_PAN_SPEED, viewport.width, viewport.height - VisualDefinitions.CAM_PAN_SPEED);
	}

	public Rectangle convertViewportToBlocks(Rectangle view) {
		Rectangle rect = new Rectangle((view.x / VisualDefinitions.BLOCK_WIDTH), 
				(view.y / VisualDefinitions.BLOCK_HEIGHT), 
				(view.width / VisualDefinitions.BLOCK_WIDTH),
				(view.height / VisualDefinitions.BLOCK_HEIGHT));
		rect = new Rectangle(rect.x, rect.y, rect.width - rect.x, rect.height - rect.y);
		return rect;
	}
	
	public void lookAtBlock(Point block) {
		world.getUtil().Log("Looking at " + block.x + ", " + block.y + ".");
		int pixelX = block.x * VisualDefinitions.BLOCK_WIDTH;
		int pixelY = block.y * VisualDefinitions.BLOCK_HEIGHT;
		viewport = new Rectangle(pixelX - (this.getWidth() / 2), pixelY - (this.getHeight() / 2), this.getWidth(), this.getHeight());
	}


	public void setViewport(Rectangle viewport) {
		this.viewport = viewport;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		render((Graphics2D)g);
	}


	public void render(Graphics2D g2d) {
		if (viewport == null) {
			viewport = new Rectangle(0, 0, this.getWidth(), this.getHeight());
			previousViewport = new Rectangle(-1, -1, -1, -1);
			
			lookAtBlock(new Point(world.getWidth() / 2, world.getHeight() / 2));
		}

		if (world.isGenerated()) {
			if (drawDebug) {
				drawDebug(g2d);
			}

			drawWorld(g2d);
			drawHUD(g2d);
			drawEntities(g2d);
		}
	}

	public void update() {
		world.update();
	}

	public void drawDebug(Graphics2D g) {
		Color originalColor = g.getColor();
		Stroke originalStroke = g.getStroke();

		g.setColor(Color.MAGENTA);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		g.setColor(originalColor);
		g.setStroke(originalStroke);
	}

	public void drawWorld(Graphics2D g) {
		Color originalColor = g.getColor();
		Stroke originalStroke = g.getStroke();
		FontMetrics fm = g.getFontMetrics();
		
		Rectangle blockViewport = convertViewportToBlocks(viewport);

		if (!previousViewport.equals(convertViewportToBlocks(viewport))) {
			drawData = world.getViewportData(blockViewport);
			previousViewport = convertViewportToBlocks(viewport);
		}

		int blockWidth = (viewport.width / VisualDefinitions.BLOCK_WIDTH);
		int blockHeight = (viewport.height / VisualDefinitions.BLOCK_HEIGHT);

		int offsetX = viewport.width - (blockWidth * VisualDefinitions.BLOCK_WIDTH);
		int offsetY = viewport.height - (blockHeight * VisualDefinitions.BLOCK_HEIGHT);
		
		Point cursor = ui.getMousePosition();
		Point block = null;
		if (cursor != null) {
			SwingUtilities.convertPointFromScreen(cursor, ui);
			block = new Point((cursor.x - offsetX) / VisualDefinitions.BLOCK_WIDTH, ((cursor.y - offsetY) / VisualDefinitions.BLOCK_HEIGHT));
		}
		Rectangle cursorRect = null;
		String cursorText = "Void";
		for (int y = 0; y < drawData[0].length; y++) {
			if (drawData[0][y] == null) {
				g.setColor(Color.BLACK);
				g.fillRect(-(VisualDefinitions.BLOCK_WIDTH - offsetX),
						offsetY + (y * VisualDefinitions.BLOCK_HEIGHT) - VisualDefinitions.BLOCK_HEIGHT,
						VisualDefinitions.BLOCK_WIDTH, 
						VisualDefinitions.BLOCK_HEIGHT);
				g.setColor(originalColor);
			}else {
				Image tex = textures.get(drawData[0][y].getBlockID().getID());
				g.drawImage(tex, 
						-(VisualDefinitions.BLOCK_WIDTH - offsetX),
						offsetY + (y * VisualDefinitions.BLOCK_HEIGHT) - VisualDefinitions.BLOCK_HEIGHT,
						VisualDefinitions.BLOCK_WIDTH, 
						VisualDefinitions.BLOCK_HEIGHT, this);
			}
		}
		for (int x = 0; x < drawData.length; x++) {
			if (drawData[x][0] == null) {
				g.setColor(Color.BLACK);
				g.fillRect(offsetX + ((x) * VisualDefinitions.BLOCK_WIDTH) - VisualDefinitions.BLOCK_WIDTH,
						-(VisualDefinitions.BLOCK_HEIGHT - offsetY),
						VisualDefinitions.BLOCK_WIDTH, 
						VisualDefinitions.BLOCK_HEIGHT);
				g.setColor(originalColor);
			}else {
				Image tex = textures.get(drawData[x][0].getBlockID().getID());
				g.drawImage(tex, 
						offsetX + ((x) * VisualDefinitions.BLOCK_WIDTH) - VisualDefinitions.BLOCK_WIDTH,
						-(VisualDefinitions.BLOCK_HEIGHT - offsetY),
						VisualDefinitions.BLOCK_WIDTH, 
						VisualDefinitions.BLOCK_HEIGHT, this);
			}
		}
		for (int x = 1; x < drawData.length; x++) {
			for (int y = 1; y < drawData[0].length; y++) {
				if (drawData[x][y] == null) {
					g.setColor(Color.BLACK);
					g.fillRect(offsetX + ((x - 1) * VisualDefinitions.BLOCK_WIDTH),
							offsetY + ((y - 1) * VisualDefinitions.BLOCK_HEIGHT),
							VisualDefinitions.BLOCK_WIDTH, 
							VisualDefinitions.BLOCK_HEIGHT);
					g.setColor(originalColor);
				}else {
					Image tex = textures.get(drawData[x][y].getBlockID().getID());
					if (tex != null) {
						g.drawImage(tex, 
								offsetX + ((x - 1) * VisualDefinitions.BLOCK_WIDTH),
								offsetY + ((y - 1) * VisualDefinitions.BLOCK_HEIGHT),
								VisualDefinitions.BLOCK_WIDTH, 
								VisualDefinitions.BLOCK_HEIGHT, this);
					}else {
						g.setColor(Color.MAGENTA);
						g.fillRect(offsetX + ((x - 1) * VisualDefinitions.BLOCK_WIDTH),
								offsetY + ((y - 1) * VisualDefinitions.BLOCK_HEIGHT),
								VisualDefinitions.BLOCK_WIDTH, 
								VisualDefinitions.BLOCK_HEIGHT);
						g.setColor(originalColor);
					}
					if (block != null && block.x == x && block.y == y) {
						cursorRect = new Rectangle(offsetX + (x * VisualDefinitions.BLOCK_WIDTH),
								offsetY + (y * VisualDefinitions.BLOCK_HEIGHT),
								VisualDefinitions.BLOCK_WIDTH, 
								VisualDefinitions.BLOCK_HEIGHT);
//						cursorText = "(" + (blockViewport.x + (x - 1)) + ", " + (blockViewport.y + (y - 1)) + ") " + drawData[x][y].getBlockID().toString().replace(
//								'_', ' ');
					}
				}
			}
		}

		if (cursorRect != null) {
			g.setStroke(new BasicStroke(1));
			g.drawRect(cursorRect.x, cursorRect.y, cursorRect.width, cursorRect.height);
			g.drawString(cursorText, cursorRect.x + cursorRect.width + (cursorRect.width / 3), cursorRect.y - fm.getHeight());
			g.setStroke(originalStroke);
		}

		if (cursor != null) {
			int leftRight = 0;
			if (cursor.x < ui.getLocation().x + VisualDefinitions.BLOCK_HEIGHT) {
				leftRight = -1;
			}else if (cursor.x > ui.getLocation().x + ui.getWidth() - VisualDefinitions.BLOCK_HEIGHT) {
				leftRight = 1;
			}

			if (leftRight < 0) {
				viewLeft();
			}else if (leftRight > 0) {
				viewRight();
			}

			int upDown = 0;
			if (cursor.y < ui.getLocation().y + VisualDefinitions.BLOCK_HEIGHT) {
				upDown = -1;
			}else if (cursor.y > ui.getLocation().y + ui.getHeight() - VisualDefinitions.BLOCK_HEIGHT) {
				upDown = 1;
			}

			if (upDown < 0) {
				viewUp();
			}else if (upDown > 0) {
				viewDown();
			}
		}

		g.setColor(originalColor);
		g.setStroke(originalStroke);
	}

	public void drawHUD(Graphics2D g) {
		Color originalColor = g.getColor();
		Stroke originalStroke = g.getStroke();

		g.setColor(originalColor);
		g.setStroke(originalStroke);
	}

	public void drawEntities(Graphics2D g) {
		Color originalColor = g.getColor();
		Stroke originalStroke = g.getStroke();

		g.setColor(originalColor);
		g.setStroke(originalStroke);
	}

	public class Renderer implements Runnable {
		private final static int MAX_FPS = 60;	
		private final static int MAX_FRAME_SKIPS = 5;	
		private final static int FRAME_PERIOD = 1000 / MAX_FPS;	

		@Override
		public void run() {
			long beginTime;
			long timeDiff;
			int sleepTime;
			int framesSkipped;

			sleepTime = 0;

			while (true) {
				try {
					beginTime = System.currentTimeMillis();
					framesSkipped = 0;
					Game.this.update();
					Game.this.repaint();
					timeDiff = System.currentTimeMillis() - beginTime;
					sleepTime = (int)(FRAME_PERIOD - timeDiff);

					if (sleepTime > 0) {
						try {
							Thread.sleep(sleepTime);	
						} catch (InterruptedException e) {}
					}

					while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
						Game.this.update(); 
						sleepTime += FRAME_PERIOD;	
						framesSkipped++;
					}
				} finally {
					Toolkit.getDefaultToolkit().sync();
				}
			}
		}
	}
}
