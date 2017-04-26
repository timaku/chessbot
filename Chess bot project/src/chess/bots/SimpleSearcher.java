package chess.bots;

import java.util.List;

import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;

/**
 * This class should implement the minimax algorithm as described in the
 * assignment handouts.
 */
public class SimpleSearcher<M extends Move<M>, B extends Board<M, B>> extends
        AbstractSearcher<M, B> {
	
	public SimpleSearcher() {
		super();
	}
	
    public M getBestMove(B board, int myTime, int opTime) {
        /* Calculate the best move */
    	
    	BestMove<M> best = minimax(this.evaluator, board, ply);
        reportNewBestMove(best.move);
        return best.move;
    }

    static <M extends Move<M>, B extends Board<M, B>> BestMove<M> minimax(Evaluator<B> evaluator, 
    			B board, int depth) {
    	if(depth == 0) {
        	BestMove<M> result = new BestMove<M>(null, evaluator.eval(board));
        	return result;
        }
    	List<M> moves = board.generateMoves();
    	if(moves.isEmpty()) { 
    		if(board.inCheck()) {
    			return new BestMove<M>(null, -evaluator.mate()-depth);
    		} else {
    			return new BestMove<M>(null, -evaluator.stalemate());
    		}
    	}
    	BestMove<M> bestValue = new BestMove<M>(null,-evaluator.infty());
    	for (M move : board.generateMoves()) {
    		board.applyMove(move);
    		int best = -minimax(evaluator, board, depth - 1).value;
    		board.undoMove();
    		if (best > bestValue.value) {
    			bestValue.value = best;
    			bestValue.move = move;
    		}
    	}
    	return bestValue;
    	
    }


}