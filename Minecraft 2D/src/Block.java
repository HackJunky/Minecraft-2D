import java.awt.Point;
import java.io.Serializable;

public class Block implements Serializable {
	private static final long serialVersionUID = -8249714158244563789L;
	private BlockID block;
	private float lightValue;
	
	public static enum BlockID { 
		Air(0, 1.0, null, "Air.png"),
		Bedrock(1, 1.0, new Point(0, 0), "Bedrock.png"),
		Dirt(2, 0.10, new Point(0, 10), "Dirt.png"), 
		Grass(3, 1.0, null, "Grass.png"),
		Clay(4, 0.1, new Point(8, 4), "Clay.png"),
		Cobblestone(5, 1.0, null, "Cobblestone.png"),
		Ice(6, 1.0, null, "Ice.png"),
		Wood(7, 1.0, null, "Wood.png"), 
		Stone(8, 1.0, null, "Stone.png"),
		Sand(9, 1.0, null, "Sand.png"), 
		Diamond_Ore(10, 0.03, new Point(1, 3), "Diamond Ore.png"),
		Brick(11, 1.0, null, "Brick.png"),
		Cactus(12, 1.0, null, "Cactus.png"),
		Coal_Ore(13, 0.1, new Point(1, 10), "Coal Ore.png"),  
		Gold_Ore(14, 0.02, new Point(1, 4), "Gold Ore.png"),
		Gravel(15, 0.2, new Point(6, 10), "Gravel.png"), 
		Iron_Ore(16, 0.1, new Point(1, 10), "Iron Ore.png"), 
		Lapiz_Block(17, 1.0, null, "Lapiz Block.png"),
		Lapiz_Ore(18, 0.05, new Point(1, 5), "Lapiz Ore.png"),
		Log(19, 1.0, null, "Log.png"), 
		Mossy_Cobblestone(20, 1.0, null, "Mossy Cobblestone.png"), 
		Redstone_Ore(21, 0.06, new Point(1, 5), "Redstone Ore.png"),
		Sandstone(22, 1.0, null, "Sandstone.png"), 
		Snow(23, 1.0, null, "Snow.png"), 
		Snowy_Grass(24, 1.0, null, "Snowy Grass.png"), 
		Stone_Brick(25, 1.0, null, "Stone Brick.png"), 
		Sugar_Cane(26, 1.0, null, "Sugar Cane.png"),
		Yellow_Flower(27, 0.4, null, "Yellow Flower.png"),
		Rose(28, 0.45, null, "Rose.png");
		
		private int id;
		private double spawnChance;
		private Point layers;
		private String textureName;
		
		private BlockID(int i, double s, Point l, String t) {
			this.id = i;
			this.spawnChance = s;
			this.layers = l;
			this.textureName = t;
		}
		
		public int getID() {
			return id;
		}
		
		public double getSpawnChance() {
			return spawnChance;
		}
		
		public Point getSpawnLayers() {
			return layers;
		}
		
		public String getTextureName() { 
			return textureName;
		}
	}
	
	public Block(BlockID block) {
		this.block = block;
	}
	
	public void tick() {
		
	}
	
	public BlockID getBlockID() {
		return block;
	}
	
	public float getLightValue() {
		return lightValue;
	}
}
