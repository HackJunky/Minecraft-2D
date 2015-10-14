import java.awt.image.BufferedImage;
import org.w3c.dom.css.Rect;


public abstract class Entity {
	String name;
	Vector3 position;
	BufferedImage sprite;
	Rect bounds;
	
	public Entity(String name, Vector3 position, BufferedImage sprite, Rect bounds) {
		this.name = name;
		this.position = position;
		this.sprite = sprite;
		this.bounds = bounds;
	}
	
	public String getName() {
		return name;
	}
	
	public Vector3 getPos() {
		return position;
	}
	
	public Rect getBounds() {
		return bounds;
	}
}
