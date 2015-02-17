/* /Benchmark/src/main/java/org/Benchmark/main.java

   Copyright (C) 2014 Emmanuel Coquery.

This file is part of SATMiner

SATMiner is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

SATMiner is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with SATMiner; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package org.MainBench;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.Benchmark.Benchmark;

/**
 *
 * @author ucomignani
 */
public class main {
	
	public static void main(String[] args) throws Exception {

		String fichierSatQL = "/abalo.satql";
		int nbIterations = 100;
		String nomBench = "abalone";
		
		execBench(nomBench, "BASE", fichierSatQL, nbIterations);
		execBench(nomBench, "PRAND", fichierSatQL, nbIterations);
		execBench(nomBench, "PGUIDE", fichierSatQL, nbIterations);
		execBench(nomBench, "PBCPGUIDE", fichierSatQL, nbIterations);
		execBench(nomBench, "PBCPGUIDE_T 10", fichierSatQL, nbIterations);

	}

	private static void execBench(String nomBench, String algo, String fichierSatQL, int nbIterations) throws Exception {

		/*
		 * BdD
		 */
		Connection connection = null;      
		connection = pgConnect();	
		Statement st = initStatement(connection);
		ResultSet rs = null;
		String querySQL = "";

		/*
		 * gestion execution et stdout
		 */
		Runtime rt = Runtime.getRuntime();
		Process pr = null;
		String output;
		BufferedReader in = null;
		int nbModeles = 0;

		/*
		 * mesures
		 */
		float debut = 0;
		float fin = 0;
		float tpsExec = -1;
		float tpsDepartExec = 0;

		ArrayList<Map<String, ArrayList<String>>> attributsModeles = null; // pour lister les occurences des attributs dans les differents ensembles d'attributs
		double sommeDistancesModeleActuel = 0;
		double moyenneDistancesModeleActuel = 0;
		double sommeDistancesTotales = 0;
		double moyenneDistancesGlobale = 0;

		/*
		 * execution
		 */
		System.out.println("\n ============================= Debut des tests ============================= \n");

		for(int i = 0; i<nbIterations; i++)
		{
			tpsDepartExec = System.nanoTime();

			System.out.println("\ndebut de l'iteration " + (i+1));

			nbModeles = 0;
			debut = System.nanoTime();
			pr = rt.exec("java -jar ../Benchmark/target/Benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar " + algo + " " + fichierSatQL);

			fin = System.nanoTime();

			/*
			 * insertion en base des temps d'execution
			 */
			try {
				tpsExec = ((float) (fin-debut)) / 1000f;
				querySQL = "INSERT INTO diversification.bench_temps(id_exec, nom_bench, algo_utilise, temps_exec) VALUES (" + tpsDepartExec + ", '" + nomBench + "', '" + algo + "', "+ Float.toString(tpsExec) + ");" ;
				st.executeUpdate(querySQL);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println(e.toString());
			}

			System.out.println("fin de l'iteration " + (i+1));

			/*
			 * insertion en base des modeles et distances
			 */
			attributsModeles = new ArrayList<Map<String, ArrayList<String>>>();
			in = new BufferedReader(
					new InputStreamReader(pr.getInputStream()) );
			while ((output = in.readLine()) != null) {
				//System.out.println("renvois: "+output);

				if(output.startsWith("found: "))
				{
					nbModeles++;
					output = output.substring(7);


					/*
					 * calcul des distances
					 */
					sommeDistancesModeleActuel = calculSommeDistances(attributsModeles, output);
					moyenneDistancesModeleActuel = sommeDistancesModeleActuel/nbModeles;
					if(sommeDistancesModeleActuel == 0) // permet de detecter les nouvelles iterations
						sommeDistancesTotales = 0;
					
					sommeDistancesTotales += sommeDistancesModeleActuel;
					if(nbModeles > 1)
						moyenneDistancesGlobale = sommeDistancesTotales/(nbModeles*nbModeles - nbModeles);
					else
						moyenneDistancesGlobale = sommeDistancesTotales;
					
					System.out.println("dist= " + moyenneDistancesGlobale);
					
					querySQL = "INSERT INTO diversification.bench_modeles(id_exec, id_model, resultat_requete, moy_dist_modele, moy_dist_globale) VALUES (" + tpsDepartExec + ", " + nbModeles + ", '"+ output + "', " + moyenneDistancesModeleActuel + ", " + moyenneDistancesGlobale + ");" ;
					st.executeUpdate(querySQL);
				}
			}
			in.close();
			System.out.println("fin de l'insertion en base des modeles");
		}

		System.out.println("\n ============================= Fin des tests ============================= \n");

		/*
		 * fermeture de la connection
		 */
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static Statement initStatement(Connection connection) {
		Statement st = null;

		try {
			st = connection.createStatement();	        
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		

		return st;
	}

	private static Connection pgConnect() {
		Driver d = new org.postgresql.Driver();
		try {
			return d.connect(
					"jdbc:postgresql://localhost/benchs_satql?user=satql&password=satql",
					null);
		} catch (SQLException e) {
			return null;
		}
	}

	private static double calculSommeDistances(
			ArrayList<Map<String, ArrayList<String>>> attributsModeles,
			String output) {
		
		Map<String, ArrayList<String>> modeleActuel = new TreeMap<String, ArrayList<String>>();
		int nbAttributsCommuns = 0;
		int nbAttributsDifferents = 0;
		double sommeDistances = 0;

		String[] ensembles = output.split("\\|");
		String[] ensTmp = null;
		ArrayList<String> listeAttributsModeleCourant = null;
		ArrayList<String> listeAttributsTmp = null;
		String[] attributs = null;

		
		for(int i =0; i<ensembles.length; i++)
		{
			ensTmp = ensembles[i].split(";");
			attributs = ensTmp[1].split(",");

			listeAttributsModeleCourant = new ArrayList<String>();
			
			// on cree la liste d;attributs
			for(int j =0; j<attributs.length; j++)
			{
				// ajout de l'attribut
				listeAttributsModeleCourant.add(attributs[j]);
			}
			
			// calcul de la distance aux modeles precedents
			Iterator<Map<String, ArrayList<String>>> itModeles = attributsModeles.iterator();
			while(itModeles.hasNext())
			{
				// calcul le nombre d'attributs communs
				listeAttributsTmp = itModeles.next().get(ensTmp[0]);
				if(listeAttributsTmp != null)
				{
					nbAttributsCommuns = 0;
					
					for(int j =0; j<attributs.length; j++)
					{
						if(listeAttributsTmp.contains(attributs[j]))
							nbAttributsCommuns++;
					}
				}
				
				// on ajoute la moyenne du nombre d'attributs differents entre les ensembles d'attributs des modeles
				nbAttributsDifferents = (attributs.length - nbAttributsCommuns) + (listeAttributsTmp.size() - nbAttributsCommuns);
				sommeDistances +=  (float) nbAttributsDifferents / (nbAttributsDifferents + nbAttributsCommuns);
			}
			
			// ajout de la liste a la structure
			modeleActuel.put(ensTmp[0], listeAttributsModeleCourant);
		}
		
		attributsModeles.add(modeleActuel);
		
		return sommeDistances;
	}
}
