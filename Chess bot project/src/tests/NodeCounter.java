package tests;

public class NodeCounter {
	private int nodes;
	
	public NodeCounter() {
		nodes = 0;
	}
	
	public int getCount() {
		return nodes;
	}
	
	public void increment() {
		nodes++;
	}
	
	public String toString() {
		return ""+nodes;
	}
}
