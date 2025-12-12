package org.cloudbus.cloudsim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ridicula5 {

	private static String ARQUIVO_PERDA_NS2="/home/lucio/modeloNS2_3data_100serv_500vm.perda";
	
	public Ridicula5(){

		//Abre o arquivo com o resultado da vazao
		//Caso o link nao seja encontrado, nao ocorre fluxo no link
		//(Eh necessario verificar se a ordem inversa entre 
		//nodoOrigem e nodoDestino ocorre porque o canal eh duplex,
		//ou seja, 3--6 eh equivalente a 6--3)
		
		String nodoOrigem1="0";
		String nodoDestino1="6";
		String nodoOrigem2="";
		String nodoDestino2="";
		
		boolean achou=false;
		
		try
		{
			//Guarda as informacoes do arquivo
			StringBuffer info = new StringBuffer();

			//Linhas do arquivo
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_PERDA_NS2));
			
			linha=file.readLine();
			while (linha!=null) {
				//Faz o parser da linha
				String REGEX = "";
				Matcher matcher;
				Pattern pattern;
				
				REGEX = "(.*)--(.*) (.*) (.*) (.*)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linha);
				if (matcher.find()){
					nodoOrigem2=matcher.group(1);
					nodoDestino2=matcher.group(2);
					
					//Verifica se existe perda no link 
					//3--6 eh equivalente ah 6--3 para canal duplex
					double porcentagemPerda=0;
					if(nodoOrigem1.equals(nodoOrigem2)&&nodoDestino1.equals(nodoDestino2)||
							nodoOrigem1.equals(nodoDestino2)&&nodoDestino1.equals(nodoOrigem2)){
						achou=true;
						System.out.println(matcher.group(3));
						System.out.println(matcher.group(4));
						porcentagemPerda = (Double.parseDouble(matcher.group(4)) * 100) / Double.parseDouble(matcher.group(3));
						System.out.println("Porcentagem perda: " + porcentagemPerda);
					}//fim if				
					
				}//fim if
				
				//Proxima linha
				linha=file.readLine();
			}//fim for			
			
			//close the file			
			file.close();	
						
		} catch(Exception e){
			System.out.println("Excecao 10 ao abrir o arquivo: " + ARQUIVO_PERDA_NS2 + "\n" + e.getMessage());
		}//fim catch
		
	}//fim construtor

	public static void main(String[] args) {

		new Ridicula5();

	}//fim main

}
