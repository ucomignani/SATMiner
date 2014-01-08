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

//import java.io.*;
//import boolvar.examples.CallCenter;
import boolvar.model.constraints.PBconstraint;
import java.util.ArrayList;
import java.util.Random;
import boolvar.model.Literal;
import boolvar.model.Model;
import boolvar.model.Variable;
import boolvar.model.constraints.Constraint;
import boolvar.output.CNFformula;
import boolvar.output.CnfProblem;
import boolvar.output.PbProblem;
//import boolvar.output.CNFformula;
import boolvar.utility.*;

public class Test
{

    static Random randGen = new Random(947);
    static int variant = PBconstraint.AUTO;

    public static void main(String[] args) throws QuotaException
    {
        testSequence();
        //String[] input = {"2","100","6","200","0","0"};
        //Binpacking.makeBinPacking(args);
        //encodeRandomPB(args);
        //encodeDiabolicPlannig(args);

        //String[] input = {"50","5","1000","90","110","5"};
        //encodeBinPackingToPB(input);

        //25 boites * 100
		/*int[] coeff = {99,99,96,96,92,92,91,88,87,86,85,76,74,72,69,67,67,62,61,56,52,51,49,46,44,42,40,40,
        33,33,30,30,29,28,28,27,25,24,23,22,21,20,17,14,13,11,10,7,7,3};*/

        //18 boites * 1000
		/*int[] coeff = {395,394,394,391,390,389,388,384,383,382,380,379,376,371,368,365,360,360,354,
        350,346,346,344,342,340,335,335,333,330,330,328,327,317,316,311,310,310,306,
        300,300,297,296,295,294,294,286,285,278,275,275};*/

        //4 boites * 1000
		/*int[] coeff = {257,251,245,249,205,204,203,202,201,201,200,199,195,188,170,168,167,165,166,164};*/

        //int[] coeff = {211,203,202,201,200,199,198,197,196,194,175,167,166,165,164,162};
        //encodeBinPackingToPB(3,1000,coeff,3);
        //PBconstraint.test();

        /*
        final int n = 50;
        int sum = 0;
        Variable v[] = new Variable[n];
        for(int i=0; i<n; i++)
        v[i] = new Variable();
        Literal l[] = new Literal[n];
        for(int i=0; i<n; i++)
        l[i] = new Literal(v[i], true);
        int c[] = new int[n];
        for(int i=0; i<n; i++)
        {
        c[i] = 1;
        sum+=(1);
        }
        PBconstraint q = new PBconstraint(l,c,sum/2);
        PBconstraint.setVariant(PBconstraint.BDD);
        System.out.println(q.toString());
        System.out.println(q.getCNF().fullSize());


        Cardinality t = new Cardinality(v, sum/2, sum);
        Cardinality.setVariant(Cardinality.BUBBLE);
        System.out.println(t.toString());
        System.out.println(t.getCNF().fullSize());
        /**/

        //System.out.println(testScalePBencoding());

        //A little example
        //CallCenter example = new CallCenter();
        //System.out.println(example.makePlanning().length());

        //System.out.println(Golomb.encodeGolomb(args));
    }

    static void testSequence() throws QuotaException
    {
        PBconstraint.test();
    }

    static int sizeTestOneEncoding(int nVar) throws QuotaException
    {
        /*System.out.println("--> "+nVar);*/

        Variable[] vars = new Variable[nVar];
        for (int i = 0; i < nVar; i++)
        {
            vars[i] = new Variable();
        }

        Literal[] lits = new Literal[nVar];
        int[] coeffs = new int[nVar];
        for (int i = 0; i < nVar; i++)
        {
            lits[i] = vars[i].getPosLit();
            //coeffs[i] = randGen.nextInt(nVar*10)+1;
            coeffs[i] = i + 1;
        }

        int sum = 0;
        for (int i = 0; i < nVar; i++)
        {
            sum += coeffs[i];
        }

        int bound = sum / 2;

        PBconstraint q = new PBconstraint(lits, coeffs, bound);
        PBconstraint.setVariant(variant);

        int ticket = Quota.beginTry();
        CNFformula out = q.getCNF();
        //int size = Quota.getNbLitUsed(ticket);
        int size = out.fullSize();
        Quota.abandonTry(ticket);

        return size;
    }

    static String testScalePBencoding() throws QuotaException
    {
        final int[] nVar =
        {
            50
        };
        final int nTries = 1;

        String outputSizes = "";

        for (int i = 0; i < nVar.length; i++)
        {
            int sumSizes = 0;
            for (int j = 0; j < nTries; j++)
            {
                sumSizes += sizeTestOneEncoding(nVar[i]);
            }
            outputSizes += (sumSizes / nTries + " ");
            /*System.out.println(outputSizes);*/
        }

        return outputSizes;
    }

