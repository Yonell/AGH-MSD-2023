import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

public class Board extends JComponent implements MouseInputListener, ComponentListener {
	private static final long serialVersionUID = 1L;
	private static final int HUNTER_SENSE_RADIUS = 5;
	private static final int STATS_SAVE_PERIOD = 20;
	public Point[][] points;
	Random rand = new Random();
	private int size = 10;
	public int editType = 0;
	private static int ctr = 0;
	public final Map<Point, Set<Dzik>> dziks = new HashMap<Point, Set<Dzik>>();
	private int averageAttractiveness = 0;
	private List<IterationStatistics> stats = new ArrayList<IterationStatistics>();

	public static final int MAX_SIZE = 60;
	
	private static final int SFMAX = 10000000;
	private static final int MIASTO_UNATTRACTIVENESS = 10000;
	private static final int BAJORA_ATTRACTIVENESS = 100000;
	private static final int LAS_ATTRACTIVENESS = 1000;
	private static final int OTHER_DZIKS_UNATTRACTIVENESS = 20000;
	public static final int DZIK_SENSE_RADIUS = 5;

	private static final int GARBAGE_COLLECTION_FREQUENCY = 168;	
	//how often is garbage collected (in iterations)

	private static final int GARBAGE_COLLECTION_LENGTH = 24;	
	//how long is garbage collected (in iterations); must be lower than GARBAGECOLLECTIONFREQUENCY

	private static final float HUNTER_KILL_PROPABILITY = 0.1f;

	public Board(int length, int height) {
		addMouseListener(this);
		addComponentListener(this);
		addMouseMotionListener(this);
		setBackground(Color.WHITE);
		setOpaque(true);
		initialize(length, height);
	}

	public void iteration() {

		stats.add(new IterationStatistics(ctr, countDziks(), countDziksInCities(), countDziksInTheWild(), countFood()));
		if(ctr%STATS_SAVE_PERIOD == 0){
			saveStats();
		}

		if(ctr % GARBAGE_COLLECTION_FREQUENCY == 0){
			for (Point[] row : points) {
				for (Point point : row) {
					if (point.type == 3)
						point.triggerGarbageCollection();
				}
			}
		}
		if(ctr % GARBAGE_COLLECTION_FREQUENCY == GARBAGE_COLLECTION_LENGTH){
			for (Point[] row : points) {
				for (Point point : row) {
					if (point.type == 3)
						point.eatAllFood();
				}
			}
		}

		theHunterKills();

		calcAverageAttractiveness();
		calculateStaticField();
		List<Dzik> dzikList = new ArrayList<Dzik>();
		for(Set<Dzik> dzikSet : dziks.values())
			dzikList.addAll(dzikSet);
		for(Dzik dzik : dzikList)
					dzik.move();
		for(Dzik dzik : dzikList)
					dzik.eat();

		for (int x = 0; x < points.length; ++x) {
			for (int y = 0; y < points[x].length; ++y) {
				points[x][y].growFood();
			}
		}

		this.repaint();
		ctr++;
	}

	private int countDziks() {
		int sum = 0;
		for(Set<Dzik> dzikSet : dziks.values())
			for(Dzik dzik : dzikSet)
				sum += dzik.getDziksHere();
		return sum;
	}

