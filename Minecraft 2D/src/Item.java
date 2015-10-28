
public class Item {
	private Block.BlockID block;
	private int count;
	
	public Item (Block.BlockID b) {
		block = b;
		count = 1;
	}
	
	public Item (String blockname, int count) {
		this.count = count;
		block = Block.BlockID.valueOf(blockname);
	}
	
	public void setCount(int c) {
		count = c;
	}
	
	public int getCount() {
		return count;
	}
	
	public Block.BlockID getBlock() {
		return block;
	}
	
	@Override
	public String toString() {
		return block.getID() + ";" + count;
	}
}
