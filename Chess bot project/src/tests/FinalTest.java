package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import chess.board.ArrayBoard;
import chess.bots.AlphaBetaSearcher;
import chess.bots.JamboreeSearcher;
import chess.bots.ParallelSearcher;
import chess.bots.SimpleSearcher;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Move;
import cse332.chess.interfaces.Searcher;
import util.Timer;

public class FinalTest {
	
	private static final String FENS_FILE = "cutoff_fens.txt";
	private static final int TEST_PLY = 5;
	public static final Searcher[] bots = {new SimpleSearcher(), new ParallelSearcher(), 
			new AlphaBetaSearcher(), new JamboreeSearcher(25),
			new JamboreeSearcher(28)};
	
	public static void main(String[] args) throws FileNotFoundException {
		List<String> fensList = loadFens(FENS_FILE);		
		
		for(int i = 0; i < bots.length; i++) {
			Searcher s = bots[i];
    		String botName = s.getClass().toString().split(" ")[1].replace("chess.bots.", "");
			s.setDepth(TEST_PLY);
			int cutOff = 0;
			if(botName.equals("ParallelSearcher")) {
				cutOff = 3;
			} else if (botName.equals("JamboreeSearcher")) {
				cutOff = 2;
			}
			s.setCutoff(cutOff);
			
	    	s.setEvaluator(new SimpleEvaluator());

	        for(int warmup = 0; warmup < 2; warmup++){
		    	System.out.println("Starting test with " + botName + " on ply: " + TEST_PLY + " with cutoff: " + cutOff);
		
		    		
			        	
		    		for (int j = 0; j < fensList.size(); j++) {
			    		String fen = fensList.get(j);
			            
			    		System.out.println("Fen #: " + (j+1) + " Time elapsed");
			    		Long sum = (long) 0;
			    		for(int repeat = 0; repeat < 5; repeat++){
			    			Timer t = new Timer();
				    		t.start();
				    		Move a = s.getBestMove(ArrayBoard.FACTORY.create().init(fen), 0 , 0);
				    		t.stop();
				    		//System.out.println("Fen #: " + (i+1) + " for " + botName + " just made " + a + " move");
				    		sum += t.getDuration();
				    		System.out.println(t.getDuration());
			    		}
			    		System.out.printf("Average time: %.1f\n", sum / 5.0);
			    		
			        }
		    		
		    		//System.out.println("Time elapsed: " + t.getDuration() + " nanoseconds");
		    		System.out.println();
		    	
		    	}
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
