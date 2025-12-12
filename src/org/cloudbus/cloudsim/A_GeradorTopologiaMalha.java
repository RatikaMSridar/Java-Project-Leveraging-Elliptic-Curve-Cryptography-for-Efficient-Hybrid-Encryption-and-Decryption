package org.cloudbus.cloudsim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class A_GeradorTopologiaMalha {

	public static String ARQUIVO_TOPOLOGIA_MESH="/home/lucio/modeloMesh";

	public A_GeradorTopologiaMalha(){

		//O numero de nodos precisa ser multiplo do numero de 'chunck' (largura da malha)
		//para se ter um grafo conexo
		int numNodes=50;		
		//largura da malha
		int chunk=10;	

		StringBuffer linhas=new StringBuffer();
		StringBuffer linhasCabecalho=new StringBuffer();
		StringBuffer linhasNodes=new StringBuffer();
		StringBuffer linhasEdges=new StringBuffer();
		int indiceNodoOrigem=0;
		int indiceNodoDestino=0;		
		double custo = 0;
		double bandwidth = 0;

		//Nodes
		linhasNodes.append("Nodes: ( " + numNodes + " )\n");
		int i=0;
		while(i<numNodes){
			linhasNodes.append(i+"\t100\t100\t1\t1\t1\tAS_NONE\n");
			i++;
		}//fim while


		//Links Horizontais
		i=0;
		int numEdges=0;
		int indiceLinha=0;
		int contadorChunk=0;
		while(i<numNodes&&(indiceNodoDestino+1)<numNodes){
			//custo = (double) Math.round(0 + Math.random() * (100));
			//bandwidth = (double) Math.round(100 + Math.random() * (1000));
			custo = 100;
			bandwidth = 1000;
			if (contadorChunk>chunk-2){
				contadorChunk=0;
				indiceNodoOrigem++;
				indiceNodoDestino++;
			}//fim if
			//Two directions to flows in graph
			//
			//Direction: source -> destination
			if (i<numNodes/2)
				linhas.append(indiceLinha + "\t" + indiceNodoOrigem + "\t" + (indiceNodoDestino+1) + "\t" + custo + 
						"\t0.1\t" + bandwidth + "\t1\t\t1\tE_AS_NONE\tU" + "\n");
			else
				//Direction: destination <- source
				linhas.append(indiceLinha + "\t" + (indiceNodoDestino+1) + "\t" + indiceNodoOrigem + "\t" + custo + 
						"\t0.1\t" + bandwidth + "\t1\t\t1\tE_AS_NONE\tU" + "\n");
				
			indiceLinha++;
			
			contadorChunk++;
			indiceNodoOrigem++;
			indiceNodoDestino++;
			//Both directions
			//numEdges+=2;
			numEdges++;
			
			i++;
		}//fim while

		//Links Verticais
		indiceNodoOrigem=0;
		indiceNodoDestino=0;
		i=0;
		while(i<numNodes&&(indiceNodoDestino+chunk)<numNodes){			
			//custo = (double) Math.round(0 + Math.random() * (100));
			//bandwidth = (double) Math.round(500 + Math.random() * (1000));
			//Para reduzir a largura de banda nos nodos mais externos (bordas da rede)
			//Ex.: chunk=5
			//Ex.: 20%5=4 resto=0; 55%5=11 resto=0
			//Ex.: 4%5=0 resto=4; 49%5=9 resto=4
			if((indiceNodoDestino+chunk)%chunk==0 || (indiceNodoDestino+chunk)%chunk==(chunk-1)){
				custo=1;
				bandwidth=100;
				//bandwidth = (double) Math.round(100 + Math.random() * (1000));
			}
			else{
				custo=100;
				bandwidth=1000;
				//bandwidth = (double) Math.round(100 + Math.random() * (1000));
			}
			
			//Direction: source -> destination
			linhas.append(indiceLinha + "\t" + indiceNodoOrigem + "\t" + (indiceNodoDestino+chunk) + "\t" + custo + 
					"\t0.1\t" + bandwidth + "\t1\t\t1\tE_AS_NONE\tU" + "\n");
			indiceLinha++;
			//Direction: destination -> source
			/*linhas.append(indiceLinha + "\t" + (indiceNodoDestino+chunk) + "\t" + indiceNodoOrigem + "\t" + custo + 
					"\t0.1\t" + bandwidth + "\t1\t\t1\tE_AS_NONE\tU" + "\n");
			indiceLinha++;
			*/
			
			indiceNodoOrigem++;
			indiceNodoDestino++;
			//Both directions
			//numEdges+=2;
			numEdges++;
			
			i++;
		}//fim while

		linhasEdges.append("\n\nEdges: ( " + numEdges + " )\n");
		linhasEdges.append(linhas.toString());

		linhasCabecalho.append("Topology: ( " + numNodes + " Nodes, " + numEdges + " Edges )\n");
		linhasCabecalho.append("Model: (Simple Mesh)\n\n");

		StringBuffer arquivo = new StringBuffer();
		arquivo.append(linhasCabecalho.toString());
		arquivo.append(linhasNodes.toString());
		arquivo.append(linhasEdges.toString());

		System.out.println(arquivo.toString());

		try {
			ARQUIVO_TOPOLOGIA_MESH += "_"+numNodes+"nodes"+"_"+numEdges+"edges"+".brite";
			BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_TOPOLOGIA_MESH,false));			
			out.write(arquivo.toString());
			out.close();
		} catch(Exception e){
			System.out.println("Excecao ao gravar no arquivo." + e.getMessage());
		}//fim catch

	}//fim construtor

	public static void main(String[] args) {

		new A_GeradorTopologiaMalha();

	}//fim main

}
