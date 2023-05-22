import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;



public class Point{

	private static final int SFMAX = 100000;
	
	public ArrayList<Point> neighbors;
	public static Integer []types ={0,1,2,3};// 0 - pole nieokreślone, 1 - las, 2 - podmokłe, 3 - miasto
	public int type;
	public int staticField;
	public boolean isPedestrian;

	public Point() {
		type=0;
		staticField = SFMAX;
		neighbors= new ArrayList<Point>();
	}

	public Point(int type) {
		this.type=type;
		staticField = SFMAX;
		neighbors= new ArrayList<Point>();
	}
	
	public void clear() {
		staticField = SFMAX;
		
	}

	public boolean calcStaticField() {		
		return false;
	}
	
	public void move(){
	
	}

	public void addNeighbor(Point nei) {
		neighbors.add(nei);
	}
	
}