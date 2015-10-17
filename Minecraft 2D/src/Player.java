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
	
	public Player(String name, Point dimensions) {
		super(new Point(0, 0), dimensions);
		this.name = name;
	}
	
	public void setDirection(int d) {
		direction = d;
	}
	
	public String getName() {
		return name;
	}
	
	public void teleport(Point block) {
		this.setPosition(block);
	}

	@Override
	public void tick() {
		
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

}
