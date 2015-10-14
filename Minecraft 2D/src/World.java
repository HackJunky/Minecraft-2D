import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
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

	public World(int chunksWide, int chunksHigh, int chunkWidth, int chunkHeight, Util util) {
		WORLD_WIDTH = chunksWide;
		WORLD_HEIGHT = chunksHigh;
		CHUNK_WIDTH = chunkWidth;
		CHUNK_HEIGHT = chunkHeight;

		this.util = util;

		startTime = System.currentTimeMillis();

		util.Log("Generating terrain..");
	}

	public void update() {
		if (!generated) {
			generateTerrain();
		}else {

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

	public Chunk[][] getChunkData() {
		return world;
	}

	public void generateTerrain() {
		int totalChunks = WORLD_WIDTH * WORLD_HEIGHT;
		int currentChunks = 0;
		if (world == null) {
			world = new Chunk[WORLD_WIDTH][WORLD_HEIGHT];
		}
		Random rand = new Random();
		int previousSlope = (Chunk.SLOPE_MAX_HEIGHT / 2) + rand.nextInt(Chunk.SLOPE_MAX_HEIGHT);
		for (int y = 0; y < world[0].length; y++) {
			int nextSlope = (Chunk.SLOPE_MAX_HEIGHT / 2) + rand.nextInt(Chunk.SLOPE_MAX_HEIGHT);
			for (int x = 0; x < world.length; x++) {
				if (world[x][y] == null) {
					world[x][y] = new Chunk(CHUNK_WIDTH, CHUNK_HEIGHT, this, x, y, y * CHUNK_HEIGHT,  (y * CHUNK_HEIGHT) + CHUNK_HEIGHT, previousSlope, nextSlope, ((WORLD_HEIGHT / 2) * CHUNK_HEIGHT) + (CHUNK_HEIGHT / 3));
					if ((y * CHUNK_HEIGHT) + CHUNK_HEIGHT == (world[0].length - 1 * CHUNK_HEIGHT) + CHUNK_HEIGHT) {
						world[x][y].IS_LOWEST = true;
					}
				}
				previousSlope = nextSlope;
				nextSlope = (Chunk.SLOPE_MAX_HEIGHT / 2) + rand.nextInt(Chunk.SLOPE_MAX_HEIGHT);
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
}
