package org.cloudbus.cloudsim;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Programa para fazer o parser 
 * da perda e 
 * do atraso fim-a-fim em uma rede MPLS
 * 
 * @author root
 *
 */

public class A_ParserDiversos {

	private static String BASE_PATH="/home/lucio/";
	
	private static String ARQUIVO_LEITURA=BASE_PATH+"/modeloNS2_3data_100serv_500vm.tr";
	private static String ARQUIVO_SAIDA_PERDA=BASE_PATH+"/modeloNS2_3data_100serv_500vm.perda_parser";
	private static String ARQUIVO_SAIDA_ATRASO=BASE_PATH+"/modeloNS2_3data_100serv_500vm.atraso_parser";
	private static String ARQUIVO_SAIDA_VAZAO=BASE_PATH+"/modeloNS2_3data_100serv_500vm.vazao_parser";

	//Quantidade de links de rede
	private static int NUM_LINKS=15;

	public A_ParserDiversos(){

		parserPerda();

		///////////////////////////////
		parserDelay();
		
		parserVazao();
		
		System.out.println("Fim do parser.");

	}//fim construtor
	
	

	public void parserPerda(){

		int [][] packetsSendLink = new int [NUM_LINKS][NUM_LINKS];
		int [][] packetsDropLink = new int [NUM_LINKS][NUM_LINKS];
		int [][] packetsDelayLink = new int [NUM_LINKS][NUM_LINKS];

		//inicializa matrizes
		inicializarMatriz(packetsSendLink);
		inicializarMatriz(packetsDropLink);
		inicializarMatriz(packetsDelayLink);

		try{
			BufferedReader arquivo = new BufferedReader(new FileReader(ARQUIVO_LEITURA));;
			String linhaArquivo="";

			linhaArquivo=arquivo.readLine();
			StringTokenizer token=null;
			String [] t = new String[12];

			while(linhaArquivo!=null){

				token = new StringTokenizer(linhaArquivo, " ");
				//Acao
				t[0] = token.nextToken();
				//Tempo
				t[1] = token.nextToken();
				//hop_atual
				t[2] = token.nextToken();
				//proximo_hop
				t[3] = token.nextToken();
				//Tipo
				t[4] = token.nextToken();
				//PacketSize
				t[5] = token.nextToken();
				//-----
				t[6] = token.nextToken();
				//FlowID
				t[7] = token.nextToken();
				//src absoluto
				t[8] = token.nextToken();
				//dest absoluto
				t[9] = token.nextToken();
				//sequence number
				t[10] = token.nextToken();
				//packet id
				t[11] = token.nextToken();				
				
				if (t[0].equals("+")) 				
					packetsSendLink[Math.round(Float.parseFloat(t[8].toString()))][Math.round(Float.parseFloat(t[9].toString()))]++;

				if (t[0].equals("d"))
					packetsDropLink[Math.round(Float.parseFloat(t[8].toString()))][Math.round(Float.parseFloat(t[9].toString()))]++;

				//Proxima linha
				linhaArquivo=arquivo.readLine();
			}//fim while

			arquivo.close();

		} catch(Exception e){
			System.out.println("Excecao ao abrir o arquivo: " + e.getMessage());
		}//fim catch

		//Grava o resultado em arquivo
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_SAIDA_PERDA,false));
			out.write("From To SendPackets DropPackets");
			int i=0;
			int j=0;
			while(i<packetsSendLink.length){
				j=0;
				while(j<packetsSendLink[i].length){
					//Se enviou pacotes no link
					if(packetsSendLink[i][j]!=0){
						out.write("\n" + i + " " + j + " " + packetsSendLink[i][j] + " " + packetsDropLink[i][j] + " ");						
					}//fim if
					j++;
				}//fim while				
				i++;
			}//fim while
			out.close();			
		} catch(Exception e){
			System.out.println("Excecao ao gravar no arquivo: " + e.getMessage());
		}//fim catch

	}//fim parserPerda

	public void parserDelay(){

		String [] t = new String[12];

		//System.out.println("From-To AtrasoMedio");

		int maiorPID=1;
		StringTokenizer token=null;

		try {
			//try to open the file.
			BufferedReader fileResult = new BufferedReader(new FileReader(ARQUIVO_LEITURA));
			String linhaResult = fileResult.readLine();

			//Preciso saber quantos PIDs tenho no arquivo, porque
			//o calculo do delay eh baseado no PID
			while (linhaResult != null) {

				//Le todas as linhas do arquivo					
				token = new StringTokenizer(linhaResult, " ");
				//System.out.println(linhaResult);
				//Acao
				t[0] = token.nextToken();
				//Tempo
				t[1] = token.nextToken();
				//hop_atual
				t[2] = token.nextToken();
				//proximo_hop
				t[3] = token.nextToken();
				//Tipo
				t[4] = token.nextToken();
				//PacketSize
				t[5] = token.nextToken();
				//-----
				t[6] = token.nextToken();
				//FlowID
				t[7] = token.nextToken();
				//src absoluto
				t[8] = token.nextToken();
				//dest absoluto
				t[9] = token.nextToken();
				//sequence number
				t[10] = token.nextToken();
				//packet id
				t[11] = token.nextToken();					

				if (Integer.parseInt(t[11].toString())>maiorPID){
					maiorPID = Integer.parseInt(t[11].toString());
				}//fim if

				linhaResult = fileResult.readLine();

			}//fim while

			//close the file			
			fileResult.close();	
		} catch (Exception e){
			System.out.println("Excecao ao ler o arquivo: " + ARQUIVO_LEITURA + " : " + e.getMessage());
		}//fim catch

		System.out.println("MaiorPID: " + maiorPID);

		//Abre o arquivo novamente para o calculo do atraso
		float [] t_arr = new float[maiorPID+1];				

		int [][] numSamples = new int[NUM_LINKS][NUM_LINKS]; 
		float [][] totalDelay = new float[NUM_LINKS][NUM_LINKS];		

		inicializarMatriz(numSamples);

		//inicializa totalDelay
		int i=0;
		int j=0;
		while(i<totalDelay.length){
			j=0;
			while(j<totalDelay[i].length){
				totalDelay[i][j]=0;
				j++;
			}//fim while			
			i++;
		}//fim while		

		try {
			//try to open the file.
			BufferedReader fileResult = new BufferedReader(new FileReader(ARQUIVO_LEITURA));
			String linhaResult = fileResult.readLine();

			i=0;
			while (linhaResult != null) {

				//System.out.println("["+linhaResult+"]");

				//Le todas as linhas do arquivo					
				token = new StringTokenizer(linhaResult, " ");
				//System.out.println(linhaResult);
				t[0] = token.nextToken();
				t[1] = token.nextToken();
				t[2] = token.nextToken();
				t[3] = token.nextToken();
				t[4] = token.nextToken();
				t[5] = token.nextToken();
				t[6] = token.nextToken();
				t[7] = token.nextToken();
				t[8] = token.nextToken();
				t[9] = token.nextToken();
				t[10] = token.nextToken();
				t[11] = token.nextToken();					

				if (t[0].equals("+")){
					//t_arr[PID]=timeOfStart
					//System.out.println(t[11].toString());
					t_arr[Integer.parseInt(t[11].toString())]=Float.parseFloat(t[1].toString());
				}//fim if

				if (t[0].equals("r")){
					if (t_arr[Integer.parseInt(t[11].toString())]>0){
						//System.out.println("Passei por aqui");
						//Incrementa o numero de amostras para o atraso no link
						numSamples[Math.round(Float.parseFloat(t[8].toString()))][Math.round(Float.parseFloat(t[9].toString()))]++;
						//Atraso medio no link
						totalDelay[Math.round(Float.parseFloat(t[8].toString()))][Math.round(Float.parseFloat(t[9].toString()))]+=(Float.parseFloat(t[1].toString()) - t_arr[Integer.parseInt(t[11].toString())]);
						//System.out.println("linha: " + i + " origem: " + Math.round(Float.parseFloat(t[8].toString())) + " destino: " + Math.round(Float.parseFloat(t[9].toString())));
					}//fim if

				}//fim if

				linhaResult = fileResult.readLine();
				i++;

			}//fim while

			//close the file			
			fileResult.close();	
		} catch (Exception e){
			System.out.println("Excecao 21: Erro ao ler o arquivo: " + ARQUIVO_LEITURA + " : " + e.getMessage());
		}//fim catch

		//System.out.println("Passei por aqui");

		/////////////

		//Resultados
		float [][] avgDelay = new float[NUM_LINKS][NUM_LINKS];
		//Inicializa
		int p=0;
		int q=0;
		while(p<avgDelay.length){
			q=0;
			while(q<avgDelay[p].length){
				avgDelay[p][q]=0;
				q++;
			}//fim while
			p++;
		}//fim while


		StringBuffer conteudoAtrasoMedio = new StringBuffer(); 
		i=0;
		j=0;
		while (i<NUM_LINKS){
			j=0;
			while (j<NUM_LINKS){						
				//avgDelay = totalDelay / numSamples
				if (numSamples[i][j]!=0){
					avgDelay[i][j]=totalDelay[i][j]/numSamples[i][j];							
					//System.out.println(i + "--" + j + " " + String.format("%1$,.7f",avgDelay[i][j]));
					conteudoAtrasoMedio.append("\n" + i + "--" + j + " " + avgDelay[i][j]);
				}//fim if
				j++;
			}//fim while					
			i++;
		}//fim while

		//Grava o resultado em arquivo
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_SAIDA_ATRASO,false));
			out.write("From-To AtrasoMedio");
			out.write(conteudoAtrasoMedio.toString());
			out.close();
		} catch(Exception e){
			System.out.println("Excecao 22 ao gravar no arquivo." + e.getMessage());
		}//fim catch		

	}
	
	public void parserVazao(){
		
		//Le o conteudo do arquivo resultante da simulacao no NS2
		//Para cada aresta
		int i=0;
		String [][] fromNode = new String[NUM_LINKS][NUM_LINKS];
		String [][] toNode = new String[NUM_LINKS][NUM_LINKS];
		int [][] totalBits = new int[NUM_LINKS][NUM_LINKS];

		String [] t = new String[7];
		double [][] timeBegin = new double[NUM_LINKS][NUM_LINKS];
		double [][] timeEnd = new double[NUM_LINKS][NUM_LINKS]; 
		double [][] duration = new double[NUM_LINKS][NUM_LINKS];
		double [][] throughput = new double[NUM_LINKS][NUM_LINKS];
		StringBuffer parserVazao = new StringBuffer("From-To TotalBitsTransmitted Duration Throughput(Kbps)");

		StringTokenizer token;
		try {
			//try to open the file.
			BufferedReader fileResult = new BufferedReader(new FileReader(ARQUIVO_LEITURA));
			String linhaResult = fileResult.readLine();				
			while (linhaResult != null) {

				//Le todas as linhas do arquivo					
				token = new StringTokenizer(linhaResult, " ");
				t[0] = token.nextToken();
				t[1] = token.nextToken();
				t[2] = token.nextToken();
				t[3] = token.nextToken();
				t[4] = token.nextToken();
				t[5] = token.nextToken();

				if (t[0].equals("r")){
					totalBits[Integer.parseInt(t[2].toString())][Integer.parseInt(t[3].toString())] += 8*Integer.parseInt(t[5]);
					//Se jah contabilizou algum bit anteriormente
					if (totalBits[Integer.parseInt(t[2])][Integer.parseInt(t[3])]!=0)
						timeBegin[Integer.parseInt(t[2])][Integer.parseInt(t[3])] = Double.parseDouble(t[1]);
					else
						timeEnd[Integer.parseInt(t[2])][Integer.parseInt(t[3])] = Double.parseDouble(t[1]);

				}//fim if

				linhaResult = fileResult.readLine();

			}//fim while

			//Resultados
			double vazaoTotalVMs=0;
			i=0;
			int j=0;
			while (i<NUM_LINKS){
				j=0;
				while (j<NUM_LINKS){
					if (totalBits[i][j]!=0){
						duration[i][j] = Math.abs(timeEnd[i][j] - timeBegin[i][j]);
						throughput[i][j] = totalBits[i][j]/duration[i][j]/1e3;
						vazaoTotalVMs += throughput[i][j]; 
						System.out.println(i + "--" + j + " " + totalBits[i][j] + " " + String.format("%1$,.2f",duration[i][j]) + " " + String.format("%1$,.2f",throughput[i][j]));
						parserVazao.append("\n" + i + "--" + j + " " + totalBits[i][j] + " " + String.format("%1$.2f",duration[i][j]) + " " + String.format("%1$.2f",throughput[i][j]));
					}//fim if
					j++;
				}//fim while					
				i++;
			}//fim while

			//Grava o resultado em arquivo
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_SAIDA_VAZAO,false));			
				out.write(parserVazao.toString());
				out.close();
			} catch(Exception e){
				System.out.println("10Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch
							

			//close the file			
			fileResult.close();	
		} catch (Exception e){
			System.out.println("Excecao 8: Erro ao ler o arquivo: " + ARQUIVO_LEITURA);
		}//fim catch
		
	}//end parserVazao
	

	//passado por referencia (o que alterar na string aqui, 
	//jah reflete no metodo anterior
	public void inicializarMatriz(int [][] matriz){

		int i=0;
		int j=0;
		while(i<matriz.length){
			j=0;
			while(j<matriz[i].length){
				matriz[i][j]=0;
				j++;
			}//fim while
			i++;
		}//fim while
	}//fim inicializarMatriz

	public static void main(String[] args) {

		new A_ParserDiversos();

	}//fim main

}//fim classe
