import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.UUID;

import org.w3c.dom.css.Rect;


public abstract class Entity implements Serializable{
	private static final long serialVersionUID = -2026620865575685480L;
	Point position;
	Point block;
	Point bounds;
	
	String ID;
	
	public Entity(Point position, Point bounds) {
		this.position = position;
		this.bounds = bounds;
		
		ID = UUID.randomUUID().toString();
	}
	
	public void setPosition(Point pos) {
		position = pos;
		
	}
	
	public Point getPos() {
		return position;
	}
	
	public Point getBounds() {
		return bounds;
	}
	
	public abstract void tick();
}
