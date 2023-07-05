import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.ImageIcon;

import java.io.*;
import java.util.*;

import javax.swing.JFrame;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JLabel;

public class GUI extends JPanel implements ActionListener, ChangeListener {
	private static final long serialVersionUID = 1L;
	private Timer timer;
	private Board board;
	private JButton start;
	private JButton clear;
	private JButton save;
	private JComboBox<Integer> drawType;
	private JSlider pred;
	private JFrame frame;
	private int iterNum = 0;
	private final int maxDelay = 500;
	private final int initDelay = 100;
	private boolean running = false;

	public GUI(JFrame jf) {
		frame = jf;
		timer = new Timer(initDelay, this);
		timer.stop();
	}

	public void initialize(Container container) {
		container.setLayout(new BorderLayout());
		container.setSize(new Dimension(1024, 768));

		JPanel buttonPanel = new JPanel();

		start = new JButton("Start");
		start.setActionCommand("Start");
		start.addActionListener(this);

		clear = new JButton("Calc Field");
		clear.setActionCommand("clear");
		clear.addActionListener(this);

		save = new JButton("save");
		save.setActionCommand("saveMap");
		save.addActionListener(this);
		
		pred = new JSlider();
		pred.setMinimum(0);
		pred.setMaximum(maxDelay);
		pred.addChangeListener(this);
		pred.setValue(maxDelay - timer.getDelay());
		
		drawType = new JComboBox<Integer>(new Integer []{0, 1, 2, 3, 4, 5});
		drawType.addActionListener(this);
		drawType.setActionCommand("drawType");

		BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(new File("resources/legenda_dziki.png"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Image resized = myPicture.getScaledInstance(700, 200, Image.SCALE_SMOOTH);
		JLabel picLabel = new JLabel(new ImageIcon(resized));
		//picLabel.setPreferredSize(new Dimension(500, 100));

		buttonPanel.add(start);
		buttonPanel.add(clear);
		buttonPanel.add(save);
		buttonPanel.add(drawType);
		buttonPanel.add(pred);
		buttonPanel.add(picLabel);


		board = new Board(720, 720 - buttonPanel.getHeight());
		container.add(board, BorderLayout.CENTER);
		container.add(buttonPanel, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(timer)) {
			iterNum++;
			frame.setTitle("Dzik simulator (" + Integer.toString(iterNum) + " iteration)");
			board.iteration();
		} else {
			String command = e.getActionCommand();
			if (command.equals("Start")) {
				if (!running) {
					timer.start();
					start.setText("Pause");
				} else {
					timer.stop();
					start.setText("Start");
				}
				running = !running;
				clear.setEnabled(true);

			} else if (command.equals("clear")) {
				iterNum = 0;
				timer.stop();
				start.setEnabled(true);
				board.clear();
			}
			else if (command.equals("drawType")){
				int newType = (Integer)drawType.getSelectedItem();
				board.editType = newType;
			}
			else if (command.equals("saveMap")){
				board.saveMap();
			}

		}
	}

	public void stateChanged(ChangeEvent e) {
		timer.setDelay(maxDelay - pred.getValue());
	}
}
