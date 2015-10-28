import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import org.w3c.dom.css.Rect;


public class Player extends Entity implements Serializable {
	private static final long serialVersionUID = 2365980508298472801L;
	private int direction = 0;
	private String name;
	
	private double health = 10;
	private double armor = 0;
	private double xp = 50;
	private double food = 10;
	
	private Item[] hotbar;
	private Item[] inventory;
	
	public Player(Point blockSize, Point location, String name) {
		super(blockSize, location, new Point(blockSize.x, 2 * blockSize.y));
		this.name = name;
		direction = 0;
	}
	
	public void setDirection(int d) {
		direction = d;
	}
	
	public String getName() {
		return name;
	}
	
	public void teleport(Point block) {
		this.setBlockPosition(block);
	}

	@Override
	public void entityTick() {
		
	}
	
	public int getDirection() {
		return direction;
	}

	public double getHealth() {
		return health;
	}

	public double getArmor() {
		return armor;
	}

	public double getXp() {
		return xp;
	}

	public double getFood() {
		return food;
	}
	
	public Item[] getHotbar() {
		return hotbar;
	}
	
	public Item[] getInventory() {
		return inventory;
	}
	
}
