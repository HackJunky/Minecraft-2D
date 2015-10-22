import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class World implements Serializable {
	private static final long serialVersionUID = 5471323799966531710L;
	private Chunk[][] world;
	private ArrayList<Entity> entities;
	private Util util;

	private static int WORLD_WIDTH;
	private static int WORLD_HEIGHT;
	private static int CHUNK_WIDTH;
	private static int CHUNK_HEIGHT;

	private static double WORLD_GRAVITY = -9.8;
	private float deltaTime = 0f;

	private Color skyColor = Color.DARK_GRAY;
	private Color worldOverlayColor = new Color(0, 0, 0, 0.0f);		
	private float worldTime = -1.0f;

	private long lastTime = 0;
	private boolean generated;
	long startTime;
	int[] topography;

	boolean isServer = true;

	public World(boolean isServer, int chunksWide, int chunksHigh, int chunkWidth, int chunkHeight, Util util) {
		WORLD_WIDTH = chunksWide;
		WORLD_HEIGHT = chunksHigh;
		CHUNK_WIDTH = chunkWidth;
		CHUNK_HEIGHT = chunkHeight;

		this.isServer = isServer;

		this.util = util;

		startTime = System.currentTimeMillis();

		util.Log("Generating terrain..");
	}

	public void update() {
		if (isServer) {
			if (lastTime == 0) {
				lastTime = System.currentTimeMillis();
			}
			if (topography == null) {
				topography = generateTopography(CHUNK_WIDTH * WORLD_WIDTH);
			}
			if (!generated) {
				entities = new ArrayList<Entity>();
				generateTerrain();
			}else {
				for (Entity e : entities) {
					e.tick();
				}
			}
			calculateTime();
			deltaTime = System.currentTimeMillis() - lastTime;
			lastTime = System.currentTimeMillis();
		}
	}

	public void calculateTime() {
		worldTime = floatLerp(worldTime, 1.0f, deltaTime);
		if (worldTime >= 0.95f) {
			worldTime = -1.0f;
		}
		if (worldTime <= -0.8 || worldTime >= 0.8) {
			//Night
			//Color from Sky -> Night Color
			skyColor = colorLerp(skyColor, new Color(70, 80, 100, 255), deltaTime);
		}else {
			if (worldTime <= -0.6 || worldTime >= 0.6) {
				//Sunrise/Sunset
				if (worldTime < 0) {
					//Sunrise
					skyColor = colorLerp(skyColor, new Color(140, 200, 240, 255), deltaTime);
				}else {
					//Sunset
					skyColor = colorLerp(skyColor, new Color(70, 80, 100, 255), deltaTime);
				}
			}else {
				//Day
				skyColor = colorLerp(skyColor, new Color(140, 200, 240, 255), deltaTime);
			}
		}
		
		//System.out.println("Time: " + worldTime + "@SkyColor: " + skyColor.toString());
	}

	public float floatLerp(float start, float end, float deltaTime) {
		double distance = (end - start);
		double percent = deltaTime / 1000 / 1000;
		double lerp = (start + (percent * distance));
		return (float)lerp;
	}

	public double intLerp(int start, int end, float deltaTime) {
		double distance = (end - start);
		double percent = deltaTime / 1000;
		double lerp = (start + (percent * distance));
		System.out.println(lerp);
		return lerp;
	}

	public Color colorLerp(Color a, Color b, float deltaTime) {
		return new Color((int)intLerp(a.getRed(), b.getRed(), deltaTime), (int)intLerp(a.getGreen(), b.getGreen(), deltaTime), (int)intLerp(a.getBlue(), b.getBlue(), deltaTime), (int)intLerp(a.getAlpha(), b.getAlpha(), deltaTime));
	}

	public Point convertToWorldspace(int chunkX, int chunkY, Point pos) {
		int offsetX = chunkX * CHUNK_WIDTH;
		int offsetY = chunkY * CHUNK_HEIGHT;
		return new Point(pos.x + offsetX, pos.y + offsetY);
	}

	public Block[][] getViewportData(Rectangle blockViewport) {
		Block[][] output = new Block[blockViewport.width + 1][blockViewport.height + 1];
		for (int x = 0; x < blockViewport.width + 1; x++) {
			int xInverse = blockViewport.width - x;
			for (int y = 0; y < blockViewport.height + 1; y++) {
				int yInverse = blockViewport.height - y;
				Block block = getBlockByCoords(new Point(x + blockViewport.x, y + blockViewport.y));
				if (block != null) {
					output[xInverse][yInverse] = block;
				}
			}
		}
		return output;
	}

	synchronized public void applyChanges(ArrayList<Packet.Modification> mods) {
		for (int i = 0; i < mods.size(); i++) {
			Packet.Modification mod = mods.get(i);
			setBlock(mod.getPoint(), mod.getBlock());
			util.Log("Updating (" + mod.getPoint().x + ", " + mod.getPoint().y + ") to " + mod.getBlock().getBlockID().toString() + ".");
		}
	}

	public void setBlock(Point block, Block data) {
		int x = block.x / CHUNK_WIDTH;
		int y = block.y / CHUNK_HEIGHT;
		int dataX = block.x - (x * CHUNK_WIDTH);
		int dataY = block.y - (y * CHUNK_HEIGHT);
		world[x][y].getData()[dataX][dataY] = data;
	}

	public Block getBlockByCoords(Point block) {
		int x = block.x / CHUNK_WIDTH;
		int y = block.y / CHUNK_HEIGHT;
		int dataX = block.x - (x * CHUNK_WIDTH);
		int dataY = block.y - (y * CHUNK_HEIGHT);
		return world[x][y].getData()[dataX][dataY];
	}

	public Chunk getChunkByCoords(Point block) {
		int x = block.x / CHUNK_WIDTH;
		int y = block.y / CHUNK_HEIGHT;
		return world[x][y];
	}


	public void generateTerrain() {
		int totalChunks = WORLD_WIDTH * WORLD_HEIGHT;
		int currentChunks = 0;
		if (world == null) {
			world = new Chunk[WORLD_WIDTH][WORLD_HEIGHT];
		}
		int oceanLevel = ((WORLD_HEIGHT / 2) * CHUNK_HEIGHT) + (CHUNK_HEIGHT / 2);
		for (int y = 0; y < world[0].length; y++) {
			int startY = y * CHUNK_HEIGHT;
			int endY = startY + CHUNK_HEIGHT;
			int[] chunkMap = null;
			for (int x = 0; x < world.length; x++) {
				if (world[x][y] == null) {
					if (oceanLevel < endY && oceanLevel > startY) {
						chunkMap = splitArray(topography, x * CHUNK_WIDTH, (x * CHUNK_WIDTH) + CHUNK_WIDTH);
					}
					world[x][y] = new Chunk(
							CHUNK_WIDTH, CHUNK_HEIGHT,
							x, y, 
							startY,
							endY,
							chunkMap,
							oceanLevel);
					if (endY == WORLD_HEIGHT * CHUNK_HEIGHT - 1) {
						world[x][y].setLowest(true);
					}
				}
			}
		}
		for (int x = 0; x < world.length; x++) {
			for (int y = 0; y < world[0].length; y++) {
				world[x][y].tick();
				if (world[x][y].isGenerated()) {
					currentChunks++;
				}
			}
		}
		if (totalChunks == currentChunks) {
			generated = true;
			util.Log((WORLD_WIDTH * CHUNK_WIDTH) + "x" + (WORLD_HEIGHT * CHUNK_HEIGHT) + " terrain built in " + (System.currentTimeMillis() - startTime) + "ms.");
		}
	}

	public int[] splitArray(int[] arr, int start, int end) {
		int[] output = new int[end - start];
		int j = 0;
		for (int i = start; i < end; i++) {
			output[j] = arr[i];
			j++;
		}
		return output;
	}

	public int[] generateTopography(int size) {
		long start = System.currentTimeMillis();
		Random r = new Random();
		int[] output = new int[size];
		int previous = 0;
		for (int i = 0; i < size; i++) {
			boolean a = r.nextBoolean();
			boolean b = r.nextBoolean();
			if (b) {
				output[i] = previous;
			}else {
				if (a) {
					if (previous - 1 >= 0) {
						previous--;
					}
					output[i] = previous;
				}else {
					if (previous + 1 <= Chunk.SLOPE_MAX_HEIGHT) {
						previous++;
					}
					output[i] = previous;
				}
			}
		}
		util.Log("Generated terrain map in " + (System.currentTimeMillis() - start) + " ms.");
		return output;
	}

	public boolean isGenerated() {
		return generated;
	}

	public void setGenerated(boolean b) {
		generated = b;
	}

	synchronized public Chunk[][] getChunkData() {
		return world;
	}

	synchronized public void setChunkData(Chunk[][] data) {
		generated = true;
		world = data;
	}

	public Color getSkyColor() {
		return skyColor;
	}

	public void setEntities(ArrayList<Entity> entities) {
		this.entities = entities;
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}

	public Util getUtil() {
		return util;
	}

	public int getHeight() {
		return CHUNK_HEIGHT * WORLD_HEIGHT;
	}

	public int getWidth() {
		return CHUNK_WIDTH * WORLD_WIDTH;
	}

	public Color getLight() {
		return worldOverlayColor;
	}
}
