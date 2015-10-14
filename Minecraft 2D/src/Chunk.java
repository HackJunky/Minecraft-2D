import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
public class Chunk implements Serializable{
	private static final long serialVersionUID = 7421916881162189998L;
	private World world;

	static int CHUNK_X;
	static int CHUNK_Y;

	static int CHUNK_WIDTH;
	static int CHUNK_HEIGHT;

	int LAYER_START_Y;
	int LAYER_END_Y;

	static int WORLD_OCEAN_LEVEL = 72;
	static float ORE_SPAWN_CHANCE = 0.50f;
	static int ORE_PATTERN_SIZE = 3;
	static double POCKET_SIZE_MAX_SCALAR = 2;
	static int CAVE_MAX_SIZE = 15;
	static int SLOPE_MAX_HEIGHT = 6;
	static boolean IS_LOWEST = false;

	private Block[][] data;

	private boolean generated;
	private int generationStage;

	private String CHUNK_ID;

	public Chunk(int blocksWide, int blocksHigh, World world, int chunkX, int chunkY, int startY, int endY) {
		CHUNK_WIDTH = blocksWide;
		CHUNK_HEIGHT = blocksHigh;
		CHUNK_X = chunkX;
		CHUNK_Y = chunkY;
		LAYER_START_Y = startY;
		LAYER_END_Y = endY;
		this.world = world;

		CHUNK_ID = UUID.randomUUID().toString();

		world.getUtil().Log("Registering chunk '" + CHUNK_ID + "' for layers " + LAYER_START_Y + " to " + LAYER_END_Y + "...");
	}

	public void tick() {
		if (!generated) {
			if (data == null) {
				data = new Block[CHUNK_WIDTH][CHUNK_HEIGHT];
			}
			if (generationStage == 0) {
				generateFill();
			}else if (generationStage == 1) {
				generateCaves();
			}else if (generationStage == 2) {
				generateOres();
			}else if (generationStage == 3) {
				generateLiquids();
			}else if (generationStage == 4) {
				generateFeatures();
			}else if (generationStage == 5) {
				generated = true;
			}
		}else {
			for (int x = 0; x < data.length; x++) {
				for (int y = 0; y < data[0].length; y++) {
					data[x][y].tick();
				}
			}
		}
	}

	public Block[][] getData() {
		return data;
	}

	private void generateFill() {
		for (int x = 0; x < data.length; x++) {
			for (int y = 0; y < data[0].length; y++) {
				if (LAYER_START_Y + y > WORLD_OCEAN_LEVEL) {
					data[x][y] = new Block(Block.BlockID.Air);
				}else {
					data[x][y] = new Block(Block.BlockID.Stone);
				}
			}
		}
		generationStage++;
	}

	private void generateOres() {
		if (IS_LOWEST) {
			for (int x = 0; x < data.length; x++) {
				data[x][0] = new Block(Block.BlockID.Bedrock);
			}
		}
		ArrayList<Block.BlockID> ores = new ArrayList<Block.BlockID>();
		for (int i = 0; i < Block.BlockID.values().length; i++) {
			Block.BlockID block = Block.BlockID.values()[i];
			if (block.getSpawnLayers() != null && block.getSpawnChance() < 1.0) {
				ores.add(block);
			}
		}
		for (int x = 0; x < data.length; x += ORE_PATTERN_SIZE) {
			if (x > data.length) {
				break;
			}
			int stoneLayer = -1;
			for (int y = 0; y < data[0].length; y++) {
				if (stoneLayer == -1 && data[x][y].getBlockID().equals(Block.BlockID.Stone)) {
					stoneLayer = y;
					break;
				}
			}
			for (int y = stoneLayer; y < data[0].length; y++) {
				Random rand = new Random();
				if (ORE_SPAWN_CHANCE < rand.nextFloat()) {
					for (int i = 0; i < ores.size(); i++) {
						Block.BlockID block = ores.get(i);
						int oreLayer = getOreLayer(y);
						Point spawnLayers = block.getSpawnLayers();
						if (spawnLayers.x < oreLayer && spawnLayers.y > oreLayer) {
							if (block.equals(Block.BlockID.Gravel)) {
								int[][] gravelPocket = generatePocket((int)(ORE_PATTERN_SIZE * (rand.nextDouble() * POCKET_SIZE_MAX_SCALAR)), (int)(ORE_PATTERN_SIZE *  (rand.nextDouble() * POCKET_SIZE_MAX_SCALAR) / 2));
								for (int ax = 0; ax < gravelPocket.length; ax++) {
									for (int ay = 0; ay < gravelPocket[0].length; ay++) {
										if (x + ax < CHUNK_WIDTH && y + ay < CHUNK_HEIGHT && x + ax > 0 && y + ay > 0) {
											if (data[x + ax][y + ay].getBlockID().equals(Block.BlockID.Stone)) {
												if (gravelPocket[ax][ay] == 1) {
													data[x + ax][y + ay] = new Block(Block.BlockID.Gravel);
												}
											}
										}
									}
								}
							}else {
								double spawnChance = block.getSpawnChance();
								float spawnSelector = rand.nextFloat();
								if (spawnSelector < spawnChance) {
									int[][] orePattern = generateOrePattern(ORE_PATTERN_SIZE);
									for (int ax = 0; ax < orePattern.length; ax++) {
										for (int ay = 0; ay < orePattern[0].length; ay++) {
											if (x + ax < CHUNK_WIDTH && y + ay < CHUNK_HEIGHT && x + ax > 0 && y + ay > 0) {
												if (data[x + ax][y + ay].getBlockID().equals(Block.BlockID.Stone)) {
													if (orePattern[ax][ay] == 1) {
														data[x + ax][y + ay] = new Block(block);
													}
												}
											}
										}
									}
									break;
								}
							}
						}
					}
				}
			}
		}
		generationStage++;
	}

