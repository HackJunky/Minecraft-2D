import java.io.Serializable;

public class Payload implements Serializable {
	private static final long serialVersionUID = 6453574971443183184L;
	private Chunk[][] data;
	private int WORLD_WIDTH;
	private int WORLD_HEIGHT;
	private int CHUNK_WIDTH;
	private int CHUNK_HEIGHT;
	private int BLOCK_WIDTH;
	private int BLOCK_HEIGHT;
	private Player player;
	
	public Payload(Chunk[][] data, int WORLD_WIDTH, int WORLD_HEIGHT, int CHUNK_WIDTH, int CHUNK_HEIGHT, int BLOCK_WIDTH, int BLOCK_HEIGHT, Player p) {
		this.data = data;
		this.WORLD_WIDTH = WORLD_WIDTH;
		this.WORLD_HEIGHT = WORLD_HEIGHT;
		this.CHUNK_WIDTH = CHUNK_WIDTH;
		this.CHUNK_HEIGHT = CHUNK_HEIGHT;
		this.BLOCK_WIDTH = BLOCK_WIDTH;
		this.BLOCK_HEIGHT = BLOCK_HEIGHT;
		
		this.player = p;
	}

	public Chunk[][] getData() {
		return data;
	}
	
	public int getWorldWidth() {
		return WORLD_WIDTH;
	}
	
	public int getWorldHeight() {
		return WORLD_HEIGHT;
	}
	
	public int getChunkWidth() {
		return CHUNK_WIDTH;
	}
	
	public int getChunkHeight() {
		return CHUNK_HEIGHT;
	}
	
	public int getBlockWidth() {
		return BLOCK_WIDTH;
	}
	
	public int getBlockHeight() {
		return BLOCK_HEIGHT;
	}
	
	public Player getPlayer() {
		return player;
	}
}
