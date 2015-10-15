import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import org.w3c.dom.css.Rect;


public class Player extends Entity implements Serializable {
	private static final long serialVersionUID = 2365980508298472801L;
	private BufferedImage[] sprites;
	
	private int direction = 0;
	private String name;
	
	private double health;
	private double armor;
	private double xp;
	private double food;
	
	public Player(String name, BufferedImage[] sprites, Rect dimensions) {
		super(new Point(0, 0), sprites[1], dimensions);
		this.sprites = sprites;
		this.name = name;
	}
	
	public void setDirection(int d) {
		this.setSprite(sprites[direction + 1]);
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

}
