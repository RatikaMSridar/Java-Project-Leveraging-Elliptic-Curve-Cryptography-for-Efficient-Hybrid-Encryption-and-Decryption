package org.cloudbus.cloudsim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ridicula4 {

	public Ridicula4(){

		String REGEX = "";
		Matcher matcher;
		Pattern pattern;

		String linhaResult = "                                AMOUNT_ALLOCS( 300)        1.000000            3.955556";

				REGEX = "AMOUNT\\_ALLOCS\\( (.*)\\)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linhaResult);
				if (matcher.find()){
					System.out.println("["+matcher.group(1)+"]");					
					StringTokenizer t2 = new StringTokenizer(linhaResult, " ");
					while(t2.hasMoreElements())
						System.out.println("["+t2.nextToken()+"]");
					
					/*StringTokenizer t2 = new StringTokenizer(primeiroElemento, ",");
				//string CONSUMOENERGIASERVIDOR
				t2.nextToken();		
				//string com o indice do servidor
				String t2_server = t2.nextToken();
				//System.out.println("[Server: "+t2_server+"]");						
				//Segundo elemento da linha
				String consumoEnergia = t1.nextToken();
				//Insere no mesmo indice do servidor
				//String resultConsumoEnergia=Double.parseDouble(consumoEnergia)+"";

				System.out.println(String.format("%1$,.2f", Double.parseDouble(consumoEnergia)));
					 */
				}//fim if

	}//fim construtor

	public static void main(String[] args) {

		new Ridicula4();

	}//fim main

}
