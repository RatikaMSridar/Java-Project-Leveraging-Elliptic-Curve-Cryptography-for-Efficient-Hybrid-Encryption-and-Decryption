package org.cloudbus.cloudsim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ridicula {

	public Ridicula(){

		int v=999999999;
		
		
		String alelo = "00011100";
		System.out.println("Antes:  " + alelo);
		StringBuilder conteudoBinarioAlelo = new StringBuilder(alelo);
		int ponto1=7;
		char bit1 = alelo.charAt(ponto1);
		int ponto2=4;
		char bit2 = alelo.charAt(ponto2);
		conteudoBinarioAlelo.setCharAt(ponto1, bit2);
		conteudoBinarioAlelo.setCharAt(ponto2, bit1);
		alelo = conteudoBinarioAlelo.toString();
		System.out.println("Depois: " + alelo);
			
		int NUM_BITS=8;
		int valor=0;
		int i=0;
		while (i<100){
			valor=(int) Math.round(0 + Math.random() * (NUM_BITS-1));
			if (valor==NUM_BITS)
				System.out.print("Achei");
			i++;
		}//fim while
		
	}//fim construtor

	public static void main(String[] args) {

		new Ridicula();

	}//fim main

}
