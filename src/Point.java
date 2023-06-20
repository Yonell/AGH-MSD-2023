import java.util.ArrayList;
import java.util.Random;


public class Point{

	private static final int SFMAX = 100000;
	
	public ArrayList<Point> neighbors;

	@Deprecated
	public static Integer [] types = {0,1,2,3,4};// 0 - pole nieokreślone, 1 - las, 2 - podmokłe, 3 - miasto, 4 - myśliwy

	public boolean garbageCollection;
	public int type;
	public int staticField;
	public int x;
	public int y;

	public void setCurrentFood(float currentFood) {
		this.currentFood = currentFood;
	}

	private float currentFood;
	private float foodCap;
	private Random rng = new Random();
	private static final float MIN_FOOD_PER_ROUND = 0.008f;
	private static final float MAX_FOOD_PER_ROUND = 0.08f;
	private static final float MIN_STARTING_FOOD = 0.05f;
	private static final float MAX_STARTING_FOOD = 1.0f;
	private static final float MIN_FOOD_CAP = 1.0f;
	public static final float MAX_FOOD_CAP = 3.0f;
	private static final float MIN_GARBAGE_FOOD = 10.0f;
	public static final float MAX_GARBAGE_FOOD = 25.0f;

	public Point(int x, int y) {
		this.x=x;
		this.y=y;
		type=0;
		staticField = SFMAX;
		neighbors= new ArrayList<Point>();
		this.foodCap = rng.nextFloat(MIN_FOOD_CAP, MAX_FOOD_CAP);
		this.currentFood = rng.nextFloat(MIN_STARTING_FOOD, MAX_STARTING_FOOD);
	}

	public Point(int type) {
		this.type=type;
		staticField = SFMAX;
		neighbors= new ArrayList<Point>();
		this.foodCap = rng.nextFloat(MIN_FOOD_CAP, MAX_FOOD_CAP);
		this.currentFood = rng.nextFloat(MIN_STARTING_FOOD, MAX_STARTING_FOOD);
	}

	public void growFood() {
		switch (this.type){
			case 1:{
				this.currentFood += rng.nextFloat(MIN_FOOD_PER_ROUND, MAX_FOOD_PER_ROUND);
				this.currentFood = Math.min(this.foodCap, this.currentFood);
			} break;
			case 3: {
				if (this.garbageCollection) {
					this.garbageCollection = false;
					this.currentFood = rng.nextFloat(MIN_GARBAGE_FOOD, MAX_GARBAGE_FOOD);
				}
			} break;
			default: {
				this.currentFood = 0.0f;
			} break;
		}
	}

	public float getCurrentFood() {
		return this.currentFood;
	}

	public float calculateFoodSmell(int depth){
		if(depth == 1){
			return this.currentFood;
		}
		else {
			float maxVal = 0.0f;
			for (Point nei : neighbors) {
				float retVal = nei.calculateFoodSmell(depth-1);
				maxVal = Math.max(maxVal, retVal);
			}
			return Math.max(this.currentFood, maxVal/2);
		}
	}

	public float eatAllFood() {
		float toBeEaten = this.currentFood;
		this.currentFood = 0;
		return toBeEaten;
	}

	public void triggerGarbageCollection(){
		this.garbageCollection = true;
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