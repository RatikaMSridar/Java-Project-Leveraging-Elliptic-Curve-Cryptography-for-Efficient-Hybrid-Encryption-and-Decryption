package org.cloudbus.cloudsim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ridicula7 {
	
	public Ridicula7(){

		LinkedList posts = new LinkedList();
		int i=0;
		while(i<100){
			posts.add(i);
			i++;
		}//fim while
		
		System.out.println("Size: " + posts.size());
		
		i=0;
		while(!posts.isEmpty()){
			System.out.println("Removi elemento:" + posts.getFirst());
			posts.removeFirst();
			i++;
		}//fim while
		
		String s = "{\"0\": [ 1 2 3 4 ]}";
		StringTokenizer t = new StringTokenizer(s, "\"{[]}: ");
		while(t.hasMoreTokens())
			System.out.println(t.nextToken());

	}//fim construtor

	public static void main(String[] args) {

		new Ridicula7();

	}//fim main

}