    public static void encodeBinPackingToPB(int nBox, int wBox, int[] weight, int encoding)
    {
        int nObj = weight.length;

        Model m = new Model();

        Literal[][] lit = new Literal[nBox][nObj];
        for (int i = 0; i < nBox; i++)
        {
            for (int j = 0; j < nObj; j++)
            {
                lit[i][j] = new Variable().getPosLit();
            }
        }

        for (int i = 0; i < nBox; i++)
        {
            m.post(new PBconstraint(lit[i], weight, wBox));
        }

        int[] coeff = new int[nBox];
        for (int i = 0; i < nBox; i++)
        {
            coeff[i] = 1;
        }

        for (int j = 0; j < nObj; j++)
        {
            Literal[] boxP = new Literal[nBox];
            Literal[] boxN = new Literal[nBox];
            for (int i = 0; i < nBox; i++)
            {
                boxP[i] = lit[i][j];
                boxN[i] = lit[i][j].neg();
            }
            m.post(new PBconstraint(boxP, coeff, 1));
            m.post(new PBconstraint(boxN, coeff, nBox - 1));
        }

        PBconstraint.setVariant(encoding);

        //System.out.println(m.toString());
        CnfProblem output = new CnfProblem();
        output.read(m);
        System.out.println(output.getnClauses() + " clauses et " + output.getnLits() + " literaux produits");
        //System.out.println(output.getOutput());
    }

    public static void modelBinPackingToPB(int nBox, int wBox, int[] weight, int encoding)
    {
        int nObj = weight.length;

        Model m = new Model();

        Literal[][] lit = new Literal[nBox][nObj];
        for (int i = 0; i < nBox; i++)
        {
            for (int j = 0; j < nObj; j++)
            {
                lit[i][j] = new Variable().getPosLit();
            }
        }

        for (int i = 0; i < nBox; i++)
        {
            m.post(new PBconstraint(lit[i], weight, wBox));
        }

        int[] coeff = new int[nBox];
        for (int i = 0; i < nBox; i++)
        {
            coeff[i] = 1;
        }

        for (int j = 0; j < nObj; j++)
        {
            Literal[] boxP = new Literal[nBox];
            Literal[] boxN = new Literal[nBox];
            for (int i = 0; i < nBox; i++)
            {
                boxP[i] = lit[i][j];
                boxN[i] = lit[i][j].neg();
            }
            m.post(new PBconstraint(boxP, coeff, 1));
            m.post(new PBconstraint(boxN, coeff, nBox - 1));
        }

        PBconstraint.setVariant(encoding);

        System.out.println(m.toString());
        //CnfProblem output = new CnfProblem();
        //output.read(m);
        //System.out.println(output.getnClauses()+" clauses et "+output.getnLits()+" literaux produits");
        //System.out.println(output.getOutput());
    }

    public static void encodeBinPackingToPB(String[] args)
    {
        if (args.length != 6)
        {
            System.err.println("Erreur, les 6 arguments suivants sont attendus :\n" +
                    "1. nombre d'objets\n" +
                    "2. nombre de boites\n" +
                    "3. poids des boites\n" +
                    "4. poids mini d'un objet\n" +
                    "5. poids maxi d'un objet\n" +
                    "6 : encoding [1 : LIN, 2 : DIR, 3 : BDD, 4 : LPW, 5 : GPW, 6 : AUTO]");
            return;
        }

        int nObj = Integer.parseInt(args[0]);
        int nBox = Integer.parseInt(args[1]);
        int wBox = Integer.parseInt(args[2]);
        int wMin = Integer.parseInt(args[3]);
        int wMax = Integer.parseInt(args[4]);
        int encoding = Integer.parseInt(args[5]);

        int[] weight = new int[nObj];
        for (int i = 0; i < nObj; i++)
        {
            weight[i] = wMin + randGen.nextInt(wMax - wMin + 1);
        }

        Model m = new Model();

        Literal[][] lit = new Literal[nBox][nObj];
        for (int i = 0; i < nBox; i++)
        {
            for (int j = 0; j < nObj; j++)
            {
                lit[i][j] = new Variable().getPosLit();
            }
        }

        for (int i = 0; i < nBox; i++)
        {
            m.post(new PBconstraint(lit[i], weight, wBox));
        }

        int[] coeff = new int[nBox];
        for (int i = 0; i < nBox; i++)
        {
            coeff[i] = 1;
        }

        for (int j = 0; j < nObj; j++)
        {
            Literal[] boxP = new Literal[nBox];
            Literal[] boxN = new Literal[nBox];
            for (int i = 0; i < nBox; i++)
            {
                boxP[i] = lit[i][j];
                boxN[i] = lit[i][j].neg();
            }
            m.post(new PBconstraint(boxP, coeff, 1));
            m.post(new PBconstraint(boxN, coeff, nBox - 1));
        }

        PBconstraint.setVariant(encoding);

        //System.out.println(m.toString());
        CnfProblem output = new CnfProblem();
        output.read(m);
        System.out.println(output.getnClauses() + " clauses et " + output.getnLits() + " literaux produits");
        //System.out.println(output.getOutput());
    }

