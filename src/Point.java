import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;



public class Point{

	private static final int SFMAX = 100000;
	
	public ArrayList<Point> neighbors;
	public static Integer []types ={0,1,2,3};// 0 - pole nieokreślone, 1 - las, 2 - podmokłe, 3 - miasto
	public int type;
	public int staticField;
	public int dynamicField1;
	public int dynamicField2;
	public boolean isPedestrian;
	public int x;
	public int y;

	public Point(int x, int y) {
		this.x=x;
		this.y=y;
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

	public void setStaticField(int staticField) {
		this.staticField = staticField;
	}

	public void addNeighbor(Point nei) {
		neighbors.add(nei);
	}

	// equals and hashCode. point is equal to another point if it is the same object

	public boolean equals(Object o){
		if(o instanceof Point p){
			return this==p;
		}
		return false;
	}
	public int hashCode(){
		return super.hashCode();
	}
}