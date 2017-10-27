package chess.bots;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;

public class JamboreeSearcher<M extends Move<M>, B extends Board<M, B>> extends AbstractSearcher<M, B> {

	private static final int DIVIDE_CUTOFF = 3;
	private static final double PERCENTAGE_SEQUENTIAL = .5;
	private static ForkJoinPool POOL;
	
	public JamboreeSearcher() {
		super();
		POOL = new ForkJoinPool();
	}

	public JamboreeSearcher(int cores) {
		super();
		POOL = new ForkJoinPool(cores);
	}

	public M getBestMove(B board, int myTime, int opTime) {
		BestMove<M> m = POOL.invoke(new JamboreeTask<M, B>(-evaluator.infty(), evaluator.infty(), board, evaluator, ply, cutoff));
		reportNewBestMove(m.move);
		return m.move;

	}

	@SuppressWarnings("serial")
	private static class JamboreeTask<M extends Move<M>, B extends Board<M, B>> extends RecursiveTask<BestMove<M>> {
		private List<M> moves;
		private int lo, hi;
		private final int DEPTH, CUTOFF;
		private int alpha, beta;
		private B board;
		private final Evaluator<B> EVAL;
		private final M MOVE;

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
		}

		public JamboreeTask(int alpha, int beta, B board, Evaluator<B> evaluator, int depth, int cutoff) {
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
		}

		@Override
		@SuppressWarnings("unchecked")
		public BestMove<M> compute() {
			
			BestMove<M> best = new BestMove<M>(null, alpha);
			if (moves == null) {
				if (MOVE != null) {
					board = board.copy();
					board.applyMove(MOVE);
				}
				if (DEPTH <= CUTOFF) {
					BestMove<M> m = AlphaBetaSearcher.alphaBeta(EVAL, board, DEPTH, alpha, beta);
					return m;
				}
				moves = board.generateMoves();
				if(moves.isEmpty()) { 
		    		if(board.inCheck()) {
		    			return new BestMove<M>(null, -EVAL.mate() - DEPTH);
		    		} else {
		    			return new BestMove<M>(null, -EVAL.stalemate());
		    		}
		    	}
				int seqCutoff = (int) (PERCENTAGE_SEQUENTIAL * moves.size());
				for (int i = 0; i < seqCutoff; i++) {
					int value = -(new JamboreeTask<M, B>(moves.get(i), -beta, -alpha , board, EVAL, DEPTH - 1, CUTOFF).compute().value);
					if (value > alpha) {
						alpha = value;
						best.value = alpha;
						best.move = moves.get(i);
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
				JamboreeTask<M, B> right = new JamboreeTask<>(moves, mid + 1, hi, alpha, beta, board, EVAL, DEPTH, CUTOFF);
				left.fork();
				BestMove<M> r = right.compute();
				BestMove<M> l = left.join();
				if (r.value > l.value) {
					if (r.value > best.value) {
						return r;
					} else {
						return best;
					}
				} else {
					if (l.value > best.value) {
						return l;
					} else {
						return best;
					}
				}
			} else {
				JamboreeTask<M, B>[] arr = new JamboreeTask[hi - lo + 1];
				int offset = 0;
				while (lo + offset <= hi - 1) {
					arr[offset] = new JamboreeTask<M, B>(moves.get(lo + offset), -beta, -alpha, board, EVAL, DEPTH - 1, CUTOFF);
					arr[offset].fork();
					offset++;
				}
				arr[offset] = new JamboreeTask<M, B>(moves.get(lo + offset), -beta, -alpha, board, EVAL, DEPTH - 1, CUTOFF);
				BestMove<M>[] computed = new BestMove[hi - lo + 1];
				computed[offset] = arr[offset].compute();
				computed[offset].negate();
				if (computed[offset].value > best.value) {
					best.value = computed[offset].value;
					best.move = moves.get(lo + offset);
				}
				offset--;
				while (offset >= 0) {
					computed[offset] = arr[offset].join();
					computed[offset].negate();
					if (computed[offset].value > best.value) {
						best.value = computed[offset].value;
						best.move = moves.get(lo + offset);
					}
					offset--;
				}
				return best;
			}
		}
	}
}