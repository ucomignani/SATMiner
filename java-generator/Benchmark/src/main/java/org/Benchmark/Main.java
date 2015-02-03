package org.Benchmark;

public class Main {

	public static void main(String[] args) throws Exception {
		Benchmark benchmark = new Benchmark();
		
		System.out.println("\n ============================= Depart des tests ============================= \n");
		
		benchmark.benchPerformances(10);
	}
}