	private void saveStats() {
		try {
			File file = new File("out/stats.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
			BufferedWriter bw = new BufferedWriter(fw);
			for(IterationStatistics stat : stats){
				bw.write(stat.toString());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int countFood() {
		int sum = 0;
		for(int x = 0; x < points.length; ++x){
			for(int y = 0; y < points[x].length; ++y){
				sum += points[x][y].getCurrentFood();
			}
		}
		return sum;
	}

	private int countDziksInTheWild() {
		int sum = 0;
		for(int x = 0; x < points.length; ++x){
			for(int y = 0; y < points[x].length; ++y){
				if(points[x][y].type == 1 || points[x][y].type == 2)
					for(Dzik dzik : this.dziks.get(points[x][y]))
						sum += dzik.getDziksHere();
			}
		}
		return sum;
	}

	private int countDziksInCities() {
		int sum = 0;
		for(int x = 0; x < points.length; ++x){
			for(int y = 0; y < points[x].length; ++y){
				if(points[x][y].type == 3)
					for(Dzik dzik : this.dziks.get(points[x][y]))
						sum += dzik.getDziksHere();
			}
		}
		return sum;
	}

	public int allDziksHere(int x, int y){
		int dzik_sum = 0;
		for (Dzik dzik:dziks.get(points[x][y])){
			dzik_sum += dzik.getDziksHere();
		}
		return dzik_sum;
	}

	public void clear() {
		for (int x = 0; x < points.length; ++x) {
			for (int y = 0; y < points[x].length; ++y) {
				dziks.remove(points[x][y]);
			}
		}
		calculateStaticField();
		this.repaint();
	}

	private void initialize(int length, int height) {
		points = new Point[length][height];

		for (int x = 0; x < points.length; ++x)
			for (int y = 0; y < points[x].length; ++y)
				points[x][y] = new Point(x,y);

		for (int x = 1; x < points.length-1; ++x) {
			for (int y = 1; y < points[x].length-1; ++y) {
				points[x][y].addNeighbor(points[x-1][y-1]);
				points[x][y].addNeighbor(points[x-1][y]);
				points[x][y].addNeighbor(points[x-1][y+1]);
				points[x][y].addNeighbor(points[x][y-1]);
				points[x][y].addNeighbor(points[x][y+1]);
				points[x][y].addNeighbor(points[x+1][y-1]);
				points[x][y].addNeighbor(points[x+1][y]);
				points[x][y].addNeighbor(points[x+1][y+1]);
			}
		}
		File file = new File("resources/map.txt");
		if(file.exists()){
			try{
				java.util.Scanner sc = new java.util.Scanner(file);
				int height2 = sc.nextInt();
				int width = sc.nextInt();
				for (int y = 1; y <= height2; ++y) {
					for (int x = 1; x <= width; ++x) {
						int type = sc.nextInt();
						points[x][y].type = type;
					}
				}
				sc.close();
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		for (int x = 0; x < points.length; ++x) {
			for (int y = 0; y < points[x].length; ++y) {
				dziks.put(points[x][y], new HashSet<Dzik>());
			}
		}
		this.repaint();
	}
	private void calcAverageAttractiveness(){
		int sum = 0;
		for (int i = 1; i < points.length-1; ++i) {
			for (int j = 1; j < points[i].length-1; ++j) {
				sum -= points[i][j].type == 3 ? MIASTO_UNATTRACTIVENESS : 0;
				sum += points[i][j].type == 2 ? BAJORA_ATTRACTIVENESS : 0;
				sum += points[i][j].type == 1 ? LAS_ATTRACTIVENESS : 0;
				sum -= allDziksHere(i,j) * OTHER_DZIKS_UNATTRACTIVENESS;
			}
		}
		averageAttractiveness = sum/ MAX_SIZE / MAX_SIZE;
	}

	private int calculateValueToAdd(int x, int y, int dist){
		int sum = 0;
		sum -= points[x][y].type == 3 ? MIASTO_UNATTRACTIVENESS / dist : 0;
		sum += points[x][y].type == 2 ? BAJORA_ATTRACTIVENESS / dist : 0;
		sum += points[x][y].type == 1 ? LAS_ATTRACTIVENESS / dist : 0;
		sum -= allDziksHere(x,y) * OTHER_DZIKS_UNATTRACTIVENESS / dist;
		return sum;
	}
	private int calcPointsStaticField(int x, int y){
		int sum = 0;
		for(int i = x- DZIK_SENSE_RADIUS; i <= x+ DZIK_SENSE_RADIUS; ++i){
			for(int j = y- DZIK_SENSE_RADIUS; j <= y+ DZIK_SENSE_RADIUS; ++j){
				int dist = (Math.abs(x - i) + Math.abs(y - j) + 1);
				if(i > 0 && i <= MAX_SIZE && j > 0 && j <= MAX_SIZE){
					sum += calculateValueToAdd(i,j,dist);
				} else {
					if(i <= 0){
						if (j <= 0) {
							sum += calculateValueToAdd(1,1,dist);
						} else if (j > MAX_SIZE) {
							sum += calculateValueToAdd(1,MAX_SIZE,dist);
						} else {
							sum += calculateValueToAdd(1,j,dist);
						}
					} else if (i > MAX_SIZE) {
						if (j <= 0) {
							sum += calculateValueToAdd(MAX_SIZE,1,dist);
						} else if (j > MAX_SIZE) {
							sum += calculateValueToAdd(MAX_SIZE,MAX_SIZE,dist);
						} else {
							sum += calculateValueToAdd(MAX_SIZE,j,dist);
						}
					} else if (j <= 0) {
						sum += calculateValueToAdd(i,1,dist);
					} else {
						sum += calculateValueToAdd(i,MAX_SIZE,dist);
					}
				}
			}
		}

		return sum;
	}
	
	public void calculateStaticField(){
		for (int x = 1; x <= MAX_SIZE; ++x)
			for (int y = 1; y <= MAX_SIZE; ++y)
				if(dziks.get(points[x][y]).size() > 0){
					if(x!=1){
						points[x-1][y].setStaticField(calcPointsStaticField(x-1, y));
						if(y!=1){
							points[x-1][y-1].setStaticField(calcPointsStaticField(x-1, y-1));
						}
						if(y!= MAX_SIZE){
							points[x-1][y+1].setStaticField(calcPointsStaticField(x-1, y+1));
						}
					}
					if(x!= MAX_SIZE){
						points[x+1][y].setStaticField(calcPointsStaticField(x+1, y));
						if(y!=1){
							points[x+1][y-1].setStaticField(calcPointsStaticField(x+1, y-1));
						}
						if(y!= MAX_SIZE){
							points[x+1][y+1].setStaticField(calcPointsStaticField(x+1, y+1));
						}
					}
					if(y!=1){
						points[x][y-1].setStaticField(calcPointsStaticField(x, y-1));
					}
					if(y!= MAX_SIZE){
						points[x][y+1].setStaticField(calcPointsStaticField(x, y+1));
					}
					points[x][y].setStaticField(calcPointsStaticField(x, y));
				}
	}

	void theHunterKills(){
		for (int x = 1; x <= MAX_SIZE; ++x)
			for (int y = 1; y <= MAX_SIZE; ++y)
				if(points[x][y].type == 4){
					for(int i = x- HUNTER_SENSE_RADIUS; i <= x+ HUNTER_SENSE_RADIUS; ++i){
						for(int j = y- HUNTER_SENSE_RADIUS; j <= y+ HUNTER_SENSE_RADIUS; ++j){
							if(i > 0 && i <= MAX_SIZE && j > 0 && j <= MAX_SIZE){
								if(dziks.get(points[i][j]).size() > 0 && rand.nextFloat() < HUNTER_KILL_PROPABILITY) {
									((Dzik) (dziks.get(points[i][j]).toArray()[0])).kill_one();
									points[i][j].setCurrentFood(0);
								}
							}
						}
					}
				}
	}

	protected void paintComponent(Graphics g) {
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
		g.setColor(Color.GRAY);
		drawNetting(g, size);
	}

	private void drawNetting(Graphics g, int gridSpace) {
		Insets insets = getInsets();
		int firstX = insets.left;
		int firstY = insets.top;
		int lastX = this.getWidth() - insets.right;
		int lastY = this.getHeight() - insets.bottom;

		int x = firstX;
		while (x < lastX) {
			g.drawLine(x, firstY, x, lastY);
			x += gridSpace;
		}

		int y = firstY;
		while (y < lastY) {
			g.drawLine(firstX, y, lastX, y);
			y += gridSpace;
		}

		for (x = 1; x < points.length-1; ++x) {
			for (y = 1; y < points[x].length-1; ++y) {
				if(points[x][y].type==0){
					g.setColor(new Color(1.0f, 1.0f, 1.0f));
				}
				else if (points[x][y].type==1){
					float colorWeight = points[x][y].getCurrentFood() / Point.MAX_FOOD_CAP;
					colorWeight *= 0.4;
					g.setColor(new Color(0.0f, 1.0f - colorWeight, 0.0f, 1.0f));
				}
				else if (points[x][y].type==2){
					g.setColor(new Color(0.0f, 0.0f, 1.0f, 1.0f));
				}
				else if (points[x][y].type==3){
					float colorWeight = points[x][y].getCurrentFood() / Point.MAX_GARBAGE_FOOD;
					colorWeight *= 0.3;
					g.setColor(new Color(0.5f - colorWeight, 0.5f - colorWeight, 0.5f - colorWeight, 0.7f));
				}
				else if (points[x][y].type==4){
					g.setColor(new Color(0.5f,0.3f,0.3f,1.0f));
				}
				if(dziks.get(points[x][y]).size()>0){
					float color = (float) (allDziksHere(x,y) * 0.1 + 0.4);

					if (color > 1) {color = 1;}

					g.setColor(new Color(1.0f, 0.5f, 0.2f, color));
				}
				g.fillRect((x * size) + 1, (y * size) + 1, (size - 1), (size - 1));
			}
		}

	}

	public void mouseClicked(MouseEvent e) {
		int x = e.getX() / size;
		int y = e.getY() / size;
		if ((x <= MAX_SIZE) && (x > 0) && (y <= MAX_SIZE) && (y > 0)) {
			if(editType==5){
				//dziks.get(points[x][y]).add(new Dzik(x, y, this));
				//let's make it a bit more random:
				dziks.get(points[x][y]).add(new Dzik(x, y, this,((int) (Math.random() * 10)) % 6 + 1));
			}
			else{
				points[x][y].type = editType;
			}
			this.repaint();
		}
	}

	public void componentResized(ComponentEvent e) {
		//TODO: Delete these
		//int dlugosc = (this.getWidth() / size) + 1;
		//int wysokosc = (this.getHeight() / size) + 1;
		//initialize(dlugosc, wysokosc);
	}

	public void mouseDragged(MouseEvent e) {
		int x = e.getX() / size;
		int y = e.getY() / size;
		if ((x <= MAX_SIZE) && (x > 0) && (y <= MAX_SIZE) && (y > 0)) {
			if(editType==5){
				//dziks.get(points[x][y]).add(new Dzik(x, y, this));
				dziks.get(points[x][y]).add(new Dzik(x, y, this,((int) (Math.random() * 10)) % 6 + 1));
			}
			else{
				points[x][y].type = editType;
			}
			this.repaint();
		}
	}

	public void saveMap(){
		try {
			File file = new File("resources/map.txt");
			if(!file.exists()){
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(MAX_SIZE + " " + MAX_SIZE + "\n");
			for (int y = 1; y <= MAX_SIZE; ++y) {
				for (int x = 1; x <= MAX_SIZE; ++x) {
					bw.write(points[x][y].type + " ");
				}
				bw.write("\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}
	
}
