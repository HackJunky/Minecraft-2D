import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.UUID;

import org.w3c.dom.css.Rect;


public abstract class Entity implements Serializable{
	private static final long serialVersionUID = -2026620865575685480L;
	private Point WORLD_BLOCKSIZE;
	
	private Point position;
	private Point offset;
	
	private Point bounds;
	
	private String ID;
	
	public Entity(Point blockSize, Point position, Point bounds) {
		this.position = position;
		this.WORLD_BLOCKSIZE = blockSize;
		this.bounds = bounds;
		ID = UUID.randomUUID().toString();
		offset = new Point(0, 0);
	}
	
	public void setBlockPosition(Point pos) {
		position = pos;
	}
	
	public void setPixelPosition(Point pixel) {
		int blockX = pixel.x / WORLD_BLOCKSIZE.x;
		int blockY = pixel.y / WORLD_BLOCKSIZE.y;
		int pixelX = (pixel.x - (blockX * WORLD_BLOCKSIZE.x));
		int pixelY = (pixel.y - (blockY * WORLD_BLOCKSIZE.y));
		position = new Point(blockX, blockY);
		offset = new Point(pixelX, pixelY);
	}
	
	public Point getBlockPos() {
		return position;
	}
	
	public Point getPixelOffset() {
		return offset;
	}
	
	public Point getBounds() {
		return bounds;
	}
	
	public void tick() {
		
		entityTick();
	}
	
	public String getID() {
		return ID;
	}
	
	public abstract void entityTick();
}
