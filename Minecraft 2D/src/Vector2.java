import java.io.Serializable;

public class Vector2 implements Serializable{
	private static final long serialVersionUID = -1322125261813116299L;
	int x;
	int y;

	public Vector2(int x, int y) {
		this.x = x; 
		this.y = y;
	}
	
	public boolean Equals(Vector2 comparator) {
		if (comparator.x == x && comparator.y == y) {
			return true;
		}else {
			return false;
		}
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
