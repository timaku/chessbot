package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import chess.board.ArrayBoard;
import chess.bots.JamboreeSearcher;
import chess.bots.ParallelSearcher;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Move;
import util.Timer;

public class SequentialCutoffTest {
	
	private static final String FENS_FILE = "cutoff_fens.txt";
	private static final int[] TEST_PLY = {5};
	
	public static void main(String[] args) throws FileNotFoundException {
        List<String> fensList = loadFens(FENS_FILE);
    	
    	for(int ply : TEST_PLY) {
    		
    		for(int j = 0; j <= ply; j++) { //setting our cutoffs
	    		JamboreeSearcher s = new JamboreeSearcher(); //edit me with different bots!!!!!!!!!
	    		String botName = s.getClass().toString().split(" ")[1].replace("chess.bots.", "");
	    		//PrintStream out = new PrintStream(new File(botName + " node count of ply " + ply + ".txt"));
	    		System.out.println("Starting test with " + botName + " on ply: " + ply + " with cutoff: " + j);
	
	    		s.setDepth(ply);
		        s.setCutoff(j);
		    	s.setEvaluator(new SimpleEvaluator());
		        	
	    		for (int i = 0; i < fensList.size(); i++) {
		    		String fen = fensList.get(i);
		            
		    		System.out.println("Fen #: " + (i+1) + " Time elapsed");
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
		    		System.out.println("Average time: " + sum / 5.0);
		    		
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
