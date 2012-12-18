/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pagingTests.supportFiles;

import com.jme3.math.FastMath;
import java.util.List;

/**
 *
 * @author t0neg0d
 */
public class Vector2i {
	public int x;
	public int y;

	/**
	 * Creates a new empty Vector2i
	 */
	public Vector2i() {

	}

	/**
	 * Creates a new Vector2i from the supplied x, y integers
	 * @param x  The x value of the Vector2i
	 * @param y  The y value of the Vector2i
	 */
	public Vector2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Sets the x value of the Vector2i
	 * @param x 
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Sets the y value of the Vector2i
	 * @param y 
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Calculates the distance between this Vector2i and another
	 * @param vector  The second Vector2i used to calculate distance
	 * @return 
	 */
	public int distance(Vector2i vector) {
		int x1 = this.x - vector.x;
		int y1 = this.y - vector.y;
		return (int)FastMath.sqrt(x1*x1+y1*y1);
	}

	/**
	 * Searches the supplies list of Vector2i for a value comparison instance of this Vector2i
	 * @param vectors  List of Vector2i to search 
	 * @return 
	 */
	public boolean containedBy(List<Vector2i> vectors) {
		boolean contains = false;
		for (Vector2i vector : vectors) {
			if (vector.x == x && vector.y == y) {
				contains = true;
				break;
			}
		}
		return contains;
	}

	@Override
	public Vector2i clone() {
		return new Vector2i(x, y);
	}

	@Override
	public String toString() {
		return "(" + String.valueOf(x) +"," + String.valueOf(y) + ")";
	}
}
