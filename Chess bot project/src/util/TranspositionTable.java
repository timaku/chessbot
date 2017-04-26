package util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import chess.bots.BestMove;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Move;

public class TranspositionTable<M extends Move<M>, B extends Board<M, B>> {
	
	private static final int TABLE_STARTING_SIZE = 50000;
	private static final int TABLE_CULL_SIZE = 45000;
	private static final int TABLE_CULL_STOP = 40000;
	private static final int TABLE_RECENT_SIZE = 1000;
	
	private final Map<B, TableEntry<M, B>> TABLE;
	private final Map<B, Integer> RECENT;
	private AtomicBoolean isCulling;
	
	public TranspositionTable() {
		TABLE = new ConcurrentHashMap<>(TABLE_STARTING_SIZE);
		RECENT = new ConcurrentHashMap<>(TABLE_RECENT_SIZE);
	}
	
	public void clearRecent() {
		RECENT.clear();
	}
	
	public TableEntry<M, B> lookup(B board) {
		TableEntry<M, B> result = TABLE.get(board);
		if (result != null) {
			RECENT.put(board, 0);
		}
		return result;
	}
	
	public void add(B board, TableEntry entry) {
		if (TABLE.size() > TABLE_CULL_SIZE && !isCulling.getAndSet(true)) {
			cull();
		}
		TABLE.put(board, entry);
	}
	
	private synchronized void cull() {
		Iterator<B> keys = TABLE.keySet().iterator();
		while (TABLE.size() > TABLE_CULL_STOP) {
			B key = keys.next();
			if (!RECENT.containsKey(key)) {
				keys.remove();
			}
		}
	}
	
	public static class TableEntry<M extends Move<M>, B extends Board<M, B>> {
		public final Integer MIN, MAX, EXACT, DEPTH;
		public final Stack<BestMove<M>> BEST;
		public final List<M> MOVES;

		public TableEntry(Integer min, Integer max, Integer exact, Integer depth, Stack<BestMove<M>> best, List<M> moves) {
			MIN = min;
			MAX = max;
			EXACT = exact;
			BEST = best;
			MOVES = moves;
			DEPTH = depth;
		}
	}
}
