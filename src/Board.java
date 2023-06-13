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
	public Point[][] points;
	private int size = 10;
	public int editType = 0;
	public final Map<Point, Set<Dzik>> dziks = new HashMap<Point, Set<Dzik>>();
	private int averageAttractiveness = 0;
	private static final int MAXSIZE = 60;
	
	private static final int SFMAX = 10000000;
	private static final int CITYUNATTRACTIVENESS = 10000;
	private static final int BAJORAATTRACTIVENESS = 100000;
	private static final int LASATTRACTIVENESS = 1000;
	private static final int INNEDZIKIUNATTRACTIVENESS = 20000;
	private static final int DZIKWECHRADIUS = 5;

	public Board(int length, int height) {
		addMouseListener(this);
		addComponentListener(this);
		addMouseMotionListener(this);
		setBackground(Color.WHITE);
		setOpaque(true);
		initialize(length, height);
	}

	public void iteration() {
		calcAverageAttractiveness();
		calculateStaticField();
		List<Dzik> dzikList = new ArrayList<Dzik>();
		for(Set<Dzik> dzikSet : dziks.values())
			dzikList.addAll(dzikSet);
		for(Dzik dzik : dzikList)
					dzik.move();
		
		this.repaint();
	}

	public int allDziksHere(int x, int y){
		int dzik_sum = 0;
		for (Dzik dzik:dziks.get(points[x][y])){
			dzik_sum += dzik.getDziks_here();
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
				sum -= points[i][j].type == 3 ? CITYUNATTRACTIVENESS : 0;
				sum += points[i][j].type == 2 ? BAJORAATTRACTIVENESS : 0;
				sum += points[i][j].type == 1 ? LASATTRACTIVENESS : 0;
				sum -= allDziksHere(i,j) * INNEDZIKIUNATTRACTIVENESS;
			}
		}
		averageAttractiveness = sum/MAXSIZE/MAXSIZE;
	}
	private int calcPointsStaticField(int x, int y){
		//temporary fix
		if(points[x][y].type == 3)
			return -SFMAX;
		int sum = 0;
		for(int i = x-DZIKWECHRADIUS; i <= x+DZIKWECHRADIUS; ++i){
			for(int j = y-DZIKWECHRADIUS; j <= y+DZIKWECHRADIUS; ++j){
				int dist = (Math.abs(x - i) + Math.abs(y - j) + 1);
				if(i > 0 && i <= MAXSIZE && j > 0 && j <= MAXSIZE){
					sum -= points[i][j].type == 3 ? CITYUNATTRACTIVENESS / dist : 0;
					sum += points[i][j].type == 2 ? BAJORAATTRACTIVENESS / dist : 0;
					sum += points[i][j].type == 1 ? LASATTRACTIVENESS / dist : 0;
					sum -= allDziksHere(i,j) * INNEDZIKIUNATTRACTIVENESS / dist;
				} else {
					sum += averageAttractiveness / dist;
				}
			}
		}

		return sum;
	}
	
	public void calculateStaticField(){
		for (int x = 1; x <= MAXSIZE; ++x)
			for (int y = 1; y <= MAXSIZE; ++y)
				if(dziks.get(points[x][y]).size() > 0){
					if(x!=1){
						points[x-1][y].setStaticField(calcPointsStaticField(x-1, y));
						if(y!=1){
							points[x-1][y-1].setStaticField(calcPointsStaticField(x-1, y-1));
						}
						if(y!=MAXSIZE){
							points[x-1][y+1].setStaticField(calcPointsStaticField(x-1, y+1));
						}
					}
					if(x!=MAXSIZE){
						points[x+1][y].setStaticField(calcPointsStaticField(x+1, y));
						if(y!=1){
							points[x+1][y-1].setStaticField(calcPointsStaticField(x+1, y-1));
						}
						if(y!=MAXSIZE){
							points[x+1][y+1].setStaticField(calcPointsStaticField(x+1, y+1));
						}
					}
					if(y!=1){
						points[x][y-1].setStaticField(calcPointsStaticField(x, y-1));
					}
					if(y!=MAXSIZE){
						points[x][y+1].setStaticField(calcPointsStaticField(x, y+1));
					}
					points[x][y].setStaticField(calcPointsStaticField(x, y));
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
					g.setColor(new Color(0.0f, 1.0f, 0.0f, 1.0f));
				}
				else if (points[x][y].type==2){
					g.setColor(new Color(0.0f, 0.0f, 1.0f, 1.0f));
				}
				else if (points[x][y].type==3){
					g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.7f));
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
		if ((x <= MAXSIZE) && (x > 0) && (y <= MAXSIZE) && (y > 0)) {
			if(editType==4){
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
		int dlugosc = (this.getWidth() / size) + 1;
		int wysokosc = (this.getHeight() / size) + 1;
		//initialize(dlugosc, wysokosc);
	}

	public void mouseDragged(MouseEvent e) {
		int x = e.getX() / size;
		int y = e.getY() / size;
		if ((x <= MAXSIZE) && (x > 0) && (y <= MAXSIZE) && (y > 0)) {
			if(editType==4){
				//dziks.get(points[x][y]).add(new Dzik(x, y, this));
				dziks.get(points[x][y]).add(new Dzik(x, y, this,((int) (Math.random() * 10)) % 6 + 1));
			}
			else{
				points[x][y].type = editType;
			}
			this.repaint();
		}
	}

	public void save_map(){
		try {
			File file = new File("resources/map.txt");
			if(!file.exists()){
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(MAXSIZE + " " + MAXSIZE + "\n");
			for (int y = 1; y <= MAXSIZE; ++y) {
				for (int x = 1; x <= MAXSIZE; ++x) {
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
