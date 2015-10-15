import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.UUID;

import org.w3c.dom.css.Rect;


public abstract class Entity implements Serializable{
	private static final long serialVersionUID = -2026620865575685480L;
	Point position;
	Point block;
	BufferedImage sprite;
	Rect bounds;
	
	String ID;
	
	public Entity(Point position, BufferedImage sprite, Rect bounds) {
		this.position = position;
		this.sprite = sprite;
		this.bounds = bounds;
		
		ID = UUID.randomUUID().toString();
	}
	
	public void setSprite(BufferedImage sprite) {
		this.sprite = sprite;
	}
	
	public void setPosition(Point pos) {
		position = pos;
		
	}
	
	public Point getPos() {
		return position;
	}
	
	public Rect getBounds() {
		return bounds;
	}
	
	public abstract void tick();
}