    public static void encodeRandomPB(String[] args)
    {
        if (args.length != 7)
        {
            System.err.println("Erreur, les 7 arguments suivants sont attendus :\n" +
                    "1. nombre de variables\n" +
                    "2. taille des contraintes\n" +
                    "3. nombre de contraintes\n" +
                    "4. valeur minimum des coefficients\n" +
                    "5. valeur maximum des coefficients\n" +
                    "6. valeur de la borne des contraintes\n" +
                    "7. codage 1=LIN, 2=DIRECT, 3=BDD, 4=PW\n");
            return;
        }

        int nVar = Integer.parseInt(args[0]);
        int tConst = Integer.parseInt(args[1]);
        int nConst = Integer.parseInt(args[2]);
        int minCoeff = Integer.parseInt(args[3]);
        int maxCoeff = Integer.parseInt(args[4]);
        int bound = Integer.parseInt(args[5]);
        int encoding = Integer.parseInt(args[6]);

        Model m = new Model();

        Variable[] vars = new Variable[nVar];
        for (int i = 0; i < nVar; i++)
        {
            vars[i] = new Variable();
        }

        for (int i = 0; i < nConst; i++)
        {
            Variable[] urne = new Variable[nVar];
            for (int j = 0; j < nVar; j++)
            {
                urne[j] = vars[j];
            }

            Variable[] usedVar = new Variable[tConst];
            int nUrne = urne.length;
            for (int j = 0; j < tConst; j++)
            {
                int p = randGen.nextInt(nUrne);
                usedVar[j] = urne[p];
                nUrne--;
                urne[p] = urne[nUrne];
            }

            Literal[] usedLit = new Literal[tConst];
            for (int j = 0; j < tConst; j++)
            {
                usedLit[j] = usedVar[j].getLit(randGen.nextBoolean());
            }

            int[] coeffs = new int[tConst];
            for (int j = 0; j < tConst; j++)
            {
                coeffs[j] = minCoeff + randGen.nextInt(maxCoeff - minCoeff + 1);
            }

            Constraint q = new PBconstraint(usedLit, coeffs, bound);
            m.post(q);
        }

        PBconstraint.setVariant(encoding);

        CnfProblem output = new CnfProblem();
        output.read(m);
        System.out.println(output.getOutput());
    }

