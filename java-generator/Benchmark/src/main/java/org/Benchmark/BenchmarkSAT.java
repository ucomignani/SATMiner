package org.Benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;


public class BenchmarkSAT {
	public static void main(String[] args) throws Exception {

		
	}
	
	//TODO ecrire bench performances
	public void benchPerformances(int nbIterations){
		PrintWriter writer = null;
		File fichier = null;

		float debut = 0;
		float fin = 0;
		float tpsExec;

		try{

			fichier = new File("bench_" + System.currentTimeMillis() + ".txt");

			writer = new PrintWriter(fichier, "UTF-8");

		}catch(FileNotFoundException e){
			System.out.println("not found");
		}catch(UnsupportedEncodingException e){
			System.out.println("encoding error");
		}

		for(int l=0; l<nbIterations; l++){
			debut = System.nanoTime();
			
			// fonction a evaluer

			fin = System.nanoTime();
			tpsExec = ((float) (fin-debut)) / 1000f;

			writer.println(tpsExec);
		}
	}
	
	//TODO ecrire bench qualite
	public void benchQualite(int nbIterations){
		PrintWriter writer = null;
		File fichier = null;

		try{

			fichier = new File("bench_" + System.currentTimeMillis() + ".txt");

			writer = new PrintWriter(fichier, "UTF-8");

		}catch(FileNotFoundException e){
			System.out.println("not found");
		}catch(UnsupportedEncodingException e){
			System.out.println("encoding error");
		}

		for(int l=0; l<nbIterations; l++){
			
			// fonction a evaluer

			writer.println("test");
		}
	}
}
