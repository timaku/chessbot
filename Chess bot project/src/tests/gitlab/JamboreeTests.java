package tests.gitlab;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Move;
import cse332.chess.interfaces.Searcher;

import chess.bots.JamboreeSearcher;

import tests.TestsUtility;
import tests.gitlab.TestingInputs;

public class JamboreeTests extends SearcherTests {

    public static void main(String[] args) { new JamboreeTests().run(); }
    public static void init() { STUDENT = new JamboreeSearcher<ArrayMove, ArrayBoard>(); }

	
	@Override
	protected void run() {
        SHOW_TESTS = true;
        PRINT_TESTERR = true;

        ALLOWED_TIME = 20000;
	    
        test("depth2", TestingInputs.FENS_TO_TEST.length);
        test("depth3", TestingInputs.FENS_TO_TEST.length);
        test("depth4", TestingInputs.FENS_TO_TEST.length);

        ALLOWED_TIME = 60000;
        test("depth5", TestingInputs.FENS_TO_TEST.length);
		
		finish();
	} 
}
