package com.gmail.smanis.konstantinos.qttt;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

enum CellState {X1, O2, X3, O4, X5, O6, X7, O8, X9}
enum Player {X, O}

public class State {
	public interface OnProgressListener {
		void onProgress(int current, int max);
	}

	private final int[][] cLines = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, //rows
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, //columns
        {0, 4, 8}, {2, 4, 6}             //diagonals
	};
	private List<CellState> mClassicBoard;
	private List<EnumSet<CellState>> mQuantumBoard;
	private int mTurn, mDepth;
	private Move mLastMove, mInput;
	private Boolean mEntangled;
	private GameResult mResult;

	public State() {
		reset();
	}

	public boolean applyInput(int cellIndex) {
		if (gameOver()) {
			return false;
		}

		CellState cCell = mClassicBoard.get(cellIndex);
		if (cCell != null) {
			return false;
		}

		if (entangled()) {
            if (cellIndex != mLastMove.firstCellIndex() &&
                cellIndex != mLastMove.secondCellIndex()) {
                return false;
            }
			applyMove(new Move(cellIndex, previousMark()));
		} else {
			if (mInput == null) {
				if (openCells().size() == 1) {
					applyMove(new Move(cellIndex, cellIndex, CellState.X9));
				} else {
					mInput = new Move(cellIndex, -1, currentMark());
					mQuantumBoard.get(cellIndex).add(currentMark());
				}
			} else {
				if (cellIndex == mInput.firstCellIndex()) {
					mQuantumBoard.get(cellIndex).remove(currentMark());
				} else {
					mInput.setSecondCellIndex(cellIndex);
					if (mInput.firstCellIndex() > mInput.secondCellIndex()) {
						int temp = mInput.firstCellIndex();
						mInput.setFirstCellIndex(mInput.secondCellIndex());
						mInput.setSecondCellIndex(temp);
					}
					applyMove(mInput);
				}
				mInput = null;
			}
		}
		return true;
	}
	public void applyMove(Move m) {
		if (m == null) {
			return;
		}

		switch (m.type()) {
        case REGULAR:
            if (m.firstCellIndex() != m.secondCellIndex()) {
                mQuantumBoard.get(m.firstCellIndex()).add(m.cellState());
                mQuantumBoard.get(m.secondCellIndex()).add(m.cellState());
            } else {
                mQuantumBoard.get(m.firstCellIndex()).add(CellState.X9);
                mClassicBoard.set(m.firstCellIndex(), CellState.X9);
            }
            ++mTurn;
            break;
        case COLLAPSE:
            List<Integer> collapsedCells = new ArrayList<>(9);
            collapse(m.firstCellIndex(), m.cellState(), collapsedCells);
            m.setCollapsedCells(collapsedCells);
            break;
		}
		++mDepth;
		m.setPreviousMove(mLastMove);
		mLastMove = m;
		mEntangled = null;
		mResult = null;
	}
	public List<Move> availableMoves(boolean reorderMoves) {
		List<Move> ret = new ArrayList<>();
		if (!gameOver()) {
			if (entangled()) {
				ret.add(new Move(mLastMove.firstCellIndex(), mLastMove.cellState()));
				ret.add(new Move(mLastMove.secondCellIndex(), mLastMove.cellState()));
			} else {
				List<Integer> openCells = openCells();
				if (openCells.size() == 1) {
					//Special case: board is full by the last X move;
					//place both marks in the only free cell.
					ret.add(new Move(openCells.get(0), openCells.get(0), CellState.X9));
				} else {
					CellState currentMark = currentMark();
					for (int i = 0; i + 1 < openCells.size(); ++i) {
						for (int j = i + 1; j < openCells.size(); ++j) {
							ret.add(new Move(openCells.get(i), openCells.get(j), currentMark));
						}
					}
					if (reorderMoves) {
						reorderMoves(ret);
					}
				}
			}
		}
		return ret;
	}
	public void botPlay() {
		Scanner s = new Scanner(System.in);
		char mark;
		do {
			System.out.print("Select your mark (X or O): ");
			mark = s.next().charAt(0);
		} while (mark != 'X' && mark != 'O');
		Player humanPlayer = (mark == 'X' ? Player.X : Player.O);

		while (!gameOver()) {
			System.out.println(this);
			if (currentPlayer() == humanPlayer) {
				System.out.println("It's your turn!");

				System.out.println("Available moves:");
				List<Move> moves = availableMoves(false);
				for (int i = 0; i < moves.size(); ++i) {
					System.out.println(String.format("%2d: %s", i + 1, moves.get(i)));
				}
				int input;
				do {
					System.out.print(String.format("Input your choice (1-%d): ", moves.size()));
					input = s.nextInt();
				} while (input < 1 || input > moves.size());
				applyMove(moves.get(input - 1));
			} else {
				System.out.println("It's the computer's turn!");

				applyMove(minimaxMove(null));
			}
		}
		System.out.println(this);
		System.out.println(result());
		s.close();
	}
	private int cellPair(CellState mark, int cellIndex) {
		for (int i = 0; i < mQuantumBoard.size(); ++i) {
			if (i != cellIndex && mQuantumBoard.get(i).contains(mark)) {
				return i;
			}
		}
		return -1;
	}
	public List<CellState> classicBoard() {
		return mClassicBoard;
	}
	private void collapse(int cellIndex, CellState mark, List<Integer> collapsedCells) {
		mClassicBoard.set(cellIndex, mark);
		collapsedCells.add(cellIndex);

		for (CellState cs : mQuantumBoard.get(cellIndex)) {
			if (cs == mark) {
				continue;
			}

			int cellPair = cellPair(cs, cellIndex);
			if (collapsedCells.contains(cellPair)) {
				continue;
			}

			collapse(cellPair, cs, collapsedCells);
		}
	}
	private CellState currentMark() {
		return CellState.values()[mTurn];
	}
	public Player currentPlayer() {
		return ((mTurn & 1) == 0 ? Player.X : Player.O);
	}
	public int currentTurn() {
		return mTurn;
	}
	public boolean entangled() {
		if (mEntangled == null) {
			mEntangled = entangledEvaluation();
		}
		return mEntangled;
	}
	public List<Integer> entangledCells() {
		List<Integer> ret = new ArrayList<>();
		if (!entangled()) {
			return ret;
		}

		Stack<Integer> stack = new Stack<>();
		stack.push(mLastMove.firstCellIndex());
		ret.add(mLastMove.firstCellIndex());
		do {
			int i = stack.pop();
			for (CellState cs : mQuantumBoard.get(i)) {
				int neighbour = cellPair(cs, i);
				if (!ret.contains(neighbour)) {
					stack.push(neighbour);
					ret.add(neighbour);
				}
			}
		} while (!stack.isEmpty());
		return ret;
	}
	private boolean entangledEvaluation() {
		if (mLastMove == null || mLastMove.type() == Move.Type.COLLAPSE ||
            mQuantumBoard.get(mLastMove.firstCellIndex()).size() < 2 ||
            mQuantumBoard.get(mLastMove.secondCellIndex()).size() < 2) {
			return false;
		}

		class Node {
			int m_self, m_parent;
			Node(int self) {
				m_self = self;
				m_parent = -1;
			}
			Node(int self, int parent) {
				m_self = self;
				m_parent = parent;
			}
		}
		Stack<Node> stack = new Stack<>();
		boolean[] visited = new boolean[9];

		stack.push(new Node(mLastMove.firstCellIndex()));
		visited[mLastMove.firstCellIndex()] = true;
		do {
			Node node = stack.pop();
			for (CellState cs : mQuantumBoard.get(node.m_self)) {
				int neighbour = cellPair(cs, node.m_self);
				if (!visited[neighbour]) {
					stack.push(new Node(neighbour, node.m_self));
					visited[neighbour] = true;
				} else if (neighbour != node.m_parent) {
					return true;
				}
			}
		} while (!stack.isEmpty());
		return false;
	}
	public boolean gameOver() {
		return result().gameOver();
	}
    public void generateOpeningMoves(int turn) throws FileNotFoundException {
        System.setOut(new PrintStream(new FileOutputStream(turn + ".txt")));
        switch (turn) {
		case 0:
			printMoves();
			break;
        case 1:
            for (Move m1 : availableMoves(false)) {
                applyMove(m1);
                printMoves();
                undoLastMove();
            }
            break;
        case 2:
            for (Move m1 : availableMoves(false)) {
                applyMove(m1);
                for (Move m2 : availableMoves(false)) {
                    applyMove(m2);
                    printMoves();
                    if (entangled()) {
                        for (Move m2e : availableMoves(false)) {
                            applyMove(m2e);
                            printMoves();
                            undoLastMove();
                        }
                    }
                    undoLastMove();
                }
                undoLastMove();
            }
            break;
        case 3:
            for (Move m1 : availableMoves(false)) {
                applyMove(m1);
                for (Move m2 : availableMoves(false)) {
                    applyMove(m2);
                    if (entangled()) {
                        for (Move m2e : availableMoves(false)) {
                            applyMove(m2e);
                            for (Move m3 : availableMoves(false)) {
                                applyMove(m3);
                                printMoves();
                                undoLastMove();
                            }
                            undoLastMove();
                        }
                    } else {
                        for (Move m3 : availableMoves(false)) {
                            applyMove(m3);
                            printMoves();
                            if (entangled()) {
                                for (Move m3e : availableMoves(false)) {
                                    applyMove(m3e);
                                    printMoves();
                                    undoLastMove();
                                }
                            }
                            undoLastMove();
                        }
                    }
                    undoLastMove();
                }
                undoLastMove();
            }
            break;
        case 4:
            for (Move m1 : availableMoves(false)) {
                applyMove(m1);
                for (Move m2 : availableMoves(false)) {
                    applyMove(m2);
                    if (entangled()) {
                        for (Move m2e : availableMoves(false)) {
                            applyMove(m2e);
                            for (Move m3 : availableMoves(false)) {
                                applyMove(m3);
                                for (Move m4 : availableMoves(false)) {
                                    applyMove(m4);
                                    printMoves();
                                    if (entangled()) {
                                        for (Move m4e : availableMoves(false)) {
                                            applyMove(m4e);
                                            printMoves();
                                            undoLastMove();
                                        }
                                    }
                                    undoLastMove();
                                }
                                undoLastMove();
                            }
                            undoLastMove();
                        }
                    } else {
                        for (Move m3 : availableMoves(false)) {
                            applyMove(m3);
                            if (entangled()) {
                                for (Move m3e : availableMoves(false)) {
                                    applyMove(m3e);
                                    for (Move m4 : availableMoves(false)) {
                                        applyMove(m4);
                                        printMoves();
                                        undoLastMove();
                                    }
                                    undoLastMove();
                                }
                            } else {
                                for (Move m4 : availableMoves(false)) {
                                    applyMove(m4);
                                    printMoves();
                                    if (entangled()) {
                                        for (Move m4e : availableMoves(false)) {
                                            applyMove(m4e);
                                            printMoves();
                                            undoLastMove();
                                        }
                                    }
                                    undoLastMove();
                                }
                            }
                            undoLastMove();
                        }
                    }
                    undoLastMove();
                }
                undoLastMove();
            }
            break;
        default:
            break;
        }
        System.setOut(System.out);
    }
	public boolean hasIncompleteInput() {
		return (mInput != null);
	}
	private boolean isFull() {
		for (CellState cs : mClassicBoard) {
			if (cs == null) {
				return false;
			}
		}
		return true;
	}
	public boolean isUndoAvailable() {
		return (mInput != null || mLastMove != null);
	}
	public Move lastMove() {
        return mLastMove;
    }
	private EnumSet<Player> lineWinner(int[] line) {
		EnumSet<Player> ret = EnumSet.noneOf(Player.class);
		CellState[] cs = {
			mClassicBoard.get(line[0]),
			mClassicBoard.get(line[1]),
			mClassicBoard.get(line[2])
		};
		if (cs[0] != null && cs[1] != null && cs[2] != null &&
			(cs[0].ordinal() & 1) == (cs[1].ordinal() & 1) &&
			(cs[1].ordinal() & 1) == (cs[2].ordinal() & 1)) {
			ret = EnumSet.of((cs[0].ordinal() & 1) == 0 ? Player.X : Player.O);
		}
		return ret;
	}
	public void listPlay() {
		Scanner s = new Scanner(System.in);
		System.out.println(this);
		List<Move> moves = availableMoves(false);
		while (!moves.isEmpty()) {
			System.out.println("Available moves:");
			for (int i = 0; i < moves.size(); ++i) {
				System.out.println(String.format("%2d: %s", i + 1, moves.get(i)));
			}
			int input;
			do {
				System.out.print(String.format("Input your choice (1-%d): ", moves.size()));
				input = s.nextInt();
			} while (input < 1 || input > moves.size());
			applyMove(moves.get(input - 1));

			System.out.println(this);
			moves = availableMoves(false);
		}
		System.out.println(result());
		s.close();
	}
	public List<Move> lookupNextMove(InputStream is) {
		List<Move> ret = new ArrayList<>();
		BufferedReader br = null;
		try {
            Utility moveUtility = null;
			boolean found = false;
			String line, moveHistory = moveHistory();
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				if (found) {
					if (line.length() == 0) {
						break;
					}

					Move m = Move.valueOf(line);
                    if (m != null) {
                        m.setCellState(m.type() == Move.Type.REGULAR ? currentMark() : previousMark());
                        m.setUtility(moveUtility);
                        ret.add(m);
                    }
				} else if (line.startsWith("(")) {
					String[] fields = line.split(":");
					if (fields[0].equals(moveHistory)) {
						found = true;
                        moveUtility = Utility.valueOf(fields[1]);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
	private List<Move> minimaxEvaluation(OnProgressListener l) {
		Player player = currentPlayer();
		List<Move> moves = availableMoves(false);
		for (int i = 0; i < moves.size(); ++i) {
			Move move = moves.get(i);
			applyMove(move);
			if (player == Player.X) {
				move.setUtility(minValue(Integer.MIN_VALUE, Integer.MAX_VALUE, false));
			} else {
				move.setUtility(maxValue(Integer.MIN_VALUE, Integer.MAX_VALUE, false));
			}
			undoLastMove();

//			System.out.println(move);
			if (l != null) {
				l.onProgress(i + 1, moves.size());
			}
		}
		return moves;
	}
	public Move minimaxMove(OnProgressListener l) {
		List<Move> moves = minimaxEvaluation(l);
		if (moves.isEmpty()) {
			return null;
		}

		if (currentPlayer() == Player.X) {
			return Collections.max(moves, new Move.MaxUtilComparator());
		} else {
			return Collections.min(moves, new Move.MinUtilComparator());
		}
	}
	public List<Move> minimaxMoves(OnProgressListener l) {
		List<Move> moves = minimaxEvaluation(l);
		if (moves.isEmpty()) {
			return moves;
		}

		Comparator<Move> cmp;
		if (currentPlayer() == Player.X) {
			cmp = Collections.reverseOrder(new Move.MaxUtilComparator());
		} else {
			cmp = new Move.MinUtilComparator();
		}
		Collections.sort(moves, cmp);

		Move m0 = moves.get(0);
		for (int i = 1; i < moves.size(); ++i) {
			Move m = moves.get(i);
			if (cmp.compare(m0, m) != 0) {
				moves.subList(i, moves.size()).clear();
				break;
			}
		}
		return moves;
	}
	private Utility minValue(int a, int b, boolean substitute) {
		if (!substitute && mLastMove.type() == Move.Type.COLLAPSE) {
			return maxValue(a, b, true);
		}

		if (gameOver()) {
			return utility();
		}

		Utility ret = new Utility(Integer.MAX_VALUE);
		for (Move move : availableMoves(true)) {
			applyMove(move);
			ret = Utility.min(ret, maxValue(a, b, false));
			undoLastMove();
			if (ret.value() <= a) {
				return ret;
			}
			b = Math.min(b, ret.value());
		}
		return ret;
	}
	private Utility maxValue(int a, int b, boolean substitute) {
		if (!substitute && mLastMove.type() == Move.Type.COLLAPSE) {
			return minValue(a, b, true);
		}

		if (gameOver()) {
			return utility();
		}

		Utility ret = new Utility(Integer.MIN_VALUE);
		for (Move move : availableMoves(true)) {
			applyMove(move);
			ret = Utility.max(ret, minValue(a, b, false));
			undoLastMove();
			if (ret.value() >= b) {
				return ret;
			}
			a = Math.max(a, ret.value());
		}
		return ret;
	}
	public String moveHistory() {
        if (mLastMove == null) {
            return "()";
        }

		StringBuilder sb = new StringBuilder();
		for (Move m = mLastMove; m != null; m = m.previousMove()) {
			sb.insert(0, String.format("(%s)", m.toShortString()));
		}
		return sb.toString();
	}
	private List<Integer> openCells() {
		List<Integer> ret = new ArrayList<>(9);
		for (int i = 0; i < mClassicBoard.size(); ++i) {
			if (mClassicBoard.get(i) == null) {
				ret.add(i);
			}
		}
		return ret;
	}
	private CellState previousMark() {
		return CellState.values()[mTurn - 1];
	}
	public List<EnumSet<CellState>> quantumBoard() {
		return mQuantumBoard;
	}
	private void reorderMoves(List<Move> moves) {
		int i = 0;
		for (int j = i + 1; j < moves.size(); ++j) {
			applyMove(moves.get(j));
			if (entangled()) {
				Collections.swap(moves, i, j);
				++i;
			}
			undoLastMove();
		}
		for (int j = i + 1; j < moves.size(); ++j) {
			if (moves.get(j).firstCellIndex() == 4 ||
                moves.get(j).secondCellIndex() == 4) {
				Collections.swap(moves, i, j);
				++i;
			}
		}
		for (int j = i + 1; j < moves.size(); ++j) {
			if (((moves.get(j).firstCellIndex() & 1) == 0) ||
                ((moves.get(j).secondCellIndex() & 1) == 0)) {
				Collections.swap(moves, i, j);
				++i;
			}
		}
	}
	public void reset() {
		mClassicBoard = new ArrayList<>(9);
		mQuantumBoard = new ArrayList<>(9);
		for (int i = 0; i < 9; ++i) {
			mClassicBoard.add(null);
			mQuantumBoard.add(EnumSet.noneOf(CellState.class));
		}
		mTurn = 0;
		mDepth = 0;
		mLastMove = null;
		mInput = null;
		mEntangled = null;
		mResult = null;
	}
	private GameResult resultEvaluation() {
		GameResult ret = new GameResult();
		//It takes at least 5 turns for a win!
		if (mTurn < 5) {
			return ret;
		}

		int xWins = 0, oWins = 0, xWinMove = -1, oWinMove = -1;
		for (int[] line : cLines) {
			EnumSet<Player> lineWinner = lineWinner(line);
			if (!lineWinner.isEmpty()) {
				int[] arr = {
					mClassicBoard.get(line[0]).ordinal(),
					mClassicBoard.get(line[1]).ordinal(),
					mClassicBoard.get(line[2]).ordinal()
				};
				int winMove = Math.max(arr[0], Math.max(arr[1], arr[2])) + 1;

				if (lineWinner.contains(Player.X)) {
					++xWins;
					xWinMove = winMove;
				} else if (lineWinner.contains(Player.O)) {
					++oWins;
					oWinMove = winMove;
				}
			}
		}

		if (xWins == 2) {
            ret.setXResult(GameResult.PlayerResult.DOUBLE_COMPLETE_WIN);
		} else if (xWins == 1 && oWins == 0) {
            ret.setXResult(GameResult.PlayerResult.COMPLETE_WIN);
		} else if (xWins == 1 && oWins == 1) {
            if (xWinMove < oWinMove) {
                ret.setXResult(GameResult.PlayerResult.NARROW_WIN_FIRST);
            } else {
                ret.setXResult(GameResult.PlayerResult.NARROW_WIN_SECOND);
            }
		} else if (xWins == 0 && oWins == 1) {
            ret.setXResult(GameResult.PlayerResult.LOSS);
		} else if (isFull()) {
            ret.setXResult(GameResult.PlayerResult.DRAW);
		}
		return ret;
	}
	public GameResult result() {
		if (mResult == null) {
			mResult = resultEvaluation();
		}
		return mResult;
	}
	private void printMoves() {
		List<Move> moves = minimaxMoves(null);
		System.out.println(String.format("%s:%s", moveHistory(), moves.get(0).utility().toShortString()));
		for (Move m : moves) {
			System.out.println(m.toShortString());
		}
		System.out.println();
	}
	public void undoLastMove() {
		if (mInput != null) {
			mQuantumBoard.get(mInput.firstCellIndex()).remove(mInput.cellState());
			mInput = null;
			return;
		}

		if (mLastMove == null) {
			return;
		}

		switch (mLastMove.type()) {
        case REGULAR:
            if (mLastMove.firstCellIndex() != mLastMove.secondCellIndex()) {
                mQuantumBoard.get(mLastMove.firstCellIndex()).remove(mLastMove.cellState());
                mQuantumBoard.get(mLastMove.secondCellIndex()).remove(mLastMove.cellState());
            } else {
                mQuantumBoard.get(mLastMove.firstCellIndex()).remove(CellState.X9);
                mClassicBoard.set(mLastMove.firstCellIndex(), null);
            }
            --mTurn;
            break;
        case COLLAPSE:
            for (Integer i : mLastMove.collapsedCells()) {
                mClassicBoard.set(i, null);
            }
            break;
		}
		--mDepth;
		mLastMove = mLastMove.previousMove();
		mEntangled = null;
		mResult = null;
	}
	private Utility utility() {
		switch (result().xResult()) {
        case DOUBLE_COMPLETE_WIN:
            return new Utility(3, mDepth);
        case COMPLETE_WIN:
            return new Utility(2, mDepth);
        case NARROW_WIN_FIRST:
            return new Utility(1, mDepth);
        case DRAW:
            return new Utility(0, mDepth);
        case NARROW_WIN_SECOND:
            return new Utility(-1, mDepth);
        case LOSS:
            return new Utility(-2, mDepth);
        case INVALID:
        default:
            return null;
		}
	}
	public List<Integer> winningCells() {
		List<Integer> ret = new ArrayList<>();
		if (!gameOver() || result().xResult() == GameResult.PlayerResult.DRAW) {
			return ret;
		}

		for (int[] line : cLines) {
			if (!lineWinner(line).isEmpty()) {
				for (int cell : line) {
					ret.add(cell);
				}
			}
		}
		return ret;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int iGridRow = 0; iGridRow < 3; ++iGridRow) {
			for (int iCellRow = 0; iCellRow < 3; ++iCellRow) {
				for (int iGridCol = 0; iGridCol < 3; ++iGridCol) {
					CellState cCell = mClassicBoard.get(iGridRow * 3 + iGridCol);
					EnumSet<CellState> qCell = mQuantumBoard.get(iGridRow * 3 + iGridCol);
					for (int iCellCol = 0; iCellCol < 3; ++iCellCol) {
						CellState mark = CellState.values()[iCellRow * 3 + iCellCol];
						sb.append(qCell.contains(mark) ? mark.toString() : "  ");
						sb.append(cCell != null && cCell == mark ? '*' : ' ');
					}
					sb.append(iGridCol < 2 ? "|" : System.getProperty("line.separator"));
				}
			}
			if (iGridRow < 2) {
				sb.append("-----------------------------").append(System.getProperty("line.separator"));
			}
		}
		return sb.toString();
	}
}
