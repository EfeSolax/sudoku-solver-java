
package com.javaprojectsudokulatest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

enum Type {
	SQR,
	ROW,
	COL
}

class WrongWayException extends Exception {
		
	public WrongWayException(String message) {
		super(message);
	}
}

public class Sudoku {
	public static AtomicInteger lastId = new AtomicInteger(0);
	
    private Block[][] board = new Block[9][9];
	private final int id;
	private final static Object lock = new Object();
	
	public Sudoku() {
		this.id = lastId.incrementAndGet();
		
		synchronized (lock) {
			System.out.println("A sudoku object has been created whose id is: " + id);
		}
	}
	
	public Block[][] getBoard() {
        return board;
    }
	
	private void setBoard(Block[][] board) {
		this.board = board;
	}
	
	public int getId() {
		return this.id;
	}
	
	
	
	/* TABLOYU TANIMLAMA İLE İLGİLİ FONKSİYONLAR */
    	
	public void defineRow(String row, int r) {
        Block[] row_ = new Block[9];
        
        String[] row_str_a = row.split(" ");
        int i = r/3;
        
        List<Integer> rowValues = new ArrayList<>();
        
        for (String k : row_str_a) {
            rowValues.add(Integer.valueOf(k));
        }
        
		int index = 0;
        for (int k = 0; k < rowValues.size(); k++) {
            int j = k/3 + 1;
            int sqr_no = i*3 + j;
			
            if (rowValues.get(k) == 0) row_[index] = new Block(r, k, sqr_no);
            else row_[index] = new Block(r, k, sqr_no, rowValues.get(k));
            
			index++;
        }
		
        board[r] = row_;
    }
    
