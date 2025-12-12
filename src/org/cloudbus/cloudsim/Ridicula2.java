package org.cloudbus.cloudsim;

import java.util.*;

//Classe para otimizar o uso de loop dentro de loop

public class Ridicula2 {

	public Ridicula2(){

		int NUM_VMS=1000;
		int NUM_SERVIDORES=1000;

/*		int i=0;		
		ArrayList n1 = new ArrayList();		
		while (i<=NUM_VMS){
			n1.add(i);
			i++;
		}//fim while
		int j=0;		
		ArrayList n2 = new ArrayList();		
		while (j<=NUM_SERVIDORES){
			n2.add(j);
			j++;
		}//fim while		
		
		//Restricoes inteiras
		System.out.println("\nPasso1: Restricoes inteiras");
		String restricoesInteiras="";		
		Iterator it1 = n1.iterator();
		Iterator it2 = n2.iterator();
		i=0;
		//Tempo de inicio
		java.util.Date d1;
		java.util.Date d2;
		d1 = new java.util.Date();		
		long tempoInicio = (long) d1.getTime();		
		while(it1.hasNext()){
			//restricoesInteiras += "a" + "_" + it1.next(); 
			it1.next();
			while(it2.hasNext()){
				//restricoesInteiras += "_" + it2.next();
				it2.next();
				//System.out.print("0 ");
			}
			//Reinicia o iterator
			it2=n2.iterator();
			//System.out.print(i++ + "\n");
		}//fim while
		d2 = new java.util.Date();
		long tempoFim = (long) d2.getTime();
		System.out.println(tempoFim-tempoInicio);		
//		System.out.println(restricoesInteiras);
*/
		//Tempo de inicio
		java.util.Date d1;
		java.util.Date d2;
		d1 = new java.util.Date();		
		long tempoInicio = (long) d1.getTime();
		//Restricoes inteiras
		System.out.println("\nPasso1: Restricoes inteiras");
		StringBuffer restricoesInteiras=new StringBuffer();
		int i=0;
		int j=0;
		while (i<NUM_VMS){

			j=0;
			while (j<NUM_SERVIDORES){

				restricoesInteiras.append("a" + "_" + i + "_" + j);
				j++;
				if(j<NUM_SERVIDORES)
					restricoesInteiras.append(" + ");
			}//fim while
			restricoesInteiras.append(" = 1;\n");
			i++;
			System.out.print(i + " ");
		}//fim while
				
		d2 = new java.util.Date();
		long tempoFim = (long) d2.getTime();
		System.out.println(tempoFim-tempoInicio);
		
		System.out.println(restricoesInteiras);

	}//fim construtor

	public static void main(String[] args) {

		new Ridicula2();

	}//fim main

}
