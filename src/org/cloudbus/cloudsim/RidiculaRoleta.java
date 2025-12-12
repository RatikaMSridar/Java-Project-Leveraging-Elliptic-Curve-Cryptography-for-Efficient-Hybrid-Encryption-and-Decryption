/**
 * Goal: Evaluate fitness selection 
 * 
 */

package org.cloudbus.cloudsim;

public class RidiculaRoleta {

	//P: [1]
	//   [2]
	//   [3]
	//   ...
	//   
	String [][] P = new String [4][1];

	public RidiculaRoleta(){

		selecionar();

	}//end constructor

	public void selecionar(){

		System.out.println("\n---selecionar---");

		//P: [1]
		//   [2]
		//   ...
		//   [9]		
		int i=0;
		int j=0;
		while(i<P.length){
			//Fitness
			//P[i][0]= (int)(1 + Math.random()*10)+"";
			P[i][0]= i+"";
			i++;
		}//end while		

		String [] roleta = new String[P.length];
		//O vetor eh passado por referencia. Entao,
		//o que for feito no metodo para o vetor
		//sera atribuido aqui tambem
		gerarRoleta(roleta);

		i=0;
		j=0;
		double p=0;
		int cromoselected=0;

		while (i<P.length/2){
			//while (i<TAM_POPULACAO){
			//Probabilidade da cromo ser selecionada
			p = (double) Math.random();
			System.out.println("\nProbabilidade: " + p);

			cromoselected = individuoRoletaInvertida(roleta,p);

			System.out.println("Selected index: " + cromoselected);

			i++;
		}//fim while

		exibirPopulacao(P);

	}//fim selecionar

	public void gerarRoleta(String [] roleta){

		double soma=0;
		double energia=0;

		int i=0;
		while (i<P.length){

			energia = Double.parseDouble(P[i][P[i].length-1]);
			soma += energia;

			i++;
		}//fim while

		double anterior=0;
		double probabilidade=0;
		double somatorioProbabilidades=0;

		//exibirPopulacao(P);
		System.out.print("Roleta antes: ");
		i=0;
		while (i<P.length){

			energia = Double.parseDouble(P[i][P[i].length-1]);
			//probabilidade = 1/((anterior + energia)/soma);
			probabilidade = ((anterior + energia)/soma);
			roleta[i] = anterior+probabilidade + "";
			System.out.print("["+roleta[i] + "] ");

			anterior = probabilidade;			

			somatorioProbabilidades+=probabilidade;
			i++;

		}//fim while
		System.out.println("\nSomatorio Antes: " + somatorioProbabilidades);

		//Normaliza as porcentagens da roleta (ate aqui, 
		//o somatorio de porcentagens gera valores maiores que 1
		System.out.print("\nRoleta meio: ");
		double somatorioMeio=0;
		i=0;
		roleta[i]="0";		
		System.out.print("["+ roleta[i] + "] ");
		i++;
		while(i<P.length){
			//roleta[i] = 1/(Double.parseDouble(roleta[i])/somatorioProbabilidades) + "";
			roleta[i] = 1/Double.parseDouble(roleta[i]) +"";
			somatorioMeio += Double.parseDouble(roleta[i]);
			System.out.print("["+ roleta[i] + "] ");
			i++;
		}//fim while		
		System.out.println("\nSomatorio Meio: " + somatorioMeio);

		//For normalization
		System.out.print("\nRoleta depois: ");
		i=0;
		double somatorioDepois=0;
		while(i<P.length){
			roleta[i] = (Double.parseDouble(roleta[i])/somatorioMeio) +"";
			somatorioDepois += Double.parseDouble(roleta[i]);
			System.out.print("["+ roleta[i] + "] ");
			i++;
		}//end while		
		System.out.println("\nSomatorio Depois: " + somatorioDepois);

		/*try{
			Thread t = new Thread();			
			t.sleep(10000);
		} catch(Exception e){}
		 */

	}//fim gerarRoleta

	public int individuoRoletaInvertida(String [] roleta, double p){

		double anterior=0;
		boolean achou=false;
		int cromoselected=0;

		int i=roleta.length-1;
		while (i>0 && !achou){

			if (p>=anterior && p <= Double.parseDouble(roleta[i])){
				cromoselected = i;
				achou=true;
			} else
				anterior = Double.parseDouble(roleta[i]);

			i--;
		}//fim while

		return cromoselected;

	}//fim individuoRoletaInvertida
	
	public int individuoRoleta(String [] roleta, double p){

		double anterior=0;
		boolean achou=false;
		int cromoselected=0;

		int i=0;
		while (i<roleta.length && !achou){

			if (p>=anterior && p <= Double.parseDouble(roleta[i])){
				cromoselected = i;
				achou=true;
			} else
				anterior = Double.parseDouble(roleta[i]);

			i++;
		}//fim while

		return cromoselected;

	}//fim individuoRoleta

	public void exibirPopulacao(String [][] A){

		System.out.println("---");

		int i=0;
		int j=0;
		while (i<A.length){

			System.out.print("["+i+"] ");

			while (j<A[i].length-1){

				System.out.print(A[i][j] + " ");
				j++;

			}//fim while
			//Imprime o campo de fitness (double)
			System.out.print(A[i][j] + "\n");			

			i++;
			j=0;
		}//fim while		

	}//fim exibir

	public static void main(String[] args) {

		new RidiculaRoleta();

	}//end main

}//end class
