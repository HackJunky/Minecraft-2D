import java.io.Serializable;

public class Vector3 implements Serializable{
	private static final long serialVersionUID = 1155027505662172251L;
	int x;
	int y;
	int z;
	
	public Vector3(int x, int y, int z) {
		this.x = x; 
		this.y = y;
		this.z = z;
	}
	
	public boolean Equals(Vector3 comparator) {
		if (comparator.x == x && comparator.y == y && comparator.z == z) {
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

	public int getZ() {
		return z;
	}
}
