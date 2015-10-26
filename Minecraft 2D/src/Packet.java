import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

public class Packet implements Serializable{
	private static final long serialVersionUID = 9108779363102530646L;
	
	enum Operation {
		DELETE, CREATE
	}
	
	private boolean state;
	private ArrayList<Entity> entities;
	private Player player;
	private ArrayList<Modification> changes;

	private Color skyColor;
	private Color worldLight;
	
	public Packet(boolean state) {
		this.state = state;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void setEntities(ArrayList<Entity> entities) {
		this.entities = entities;
	}
	
	public ArrayList<Entity> getEntities() {
		return entities;
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
	
	public Color getSkyColor() {
		return skyColor;
	}

	public void setSkyColor(Color skyColor) {
		this.skyColor = skyColor;
	}

	public Color getWorldLight() {
		return worldLight;
	}

	public void setWorldLight(Color worldLight) {
		this.worldLight = worldLight;
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
