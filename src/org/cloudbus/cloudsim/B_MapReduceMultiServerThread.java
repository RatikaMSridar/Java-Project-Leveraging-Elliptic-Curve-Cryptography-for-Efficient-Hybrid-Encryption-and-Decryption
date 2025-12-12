package org.cloudbus.cloudsim;

import java.net.*;
import java.io.*;

public class B_MapReduceMultiServerThread extends Thread {
	private Socket socket = null;

	public B_MapReduceMultiServerThread(Socket socket) {
		super("B_MapReduceMultiServerThread");
		this.socket = socket;
	}

	public void run() {

		try {
			//Adquire objetos de entrada e saida
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							socket.getInputStream()));

			String inputLine, outputLine;
			B_ProtocoloServidor kkp = new B_ProtocoloServidor();
			outputLine = kkp.processInput(null);
			out.println(outputLine);

			//Le dados da entrada, e retorna para o cliente
			while ((inputLine = in.readLine()) != null) {
				outputLine = kkp.processInput(inputLine);
				out.println(outputLine);
				if (outputLine.equals("Bye"))
					break;
			}
			out.close();
			in.close();
			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
