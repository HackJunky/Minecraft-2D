import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;

public class World {
	private Chunk[][] world;
	private Util util;

	private static int WORLD_WIDTH;
	private static int WORLD_HEIGHT;
	private static int CHUNK_WIDTH;
	private static int CHUNK_HEIGHT;

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
			if (topography == null) {
				topography = generateTopography(CHUNK_WIDTH * WORLD_WIDTH);
			}
			if (!generated) {
				generateTerrain();
			}
		}
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

	public Point convertToWorldspace(int chunkX, int chunkY, Point pos) {
		int offsetX = chunkX * CHUNK_WIDTH;
		int offsetY = chunkY * CHUNK_HEIGHT;
		return new Point(pos.x + offsetX, pos.y + offsetY);
	}

	public Block[][] getViewportData(Rectangle blockViewport) {
		//System.out.println("Requesting viewport " + blockViewport.x + ", " + blockViewport.y + ", " + blockViewport.width + ", " + blockViewport.height);
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

	public boolean isGenerated() {
		return generated;
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

	synchronized public Chunk[][] getChunkData() {
		return world;
	}
	
	synchronized public void setChunkData(Chunk[][] data) {
		world = data;
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
					world[x][y] = new Chunk(CHUNK_WIDTH, 
							CHUNK_HEIGHT,
							this, x, y, 
							startY,
							endY,
							chunkMap,
							oceanLevel);
					if (endY == WORLD_HEIGHT * CHUNK_HEIGHT - 1) {
						world[x][y].IS_LOWEST = true;
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
}
