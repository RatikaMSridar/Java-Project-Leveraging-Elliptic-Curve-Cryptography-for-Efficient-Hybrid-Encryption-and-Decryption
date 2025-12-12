package org.cloudbus.cloudsim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ridicula8 {

	String PATH="/home/lucio/";
	int NUM_DATACENTERS = 3;
	int NUM_SERVIDORES = 10;
	int NUM_VMS = 10;
	
	String arquivo = PATH + "modeloLingo_"+
	NUM_DATACENTERS+"data_"+
	NUM_SERVIDORES+"serv_"+
	NUM_VMS+"vm";
	
	String ARQUIVO_RESULT_LINGO = arquivo + ".lgr";
	
	public Ridicula8(){

		writeActiveRouters();

	}//end construtor

	public void writeActiveRouters(){
		
		//Parser to acquire current energy 
		//Open file result
		try {
			//File lines
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO));
			//Line parser
			String REGEX = "";
			Matcher matcher;				
			Pattern pattern;
			int indexRouter=0;
			double flowRouter=0;
			StringTokenizer token;
			
			StringBuffer listaRouters = new StringBuffer();

			//Store amount of allocs in each server
			while(linha!=null){
				//Two possible cases:
				//FIN( 1)    18.0000    0.0000 
				//FIN( 1)    18.0000    
				REGEX = "FIN\\(\\s(.*)\\)\\s(.*)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linha);
				if (matcher.find()){
					//Two possible cases:
					//FIN( 1)    18.0000    0.0000 
					//FIN( 1)    18.0000    
					indexRouter = Integer.parseInt(matcher.group(1));
					token = new StringTokenizer(matcher.group(2)," ");
					flowRouter = Double.parseDouble(token.nextToken());					
					listaRouters.append(indexRouter + " " + flowRouter + " ");					
				}//end if
				linha=file.readLine();				
			}//end while

			System.out.println("["+listaRouters+"]");
				
			StringTokenizer t = new StringTokenizer(listaRouters.toString()," ");
			//Divide by 2 because FIN is a list of pairs: key->value
			int size=t.countTokens() / 2 ;
			System.out.println("Size: " + size);
			//+1 because begin at 0
			String [] FIN = new String[size+1];
			//Initialize list
			int i=0;
			while(i<FIN.length){
				FIN[i]="0";
				i++;
			}
			while(t.hasMoreTokens()){
				FIN[Integer.parseInt(t.nextToken())]=t.nextToken()+"";
			}//end while
			//1 because Lingo index begin at 1
			i=1;
			while(i<FIN.length){
				System.out.println("FIN["+i+"]: " + FIN[i]);
				i++;
			}//end while
			
		} catch (Exception e){
			System.out.println("\n7Exception openning Lingo .lgr file: " + ARQUIVO_RESULT_LINGO + " " + e.getMessage());	
		}//end catch

	}//end writeActiveRouters

	public String removerEspacos(String s) {
		StringTokenizer st = new StringTokenizer(s," ",false);
		String t="";
		while (st.hasMoreElements()) t += st.nextElement();
		return t;
	}
	
	public static void main(String[] args) {

		new Ridicula8();

	}//fim main

}
