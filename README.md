# Sudoku Solver (Java)

This project is a Java based sudoku solver which uses logical solving techniques
and backtracking with multithreading when necessary.

## Features
- Logical solving (single candidates, last missing values)
- Backtracking for complex ones
- Multithreaded guessing using Java ExecutorService
- Object oriented design
- Custom exception handling (WrongWayException)

## Background
I first wrote a Sudoku solver two years ago out of curiosity,
inspired by my family's interest in solving Sudoku.
Recently I revisited the project to refactor the code,
improve readability, reduce duplication, and make the solver
capable of solving all valid Sudoku puzzles.

## Technologies
- Java
- Collections Framework
- Concurrency (ExecutorService, Future)
- Swing

## How to Run
- Create a Sudoku object
- Define rows using `defineRow`
- Call `solve()`

## Notes
I developed independently before taking any
formal university level computer science courses.
No AI (chatgpt, gemini, claude, etc.) were used.
