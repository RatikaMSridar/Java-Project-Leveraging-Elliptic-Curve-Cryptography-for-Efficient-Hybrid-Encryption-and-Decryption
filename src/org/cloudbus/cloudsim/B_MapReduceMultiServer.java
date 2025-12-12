package org.cloudbus.cloudsim;

import java.net.*;
import java.io.*;

public class B_MapReduceMultiServer {
		
	private static String [] listaClientes = { "localhost" };
	
	private static int PORTA_SERVIDOR = 4444;
	
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		boolean listening = true;

		try {
			serverSocket = new ServerSocket(PORTA_SERVIDOR);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + PORTA_SERVIDOR);
			System.exit(-1);
		}
		
		//Para cada nova conexao, cria uma nova thread
		System.out.println("MapReduceRealcloud server iniciado na porta " + PORTA_SERVIDOR);
		while (listening)
			new B_MapReduceMultiServerThread(serverSocket.accept()).start();

		serverSocket.close();
	}
}
