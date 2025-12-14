
package com.javaprojectsudokulatest;

import java.util.concurrent.Callable;

public class SudokuThread implements Callable<Sudoku> {
	private final Sudoku sudoku;
	
	public SudokuThread(Sudoku sudoku) {
		this.sudoku = sudoku;
	}
	
	@Override
	public Sudoku call() throws Exception {
		
		try {
			sudoku.solve();
			
		} catch (WrongWayException ex) {
			// System.out.println(sudoku.getId() + " is in wrong way. Now it is being ended.");
			return null;
		}
		
		if (sudoku.isCompleted()) return sudoku;
		else return null;
	}
	
}
