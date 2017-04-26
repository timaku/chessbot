package chess.bots;

import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;

public class ClampsBeater<M extends Move<M>, B extends Board<M, B>> extends AbstractSearcher<M, B> {

	private static final int DIVIDE_CUTOFF = 3;
	private static final double PERCENTAGE_SEQUENTIAL = .5;
	private static final ForkJoinPool POOL = new ForkJoinPool();

	public M getBestMove(B board, int myTime, int opTime) {
		Stack<BestMove<M>> best = new Stack<>();
		int ply = this.ply;
		if (myTime < 30000 && ply > 3) {
			ply = 4;
		} else if (myTime < 60000 && ply > 4) {
			ply = 5;
		} else if (myTime < 80000 && ply > 5) {
			ply = 6;
		} //else if (myTime < 120000 && ply > 6) {
			//ply = 6;
	//	}
		System.err.println("Starting search at ply " + ply);
		for (int i = 1; i <= ply; i++) {
			best = POOL.invoke(
					new JamboreeTask<M, B>(-evaluator.infty(), evaluator.infty(), board, evaluator, i, cutoff, best));
			reportNewBestMove(best.peek().move);
		}
		return best.peek().move;

	}

	@SuppressWarnings("serial")
	private static class JamboreeTask<M extends Move<M>, B extends Board<M, B>>
			extends RecursiveTask<Stack<BestMove<M>>> {
		private List<M> moves;
		private int lo, hi;
		private final int DEPTH, CUTOFF;
		private int alpha, beta;
		private B board;
		private final Evaluator<B> EVAL;
		private final M MOVE;
		private Stack<BestMove<M>> best;

		public JamboreeTask(List<M> moves, int lo, int hi, int alpha, int beta, B board, Evaluator<B> evaluator,
				int depth, int cutoff) {
			this.moves = moves;
			this.lo = lo;
			this.hi = hi;
			this.alpha = alpha;
			this.beta = beta;
			this.board = board;
			this.EVAL = evaluator;
			this.DEPTH = depth;
			this.MOVE = null;
			this.CUTOFF = cutoff;
			this.best = new Stack<>();
		}

		public JamboreeTask(M move, int alpha, int beta, B board, Evaluator<B> evaluator, int depth, int cutoff) {
			this.moves = null;
			this.lo = -1;
			this.hi = -1;
			this.alpha = alpha;
			this.beta = beta;
			this.board = board;
			this.EVAL = evaluator;
			this.DEPTH = depth;
			this.MOVE = move;
			this.CUTOFF = cutoff;
			this.best = new Stack<>();
		}

		public JamboreeTask(M move, int alpha, int beta, B board, Evaluator<B> evaluator, int depth, int cutoff,
				Stack<BestMove<M>> tryFirst) {
			this.moves = null;
			this.lo = -1;
			this.hi = -1;
			this.alpha = alpha;
			this.beta = beta;
			this.board = board;
			this.EVAL = evaluator;
			this.DEPTH = depth;
			this.MOVE = move;
			this.CUTOFF = cutoff;
			this.best = tryFirst;
		}

		public JamboreeTask(int alpha, int beta, B board, Evaluator<B> evaluator, int depth, int cutoff,
				Stack<BestMove<M>> tryFirst) {
			this.moves = null;
			this.lo = -1;
			this.hi = -1;
			this.alpha = alpha;
			this.beta = beta;
			this.board = board;
			this.EVAL = evaluator;
			this.DEPTH = depth;
			this.MOVE = null;
			this.CUTOFF = cutoff;
			this.best = tryFirst;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Stack<BestMove<M>> compute() {
			if (moves == null) {
				if (MOVE != null) {
					board = board.copy();
					board.applyMove(MOVE);
				}
				if (DEPTH <= CUTOFF) {
					best.push(AlphaBetaSearcher.alphaBeta(EVAL, board, DEPTH, alpha, beta));
					return best;
				}
				moves = board.generateMoves();
				if (moves.isEmpty()) {
					if (board.inCheck()) {
						best.push(new BestMove<M>(null, -EVAL.mate() - DEPTH));
					} else {
						best.push(new BestMove<M>(null, -EVAL.stalemate()));
					}
					return best;
				}
				if (!best.isEmpty() && best.peek().move != null) {
					M move = best.pop().move;
					best = new JamboreeTask<M, B>(move, -beta, -alpha, board, EVAL, DEPTH - 1, CUTOFF, best).compute();
					int value = -best.peek().value;
					alpha = value;
					best.push(new BestMove<M>(move, value));
				}
				moves.sort(new Comparator<M>() {
					public int compare(M m1, M m2) {
						board.applyMove(m1);
						int i1 = EVAL.eval(board);
						board.undoMove();
						board.applyMove(m2);
						int i2 = EVAL.eval(board);
						board.undoMove();
						return i1 - i2;
					}
				});
				int seqCutoff = (int) (PERCENTAGE_SEQUENTIAL * moves.size());
				for (int i = 0; i < seqCutoff; i++) {
					Stack<BestMove<M>> possibility = new JamboreeTask<M, B>(moves.get(i), -beta, -alpha, board, EVAL,
							DEPTH - 1, CUTOFF).compute();
					int value = -possibility.peek().value;
					possibility.push(new BestMove<>(moves.get(i), value));
					if (value > alpha) {
						alpha = value;
						best = possibility;
					}
					if (alpha >= beta) {
						return best;
					}
				}
				lo = seqCutoff;
				hi = moves.size() - 1;
			}
			if (hi - lo + 1 > DIVIDE_CUTOFF) {
				int mid = lo + (hi - lo) / 2;
				JamboreeTask<M, B> left = new JamboreeTask<>(moves, lo, mid, alpha, beta, board, EVAL, DEPTH, CUTOFF);
				JamboreeTask<M, B> right = new JamboreeTask<>(moves, mid + 1, hi, alpha, beta, board, EVAL, DEPTH,
						CUTOFF);
				left.fork();
				Stack<BestMove<M>> r = right.compute();
				Stack<BestMove<M>> l = left.join();
				if (r.peek().value > l.peek().value) {
					if (best.isEmpty() || r.peek().value > best.peek().value) {
						return r;
					} else {
						return best;
					}
				} else {
					if (best.isEmpty() || l.peek().value > best.peek().value) {
						return l;
					} else {
						return best;
					}
				}
			} else {
				JamboreeTask<M, B>[] arr = new JamboreeTask[hi - lo + 1];
				int offset = 0;
				while (lo + offset <= hi - 1) {
					arr[offset] = new JamboreeTask<M, B>(moves.get(lo + offset), -beta, -alpha, board, EVAL, DEPTH - 1,
							CUTOFF);
					arr[offset].fork();
					offset++;
				}
				arr[offset] = new JamboreeTask<M, B>(moves.get(lo + offset), -beta, -alpha, board, EVAL, DEPTH - 1,
						CUTOFF);
				Stack<BestMove<M>>[] computed = new Stack[hi - lo + 1];
				computed[offset] = arr[offset].compute();
				computed[offset].push(new BestMove<>(moves.get(lo + offset), -computed[offset].peek().value));
				if (best.isEmpty() || computed[offset].peek().value > best.peek().value) {
					best = computed[offset];
				}
				offset--;
				while (offset >= 0) {
					computed[offset] = arr[offset].join();
					computed[offset].push(new BestMove<>(moves.get(lo + offset), computed[offset].peek().value));
					computed[offset].peek().negate();
					if (computed[offset].peek().value > best.peek().value) {
						best = computed[offset];
					}
					offset--;
				}
				return best;
			}
		}
	}
}