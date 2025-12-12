package org.cloudbus.cloudsim;

import java.io.*;
import java.net.*;

public class B_MapReduceClient {

	private static String HOSTNAME = "localhost";

	public static void main(String[] args) throws IOException {

		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;

		try {
			socket = new Socket(HOSTNAME, 4444);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + HOSTNAME);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: " + HOSTNAME);
			System.exit(1);
		}

		//Cria objeto para ler dados do servidor
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String fromServer;
		String fromUser;

		//Le dados do servidor
		StringBuffer conteudoArquivoNS2 = new StringBuffer();
		
		while ((fromServer = in.readLine()) != null) {
			System.out.println("Server: " + fromServer);
			
			if (fromServer.equals("200 OK")){
				//Le dados da entrada padrao...
				//fromUser = stdIn.readLine();
				fromUser = "GET SIM_NS2";
				try {
					Thread t = new Thread();
					t.sleep(5000);				
				} catch (Exception e){
					System.out.println("Excecao ao consultar servidor.");
				}//fim catch
				if (fromUser != null) {
					System.out.println("Client: " + fromUser);
					//... e envia para o servidor
					out.println(fromUser);
				}//fim if
			} else {
				conteudoArquivoNS2.append(fromServer);
				System.out.println(conteudoArquivoNS2.toString());
				
				//Grava o conteudo do arquivo em disco
				
				//Processa o conteudo
				
				//Retorna o resultado para o servidor
				
				break;
			}//fim else

		}//fim while

		out.close();
		in.close();
		stdIn.close();
		socket.close();
	}//fim main
	
	public void executarModeloNS2(){

		System.out.println("--Executar o modelo no NS2--");

		String comando = "ns ";

		try {
			String line;
			Process p = Runtime.getRuntime().exec(comando);
			BufferedReader bri = new BufferedReader
			(new InputStreamReader(p.getInputStream()));
			BufferedReader bre = new BufferedReader
			(new InputStreamReader(p.getErrorStream()));
			while ((line = bri.readLine()) != null) {
				//System.out.println(line);
			}
			bri.close();
			while ((line = bre.readLine()) != null) {
				//System.out.println(line);
			}
			bre.close();
			p.waitFor();
			System.out.println("Done.");
		} catch (Exception e){
			System.out.println("Excecao: Erro ao executar o modelo NS2.");
		}//fim catch

	}//fim executarModeloNS2
	
}