	public static void printBoard(Sudoku sudoku) {
		synchronized (lock) {
			Block[][] board = sudoku.getBoard();
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {

					int value = board[i][j].getValue();
					boolean is_guessed = board[i][j].getGuess() != 0;

					if (value != 0) {
						if ((j+1) % 3 != 0) System.out.print(value + " ");
						else System.out.print(value);
					}

					else if (!is_guessed && value == 0) {
						if ((j+1) % 3 != 0) System.out.print("  ");
						else System.out.print(" ");
					}

					if (is_guessed) {
						if ((j+1) % 3 != 0) System.out.print(board[i][j].getGuess() + " ");
						else System.out.print(board[i][j].getGuess());
					}

					if ((j+1) % 3 == 0) System.out.print("|");
				}

				System.out.println("");

				if ((i+1) % 3 == 0) System.out.println("------------------");
			}
		}
    }
    
	private Sudoku deepCopy() {
		Sudoku sudoku = new Sudoku();
		
		Block[][] blocks = new Block[9][9];
		
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				blocks[i][j] = new Block(board[i][j]);
			}
		}
		
		sudoku.setBoard(blocks);
		return sudoku;
	}

	
	
	
	/*	HIZLI DEĞER HESAPLAMA FONKSİYONLARI */
	
    private static int convertToSqr(int r, int c) {
        int i = r/3;
        int j = c/3 + 1;
        
        return i*3 + j;
    }

	private void isInWrongWay() throws WrongWayException {
		for (Block[] row : board) {
			for (Block block : row) {
				if (block.isEmpty() && block.getPossible().isEmpty()) {
					throw new WrongWayException("Yanlış Yoldayız.");
				}
			}
		}
	}
	
	private Set<Integer> getMissingNumbers(Set<Integer> set) {
		Set<Integer> numbers = new LinkedHashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9));
		numbers.removeAll(set);
		
		return numbers;
	}
	
	public int countEmptyCells() {
        int counter = 0;
        
        for (Block[] blocks : board) {
            for (Block block : blocks) 
				if (block.isEmpty()) counter++;
        }
        
        return counter;
    }
	
	private boolean checkBlocks1To9(Set<Block> blocks) {
		List<Integer> list = new ArrayList<>();
		List<Integer> referenceList = new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7,8,9));
		
		for (Block block : blocks) {
			list.add(block.getGuess() != 0 ? block.getGuess() : block.getValue());
		}
		
		Collections.sort(list);
		return list.equals(referenceList);
	}
	
	public boolean isCompleted() {
		if (countEmptyCells() != 0) return false;
		
		for (int i = 0; i < 9; i++) {
			if (!checkBlocks1To9(getAllBlocks(Type.SQR,i+1))) return false;
			if (!checkBlocks1To9(getAllBlocks(Type.ROW,i))) return false;
			if (!checkBlocks1To9(getAllBlocks(Type.COL,i))) return false;
		}
		return true;
	}
	
	private int[] findCellHavingTwoValues() {
		for (Block[] row : board) {
			for (Block block : row) {
				if (block.getPossible().size() == 2) {
					int[] position = {block.getRow(), block.getCol()};
					return position;
				}
			}
		}
		int[] position = {-1,-1};
		return position;
	}
	
	/* TABLO İLE İLGİLİ HÜCRELERİ VE DEĞERLERİNİ LİSTELEYEN FONKSİYONLAR */
	
	/*
	Bu metodun amacı bir kümenin tamamını dönmektir. Bu küme bir kare, bir satır
	veya bir sütun olabilir. Kare ise value değeri [1,9] aralığında bir sayı gönderilir
	fakat satır veya sütunda bu aralık [0,8] aralığıdır. Bu value değerindeki ve type
	tipindeki kümülatif yapının tüm blokları alınır ve bir block set'i olarak dönülür.
	*/
	private Set<Block> getAllBlocks(Type type, int value) {
		Set<Block> blocks = new LinkedHashSet<>();
		
		switch (type) {
			case SQR -> {
				for (Block[] row : board) {
					for (Block block : row) {
						if (block.getSqr() == value) blocks.add(block);
					}
				}
			}
			case ROW -> {
				for (int c = 0; c < 9; c++) blocks.add(board[value][c]);
			}
			case COL -> {
				for (int r = 0; r < 9; r++) blocks.add(board[r][value]);
			}
		}
		
		return blocks;
	}

	private Set<Block> getBlocksInRow(int row, boolean valueEqualsZero, boolean guessEqualsZero) {
		Set<Block> blocks = new LinkedHashSet<>();
		for (int c = 0; c < 9; c++) {
			blocks.add(board[row][c]);
		}
		
		return filterBlocks(blocks, valueEqualsZero, guessEqualsZero);
	}
	
	private Set<Block> getBlocksInCol(int col, boolean valueEqualsZero, boolean guessEqualsZero) {
		Set<Block> blocks = new LinkedHashSet<>();
		for (int r = 0; r < 9; r++) {
			blocks.add(board[r][col]);
		}
		
		return filterBlocks(blocks, valueEqualsZero, guessEqualsZero);
	}

	private Set<Block> getBlocksInSqr(int sqr, boolean valueEqualsZero, boolean guessEqualsZero) {
		Set<Block> blocks = new LinkedHashSet<>();
		for (Block[] row : board) {
			for (Block block : row) {
				if (block.getSqr() == sqr) blocks.add(block);
			}
		}
		
		return filterBlocks(blocks, valueEqualsZero, guessEqualsZero);
	}

    private Set<Block> filterBlocks(Set<Block> blocks, boolean valueEqualsZero, boolean guessEqualsZero) {
		if (!valueEqualsZero && !guessEqualsZero) return new LinkedHashSet<>();
		
        Set<Block> filteredBlocks = new LinkedHashSet<>();
        for (Block block : blocks) {
			boolean valueMatch = (block.getValue() == 0) == valueEqualsZero;
			boolean guessMatch = (block.getGuess() == 0) == guessEqualsZero;
			
			if (valueEqualsZero && guessEqualsZero) {
				if (valueMatch && guessMatch) filteredBlocks.add(block);
			}
			else if (valueEqualsZero && !guessEqualsZero) {
				if (valueMatch) filteredBlocks.add(block);
			}
			else if (!valueEqualsZero && guessEqualsZero) {
				if (guessMatch) filteredBlocks.add(block);			
			}
		}
		
        return filteredBlocks;
    }
	
	/*
	Bu metodun amacı istenen tipte ve kümedeki blockların değerlerini dönmektir.
	Value değeri sütun ve satırlarla [0,8], kare ile [1,9] şeklinde belirlenir.
	Yalnız dönülen değerler boş olmayan yani ya tahmin edilmiş ya da verilmiş 
	kutuların değerleridir. Bunun için getBlocksIn* metotları kullanılır. 
	Hem sadece tahmin edilmiş hem de sadece verilmiş olan bloklar alınır ve birleştirilir
	daha sonra bu değerler bir integer kümesi olarak dönülür.
	*/
	private Set<Integer> values(Type type, int value) {
		Set<Integer> values = new LinkedHashSet<>();
    
		Set<Block> allBlocks = getAllBlocks(type, value); 

		for (Block block : allBlocks) {
			if (block.getValue() != 0) {
				values.add(block.getValue());
			}
			if (block.getGuess() != 0) {
				values.add(block.getGuess());
			}
		}

		return values;
	}
    	
	/* BLOKLARIN KALAN SON OLUP OLMADIKLARINI TEST EDEN FONKSİYONLAR */
    
	/*
	Bu metodun amacı verilen tipteki kümede eğer son bir boş kutu kalmışsa o boş 
	kutuya gelmesi gereken değeri dönmektir. Eğer öyle bir değer yoksa yani boş olan
	birden fazla yer varsa -1 dönülür.
	Value değeri sütun ve satırlarla [0,8], kare ile [1,9] şeklinde belirlenir.
	*/
	private int findLastMissingValue(Type type, int value) {
		Set<Integer> values = null;
		
		switch (type) {
			case SQR -> values = values(type,value);
			case ROW -> values = values(type,value);
			case COL -> values = values(type,value);
		}

		
		if (values != null) {
			if (values.size() != 8) {
				return -1;
			}
		}

		Integer val = getMissingNumbers(values).iterator().next();
		return val != null ? val : -1;
    }

	/*
	Bu metodun amacı verilen tipte ve pozisyondaki kümede, verilen block dışında
	hiç possible listesinde value değerini içeren block var mı diye kontrol eder.
	Verilen konumdaki block'lar alınır ve daha sonra verilen blok çıkarılır.
	Geriye kalan bloklar tek tek kontrol edilir ve bir tane bile value'yu possible'ında
	içeren blok bulunursa false döner. Eğer hiç yoksa yani verilen block tekse true döner.
	Value değeri sütun ve satırlarla [0,8], kare ile [1,9] şeklinde belirlenir.
	*/
    private boolean isNakedSingleCandidate(Type type, Block block, int positionValue, int value) {
		if (!block.isEmpty()) {
			return false;
		}
		
		Set<Block> blocks = new LinkedHashSet<>();
		
		switch (type) {
			case SQR -> blocks = getBlocksInSqr(positionValue,true,true);
			case ROW -> blocks = getBlocksInRow(positionValue, true, true);
			case COL -> blocks = getBlocksInCol(positionValue, true, true);
			default -> {
				throw new IllegalArgumentException("Geçersiz girdi: " + type);
			}
		}
		
		blocks.remove(block);
		
		for (Block b : blocks) {
			if (b.getPossible().contains(value)) {
				return false;
			}
		}
		return true;
	}
	
	/*
	Bu metodun amacı verilen tipte ve kümede 8 tane hücre dolu mu diye bakılır.
	Ve eğer öyleyse ve tek bir boş hücre kalmışsa findLastMissingValue ile kalan son
	değer bulunur. Daha sonra o kümeye ait bloklardan boş olanlar alınır (zaten tek boş
	olması beklenir). Daha sonra bu kalan son boş yere o sayı yerleştirilir. (guess)
	*/
	private void solveByLastMissingValue(Type type, int i) {
		if (values(type,i).size() == 8) {
            int last = findLastMissingValue(type,i);
			Set<Block> blocks = new LinkedHashSet<>();
			
			switch (type) {
				case SQR -> blocks = getBlocksInSqr(i, true, true);
				case ROW -> blocks = getBlocksInRow(i, true, true);
				case COL -> blocks = getBlocksInCol(i, true, true);
			}
			                        
            for (Block block: blocks) {
				if (block.isEmpty()) {
					guess(block.getRow(), block.getCol(), block.getSqr(), last,"This is the only number that left in this " + type);
				}
			}
		}
	}
	
	
	
	/* BİR HÜCREYE GELEBİLECEK OLASI SAYILARI BULAN FONKSİYONLAR */
		
	private void determinePossibleNumbers() {
		Set<Integer> col;
		Set<Integer> row;
		Set<Integer> sqr;
		Set<Integer> union;

		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board[r].length; c++) {
				if (!board[r][c].isEmpty()) continue;
				sqr = values(Type.SQR,convertToSqr(r,c));
				col = values(Type.COL,c);
				row = values(Type.ROW,r);

				if (board[r][c].isEmpty()) {
					union = new LinkedHashSet(sqr);
					union.addAll(col);
					union.addAll(row);

					Set<Integer> possible = getMissingNumbers(union);

					for (int i : possible) {
						board[r][c].addPossible(i);  
					}
				}
			}
		}
	}
		
	/* TAHMİN YAPAN FONKSİYONLAR */
	
	private void guess(int r, int c, int s, int guess, String cause) {
        Set<Integer> sqr = values(Type.SQR,s);
        Set<Integer> row = values(Type.ROW,r);
        Set<Integer> col = values(Type.COL,c);
				
		/*System.out.println("""
						   ---------------------------------------------------------------
					 ID: """ + id 
				+ "\nNext move is => [" + r  + "," + c + "] Guess:" + guess
				+ "\nThe Cause: " + cause);*/

		
        for (int r_ : row) if (r_ == guess) throw new IllegalArgumentException("Buraya bu değer konulamaz.");
        for (int c_ : col) if (c_ == guess) throw new IllegalArgumentException("Buraya bu değer konulamaz.");
        for (int s_ : sqr) if (s_ == guess) throw new IllegalArgumentException("Buraya bu değer konulamaz.");
		
		Block block = board[r][c];
        
        if (block.getValue() == 0 && block.getGuess() == 0) {
            
            block.setGuess(guess);
            block.getPossible().clear();
			
			Set<Set<Block>> allBlocks = new LinkedHashSet<>(Arrays.asList(
					getBlocksInSqr(block.getSqr(), true, true),
					getBlocksInRow(block.getRow(), true, true),
					getBlocksInCol(block.getCol(), true, true)
			));
			
			for (Set<Block> blocks : allBlocks) {
				for (Block block_ : blocks) {
					block_.removePossible(guess);
				}
			}
        }
		
    }
	
	private void solveByLogic() {
		
		for (int i = 0; i < 9; i++) {
			solveByLastMissingValue(Type.SQR,i+1);
			solveByLastMissingValue(Type.ROW,i);
			solveByLastMissingValue(Type.COL,i);
		}
        
        for (Block[] blocks : board) {
            for (Block block : blocks) {
                if (block.isEmpty()) {
					
					if (block.getPossible().size() == 1) {
                        int val = block.getPossible().iterator().next();
						
                        guess(block.getRow(),block.getCol(),block.getSqr(),val,"There is just one possible number for this cell.");
						continue;
					}
                    
                    Set<Integer> pos = block.getPossible();

                    if (!pos.isEmpty()) {
                        for (int i : pos) {
                            if (isNakedSingleCandidate(Type.SQR, block, block.getSqr(), i)) {
								if (block.getRow() == 4 && block.getCol() == 5) {									
									Set<Integer> currentSqrValues = values(Type.SQR, block.getSqr());
									Set<Integer> currentRowValues = values(Type.ROW, block.getRow());
									Set<Integer> currentColValues = values(Type.COL, block.getCol());

									if (currentSqrValues.contains(i) || currentRowValues.contains(i) || currentColValues.contains(i)) {
										continue;
									}

								}
								guess(block.getRow(),block.getCol(),block.getSqr(), i,"This is the only cell in this sqr which includes this possible number in its possible set.");   
                                break;
                            }

                            if (isNakedSingleCandidate(Type.ROW, block,block.getRow(), i)) {
								guess(block.getRow(),block.getCol(),block.getSqr(), i,"This is the only cell in this row which includes this possible number in its possible set.");
								break;
                            }

                            if (isNakedSingleCandidate(Type.COL, block, block.getCol(), i)) {
                                guess(block.getRow(),block.getCol(),block.getSqr(), i,"This is the only cell in this col which includes this possible number in its possible set.");   
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
	
	/* SUDOKU'YU ÇÖZEN VE THREAD'LERE AYIRAN METOTLAR */
	
	public void solve() throws WrongWayException {
		if (this.getBoard()[0][0] == null) 
			throw new IllegalStateException("The board has not been created yet.");
		        
		int update;
		int exit = 0;
		determinePossibleNumbers();

        do {
			update = countEmptyCells();
            solveByLogic();
			
			if (update - countEmptyCells() == 0) {

				try {
					
					int[] position = findCellHavingTwoValues();
					if (position[0] == -1) {
						System.out.println("No cell with 2 values found");
						break;
					}
					
					Block block = board[position[0]][position[1]];
					Iterator<Integer> iter = block.getPossible().iterator();
					int value1 = iter.next();
					int value2 = iter.next();
					
					Sudoku solution = null;
					
					Sudoku copy1 = deepCopy();
					Sudoku copy2 = deepCopy();
					
					copy1.guess(position[0], position[1], convertToSqr(position[0],position[1]), value1, "Thread" + copy1.getId() + " is trying.");
					copy2.guess(position[0], position[1], convertToSqr(position[0],position[1]), value2, "Thread" + copy2.getId() + " is trying.");
					
					ExecutorService executor = Executors.newFixedThreadPool(5);
					
					Future<Sudoku> result1 = executor.submit(new SudokuThread(copy1));
					Future<Sudoku> result2 = executor.submit(new SudokuThread(copy2));
					
					executor.shutdown();
					executor.awaitTermination(1, TimeUnit.MINUTES);
					
					
					Sudoku sudoku1 = null;
					Sudoku sudoku2 = null;
					
					try {
						sudoku1 = result1.get();
						sudoku2 = result2.get();
						
					} catch (InterruptedException ex) {
						System.out.println("Interrupted exception occured.");
						
					} catch (ExecutionException ex) {
						Logger.getLogger(Sudoku.class.getName()).log(Level.SEVERE, null, ex);
					}
					
					if (sudoku1 != null) solution = sudoku1;
					if (sudoku2 != null) solution = sudoku2;
					
					if (solution != null) {
						this.setBoard(solution.getBoard());
						break;
					}
					else break;
					
				} catch (InterruptedException ex) {
					Logger.getLogger(Sudoku.class.getName()).log(Level.SEVERE, null, ex);
					
				}
			
			}
			
			isInWrongWay();
			if (isCompleted()) break;
			if (exit >= 100) break;
			
			exit++;
        } while (true);
		
		
		if (isCompleted()) {
			System.out.println("Sudoku is completed successfully.");
			printBoard(this);
		}
		
	}
	
} 
