package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.bots.AlphaBetaSearcher;
import chess.bots.JamboreeSearcher;
import chess.bots.LazySearcher;
import chess.bots.ParallelSearcher;
import chess.bots.SimpleSearcher;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Move;
import cse332.chess.interfaces.Searcher;

public class NodeCountExperiment {
	
	private static final String FENS_FILE = "fens.txt";
	private static final int[] TEST_PLY = {1,2,3,4,5};
	
	public static void main(String[] args) throws FileNotFoundException {
        List<String> fensList = loadFens(FENS_FILE);
    	
    	for(int ply : TEST_PLY) {
    		
    		
    		JamboreeSearcher s = new JamboreeSearcher(); //edit me with different bots!!!!!!!!!
    		
  
            String botName = s.getClass().toString().split(" ")[1].replace("chess.bots.", "");
    		PrintStream out = new PrintStream(new File(botName + " node count of ply " + ply + ".txt"));
    		System.out.println("Starting test with " + botName + " on ply: " + ply);

    		s.setDepth(ply);
	        s.setCutoff(ply / 2);
	    	s.setEvaluator(new SimpleEvaluator());
	        	
    		for (int i = 0; i < fensList.size(); i++) {
	    		String fen = fensList.get(i);
	            
	    		Move a = s.getBestMove(ArrayBoard.FACTORY.create().init(fen), 0 , 0);
	    		System.out.println(botName + " just made " + a + " move, number " + (i+1));
	        }
    		
    		//out.println(s.a.get());
    		System.out.println();
    	}
    
    }
    

    private static List<String> loadFens(String fileName) throws FileNotFoundException {
    	List<String> result = new ArrayList<String>();
    	Scanner input = new Scanner(new File(fileName));
    	while(input.hasNextLine()) {
    		String line = input.nextLine();
    		result.add(line);
    	}
		return result;
    }
}
