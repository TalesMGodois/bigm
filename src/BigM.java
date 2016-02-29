/**
 * Created by tales on 28/02/16.
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cs.ubbcluj.ro.mps.data.ConstrainType;
import cs.ubbcluj.ro.mps.data.interfaces.Constrain;
import cs.ubbcluj.ro.mps.data.interfaces.Mps;
import cs.ubbcluj.ro.mps.reader.MpsParser;

class BigM {
    static int bkg = 1;
    static int g;
    static int l;

    static List<String> LerMapa() throws IOException{
        MpsParser parser = new MpsParser();
        Mps mps = parser.parse("src/main/resources/adlittle");

        Map<String, Constrain> constrains = mps.getConstrains();
        List<Constrain> listConstrains = new ArrayList<Constrain>(constrains.values());

        double[] targetRaw = mps.getTargetCoefficientsRawVector();
        int countVarTarget = targetRaw.length;
        StringBuilder sbTargetFunction = new StringBuilder();

        for (int i = 0; i < countVarTarget ;i++) {
            sbTargetFunction.append((targetRaw[i]*-1)+" ") ;
        }

        double[][] coefficientsRawMatrix = mps.getCoefficientsRawMatrix();


        int countL = 0;
        int countG = 0;
        int countSubFunction = mps.getCoefficientsRawMatrix().length;

        // describe the optimization problem
        List<String> listCoefficientsMatrix = new ArrayList<String>();
        listCoefficientsMatrix.add(""+countG);
        listCoefficientsMatrix.add(""+countL);
        listCoefficientsMatrix.add(""+countVarTarget);
        listCoefficientsMatrix.add(sbTargetFunction.toString().trim());
        listCoefficientsMatrix.add(""+countSubFunction);

        StringBuilder sbBasics = new StringBuilder();

        for(int i=0; i<countSubFunction;i++){
            StringBuilder sb = new StringBuilder();
            double[] rLine = coefficientsRawMatrix[i];

            for(int k=0;k<rLine.length;k++){
                if(k> (mps.getNumberOfVariables()-1)){
                    if(rLine[k] == 1.0){
                        sbBasics.append(k+1);
                        sbBasics.append(" ");
                    }
                }

                sb.append(coefficientsRawMatrix[i][k]);
                sb.append(" ");
            }

            String lineStr = sb.toString().trim();

            listCoefficientsMatrix.add(lineStr);

            ConstrainType type = listConstrains.get(i).getType();

            switch (type) {
                case E:
                    break;
                case L:
                    countL++;
                    break;
                case G:
                    countG++;
                    break;
                default:
                    break;
            }

        }

        listCoefficientsMatrix.add(sbBasics.toString().trim());

        listCoefficientsMatrix.set(0, ""+countG);
        listCoefficientsMatrix.set(1, ""+countL);

        String strC = "";
        for(int i=0 ;i<countSubFunction ;i++){
            strC += "0 ";
        }
        listCoefficientsMatrix.add(strC.trim());

        double[] rhs = mps.getRightHandSideValuesRawVector();
        String strRhs = ""
        for(int i=0 ;i<rhs.length ;i++){
            strRhs += rhs[i]+" ";
        }
        listCoefficientsMatrix.add(strRhs.trim());

        return listCoefficientsMatrix;

    }

    public void max() throws IOException {

        StringBuffer sb = new StringBuffer();

        List<String> data = LerMapa();
        for (int i=0 ;i<data.size() ;i++) {
            String s = data.get(i);
            sb.append(s.replace(".", ","));
            sb.append("");
        }

        String matrix = sb.toString();
        matrix = matrix.substring(0, matrix.length()-1);

        Scanner in = new Scanner(matrix.trim());

        int i, j, row, col, kc, kr=0, sub, var, flag = -1, k;
        double sum, max, ba=0, kn;
        double cj[], basis[], c[], colmat[], tab[][];
        bkg = 0;

        System.out.println(	"Escolha 'm' Sendo Um Pouco Maior Do Que O Maior Número Das Equações...");
        System.out.print("Número Total De >= Nas Funções Subjetivas:");
        g = in.nextInt();
        System.out.println(g);

        System.out.print("Número Total De <= Nas Funções Subjetivas:");
        l = in.nextInt();
        System.out.println(l);

        System.out.print("Número Total De variáveis Artificiais, g");

        System.out.print("Número Total De Variáveis Na Função Objetiva:");
        var = in.nextInt();
        System.out.println(var);
        cj = new double[var];

        System.out.print("coeficientes da função objetiva:");
        for (i = 0; i < var ;i++) {
            cj[i] = in.nextDouble();
            System.out.print(cj[i]+" ");
        }

        System.out.print("Número total de funções subjetivas:");
        sub = in.nextInt();
        System.out.println(sub);

        //DECIDING TABLE DIMENSION
        col=var+4;
        row = sub + 2;
        tab = new double[row][col];
        for (i = 1; i < row - 1 ;i++) {
            System.out.println("elementos da Linha "+ i);
            for (j = 4; j < col ;j++) {
                tab[i][j] = in.nextDouble();
            }
        }

        System.out.print("Variáveis básicas:");
        basis = new double[sub];
        int basisL = data.get(data.size()-3).split(" ").length;
        for (i = 0; i < basisL ;i++){
            basis[i] = in.nextInt();
            System.out.print(basis[i]+" ");
        }

        System.out.print("Valores de 'c' da função objetiva");
        c = new double[sub];
        for (i = 0 ;i < sub; i++){
            c[i] = in.nextDouble();
            System.out.print(c[i]+" ");
        }

        System.out.print("RHS:");
        colmat = new double[sub];
        for (i = 0; i < sub ;i++){
            colmat[i] = in.nextDouble();
            System.out.print(colmat[i]+" ");
        }

        //INITIALIZING THE TABLE
        for(i=1; i < row - 1; i++){
            tab[i][0]=i;
        }

        for (i = 1; i < row - 1; i++) {
            tab[i][1] = basis[i - 1];
            tab[i][2] = c[i - 1];
            tab[i][3] = colmat[i - 1];
        }

        System.out.print("Imprime a matriz que voce inseriu:");
        System.out.print("---------------------------------------");
        for (i = 1 ;i < row - 1 ;i++) {
            for (j = 0 ;j < col ;j++) {
                System.out.println( tab[i][j]);
            }
            System.out.print("");
        }
        System.out.print("---------------------------------------");
        for (i = 4; i < col ;i++){

            tab[row - 1][i] = 0;
        }
        tab[row - 1][3] = 0;
        //STARTING THE ITERATION
        for(k=0;k < 10	;k++) {
            //INITIALIAZING FLAG
            flag = -1;
            //Z0
            for(i=1; i<row-1; i++)
                tab[row-1][3]=tab[row-1][3]+tab[i][3]*tab[i][2];
            //Zj-Cj
            for(i=4; i<col; i++) {
                sum=0;
                for(j=1;j<row-1;j++)
                sum=tab[j][i]*tab[j][2]+sum;
                tab[row-1][i]=sum-cj[i-4];
            }
            //FINDING MAXIMUM IN Zj-Cj
            max=tab[row-1][4];

            kc=4;
            for(i=4;i<col;i++) {
                if(max<tab[row-1][i]) {
                    max=tab[row-1][i];
                    kc=i;
                }
            }

            //FINDING b/a RATIO
            for(j=1;j<row-1;j++) {
                if(tab[j][kc]>0) {
                    ba=(colmat[j-1]/tab[j][kc]);
                    kr=j;
                    break;
                }
            }

            for(j=0;j < row-1;j++) {
                if((tab[j][kc]>0) && ((colmat[j-1]/tab[j][kc])<ba))
                    kr=j;
            }

            //SWAPPING KEY COLUMN WITH BASIS
            tab[kr][1]=kc-3;
            kn=tab[kr][kc];
            tab[kr][2]=cj[kc-4];

            //DIVIDING OTHER ROWS BY THE FORMULA
            for(i=1;i<row-1;i++) {
                if(i==kr)
                    continue;
                else {
                    for(j=3;j<kc;j++)
                        tab[i][j]=tab[i][j]-((tab[i][kc]*tab[kr][j])/kn);
                }
            }

            System.out.print("");
            for(i=1;i<row-1;i++) {
                if(i==kr)
                    continue;
                else {
                    for(j=kc+1;j<col;j++)
                        tab[i][j]=tab[i][j]-((tab[i][kc]*tab[kr][j])/kn);
                }
            }

            for(i=1;i<row-1;i++) {
                if(i==kr)
                    continue;
                else
                    tab[i][kc]=tab[i][kc]-((tab[i][kc]*tab[kr][kc])/kn);
            }

            //DIVIDING KEY ROW BY KEY NUMBER
            for(i=3;i<col;i++){
                tab[kr][i]=tab[kr][i]/kn;
            }

            //CHECKING IF Zj-Cj ARE ALL POSITIVE
            for(i=4;i<col;i++) {
                if(tab[row-1][i]<0)
                    flag=1;
            }

            //BREAKING THE LOOP
            if(flag == -1){
                break;
            }
        }

        System.out.print("A Solução é...");
        for (i = 1 ;i < row - 1; i++){
            System.out.println(tab[i][3]);
        }

        in.close();
    }

}