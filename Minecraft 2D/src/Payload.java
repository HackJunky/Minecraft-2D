import java.io.Serializable;

public class Payload implements Serializable {
	private static final long serialVersionUID = 6453574971443183184L;
	private Chunk[][] data;
	int WORLD_WIDTH;
	int WORLD_HEIGHT;
	int CHUNK_WIDTH;
	int CHUNK_HEIGHT;

	public Payload(Chunk[][] data, int WORLD_WIDTH, int WORLD_HEIGHT, int CHUNK_WIDTH, int CHUNK_HEIGHT) {
		this.data = data;
		this.WORLD_WIDTH = WORLD_WIDTH;
		this.WORLD_HEIGHT = WORLD_HEIGHT;
		this.CHUNK_WIDTH = CHUNK_WIDTH;
		this.CHUNK_HEIGHT = CHUNK_HEIGHT;
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
}
