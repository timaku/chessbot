package chess.bots;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;

public class ParallelSearcher<M extends Move<M>, B extends Board<M, B>> extends AbstractSearcher<M, B> {

	private static final int DIVIDE_CUTOFF = 3;
	private static final ForkJoinPool POOL = new ForkJoinPool();
	
	public ParallelSearcher() {
		super(); 
	}
	

	public M getBestMove(B board, int myTime, int opTime) {

		List<M> moves = board.generateMoves();
		BestMove<M> m = POOL.invoke(new ParallelTask<M, B>(moves, 0, moves.size() - 1, board, evaluator, ply, cutoff));
		reportNewBestMove(m.move);
		return m.move;

	}

	@SuppressWarnings("serial")
	private static class ParallelTask<M extends Move<M>, B extends Board<M, B>> extends RecursiveTask<BestMove<M>> {
		private List<M> moves;
		private int lo, hi;
		private final int DEPTH, CUTOFF;
		private B board;
		private final Evaluator<B> EVAL;
		private final M MOVE;

		public ParallelTask(List<M> moves, int lo, int hi, B board, Evaluator<B> evaluator, int depth, int cutoff) {
			this.moves = moves;
			this.lo = lo;
			this.hi = hi;
			this.board = board;
			this.EVAL = evaluator;
			this.DEPTH = depth;
			this.MOVE = null;
			this.CUTOFF = cutoff;
		}
		
		public ParallelTask(M move,  B board, Evaluator<B> evaluator, int depth, int cutoff) {
			this.moves = null;
			this.lo = -1;
			this.hi = -1;
			this.board = board;
			this.EVAL = evaluator;
			this.DEPTH = depth;
			this.MOVE = move;
			this.CUTOFF = cutoff;
		}

		@Override
		@SuppressWarnings("unchecked")
		public BestMove<M> compute() {
			if (MOVE != null) {
				board = board.copy();
				board.applyMove(MOVE);
				moves = board.generateMoves();
				lo = 0;
				hi = moves.size() - 1;
			}
			if (moves.isEmpty()) {
				if (board.inCheck()) {
					return new BestMove<M>(null, -EVAL.mate() - DEPTH);
				} else {
					return new BestMove<M>(null, -EVAL.stalemate());
				}
			}
			if (DEPTH <= CUTOFF) {
				BestMove<M> result = SimpleSearcher.minimax(EVAL, board, DEPTH);
				return result;
			} else if (hi - lo + 1 > DIVIDE_CUTOFF) {
				int mid = (lo + hi) / 2;
				ParallelTask<M, B> left = new ParallelTask<>(moves, lo, mid, board, EVAL, DEPTH, CUTOFF);
				ParallelTask<M, B> right = new ParallelTask<>(moves, mid + 1, hi, board, EVAL, DEPTH, CUTOFF);
				left.fork();
				BestMove<M> r = right.compute();
				BestMove<M> l = left.join();
				return r.value > l.value ? r : l;
			} else {
				BestMove<M> bestMove = new BestMove<M>(null, -EVAL.infty());
				ParallelTask<M, B>[] arr = new ParallelTask[hi - lo + 1];
				int offset = 0;
				while (lo + offset <= hi - 1) {
					arr[offset] = new ParallelTask<M, B>(moves.get(lo + offset), board, EVAL, DEPTH - 1, CUTOFF);
					arr[offset].fork();
					offset++;
				}
				arr[offset] = new ParallelTask<M, B>(moves.get(lo + offset), board, EVAL, DEPTH - 1, CUTOFF);
				BestMove<M>[] computed = new BestMove[hi - lo + 1];
				computed[offset] = arr[offset].compute();
				computed[offset].negate();
				if (computed[offset].value > bestMove.value) {
					bestMove.value = computed[offset].value;
					bestMove.move = moves.get(lo + offset);
				}
				offset--;
				while (offset >= 0) {
					computed[offset] = arr[offset].join();
					computed[offset].negate();
					if (computed[offset].value > bestMove.value) {
						bestMove.value = computed[offset].value;
						bestMove.move = moves.get(lo + offset);
					}
					offset--;
				}
				return bestMove;
			}
		}
	}
}