	private int getOreLayer(int y) {
		int layerSize = WORLD_OCEAN_LEVEL / 10;
		int currentY = LAYER_START_Y + y;
		return currentY / layerSize;
	}

	private int[][] generatePocket(int width, int height) {
		int[][] pattern = new int[width][height];
		for (int x = 0; x < pattern.length; x++) {
			for (int y = 0; y < pattern[0].length; y++) {
				pattern[x][y] = 0;
			}
		}
		Random r = new Random();
		for (int x = r.nextInt((width / 5) + 1); x < pattern.length - r.nextInt((width / 5) + 1); x++) {
			for (int y = r.nextInt((height / 5) + 1); y < pattern[x].length - r.nextInt((height / 5) + 1); y++) {
				pattern[x][y] = 1;
			}
		}
		return pattern;
	}

	private int[][] generateOrePattern(int size) {
		int[][] pattern = new int[size][size];
		for (int x = 0; x < pattern.length; x++) {
			for (int y = 0; y < pattern[0].length; y++) {
				pattern[x][y] = new Random().nextInt(2);
			}
		}
		return pattern;
	}

	private void generateCaves() {
		for (int x = 0; x < CHUNK_WIDTH; x += 2 * CAVE_MAX_SIZE) {
			int stoneHeight = 0;
			for (int y = 0; y < data[0].length; y++) {
				if (data[x][y].getBlockID().equals(Block.BlockID.Stone)) {
					stoneHeight = y;
					break;
				}
			}
			Random rand = new Random();
			if (rand.nextFloat() < 0.5f) {
				int diff = rand.nextInt(CHUNK_HEIGHT - stoneHeight);
				int height = stoneHeight + diff;
				if (height < data[0].length - CAVE_MAX_SIZE) {
					int size = (int)(CAVE_MAX_SIZE * rand.nextFloat());
					if (size > 0) {

					}
				}
			}
		}
		generationStage++;
	}

	private void generateLiquids() {
		generationStage++;
	}

	private void generateFeatures() {
		Random rand = new Random();
		int STEP_CHANGE = 2;
		int HEIGHT_START = (SLOPE_MAX_HEIGHT / 3) + rand.nextInt(SLOPE_MAX_HEIGHT);

		int slope = rand.nextInt(STEP_CHANGE);
		int height = HEIGHT_START;

		int stoneHeight = 0;

		for (int x = 0; x < data.length; x++) {
			for (int y = 0; y < data[0].length; y++) {
				if (data[x][y].getBlockID().equals(Block.BlockID.Stone)) {
					stoneHeight = y;
					break;
				}
			}
			int direction = rand.nextInt(2);
			if (direction == 0) {
				height += slope;
			}else if (direction == 1) {
				height -= slope;
			}
			if (stoneHeight > 0) {
				for (int y = stoneHeight - height; y <= stoneHeight + height; y++) {
					if (y > 0) {
						if (y == stoneHeight - height) {
							data[x][y] = new Block(Block.BlockID.Grass);
						}else if (y <= stoneHeight) {
							data[x][y] = new Block(Block.BlockID.Stone);
						}else {
							data[x][y] = new Block(Block.BlockID.Dirt);
						}
					}
				}
			}
		}

		generationStage++;
	}

	public boolean isGenerated() {
		return generated;
	}

	@Override
	public boolean equals(Object c) {
		return CHUNK_ID.equals(((Chunk) c).getID());
	}

	public String getID() {
		return CHUNK_ID;
	}

}
