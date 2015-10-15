import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

public class Packet implements Serializable{
	private static final long serialVersionUID = 9108779363102530646L;
	
	enum Operation {
		DELETE, CREATE
	}
	
	private boolean state;
	private ArrayList<Modification> changes;
	
	public Packet(boolean state) {
		this.state = state;
	}
	
	public void setChanges(ArrayList<Modification> changes) {
		this.changes = changes;
	}
	
	public ArrayList<Modification> getChanges() {
		return changes;
	}
	
	public boolean getState() {
		return state;
	}
	
	public class Modification implements Serializable{
		private static final long serialVersionUID = -3391907845648072318L;
		private Point blockPoint;
		private Block block;
		
		public Modification(Point p, Block b) {
			blockPoint = p;
			block = b;
		}
		
		public Point getPoint() {
			return blockPoint;
		}
		
		public Block getBlock() {
			return block;
		}
	}
}
