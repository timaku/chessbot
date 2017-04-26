package traffic;

import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import chess.bots.AlphaBetaSearcher;
import chess.bots.BestMove;
import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;

public class TrafficSearcher<M extends Move<M>, B extends Board<M, B>> extends AbstractSearcher<M, B> {

	public M getBestMove(B board, int myTime, int opTime) {
        /* Calculate the best move */
        BestMove<M> best = alphaBeta(this.evaluator, board, ply, -evaluator.infty(), evaluator.infty());
        reportNewBestMove(best.move);
        return best.move;
    }

    static <M extends Move<M>, B extends Board<M, B>> BestMove<M> alphaBeta(Evaluator<B> evaluator, 
    			B board, int depth, int alpha, int beta) {
    	if(depth == 0) {
        	BestMove<M> result = new BestMove<M>(null, evaluator.eval(board));
        	return result;
        }
    	List<M> moves = board.generateMoves();
    	if(moves.isEmpty()) { 
    		return new BestMove<M>(null, evaluator.eval(board));
    	}
    	BestMove<M> bestValue = new BestMove<M>(null, alpha);
    	for (M move : board.generateMoves()) {
    		board.applyMove(move);
    		int value = -alphaBeta(evaluator, board, depth - 1, -beta, -alpha).value;
    		board.undoMove();
    		
    		if (value > alpha) {
    			alpha = value;
    			bestValue.move = move;
    			bestValue.value = alpha;
    		}
    		
    		if (alpha >= beta) {
    			return bestValue;
    		}
    	}
    	return bestValue;
    	
    }
}