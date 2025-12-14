
package com.javaprojectsudokulatest;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UI extends JFrame {
	public final Sudoku sudoku = new Sudoku();
	
	private final int length = 50;
	private final int smallStretch = 0;
	private final int bigStretch = 15;
	private final int gap = 20;
	private final JTextField[][] table = new JTextField[9][9];
	
	public UI() {
		final int side = 9*length + 6*smallStretch + 2*bigStretch + 2*gap;
		setTable();
		
		JButton clear = new JButton("Clear All");
		clear.setBounds(gap, side, side-40, 40);
		this.add(clear);
		
		JButton fill = new JButton("Solve");
		fill.setBounds(gap, side+50, side-40, 40);
		this.add(fill);
		
		this.setSize(new Dimension(side, side+130));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(null);
		
		clear.addActionListener(e -> {
			this.clearTable();
		});
		
		fill.addActionListener(e -> {
			try {
				this.setBoard();
				sudoku.solve();
				this.placeBoard();
				
			} catch (WrongWayException ex) {
				Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
			}
		});
	}
	
	public void test() {
		String row0, row1, row2, row3, row4, row5, row6, row7, row8;
		row0 = "0 0 0 0 0 0 0 0 0";
        row1 = "0 0 0 0 0 0 0 0 0";
        row2 = "0 0 0 0 0 0 0 0 0";
        row3 = "0 0 0 0 0 0 0 0 0";
        row4 = "0 0 0 0 0 0 0 0 0";
        row5 = "0 0 0 0 0 0 0 0 0";
        row6 = "0 0 0 0 0 0 0 0 0";
        row7 = "0 0 0 0 0 0 0 0 0";
        row8 = "0 0 0 0 0 0 0 0 0";

		String[] rows = {row0, row1, row2, row3, row4, row5, row6, row7, row8};

		for (int i = 0; i < 9; i++) {
			sudoku.defineRow(rows[i], i);
		}

		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				Block block = sudoku.getBoard()[i][j];

				if (block.getValue() != 0) 
					table[i][j].setText(String.valueOf(block.getValue()));
				else if (block.getGuess() != 0) 
					table[i][j].setText(String.valueOf(block.getGuess()));
			}
		}
	}
	
	
	private void setBoard() {
		String[] rows = {"","","","","","","","",""};
		
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				String text = table[i][j].getText();
				if (!text.isBlank()) rows[i] += text + " ";
				else rows[i] += "0 ";
			}
		}
		
		for (int i = 0; i < 9; i++) {
            sudoku.defineRow(rows[i], i);
        }
	}
	
	private void placeBoard() {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				Block block = sudoku.getBoard()[i][j];
				 
				if (block.getValue() != 0) 
					table[i][j].setText(String.valueOf(block.getValue()));
				else if (block.getGuess() != 0) 
					table[i][j].setText(String.valueOf(block.getGuess()));
			}
		}
	}
	
	private void setTable() {
		for (JTextField[] row : table) {
			for (JTextField tf : row) {
				tf = null;
			}
		}
		
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				
				JTextField digit = new JTextField();
				
				digit.setFont(new Font("Arial", java.awt.Font.PLAIN, 36));
				digit.setHorizontalAlignment(JTextField.CENTER);
				
				((AbstractDocument) digit.getDocument()).setDocumentFilter(new DocumentFilter() {
					@Override
					public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr)
						throws BadLocationException {
						if (string == null) return;

						String current = fb.getDocument().getText(0, fb.getDocument().getLength());
						String newValue = current.substring(0, offset) + string + current.substring(offset);
						if (string.matches("[1-9]+") && newValue.length() <= 1) {
							super.insertString(fb, offset, string, attr);
						}
					}

					@Override
					public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String string, AttributeSet attrs)
						throws BadLocationException {

						String current = fb.getDocument().getText(0, fb.getDocument().getLength());
						String newValue;
						
						if (string == null) newValue = current.substring(0, offset) + current.substring(offset + length);
						else newValue = current.substring(0, offset) + string + current.substring(offset + length);
						

						if (newValue.isEmpty()) {
							super.replace(fb, offset, length, string, attrs);
							return;
						}

						if (string != null && string.matches("[1-9]+") && newValue.length() <= 1) {
							super.replace(fb, offset, length, string, attrs);
						}
					}

					@Override
					public void remove(DocumentFilter.FilterBypass fb, int offset, int length)
						throws BadLocationException {
						super.remove(fb, offset, length);
					}
				});
				
				table[i][j] = digit;
				
				int[] cord = getCoordinates(j,i);
				digit.setBounds(cord[0], cord[1], length, length);
				this.add(digit);
			}

		}
		
		for (int r = 0; r < 9; r++) {			
			for (int c = 0; c < 9; c++) {
				JTextField digit = table[r][c];
				
				int i = r;
				int j = c;
				
				digit.addKeyListener(new KeyListener() {
					
					@Override
					public void keyTyped(KeyEvent e) {}

					@Override
					public void keyPressed(KeyEvent e) {
						int code = e.getKeyCode();

						switch (code) {
							case KeyEvent.VK_UP -> {
								if (i != 0) table[i-1][j].requestFocusInWindow();
							}
							case KeyEvent.VK_DOWN -> {
								if (i != 8) table[i+1][j].requestFocusInWindow();
							}
							case KeyEvent.VK_LEFT -> {
								if (j != 0) table[i][j-1].requestFocusInWindow();
							}
							case KeyEvent.VK_RIGHT -> {
								if (j != 8) table[i][j+1].requestFocusInWindow();
							}
							default -> {
							}
						}
					}

					@Override
					public void keyReleased(java.awt.event.KeyEvent e) {}
				});
			}
			
		}
	}
	
	private void clearTable() {
		for (int i = 0; i < 9; ++i) {
			for (int j = 0; j < 9; j++) {
				table[i][j].setText("");
			}
		}
	}
	
	private int[] getCoordinates(int i, int j) {
		int[] pos = new int[2];
		
		pos[0] = getPosition(i);
		pos[1] = getPosition(j);
		
		return pos;
	}
	
	private int getPosition(int a) {
		int result = gap;
		int i = a;
		
		result += i * length;
		
		if (0 <= i && i < 3) {
			result += i * smallStretch;
		}
		else if (3 <= i && i < 6) {
			result += (i-1) * smallStretch + bigStretch;
		}
		else if (6 <= i && i < 9) {
			result += (i-2) * smallStretch + (2 * bigStretch);
		}
		
		return result;
	}

}

