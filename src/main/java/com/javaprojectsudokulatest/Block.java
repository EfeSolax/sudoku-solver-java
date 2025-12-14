
package com.javaprojectsudokulatest;

import java.util.LinkedHashSet;
import java.util.Set;

public class Block {
	private final Set<Integer> possible = new LinkedHashSet<>();
    private int value = 0;
    private int guess = 0;
    private final int col;
    private final int row;
    private final int sqr;
	
	public Block(int r, int c, int s, int v) {
        this.col = c;
        this.row = r;
        this.sqr = s;
        this.value = v;
        
    }
    
    public Block(int r, int c, int s) {
        this.col = c;
        this.row = r;
        this.sqr = s;
    }
		
	public Block(Block block) {
		
		this.col = block.col;
		this.row = block.row;
		this.sqr = block.sqr;
		this.value = block.value;
		this.guess = block.guess;
		
		for (int i : block.possible) {
			this.possible.add(i);
		}
	}
    
    public void addPossible(int v) {
        if (1 <= v && v <= 9) {
            possible.add(v);
        }
    }
    
    public void removePossible(int v) {
        if (1 <= v && v <= 9) {
            possible.remove(v);
        }
    }
    
    public int getValue() {
        return this.value;
    }
    
    public void setGuess(int guess) {
        this.guess = guess;
    }
	
    public int getGuess() {
        return this.guess;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public int getSqr() {
        return sqr;
    }

    public Set<Integer> getPossible() {
        return possible;
    }
	
	public boolean isEmpty() {
		return (this.guess == 0) && (this.value == 0);
	}
	
	@Override
	public String toString() {
		return "[" + row + "," + col + "]";
	}

}
