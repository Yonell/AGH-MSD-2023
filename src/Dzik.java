import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Dzik {
    private int x;
    private int y;
    private final Board board;
    private int dziksHere;
    private float hungerLevel = 0.0f;    //Current hunger level per Dzik. Dzik won't eat, if it's negative. The higher the hunger, the more it affects Dzik's movement
    private static final float MOVING_DZIK_CONSUMPTION_RATE = 0.7f;     //How much food per round does Dzik need if it is moving
    private static final float STATIC_DZIK_CONSUMPTION_RATE = 0.3f;     //How much food per round does Dzik need if it stays on the same field
    private static final float DZIK_MAX_HUNGER = 50.0f;    //Value at which Dzik dies
    private static final float DZIK_MIN_HUNGER = -10.0f;    //How much dzik can eat "in advance"

    private static final float HUNGER_FACTOR_MULTIPLIER_A = 0.38f; //A multiplier in foodFactor = exp((hunger*A)*B
    private static final float HUNGER_FACTOR_MULTIPLIER_B = 10000.0f; //B multiplier in foodFactor = exp((hunger*A)*B

    private static final int ATTRACTIVENESS_RANDOMNESS = 20000;


    public Dzik(int x, int y, Board board) {
        this.x = x;
        this.y = y;
        this.board = board;
        this.dziksHere = 1;
    }

    public Dzik(int x, int y, Board board, int dziksHere) {
        this.x = x;
        this.y = y;
        this.board = board;
        this.dziksHere = dziksHere;
    }

    public int getDziksHere() {return dziksHere;}

    public void eat(){
        if(this.hungerLevel >= 0){
            float toBeEaten = this.board.points[this.x][this.y].eatAllFood();
            this.hungerLevel -= toBeEaten/this.dziksHere;
            this.hungerLevel = Math.max(DZIK_MIN_HUNGER, this.hungerLevel);
        }
        if(this.hungerLevel > DZIK_MAX_HUNGER){
            this.die();
        }
    }

    public void die(){
        Point currentLocation = this.board.points[this.x][this.y];
        this.board.dziks.get(currentLocation).remove(this);
    }

    public void kill_one(){
    	if(this.dziksHere > 1){
    		this.dziksHere--;
    	} else {
    		this.die();
    	}
    }

    public void move(){
        final float foodFactor = (float) (Math.exp(this.hungerLevel * HUNGER_FACTOR_MULTIPLIER_A) * HUNGER_FACTOR_MULTIPLIER_B);

        ArrayList<Point> neighbors;
        neighbors = (ArrayList<Point>) board.points[x][y].neighbors.clone();
        neighbors.add(board.points[x][y]);
        Collections.shuffle(neighbors);


        Point p = neighbors.stream()
                .filter(p3 -> p3.x>0 && p3.x <= Board.MAX_SIZE && p3.y>0 && p3.y <= Board.MAX_SIZE)
                .max(Comparator.comparingInt(
                        p2 -> p2.staticField
                        + (int) (p2.calculateFoodSmell(Board.DZIK_SENSE_RADIUS)*foodFactor)
                        + (int) (Math.random() * ATTRACTIVENESS_RANDOMNESS))
                ).get();
    	board.dziks.get(board.points[x][y]).remove(this);

        if(p.x == x && p.y == y)
            this.hungerLevel += STATIC_DZIK_CONSUMPTION_RATE;
        else
            this.hungerLevel += MOVING_DZIK_CONSUMPTION_RATE;

        x = p.x;
        y = p.y;
        board.dziks.get(board.points[x][y]).add(this);
    }


    public boolean equals(Object o){
        if(o instanceof Dzik p){
            return this==p;
        }
        return false;
    }
    public int hashCode(){
        return super.hashCode();
    }
}
