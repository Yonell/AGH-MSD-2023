import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Dzik {
    private int x;
    private int y;
    private final Board board;
    private static final int ATTRACTIVENESSRANDOMNESS = 1000000;


    public Dzik(int x, int y, Board board) {
        this.x = x;
        this.y = y;
        this.board = board;
    }

    public void move(){
        ArrayList<Point> neighbors;
        neighbors = (ArrayList<Point>) board.points[x][y].neighbors.clone();
        neighbors.add(board.points[x][y]);
        Collections.shuffle(neighbors);
    	Point p = neighbors.stream()
                .filter(p3 -> p3.x>0 && p3.x <= 60 && p3.y>0 && p3.y <= 60)
                .max(Comparator.comparingInt(p2 -> p2.staticField + (int) (Math.random() * ATTRACTIVENESSRANDOMNESS)))
                .get();
    	board.dziks.get(board.points[x][y]).remove(this);
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
