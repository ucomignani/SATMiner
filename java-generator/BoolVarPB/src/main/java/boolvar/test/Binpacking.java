/**
Copyright (c) 2008-2009 Olivier Bailleux

This file is a part of the BoolVar/PB project.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package boolvar.test;

import java.util.Random;

/*
 * Produces a random Binpacking instance and encodes it as
 * a pseudo-Boolean problem instance.
 * O. Bailleux 2009
 */

public class Binpacking 
{
	Random randGen;
	
	int nBox;
	int capaBox;
	int nObj;
	int wObj;
	int nPert;
	
	int weight[];
	
	StringBuffer output;
	
	public Binpacking(int nBox, int capaBox, int nObj, int wObj, int nPert, int seed)
	{
		randGen = new Random(seed);
		this.nBox=nBox;
		this.capaBox=capaBox;
		this.nObj=nObj;
		this.wObj=wObj;
		this.nPert=nPert;
		weight =  new int[nObj];
		output = new StringBuffer();
		
		int wOneObj = wObj / nObj;
		for(int i=0; i<nObj-1; i++)
			weight[i]=wOneObj;
		weight[nObj-1]=wObj-((nObj-1)*wOneObj);
		
		for(int i=0; i<nPert; i++)
		{
			int j1=randGen.nextInt(nObj);
			int j2=randGen.nextInt(nObj);
			if(weight[j2]>1)
			{
				weight[j1]++;
				weight[j2]--;
			}
		}
	}
	
	void out(String str)
	{
		output.append(str);
	}
	
	String var(int i, int j)
	{
		return "x"+((i*nObj)+j+1);
	}
	
	void makePB()
	{
		out("* #variable= "+(nObj*nBox)+" #constraint= "+(nObj+nBox)+"\n");
		out("* randomly generated bin-packing instance\n");
		for(int i=0; i<nBox; i++)
		{
			for(int j=0; j<nObj; j++)
				out("-"+weight[j]+" "+var(i,j)+" ");
			out(">= -"+capaBox+";\n");
		}
		for(int j=0; j<nObj; j++)
		{
			for(int i=0; i<nBox; i++)
				out("+1 "+var(i,j)+" ");
			out("= 1;\n");
		}
	}
	
	String getOutput()
	{
		return new String(output);
	}
	
	public static void makeBinPacking(String[] args)
	{
		if(args.length != 6)
		{
			System.err.println("Erreur, les 6 arguments suivants sont attendus :\n"+
					           "1. nombre de boites\n"+
					           "2. capacit� d'une boite\n"+
					           "3. nombre d'objets\n"+
					           "4. poids total des objets\n"+
					           "5. nombre de perturbations\n"+
					           "6. germe\n");
			return;
		}
		
		int nBox = Integer.parseInt(args[0]);
		int capaBox = Integer.parseInt(args[1]);
		int nObj = Integer.parseInt(args[2]);
		int wObj = Integer.parseInt(args[3]);
		int nPert = Integer.parseInt(args[4]);
		int seed = Integer.parseInt(args[5]);
		
		Binpacking exp = new Binpacking(nBox,capaBox,nObj,wObj,nPert,seed);
		exp.makePB();
		System.out.println(exp.getOutput());
	}
}
