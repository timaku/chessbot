package tests.gitlab;

import util.Timer;

public class Main {

	public static void main(String[] args) {
		Timer t = new Timer();
		JamboreeTests.init();
		t.start();
		for (int i = 0; i < 5; i++) {
			System.out.println(JamboreeTests.depth2());
		}
		t.stop();
		System.out.println(t.getDuration());
	}

}
