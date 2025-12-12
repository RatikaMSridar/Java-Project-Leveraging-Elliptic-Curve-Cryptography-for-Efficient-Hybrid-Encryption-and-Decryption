package org.cloudbus.cloudsim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ridicula6 {
	
	public Ridicula6(){

		int i=0;
		int valor=0;
		while(i<100){
			valor =  (int) Math.round(0 + Math.random() * 10);
			System.out.println(valor);
			i++;
		}//fim while

	}//fim construtor

	public static void main(String[] args) {

		new Ridicula6();

	}//fim main

}
