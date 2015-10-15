import java.awt.Point;
import java.awt.image.BufferedImage;
import org.w3c.dom.css.Rect;


public abstract class Entity {
	String name;
	Point position;
	BufferedImage sprite;
	Rect bounds;
	
	public Entity(String name, Point position, BufferedImage sprite, Rect bounds) {
		this.name = name;
		this.position = position;
		this.sprite = sprite;
		this.bounds = bounds;
	}
	
	public String getName() {
		return name;
	}
	
	public Point getPos() {
		return position;
	}
	
	public Rect getBounds() {
		return bounds;
	}
}
