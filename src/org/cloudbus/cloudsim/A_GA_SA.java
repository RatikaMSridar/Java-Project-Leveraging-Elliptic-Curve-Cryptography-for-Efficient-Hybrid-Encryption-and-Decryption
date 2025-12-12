package org.cloudbus.cloudsim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Algoritmo de otimizacao da rede
 * 
 * Proposta: AG que se comporta como um SA (Simmulated Annealing)
 * 
 * @author root
 *
 */

public class A_GA_SA {

	public static String ARQUIVO_MODELO_NS2="/home/lucio/modeloNS2_5data_10serv_1600vm.tcl";

	public A_GA_SA(){

		StringBuffer conteudoNS2 = new StringBuffer();
		try {
			//Le o arquivo com a melhor rota encontrada pelo AG (passo 1)
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_MODELO_NS2));			
			int i=0;
			int j=0;
			String l=file.readLine();
			conteudoNS2.append(l+"\n");
			while(l!=null){			
				System.out.println(l);
				l=file.readLine();
				conteudoNS2.append(l+"\n");
			}//fim while

		} catch (Exception e){
			System.out.println("Excecao 1: Excecao ao ler o arquivo " + ARQUIVO_MODELO_NS2);
		}

		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_MODELO_NS2,false));			
			out.write(conteudoNS2.toString());						
			out.close();
		} catch(Exception e){
			System.out.println("Excecao 2: Excecao ao gravar no arquivo " + ARQUIVO_MODELO_NS2 + ": " + e.getMessage());			
		}//fim catch

	}//fim construtor	

	public static void main(String[] args) {		

		new A_GA_SA();

	}//fim main

}//fim classe