    public static void encodeDiabolicPlannig(String[] args)
    {
        if (args.length != 8)
        {
            System.err.println("Erreur, les 7 arguments suivants sont attendus :\n" +
                    "1. capacite journaliere\n" +
                    "2. duree\n" +
                    "3. nombre de tranches\n" +
                    "4. nombre de segments\n" +
                    "5. nombre de perturbations de segments\n" +
                    "6. nombre de perturbations locales\n" +
                    "7. nombre de perturbations globales (0 pour consistance)\n" +
                    "8. codage 1=LIN, 2=DIRECT, 3=BDD, 4=PW\n");
            return;
        }

        int capa = Integer.parseInt(args[0]);
        int duree = Integer.parseInt(args[1]);
        int nTr = Integer.parseInt(args[2]);
        int nSeg = Integer.parseInt(args[3]);
        int pSeg = Integer.parseInt(args[4]);
        int pLoc = Integer.parseInt(args[5]);
        int pGlob = Integer.parseInt(args[6]);
        int codage = Integer.parseInt(args[7]);

        int[][] profil = new int[nTr * nSeg * 2][];

        //Calcul de la hauteur des tranches
        int[] tranche = new int[nTr];
        int sum = 0;
        for (int i = 0; i < nTr - 1; i++)
        {
            tranche[i] = capa / nTr;
            sum += tranche[i];
        }
        tranche[nTr - 1] = capa - sum;

        //Boucle de production des rectangles et profils
        int indice = 0;
        for (int t = 0; t < nTr; t++)
        {
            //Longueurs initiales des segments
            int[] segment = new int[nSeg];
            sum = 0;
            for (int i = 0; i < nSeg - 1; i++)
            {
                segment[i] = duree / nSeg;
                sum += segment[i];
            }
            segment[nSeg - 1] = duree - sum;

            //Perturbation des longueurs des segments
            for (int i = 0; i < pSeg; i++)
            {
                int f = randGen.nextInt(nSeg - 1);
                if (randGen.nextBoolean())
                {
                    if (segment[f + 1] > 1)
                    {
                        segment[f]++;
                        segment[f + 1]--;
                    }
                }
                else
                {
                    if (segment[f] > 1)
                    {
                        segment[f]--;
                        segment[f + 1]++;
                    }
                }
            }

            //Decoupage des rectangles et creation des profils
            for (int s = 0; s < nSeg; s++)
            {
                profil[indice] = new int[segment[s]];
                profil[indice + 1] = new int[segment[s]];
                for (int i = 0; i < segment[s]; i++)
                {
                    profil[indice][i] = tranche[t] / 2;
                    profil[indice + 1][i] = tranche[t] - profil[indice][i];
                }
                indice += 2;
            }
        }

        //Perturbations locales des profils
        for (int i = 0; i < pLoc; i++)
        {
            int rTranche = randGen.nextInt(nTr);
            int rSegment = randGen.nextInt(nSeg);
            indice = ((rTranche * nSeg) + rSegment) * 2;
            int j = randGen.nextInt(profil[indice].length);
            if (randGen.nextBoolean())
            {
                if (profil[indice + 1][j] > 1)
                {
                    profil[indice][j]++;
                    profil[indice + 1][j]--;
                }
            }
            else
            {
                if (profil[indice][j] > 1)
                {
                    profil[indice][j]--;
                    profil[indice + 1][j]++;
                }
            }
        }

        //Perturbations globales
        for (int i = 0; i < pGlob; i++)
        {
            boolean fin = false;
            while (!fin)
            {
                int rTranche = randGen.nextInt(nTr);
                int rSegment = randGen.nextInt(nSeg);
                indice = (((rTranche * nSeg) + rSegment) * 2) + randGen.nextInt(2);
                int j = randGen.nextInt(profil[indice].length);
                if (profil[indice][j] > 1)
                {
                    profil[indice][j]--;
                    fin = true;
                }
            }

            int rTranche = randGen.nextInt(nTr);
            int rSegment = randGen.nextInt(nSeg);
            indice = (((rTranche * nSeg) + rSegment) * 2) + randGen.nextInt(2);
            int j = randGen.nextInt(profil[indice].length);
            profil[indice][j]++;
        }

        /*
        for(int i=0; i<profil.length; i++)
        {
        for(int j=0; j<profil[i].length; j++)
        {
        System.out.print(profil[i][j]+" ");
        }
        System.out.println("");
        }
         */

        Model m = new Model();

        ArrayList<ArrayList<Literal>> loadLit = new ArrayList<ArrayList<Literal>>();
        for (int i = 0; i < duree; i++)
        {
            loadLit.add(new ArrayList<Literal>());
        }

        ArrayList<ArrayList<Integer>> loadCoeff = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < duree; i++)
        {
            loadCoeff.add(new ArrayList<Integer>());
        }

        for (int p = 0; p < 2 * nTr * nSeg; p++)
        {
            int k = profil[p].length;
            int n = duree - k + 1;
            Variable[] date = new Variable[n];
            for (int i = 0; i < n; i++)
            {
                date[i] = new Variable();
            }

            Literal[] datePlus = new Literal[n];
            for (int i = 0; i < n; i++)
            {
                datePlus[i] = date[i].getPosLit();
            }

            Literal[] dateMoins = new Literal[n];
            for (int i = 0; i < n; i++)
            {
                dateMoins[i] = date[i].getNegLit();
            }

            int[] coeff = new int[duree - k + 1];
            for (int i = 0; i < duree - k + 1; i++)
            {
                coeff[i] = 1;
            }

            m.post(new PBconstraint(datePlus, coeff, 1));
            m.post(new PBconstraint(dateMoins, coeff, n - 1));

            for (int i = 0; i < n; i++)
            {
                for (int j = 0; j < k; j++)
                {
                    loadLit.get(i + j).add(datePlus[i]);
                    loadCoeff.get(i + j).add(profil[p][j]);
                }
            }
        }

        for (int i = 0; i < duree; i++)
        {
            m.post(new PBconstraint(loadLit.get(i), loadCoeff.get(i), capa));
        }

        PBconstraint.setVariant(codage);

        //System.out.println(m.toString());

        //CnfProblem output = new CnfProblem();
        PbProblem output = new PbProblem();
        output.read(m);
        System.out.println(output.getOutput());
    }
}
