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
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Game extends JPanel {
	private static final long serialVersionUID = -8395759457708163217L;
	private Rectangle viewport;

	private World world;
	private UI ui;
	private Client client;

	private boolean drawDebug = false;

	private Block[][] drawData;
	private Rectangle previousViewport;

	private ArrayList<BufferedImage> textures;
	private ArrayList<BufferedImage> hudTextures;
	private ArrayList<BufferedImage> entityTextures;

	private Entity lookAtEntity;

	private Block[] hotbar;
	int hotbarSelectedIndex = 0;

	enum UITextures {
		Hotbar_Unselected("Tile Unselected.png"),
		Hotbar_Selected("Tile Selected.png"),
		Inventory("Inventory.png"),
		Inventory_Crafting("Crafting.png"),
		Armor("Armor.png"),
		Armor_Half("Armor Half.png"),
		Armor_Empty("Armor Empty.png"),
		Heart("Heart.png"),
		Heart_Half("Heart Half.png"),
		Heart_Empty("Heart Empty.png"),
		Hunger("Hunger.png"),
		Hunger_Half("Hunger Half.png"),
		Hunger_Empty("Hunger Empty.png"),
		XP_Empty("XPBar Empty.png"),
		XP_Full("XPBar Full.png");

		private String path;

		private UITextures(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}
	}

	enum EntityTextures {
		Player("Player Front.png"),
		Player_Left("Player Left.png"),
		Player_Right("Player Right.png");

		private String path;

		private EntityTextures(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}
	}

	public Player player;

	private ArrayList<Packet.Modification> modifications;

	int BLOCK_WIDTH = 24;
	int BLOCK_HEIGHT = 24;
	int HUD_SCALE = 6;
	int HOTBAR_SIZE = 8;
	int HUD_ICONS = 3;	
	int HUD_SPACING = 4;
	int XP_HEIGHT = 8;
	String TEXTURE_PATH_PREFIX = "textures/";
	String UI_PATH_PREFIX = "data/UI/";
	int CAM_PAN_SPEED = 5;

	static {
		System.setProperty("sun.java2d.transaccel", "True");
		// System.setProperty("sun.java2d.trace", "timestamp,log,count");
		System.setProperty("sun.java2d.opengl", "True");
		System.setProperty("sun.java2d.d3d", "True");
		System.setProperty("sun.java2d.ddforcevram", "True");
	}

	public Game(UI ui, Client c) {
		client = c;
		this.ui = ui;
		hotbar = new Block[10];
	}

	public void initialize() {
		world = client.getWorld();
		loadImages();
		new Thread(new Renderer()).start();
		this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		this.setVisible(true);

		ui.callback();
	}

	public void loadImages() {
		world.getUtil().Log("Pre-loading images into memory space...");
		long startTime = System.currentTimeMillis();
		textures = new ArrayList<BufferedImage>();
		Block.BlockID[] blocks = Block.BlockID.values();
		for (int i = 0; i < blocks.length; i++) {
			try {
				textures.add(blocks[i].getID(), 
						toBufferedImage(ImageIO.read(new File(TEXTURE_PATH_PREFIX + blocks[i].getTextureName()))));
			} catch (Exception e) {
				textures.add(blocks[i].getID(), null);
				world.getUtil().Log("Could not load " + blocks[i].getTextureName() + "!");
				//e.printStackTrace();
			}
		}
		UITextures[] hud = UITextures.values();
		hudTextures = new ArrayList<BufferedImage>();
		for (int i = 0; i < hud.length; i++) {
			try {
				hudTextures.add(i, 
						toBufferedImage(ImageIO.read(new File(UI_PATH_PREFIX + hud[i].getPath()))));
			} catch (Exception e) {
				hudTextures.add(i, null);
				world.getUtil().Log("Could not load " + hud[i].toString() + "!");
				//e.printStackTrace();
			}
		}
		EntityTextures[] entTex = EntityTextures.values();
		entityTextures = new ArrayList<BufferedImage>();
		for (int i = 0; i < entTex.length; i++) {
			try {
				entityTextures.add(i, 
						toBufferedImage(ImageIO.read(new File(TEXTURE_PATH_PREFIX + entTex[i].getPath()))));
			} catch (Exception e) {
				entityTextures.add(i, null);
				world.getUtil().Log("Could not load " + entTex[i].toString() + "!");
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
		viewport = new Rectangle(viewport.x + CAM_PAN_SPEED, viewport.y, viewport.width + CAM_PAN_SPEED, viewport.height);
	}

	public void viewRight() {
		viewport = new Rectangle(viewport.x - CAM_PAN_SPEED, viewport.y, viewport.width - CAM_PAN_SPEED, viewport.height);
	}

	public void viewUp() {
		viewport = new Rectangle(viewport.x, viewport.y + CAM_PAN_SPEED, viewport.width, viewport.height + CAM_PAN_SPEED);
	}

	public void viewDown() {
		viewport = new Rectangle(viewport.x, viewport.y - CAM_PAN_SPEED, viewport.width, viewport.height - CAM_PAN_SPEED);
	}

	public Rectangle convertViewportToBlocks(Rectangle view) {
		Rectangle rect = new Rectangle((view.x / BLOCK_WIDTH), 
				(view.y / BLOCK_HEIGHT), 
				(view.width / BLOCK_WIDTH),
				(view.height / BLOCK_HEIGHT));
		rect = new Rectangle(rect.x, rect.y, rect.width - rect.x, rect.height - rect.y);
		return rect;
	}

	public void lookAtBlock(Point block) {
		world.getUtil().Log("Looking at " + block.x + ", " + block.y + ".");
		int pixelX = block.x * BLOCK_WIDTH;
		int pixelY = block.y * BLOCK_HEIGHT;
		viewport = new Rectangle(pixelX - (this.getWidth() / 2), pixelY - (this.getHeight() / 2), pixelX + (this.getWidth() / 2), pixelY + (this.getHeight() / 2));
	}

	public void lookAtPixel(Point pixel) {
		viewport = new Rectangle(pixel.x - (this.getWidth() / 2), pixel.y - (this.getHeight() / 2), pixel.x + (this.getWidth() / 2), pixel.y + (this.getHeight() / 2));
	}

	public void setViewTarget(Entity e) {
		lookAtEntity = e;
	}	

	public void clearViewTarget() {
		lookAtEntity = null;
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
		if (world != null && world.getChunkData() != null && player != null) {
			if (viewport == null) {
				viewport = new Rectangle(0, 0, this.getWidth(), this.getHeight());
				previousViewport = new Rectangle(-1, -1, -1, -1);

				lookAtBlock(new Point(world.getWidth() / 2, world.getHeight() / 2 + (Chunk.CHUNK_HEIGHT / 2)));
			}

			if (lookAtEntity != null) {
				lookAtPixel(new Point((lookAtEntity.getBlockPos().x * BLOCK_WIDTH) + lookAtEntity.getPixelOffset().x, (lookAtEntity.getBlockPos().y * BLOCK_HEIGHT) + lookAtEntity.getPixelOffset().y));
			}

			if (world.getChunkData() != null) {
				if (drawDebug) {
					drawDebug(g2d);
				}

				drawWorld(g2d);
				drawHUD(g2d);
			}
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
		Composite originalComposite = g.getComposite();

		Rectangle blockViewport = convertViewportToBlocks(viewport);

		if (!previousViewport.equals(convertViewportToBlocks(viewport))) {
			drawData = world.getViewportData(blockViewport);
			previousViewport = convertViewportToBlocks(viewport);
		}

		int blockWidth = (viewport.width / BLOCK_WIDTH);
		int blockHeight = (viewport.height / BLOCK_HEIGHT);

		int offsetX = viewport.width - (blockWidth * BLOCK_WIDTH);
		int offsetY = viewport.height - (blockHeight * BLOCK_HEIGHT);

		Point cursor = ui.getMousePosition();
		Point block = null;
		if (cursor != null) {
			SwingUtilities.convertPointFromScreen(cursor, ui);
			block = new Point((cursor.x - offsetX) / BLOCK_WIDTH, ((cursor.y - offsetY) / BLOCK_HEIGHT));
		}
		Rectangle cursorRect = null;
		String cursorText = "Void";

		g.setColor(world.getSkyColor());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(originalColor);

		for (int y = 0; y < drawData[0].length; y++) {
			if (drawData[0][y] == null) {
				g.setColor(Color.BLACK);
				g.fillRect(-(BLOCK_WIDTH - offsetX),
						offsetY + (y * BLOCK_HEIGHT) - BLOCK_HEIGHT,
						BLOCK_WIDTH, 
						BLOCK_HEIGHT);
				g.setColor(originalColor);
			}else {
				if (!drawData[0][y].getBlockID().equals(Block.BlockID.Air)) {
					BufferedImage tex = textures.get(drawData[0][y].getBlockID().getID());
					g.drawImage(tex, 
							-(BLOCK_WIDTH - offsetX),
							offsetY + (y * BLOCK_HEIGHT) - BLOCK_HEIGHT,
							BLOCK_WIDTH, 
							BLOCK_HEIGHT, this);
				}
			}
		}
		for (int x = 0; x < drawData.length; x++) {
			if (drawData[x][0] == null) {
				g.setColor(Color.BLACK);
				g.fillRect(offsetX + ((x) * BLOCK_WIDTH) - BLOCK_WIDTH,
						-(BLOCK_HEIGHT - offsetY),
						BLOCK_WIDTH, 
						BLOCK_HEIGHT);
				g.setColor(originalColor);
			}else {
				if (!drawData[x][0].getBlockID().equals(Block.BlockID.Air)) {
					Image tex = textures.get(drawData[x][0].getBlockID().getID());
					g.drawImage(tex, 
							offsetX + ((x) * BLOCK_WIDTH) - BLOCK_WIDTH,
							-(BLOCK_HEIGHT - offsetY),
							BLOCK_WIDTH, 
							BLOCK_HEIGHT, this);
				}
			}
		}
		for (int x = 1; x < drawData.length; x++) {
			for (int y = 1; y < drawData[0].length; y++) {
				if (drawData[x][y] == null) {
					g.setColor(Color.BLACK);
					g.fillRect(offsetX + ((x - 1) * BLOCK_WIDTH),
							offsetY + ((y - 1) * BLOCK_HEIGHT),
							BLOCK_WIDTH, 
							BLOCK_HEIGHT);
					g.setColor(originalColor);
				}else {
					if (!drawData[x][y].getBlockID().equals(Block.BlockID.Air)) {
						Image tex = textures.get(drawData[x][y].getBlockID().getID());
						if (tex != null) {
							g.drawImage(tex, 
									offsetX + ((x - 1) * BLOCK_WIDTH),
									offsetY + ((y - 1) * BLOCK_HEIGHT),
									BLOCK_WIDTH, 
									BLOCK_HEIGHT, this);
						}else {
							g.setColor(Color.MAGENTA);
							g.fillRect(offsetX + ((x - 1) * BLOCK_WIDTH),
									offsetY + ((y - 1) * BLOCK_HEIGHT),
									BLOCK_WIDTH, 
									BLOCK_HEIGHT);
							g.setColor(originalColor);
						}
					}
				}
			}
		}
		
		drawEntities(g);

		g.setColor(world.getLight());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(originalColor);

		if (block != null && block.x >= 0 && block.y >= 0) {
			cursorRect = new Rectangle(offsetX + (block.x * BLOCK_WIDTH),
					offsetY + (block.y * BLOCK_HEIGHT),
					BLOCK_WIDTH, 
					BLOCK_HEIGHT);
			cursorText = "(" + (world.getWidth() - (blockViewport.x + block.x)) + ", " + (world.getHeight() - (blockViewport.y + block.y)) + ") " + drawData[block.x][block.y].getBlockID().toString().replace(
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

		if (cursor != null && lookAtEntity == null) {
			int leftRight = 0;
			if (cursor.x < ui.getLocation().x + BLOCK_HEIGHT) {
				leftRight = -1;
			}else if (cursor.x > ui.getLocation().x + ui.getWidth() - BLOCK_HEIGHT) {
				leftRight = 1;
			}

			if (leftRight < 0) {
				viewLeft();
			}else if (leftRight > 0) {
				viewRight();
			}

			int upDown = 0;
			if (cursor.y < ui.getLocation().y + BLOCK_HEIGHT) {
				upDown = -1;
			}else if (cursor.y > ui.getLocation().y + ui.getHeight() - (4 * BLOCK_HEIGHT)) {
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

		//		0 Hotbar_Unselected("Tile Unselected.png"),
		//		1 Hotbar_Selected("Tile Selected.png"),
		//		2 Inventory("Inventory.png"),
		//		3 Inventory_Crafting("Crafting.png"),
		//		4 Armor("Armor.png"),
		//		5 Armor_Half("Armor Half.png"),
		//		6 Armor_Empty("Armor Empty.png"),
		//		7 Heart("Heart.png"),
		//		8 Heart_Half("Heart Half.png"),
		//		9 Heart_Empty("Heart Empty.png"),
		//		10 Hunger("Hunger.png"),
		//		11 Hunger_Half("Hunger Half.png"),
		//		12 Hunger_Empty("Hunger Empty.png"),
		//		13 XP_Empty("XPBar Empty.png"),
		//		14 XP_Full("XPBar Full.png");

		int hotbarWidth = (this.hotbar.length * (HOTBAR_SIZE * HUD_SCALE));
		int hotbarHeight = (HOTBAR_SIZE * HUD_SCALE);
		Rectangle hotbar = new Rectangle(this.getWidth() / 2 - hotbarWidth / 2, this.getHeight() - hotbarHeight - HUD_SPACING, hotbarWidth, hotbarHeight);

		Rectangle xpbar = new Rectangle(hotbar.x, hotbar.y - 2 - XP_HEIGHT, hotbar.width, XP_HEIGHT);

		int healthWidth = 10 * (HUD_ICONS * HUD_SCALE) + (10 * (HUD_SPACING / 2)); 
		int healthHeight = (HUD_ICONS * HUD_SCALE);
		Rectangle health = new Rectangle(hotbar.x, xpbar.y - HUD_SPACING - healthHeight, healthWidth, healthHeight);

		int armorWidth = 10 * (HUD_ICONS * HUD_SCALE) + (10 * (HUD_SPACING / 2)); 
		int armorHeight = (HUD_ICONS * HUD_SCALE);
		Rectangle armor = new Rectangle(hotbar.x, health.y - HUD_SPACING - armorHeight, armorWidth, armorHeight);

		int foodWidth = 10 * (HUD_ICONS * HUD_SCALE) + (10 * (HUD_SPACING / 2)); 
		int foodHeight = (HUD_ICONS * HUD_SCALE);
		Rectangle food = new Rectangle(hotbar.x + hotbar.width - foodWidth, health.y + health.height - foodHeight, foodWidth, foodHeight);

		//g.drawRect(hotbar.x, hotbar.y, hotbar.width, hotbar.height);
		//g.drawRect(xpbar.x, xpbar.y, xpbar.width, xpbar.height);
		//g.drawRect(health.x, health.y, health.width, health.height);
		//g.drawRect(armor.x, armor.y, armor.width, armor.height);
		//g.drawRect(food.x, food.y, food.width, food.height);

		for (int i = 0; i < this.hotbar.length; i++) {
			if (i != hotbarSelectedIndex) {
				g.drawImage(hudTextures.get(0), 
						hotbar.x + (i * (HOTBAR_SIZE * HUD_SCALE)), 
						hotbar.y, 
						HOTBAR_SIZE * HUD_SCALE,
						HOTBAR_SIZE * HUD_SCALE,
						this);
			}
		}
		g.drawImage(hudTextures.get(1), 
				hotbar.x + (hotbarSelectedIndex * (HOTBAR_SIZE * HUD_SCALE)) - (int)(0.5 * HUD_SPACING),
				hotbar.y - (int)(0.5 * HUD_SPACING),
				(HOTBAR_SIZE * HUD_SCALE) + (HUD_SPACING),
				(HOTBAR_SIZE * HUD_SCALE) + (HUD_SPACING),
				this);

		g.drawImage(hudTextures.get(13), xpbar.x, xpbar.y, xpbar.width, xpbar.height, this);
		double xp = player.getXp();
		g.drawImage(hudTextures.get(14), xpbar.x, xpbar.y, (int)(xpbar.width * (xp / 50)), xpbar.height, this);

		double dblHealth = player.getHealth();
		double dblArmor = player.getArmor();
		double dblFood = player.getFood();

		for (double i = 1; i <= 10; i++) {
			if (i <= dblHealth) {
				g.drawImage(hudTextures.get(7), 
						health.x + ((int)(i - 1) * (HUD_ICONS * HUD_SCALE)), 
						health.y, 
						HUD_ICONS * HUD_SCALE,
						HUD_ICONS * HUD_SCALE,
						this);
			}else {
				if (dblHealth % 1 != 0 && i - 0.5 == dblHealth) {
					g.drawImage(hudTextures.get(8), 
							health.x + ((int)(i - 1) * (HUD_ICONS * HUD_SCALE)), 
							health.y, 
							HUD_ICONS * HUD_SCALE,
							HUD_ICONS * HUD_SCALE,
							this);
				}else {
					g.drawImage(hudTextures.get(9), 
							health.x + ((int)(i - 1) * (HUD_ICONS * HUD_SCALE)), 
							health.y, 
							HUD_ICONS * HUD_SCALE,
							HUD_ICONS * HUD_SCALE,
							this);
				}
			}
			if (i <= dblArmor) {
				g.drawImage(hudTextures.get(4), 
						armor.x + ((int)(i - 1) * (HUD_ICONS * HUD_SCALE)), 
						armor.y, 
						HUD_ICONS * HUD_SCALE,
						HUD_ICONS * HUD_SCALE,
						this);
			}else {
				if (dblArmor % 1 != 0 && i - 0.5 == dblArmor) {
					g.drawImage(hudTextures.get(5), 
							armor.x + ((int)(i - 1) * (HUD_ICONS * HUD_SCALE)), 
							armor.y, 
							HUD_ICONS * HUD_SCALE,
							HUD_ICONS * HUD_SCALE,
							this);
				}else {
					g.drawImage(hudTextures.get(6), 
							armor.x + ((int)(i - 1)  * (HUD_ICONS * HUD_SCALE)), 
							armor.y, 
							HUD_ICONS * HUD_SCALE,
							HUD_ICONS * HUD_SCALE,
							this);
				}
			}
			if (i <= dblFood) {
				g.drawImage(hudTextures.get(10), 
						food.x + ((10 - (int)(i - 1)) * (HUD_ICONS * HUD_SCALE)), 
						food.y, 
						HUD_ICONS * HUD_SCALE,
						HUD_ICONS * HUD_SCALE,
						this);
			}else {
				if (dblFood % 1 != 0 && i - 0.5 == dblFood) {
					g.drawImage(hudTextures.get(11), 
							food.x + ((10 - (int)(i - 1)) * (HUD_ICONS * HUD_SCALE)), 
							food.y, 
							HUD_ICONS * HUD_SCALE,
							HUD_ICONS * HUD_SCALE,
							this);
				}else {
					g.drawImage(hudTextures.get(12), 
							food.x + ((10 - (int)(i - 1)) * (HUD_ICONS * HUD_SCALE)), 
							food.y, 
							HUD_ICONS * HUD_SCALE,
							HUD_ICONS * HUD_SCALE,
							this);
				}	
			}
		}

		g.setColor(originalColor);
		g.setStroke(originalStroke);
	}

	public void drawEntities(Graphics2D g) {
		Color originalColor = g.getColor();
		Stroke originalStroke = g.getStroke();
		Rectangle blockViewport = convertViewportToBlocks(viewport);

		if (world.getEntities() != null) {
			for (Entity e : world.getEntities()) {
				if (blockViewport.contains(e.getBlockPos())) {
					if (e instanceof Player) {
						Rectangle drawCoords = new Rectangle(
								(((e.getBlockPos().x * BLOCK_WIDTH) + e.getPixelOffset().x) - (e.getBounds().x / 2)) - viewport.x,
								(((e.getBlockPos().y * BLOCK_HEIGHT) + e.getPixelOffset().y) - (e.getBounds().y)) - viewport.y,
								BLOCK_WIDTH,
								(2 * BLOCK_HEIGHT));
						BufferedImage tex = null;
						if (((Player)e).getDirection() == -1) {
							tex = entityTextures.get(1);
						}else if (((Player)e).getDirection() == 0) {
							tex = entityTextures.get(0);
						}else if (((Player)e).getDirection() == 1) {
							tex = entityTextures.get(2);
						}
						g.drawImage(tex, drawCoords.x, drawCoords.y, drawCoords.width, drawCoords.height, this);
					}
				}
			}
		}

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
