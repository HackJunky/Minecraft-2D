import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
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

	private ArrayList<BufferedImage> textures;
	RenderingHints renderHints;
	Composite translucent;

	private ArrayList<Packet.Modification> modifications;

	private static class VisualDefinitions {
		private static int BLOCK_WIDTH = 24;
		private static int BLOCK_HEIGHT = 24;
		
		private static int HUD_SCALE = 8;
		private static int HOTBAR_SIZE = 8;
		private static int HUD_ICONS = 3;	
		private static int HUD_SPACING = 4;
		private static int XP_HEIGHT = 8;
		
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
	}

	public void initialize() {
		new Thread(new Renderer()).start();
		this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		this.setVisible(true);
	}

	public void loadImages() {
		world.getUtil().Log("Pre-loading images into memory space...");
		long startTime = System.currentTimeMillis();
		textures = new ArrayList<BufferedImage>();
		Block.BlockID[] blocks = Block.BlockID.values();
		for (int i = 0; i < blocks.length; i++) {
			try {
				textures.add(blocks[i].getID(), 
						toBufferedImage(ImageIO.read(new File(VisualDefinitions.TEXTURE_PATH_PREFIX + blocks[i].getTextureName()))));
			} catch (Exception e) {
				textures.add(blocks[i].getID(), null);
				world.getUtil().Log("Could not load " + blocks[i].getTextureName() + "!");
				//e.printStackTrace();
			}
		}
		world.getUtil().Log("Pre-load completed in " + (System.currentTimeMillis() - startTime) + " ms.");
	}

	public BufferedImage toBufferedImage(Image img) {
		try {
			if (img instanceof BufferedImage) {
				return (BufferedImage) img;
			}
			BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D bGr = bimage.createGraphics();
			bGr.drawImage(img, 0, 0, null);
			bGr.dispose();
			return bimage;
		}catch (Exception e) {
			world.getUtil().Log("Could not buffer one or more textures!");
			return null;
		}
	}

	public ArrayList<Packet.Modification> getChanges() {
		return modifications;
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
		viewport = new Rectangle(pixelX - (this.getWidth() / 2), pixelY - (this.getHeight() / 2), pixelX + (this.getWidth() / 2), pixelY + (this.getHeight() / 2));
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

			lookAtBlock(new Point(world.getWidth() / 2, world.getHeight() / 2 + (Chunk.CHUNK_HEIGHT / 2)));
			renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}

		if (world.getChunkData() != null) {
			g2d.setRenderingHints(renderHints);

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
				BufferedImage tex = textures.get(drawData[0][y].getBlockID().getID());
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
				}
			}
		}
		
		if (block != null) {
			cursorRect = new Rectangle(offsetX + (block.x * VisualDefinitions.BLOCK_WIDTH),
					offsetY + (block.y * VisualDefinitions.BLOCK_HEIGHT),
					VisualDefinitions.BLOCK_WIDTH, 
					VisualDefinitions.BLOCK_HEIGHT);
			cursorText = "(" + blockViewport.x + block.x + ", " + blockViewport.y + block.y + ") " + drawData[block.x][block.y].getBlockID().toString().replace(
					'_', ' ');
		}
		if (cursorRect != null) {
			g.setStroke(new BasicStroke(1));
			g.drawRect(cursorRect.x, cursorRect.y, cursorRect.width, cursorRect.height);
			g.setColor(Color.WHITE);
			g.drawString(cursorText, cursorRect.x + cursorRect.width + (cursorRect.width / 3), cursorRect.y - fm.getHeight());
			g.setColor(originalColor);
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
			}else if (cursor.y > ui.getLocation().y + ui.getHeight() - (4 * VisualDefinitions.BLOCK_HEIGHT)) {
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
		
		int hotbarWidth = (10 * (VisualDefinitions.HOTBAR_SIZE * VisualDefinitions.HUD_SCALE)) + (10 * (VisualDefinitions.HUD_SPACING / 2));
		int hotbarHeight = (VisualDefinitions.HOTBAR_SIZE * VisualDefinitions.HUD_SCALE);
		Rectangle hotbar = new Rectangle(this.getWidth() / 2 - hotbarWidth / 2, this.getHeight() - hotbarHeight - VisualDefinitions.HUD_SPACING, hotbarWidth, hotbarHeight);
		
		Rectangle xpbar = new Rectangle(hotbar.x, hotbar.y - VisualDefinitions.HUD_SPACING - VisualDefinitions.XP_HEIGHT, hotbar.width, VisualDefinitions.XP_HEIGHT);
		
		int healthWidth = 10 * (VisualDefinitions.HUD_ICONS * VisualDefinitions.HUD_SCALE) + (10 * (VisualDefinitions.HUD_SPACING / 2)); 
		int healthHeight = (VisualDefinitions.HUD_ICONS * VisualDefinitions.HUD_SCALE);
		Rectangle health = new Rectangle(hotbar.x, xpbar.y - VisualDefinitions.HUD_SPACING - healthHeight, healthWidth, healthHeight);

		int armorWidth = 10 * (VisualDefinitions.HUD_ICONS * VisualDefinitions.HUD_SCALE) + (10 * (VisualDefinitions.HUD_SPACING / 2)); 
		int armorHeight = (VisualDefinitions.HUD_ICONS * VisualDefinitions.HUD_SCALE);
		Rectangle armor = new Rectangle(hotbar.x, health.y - VisualDefinitions.HUD_SPACING - armorHeight, armorWidth, armorHeight);
		
		int foodWidth = 10 * (VisualDefinitions.HUD_ICONS * VisualDefinitions.HUD_SCALE) + (10 * (VisualDefinitions.HUD_SPACING / 2)); 
		int foodHeight = (VisualDefinitions.HUD_ICONS * VisualDefinitions.HUD_SCALE);
		Rectangle food = new Rectangle(hotbar.x + hotbar.width - foodWidth, health.y + health.height - foodHeight, foodWidth, foodHeight);
		
		g.drawRect(hotbar.x, hotbar.y, hotbar.width, hotbar.height);
		g.drawRect(xpbar.x, xpbar.y, xpbar.width, xpbar.height);
		g.drawRect(health.x, health.y, health.width, health.height);
		g.drawRect(armor.x, armor.y, armor.width, armor.height);
		g.drawRect(food.x, food.y, food.width, food.height);
	
		
		
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
