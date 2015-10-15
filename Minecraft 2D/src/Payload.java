import java.io.Serializable;

public class Payload implements Serializable {
	private static final long serialVersionUID = 6453574971443183184L;
	private Chunk[][] data;
	
	public Payload(Chunk[][] data) {
		this.data = data;
	}
	
	public Chunk[][] getData() {
		return data;
	}
}
