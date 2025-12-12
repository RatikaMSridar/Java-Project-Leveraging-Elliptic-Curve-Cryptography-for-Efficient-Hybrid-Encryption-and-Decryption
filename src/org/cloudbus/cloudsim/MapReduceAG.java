package org.cloudbus.cloudsim;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapReduceAG {

	private static int RANK=0;
	private static String [] P2;
	private static String ARQUIVO_CROMOSSOMO_CLUSTER="/home/lucio/mirror/cromossomo.txt";
	private static String ARQUIVO_TOPOLOGIA_DATACENTER="/home/lucio/mirror/modeloLingo.txt";
	private static int NUM_NODES;
	private static int NUM_EDGES;
	private static int NUM_DATACENTERS;
	private static int NUM_SERVIDORES;
	private static int NUM_VMS;
	private static int NODO_DESTINO;
	
	//Indice da matriz S (servers)
	private int I_SERVER_DATACENTER=0;	
	private int I_SERVER=1;
	private int I_SERVER_CPU=2;
	private int I_SERVER_RAM=3;
	private int I_SERVER_DISK=4;
	private int I_SERVER_BW=5;  	
	private int I_SERVER_VIRTUALIZER=6;
	private int I_SERVER_COST_CPU=7;
	private int I_SERVER_COST_RAM=8;
	private int I_SERVER_COST_DISK=9;
	private int I_SERVER_COST_BW=10;

	//Indice da matriz C (cloud)
	private int I_CLOUD_VM=0;
	private int I_CLOUD_SERVER_INDEX=1;
	private int I_CLOUD_VM_CPU=2;
	private int I_CLOUD_VM_RAM=3;
	private int I_CLOUD_VM_DISK=4;
	private int I_CLOUD_VM_BW=5;
	private int I_CLOUD_VM_FLUXO=6;	
	private int I_CLOUD_VM_VIRTUALIZER=7;
	
	private static String ARQUIVO_MODELO_LINGO="/home/lucio/mirror/modeloLingo.lg4";
	private static String ARQUIVO_RESULT_LINGO="/home/lucio/mirror/modeloLingo.lgr";
	private static String ARQUIVO_RANGE_LINGO="/home/lucio/mirror/modeloLingo.range";
	private static String ARQUIVO_RESULT_QTDE_SERV="/home/lucio/mirror/modeloLingo.parser";
	
	private static String ARQUIVO_MODELO_NS2="/home/lucio/mirror/modeloNS2.tcl";
	private static String ARQUIVO_NAM_NS2="/home/lucio/mirror/modeloNS2.nam";
	private static String ARQUIVO_RESULT_NS2="/home/lucio/mirror/modeloNS2.tr";
	private static String ARQUIVO_VAZAO_NS2="/home/lucio/mirror/modeloNS2.vazao";
	private static String ARQUIVO_PERDA_NS2="/home/lucio/mirror/modeloNS2.perda";
	private static String ARQUIVO_PARSER_BANDA_LINKS="/home/lucio/mirror/modeloNS2.banda";
	
	//Comeca com a perda de pacotes=100000 para iniciar o while (~10% da vazao)
	private static int PERDA_PACOTES_TOTAL=100000;

	//Consumo de energia da rede
	private static int CONSUMO_ENERGIA_ROUTERS=0;
	private static double CONSUMO_ENERGIA_SERVIDORES=0;
	
	public MapReduceAG(){
		
		try {
			//Le o arquivo da populacao e coloca em P2
			
			//System.out.println("Processo["+RANK+"]: Fase de mapeamento");
			
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_CROMOSSOMO_CLUSTER+"_"+RANK));

			//Linhas do arquivo
			String linha = new String();
			linha=file.readLine();
			//System.out.println("Processo["+RANK+"]: Conteudo a ser processado:\n["+linha+"]");
			
			//Inicializa P2
			StringTokenizer t1 = new StringTokenizer(linha, " ");
			int tamCromo=0;
			while(t1.hasMoreTokens()){
				tamCromo++;
				t1.nextToken();
			}//fim while
			P2=new String[tamCromo];
			
			StringTokenizer t2 = new StringTokenizer(linha, " ");
			int i=0;
			while(t2.hasMoreTokens()){
				P2[i]=t2.nextToken();
				i++;
			}//fim while

			//Fecha o arquivo do cromossomo
			file.close();
			
		} catch(Exception e){
			System.out.println("Processo["+RANK+"]: Excecao ao abrir o arquivo: " + ARQUIVO_CROMOSSOMO_CLUSTER + "_" + RANK +"\n" + e.getMessage());
		}//fim catch		

		//Atualiza a topologia com base na informacao do cromossomo
		atualizarTopologia();
		
		//Transforma o conteudo do modelo.txt_* em Linguagem Lingo
		gerarModeloLingo();
		
		//Resolver o modelo Lingo com base no conteudo do modelo.lg4_*
		executarModeloLingo();
		
		//Calcula a quantidade de alocacoes por servidor
		realizarParserResultLingo();
		
		gerarModeloNS2();
		
		executarModeloNS2();
		
		double vazaoTotalVMs=0;
		double perdaPacotesTotalVMs=0;		
		vazaoTotalVMs=realizarParserVazao();
		PERDA_PACOTES_TOTAL=realizarParserPerda();
		
		//Atualiza o fitness do cromossomo
		int campoFitness=(P2.length)-1;
		
		//Fitness = somatorio dos custos de rede do cromossomo +
		//			consumo de energia dos routers +
		//			consumo de energia dos servidores +
		//          somatorio da perda de pacotes
		double fitness=0;
		double custoRede=0;
		
		int j=0;
		while(j<P2.length){
			custoRede+=bin2dec(P2[j]);
			j++;
		}//fim while		
		
		//Quer-se a rede com menor custo de links, menor consumo de energia, e menor perda de pacotes
		fitness=custoRede+CONSUMO_ENERGIA_ROUTERS+CONSUMO_ENERGIA_SERVIDORES+PERDA_PACOTES_TOTAL;

		P2[campoFitness]=fitness+"";
		
		//Grava em arquivo o fitness do cromossomo atualizado
		gravarFitnessCromossomo();
		
	}//fim construtor
	
	public void gravarFitnessCromossomo(){
		
		//System.out.println("Processo["+RANK+"]: "+"Inicio da Gravacao do fitness atualizado em arquivo.");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_CROMOSSOMO_CLUSTER+"_"+RANK,false));			
			int i=0;
			while(i<P2.length){
				out.write(P2[i]+" ");
				i++;
			}//fim while			
			out.close();
			//System.out.println("Processo["+RANK+"]: "+"Fim da Gravacao do fitness atualizado em arquivo.");
		} catch(Exception e){
			System.out.println("Processo["+RANK+"]: "+"Excecao ao gravar no arquivo." + e.getMessage());
		}//fim catch		
		
	}//fim gravarFitnessCromossomo
	
	public double realizarParserFuncaoObjetivo(){

		double valorFuncaoObjetivo=0;

		String REGEX = "";
		Matcher matcher;
		Pattern pattern;

		try {
			//Abre o arquivo com o resultado Lingo			
			BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO+"_"+RANK));
			String linhaResult=arquivoResult.readLine();

			boolean achou=false;
			while (linhaResult!=null&&!achou){
				REGEX = "Objective value:(.*)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linhaResult);
				if (matcher.find()){
					achou=true;
					//System.out.println(matcher.group(1));
					valorFuncaoObjetivo=Double.parseDouble(removerEspacos(matcher.group(1)));
				}//fim if
				linhaResult=arquivoResult.readLine();				
			}//fim while

			//close the file
			arquivoResult.close();			
		} catch(Exception e){
			System.out.println("Processo["+RANK+"]: "+"Excecao 4 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO+"_"+RANK + "\n" + e.getMessage());
		}//fim catch

		return valorFuncaoObjetivo;

	}//fim realizarParserFuncaoObjetivo
	
	public double realizarParserVazao(){

		//Para cada aresta, calcula a vazao
		//
		String [][] EDGES=null;		

		double vazaoTotalVMs=0;

		//
		//Adquire as arestas do arquivo
		try
		{
			//Guarda as informacoes do arquivo
			StringBuffer info = new StringBuffer();
			//Linhas do arquivo
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK));
			//Cabecalho
			linha = file.readLine();
			StringTokenizer token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numNodosOriginal = Integer.parseInt(token.nextToken());
			//System.out.println("NumNodosOriginal: "+ numNodosOriginal);
			token.nextToken();
			int numEdgesOriginal = Integer.parseInt(token.nextToken());
			//System.out.println("NumEdgesOriginal: " + numEdgesOriginal);
			info.append(linha+"\n");

			for (int i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for

			//get the number of nodes
			linha = file.readLine();
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numNodes = Integer.parseInt(token.nextToken());
			//Guarda todas as informacoes dos nodos
			//System.out.println("numNodes: " + numNodes);
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");
			for (int i=0; i<numNodes; i++){				
				info.append(file.readLine()+"\n");
			}//fim while

			//Adquire o numero de Edges
			for (int i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for			
			linha = file.readLine();
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numEdges = Integer.parseInt(token.nextToken());
			//Guarda todas as informacoes dos Edges
			//System.out.println("numEdges: " + numEdges);

			//Le o conteudo do arquivo resultante da simulacao no NS2
			//Para cada aresta
			int i=0;
			String [][] fromNode = new String[numNodes+numEdges][numNodes+numEdges];
			String [][] toNode = new String[numNodes+numEdges][numNodes+numEdges];
			int [][] totalBits = new int[numNodes+numEdges][numNodes+numEdges];

			String [] t = new String[7];
			double [][] timeBegin = new double[numNodes+numEdges][numNodes+numEdges];
			double [][] timeEnd = new double[numNodes+numEdges][numNodes+numEdges]; 
			double [][] duration = new double[numNodes+numEdges][numNodes+numEdges];
			double [][] throughput = new double[numNodes+numEdges][numNodes+numEdges];
			StringBuffer parserVazao = new StringBuffer("From-To TotalBitsTransmitted Duration Throughput");
			//System.out.println("From-To TotalBitsTransmitted Duration Throughput");

			try {
				//try to open the file.
				BufferedReader fileResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_NS2+"_"+RANK));
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
				i=0;
				int j=0;
				while (i<numNodes+numEdges){
					j=0;
					while (j<numNodes+numEdges){
						if (totalBits[i][j]!=0){
							duration[i][j] = Math.abs(timeEnd[i][j] - timeBegin[i][j]);
							throughput[i][j] = totalBits[i][j]/duration[i][j]/1e3;
							vazaoTotalVMs += throughput[i][j]; 
							//System.out.println(i + "--" + j + " " + totalBits[i][j] + " " + String.format("%1$,.2f",duration[i][j]) + " " + String.format("%1$,.2f",throughput[i][j]));
							parserVazao.append("\n" + i + "--" + j + " " + totalBits[i][j] + " " + String.format("%1$,.2f",duration[i][j]) + " " + String.format("%1$,.2f",throughput[i][j]));
						}//fim if
						j++;
					}//fim while					
					i++;
				}//fim while

				/*System.out.println("\n\nFrom: " + fromNode + " To: " + toNode);
				System.out.println("Total bits transmitted: " + totalBits);
				System.out.println("Duration: " + String.format("%1$,.2f",duration));
				System.out.println("Throughput: " + String.format("%1$,.2f",throughput) + "kbps");
				 */				

				//close the file			
				fileResult.close();	
			} catch (Exception e){
				System.out.println("Processo["+RANK+"]: "+"Excecao 4: Erro ao ler o arquivo: " + ARQUIVO_RESULT_NS2+"_"+RANK);
			}//fim catch

			//Grava o resultado em arquivo
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_VAZAO_NS2+"_"+RANK,false));			
				out.write(parserVazao.toString());
				out.close();
			} catch(Exception e){
				System.out.println("Processo["+RANK+"]: "+"Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

			//Thread t111 = new Thread();
			//t111.sleep(1000000);


			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Processo["+RANK+"]: "+"Excecao 6 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK + "\n" + e.getMessage());
		}//fim catch

		return vazaoTotalVMs;

	}//fim parserVazao

	public int realizarParserPerda(){

		int perdaPacotesTotal=0;

		//Para cada aresta, calcula a perda de pacotes		

		//
		//Adquire as arestas do arquivo
		try
		{
			//Guarda as informacoes do arquivo
			StringBuffer info = new StringBuffer();
			//Linhas do arquivo
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK));
			//Cabecalho
			linha = file.readLine();
			StringTokenizer token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numNodosOriginal = Integer.parseInt(token.nextToken());
			//System.out.println("NumNodosOriginal: "+ numNodosOriginal);
			token.nextToken();
			int numEdgesOriginal = Integer.parseInt(token.nextToken());
			//System.out.println("NumEdgesOriginal: " + numEdgesOriginal);
			info.append(linha+"\n");

			for (int i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for

			//get the number of nodes
			linha = file.readLine();
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numNodes = Integer.parseInt(token.nextToken());
			//Guarda todas as informacoes dos nodos
			//System.out.println("numNodes: " + numNodes);
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");
			for (int i=0; i<numNodes; i++){				
				info.append(file.readLine()+"\n");
			}//fim while

			//Adquire o numero de Edges
			for (int i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for			
			linha = file.readLine();
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numEdges = Integer.parseInt(token.nextToken());
			//Guarda todas as informacoes dos Edges
			//System.out.println("numEdges: " + numEdges);

			//Le o conteudo do arquivo resultante da simulacao no NS2
			//
			//Pacotes enviados apenas no link
			int [][] packetsSendLink = new int[numNodes+numEdges][numNodes+numEdges];
			//Pacotes enviados pela origem
			int [] packetsSend = new int[numNodes+numEdges];

			//Pacotes descartados apenas no link
			int [][] packetsDropLink = new int[numNodes+numEdges][numNodes+numEdges];
			//Pacotes descartados gerados pela VM
			int [] packetsDrop = new int[numNodes+numEdges];

			//iniciaMatriz(packetsSend);
			//iniciaVetor(packetsDrop);

			String [] t = new String[8];
			StringBuffer parserPerda = new StringBuffer("From-To PacketsSend PacketsDrop");
			//System.out.println("\nFrom-To PacketsSend PacketsDrop");

			try {
				//try to open the file.
				BufferedReader fileResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_NS2+"_"+RANK));
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
					t[6] = token.nextToken();
					t[7] = token.nextToken();

					if (t[0].equals("+"))
						//Pacotes enviados pela origem
						//packetsSend[Integer.parseInt(t[2].toString())]++;
						//Pacotes enviados apenas no link
						packetsSendLink[Integer.parseInt(t[2].toString())][Integer.parseInt(t[3].toString())]++;

					if (t[0].equals("d")){
						//Pacotes descartados gerados pela VM
						//packetsDrop[Integer.parseInt(t[7].toString())]++;
						//Pacotes descartados apenas no link
						packetsDropLink[Integer.parseInt(t[2].toString())][Integer.parseInt(t[3].toString())]++;

					}//fim if

					linhaResult = fileResult.readLine();															

				}//fim while				

				//Resultados dos links				
				int i=0;
				int j=0;
				while(i<packetsSendLink.length){
					j=0;
					while(j<packetsSendLink[i].length){
						if (packetsSendLink[i][j]!=0){
							//System.out.println(i + "--" + j + " " + packetsSendLink[i][j] + " " + packetsDropLink[i][j]);			
							parserPerda.append("\n" + i + "--" + j + " " + packetsSendLink[i][j] + " " + packetsDropLink[i][j]);

							perdaPacotesTotal+=packetsDropLink[i][j];

						}//fim if
						j++;
					}//fim while
					i++;
				}//fim while

				/*//Resultados da fonte de trafego
				System.out.println("\n\nSource PacketsSend PacketsDrop");
				parserPerda.append("\n\nSource PacketsSend PacketsDrop");
				i=0;
				while(i<packetsSend.length){
					System.out.println(i + " " + packetsSend[i] + " " + packetsDrop[i]); 
					parserPerda.append("\n" + i + " " + packetsSend[i] + " " + packetsDrop[i]);
					i++;
				}//fim while
				 */

				//close the file			
				fileResult.close();	
			} catch (Exception e){
				System.out.println("Processo["+RANK+"]: "+"Excecao 7: Erro ao ler o arquivo: " + ARQUIVO_RESULT_NS2+"_"+RANK + " Excecao: " + e.getMessage());
			}//fim catch

			//Grava o resultado em arquivo
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_PERDA_NS2+"_"+RANK,false));			
				out.write(parserPerda.toString());
				out.close();
			} catch(Exception e){
				System.out.println("Processo["+RANK+"]: "+"Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Processo["+RANK+"]: "+"Excecao 8 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK + "\n" + e.getMessage());
		}//fim catch

		return perdaPacotesTotal;

	}//fim parserPerda
	
	public void atualizarTopologia(){
		
		//Recupera as informacoes do arquivo da topologia:
		//NUM_NODES,NUM_EDGES,NUM_DATACENTERS,NUM_SERVIDORES,NUM_VMS,NODO_DESTINO
		//Todas as informacoes das arestas: EDGES_ARQUIVO
		//Todas as informacoes dos servidores: S_ARQUIVO
		//Todas as informacoes das VMs: VM_ARQUIVO

		//Adquire as informacoes do arquivo
		try
		{
			int i=0;
			int j=0;
			
			String linha = new String();
			StringBuffer info = new StringBuffer();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK));
			//Cabecalho
			for (i=0; i<3; i++) {
				linha=file.readLine();
				info.append(linha+"\n");
			}//fim for

			//get the number of nodes
			linha = file.readLine();	
			info.append(linha+"\n");
			StringTokenizer token = new StringTokenizer(linha, "( )");
			token.nextToken();
			NUM_NODES = Integer.parseInt(token.nextToken());
			//System.out.println("numNodes: " + NUM_NODES);
			//Salta o campo Nodes
			linha=file.readLine();
			info.append(linha+"\n");
			for (i=0; i<NUM_NODES; i++){				
				linha=file.readLine();
				info.append(linha+"\n");
			}//fim while

			//Adquire o numero de Edges
			for (i=0; i<2; i++) {
				linha=file.readLine();
				info.append(linha+"\n");
			}//fim for			
			linha = file.readLine();			
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			NUM_EDGES = Integer.parseInt(token.nextToken());
			//System.out.println("numEdges: " + NUM_EDGES);
			//Guarda todas as informacoes dos Edges
			//System.out.println("numEdges: " + numEdges);
			//EDGE[0][EdgeId From To Length Delay Bandwidth ASfrom ASto Type Other]
			//    [1][EdgeId From To Length Delay Bandwidth ASfrom ASto Type Other]
			//    ...
			String [][] EDGES_ARQUIVO = new String[NUM_EDGES][10];
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");			
			for (i=0; i<NUM_EDGES; i++){
				j=0;
				
				linha = file.readLine();

				token = new StringTokenizer(linha,"\t");
				//id
				EDGES_ARQUIVO[i][j++]=token.nextToken();
				//from
				EDGES_ARQUIVO[i][j++]=token.nextToken();
				//to
				EDGES_ARQUIVO[i][j++]=token.nextToken();
				//Length (Atualizar no campo Length o custo ficticio do enlace)
				EDGES_ARQUIVO[i][j++] = bin2dec(P2[i]+"")+"";
				//Salta o campo
				token.nextToken();				
				//Delay
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Bandwidth (Nao posso alterar a bandwidth pq ela jah foi contratada)
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//ASfrom
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Asto
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Type
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Other
				EDGES_ARQUIVO[i][j++] = token.nextToken();

			}//fim while
			//Atualiza as informacoes dos custos dos enlaces
			i=0;
			j=0;
			linha="";
			while(i<EDGES_ARQUIVO.length){
				j=0;				
				while(j<EDGES_ARQUIVO[i].length){
					linha+=EDGES_ARQUIVO[i][j]+"\t";
					j++;
				}//fim while
				linha+="\n";
				i++;
			}//fim while
			info.append(linha); //sem "\n" aqui

			//Adquire o numero de datacenters do arquivo
			for (i=0; i<2; i++) {
				linha=file.readLine();
				info.append(linha+"\n");
			}//fim for			
			linha=file.readLine();
			info.append(linha+"\n");		
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			NUM_DATACENTERS = Integer.parseInt(token.nextToken());
			//System.out.println("numDatacenters: " + NUM_DATACENTERS);
			//Salta o campo Datacenters
			linha=file.readLine();
			info.append(linha+"\n");
			for (i=0; i<NUM_DATACENTERS; i++){				
				linha=file.readLine();
				info.append(linha+"\n");
			}//fim while

			//Adquire o numero de servidores do arquivo
			for (i=0; i<2; i++) {
				linha=file.readLine();
				info.append(linha+"\n");
			}//fim for			
			linha=file.readLine();
			info.append(linha+"\n");		
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			NUM_SERVIDORES = Integer.parseInt(token.nextToken());
			//System.out.println("numServers: " + NUM_SERVIDORES);

			//Guarda todas as informacoes dos servidores			
			linha=file.readLine();
			info.append(linha+"\n");

			int numCamposServ=13;
			String [][] S_ARQUIVO = new String[NUM_SERVIDORES][numCamposServ];

			for(i=0; i<NUM_SERVIDORES; i++){
				linha=file.readLine();				
				token = new StringTokenizer(linha, " ");
				for(j=0; j<numCamposServ; j++){
					//Atualiza os custos de CPU do servidor de acordo com os dados do cromossomo
					//7 eh o campo de custo da CPU no arquivo .datacenter
					if(j==7){
						S_ARQUIVO[i][j]=bin2dec(P2[i]+"")+"";
						token.nextToken();
					} else
						S_ARQUIVO[i][j]=token.nextToken()+"";
				}//fim for

			}//fim for

			//Atualiza as informacoes dos custos dos servidores
			i=0;
			j=0;
			linha="";
			while(i<S_ARQUIVO.length){
				j=0;				
				while(j<S_ARQUIVO[i].length){
					linha+=S_ARQUIVO[i][j]+" ";
					j++;
				}//fim while
				linha+="\n";
				i++;
			}//fim while
			info.append(linha); //sem "\n" aqui

			//exibir(S_ARQUIVO);

			//Adquire o numero de VMs do arquivo
			for (i=0; i<2; i++) {
				linha=file.readLine();
				info.append(linha+"\n");
			}//fim for
			linha=file.readLine();
			info.append(linha+"\n");		
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			NUM_VMS = Integer.parseInt(token.nextToken());
			//System.out.println("numVMs: " + NUM_VMS);

			//Guarda todas as informacoes das VMs						
			int numCamposVMs=10;
			String [][] VM_ARQUIVO = new String[NUM_VMS][numCamposVMs];
			linha=file.readLine();
			info.append(linha+"\n");
			for(i=0; i<NUM_VMS; i++){
				linha=file.readLine();
				info.append(linha+"\n");
				token = new StringTokenizer(linha, " ");
				for(j=0; j<numCamposVMs; j++){
					VM_ARQUIVO[i][j]=token.nextToken()+"";
				}//fim for
			}//fim for

			//exibir(VM_ARQUIVO);

			//Adquire o nodo de destino
			for (i=0; i<2; i++) {
				linha=file.readLine();
				info.append(linha+"\n");
			}//fim for
			linha=file.readLine();
			info.append(linha+"\n");		
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			NODO_DESTINO = Integer.parseInt(token.nextToken());
			//System.out.println("nodoDestino: " + NODO_DESTINO);	

			//close the file			
			file.close();

			//Grava a atualizacao de custos no arquivo de topologia do datacenter
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK,false));			
				out.write(info.toString());
				out.close();
			} catch(Exception e){
				System.out.println("Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

		} catch(Exception e){
			System.out.println("Excecao 4 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK + "\n" + e.getMessage());
		}//fim catch
				
	}//fim atualizarTopologia

	public void gerarModeloLingo(){

		System.out.println("\nGerar Modelo Lingo---\n");

		StringBuffer modeloLingo = new StringBuffer();

		//Adquire as informacoes do arquivo
		try
		{
			int i=0;
			int j=0;
			
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK));
			//Cabecalho
			for (i=0; i<3; i++) {
				file.readLine();
			}//fim for

			//get the number of nodes
			linha = file.readLine();			
			StringTokenizer token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numNodes = Integer.parseInt(token.nextToken());
			//System.out.println("numNodes: " + numNodes);
			//Salta o campo Nodes
			file.readLine();
			//Guarda todas as informacoes dos Nodes
			//NODES_ARQUIVO	[0][id, type]
			//    			[1][id, type]
			//    ...
			String [][] NODES_ARQUIVO = new String[numNodes][2];
			for (i=0; i<numNodes; i++){				
				linha=file.readLine();
				token = new StringTokenizer(linha,"\t");
				//id
				NODES_ARQUIVO[i][0]=token.nextToken().toString();
				//xpos
				token.nextToken();
				//ypos
				token.nextToken();
				//indegree
				token.nextToken();
				//outdegree
				token.nextToken();
				//ASid
				token.nextToken();
				//type
				NODES_ARQUIVO[i][1]=token.nextToken().toString();
			}//fim while

			/*int i1=0;
			int j1=0;
			System.out.println("\n\n--------------");
			while(i1<numNodes){
				j1=0;
				while(j1<2){
					System.out.println(NODES_ARQUIVO[i1][j1]);
					j1++;
				}//fim while
				i1++;
			}//fim while
			 */
			
			//Adquire o numero de Edges
			for (i=0; i<2; i++) {
				file.readLine();
			}//fim for			
			linha = file.readLine();
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numEdges = Integer.parseInt(token.nextToken());
			//Guarda todas as informacoes dos Edges
			//System.out.println("numEdges: " + numEdges);
			//EDGE[0][EdgeId From To Length Delay Bandwidth ASfrom ASto Type Other]
			//    [1][EdgeId From To Length Delay Bandwidth ASfrom ASto Type Other]
			//    ...
			String [][] EDGES_ARQUIVO = new String[NUM_EDGES][10];
			//Descricao dos campos
			linha = file.readLine();
			for (i=0; i<NUM_EDGES; i++){
				j=0;

				linha = file.readLine();

				token = new StringTokenizer(linha,"\t");
				//id (0)
				EDGES_ARQUIVO[i][j++]=token.nextToken();
				//from (1)
				EDGES_ARQUIVO[i][j++]=token.nextToken();
				//to (2)
				EDGES_ARQUIVO[i][j++]=token.nextToken();
				//Length (3) (Atualizar no campo Length o custo ficticio do enlace)
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Delay (4) 
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Bandwidth (5) (Nao posso alterar a bandwidth pq ela jah foi contratada)
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//ASfrom (6)
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Asto (7)
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Type (8)
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Other (9)
				EDGES_ARQUIVO[i][j++] = token.nextToken();

			}//fim for
			
			//Adquire o numero de datacenters do arquivo
			for (i=0; i<2; i++) {
				file.readLine();
			}//fim for			
			linha = file.readLine();			
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numDatacenters = Integer.parseInt(token.nextToken());
			//System.out.println("numDatacenters: " + numDatacenters);
			//Salta o campo Datacenters
			file.readLine();
			for (i=0; i<numDatacenters; i++){				
				file.readLine();
			}//fim while

			//Adquire o numero de servidores do arquivo
			for (i=0; i<2; i++) {
				file.readLine();
			}//fim for			
			linha = file.readLine();			
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numServers = Integer.parseInt(token.nextToken());
			//System.out.println("numServers: " + numServers);

			//Guarda todas as informacoes dos servidores			
			int numCamposServ=13;
			String [][] S_ARQUIVO = new String[numServers][numCamposServ];
			file.readLine();
			for(i=0; i<numServers; i++){
				linha = file.readLine();
				token = new StringTokenizer(linha, " ");
				for(j=0; j<numCamposServ; j++){
					S_ARQUIVO[i][j]=token.nextToken()+"";
				}//fim for
			}//fim for

			//exibir(S_ARQUIVO);

			//Adquire o numero de VMs do arquivo
			for (i=0; i<2; i++) {
				file.readLine();
			}//fim for
			linha = file.readLine();			
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numVMs = Integer.parseInt(token.nextToken());
			//System.out.println("numVMs: " + numVMs);

			//Guarda todas as informacoes das VMs						
			int numCamposVMs=10;
			String [][] VM_ARQUIVO = new String[numVMs][numCamposVMs];
			file.readLine();
			for(i=0; i<numVMs; i++){
				linha = file.readLine();
				token = new StringTokenizer(linha, " ");
				for(j=0; j<numCamposVMs; j++){
					VM_ARQUIVO[i][j]=token.nextToken()+"";
				}//fim for
			}//fim for

			//exibir(VM_ARQUIVO);

			//Adquire o nodo de destino
			for (i=0; i<2; i++) {
				file.readLine();
			}//fim for
			linha = file.readLine();			
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int nodoDestino = Integer.parseInt(token.nextToken());
			//System.out.println("nodoDestino: " + nodoDestino);


			//---Inicio da transformacao
			i=0;
			j=0;

			//---Para as alocacoes com as funcoes de consumo de energia de router
			//Passo1: abaixo das restricoes de continuidade dos fluxos


			//---Para as alocacoes com as funcoes de consumo de energia de servidores
			//Consumo de energia dos servidores			
			System.out.println("\nPasso4: Consumo de energia de servidores: Restricoes para a capacidade de cores de cpu alocados");
			StringBuffer restricoesConsumoEnergiaCore=new StringBuffer();
			i=0;
			j=0;			
			while (i<numServers){
				j=0;
				restricoesConsumoEnergiaCore.append("[restricao_right_" + "coresAlocados_" + S_ARQUIVO[i][I_SERVER] + "] ");
				while (j<numVMs){
					//Le-se: requisito de cores para alocacao da VM 0;
					//cores_0 * a00 + cores_1 * a10 + cores_2 * a20 <= coresAlocados_0;
					//cores_0 * a01 + cores_1 * a11 + cores_2 * a21 <= coresAlocados_1;
					restricoesConsumoEnergiaCore.append("cores_" + j + " * " + "a" + "_" + j + "_" + S_ARQUIVO[i][I_SERVER]);
					j++;
					if(j<numVMs)
						restricoesConsumoEnergiaCore.append(" +\n");
				}//fim while
				restricoesConsumoEnergiaCore.append(" <= coresAlocados_" + S_ARQUIVO[i][I_SERVER] + ";\n");
				i++;
				System.out.print(i + " ");
			}//fim while

			System.out.println("\nPasso5: Consumo de energia de servidores: restricoes de consumo de energia (em Watts)");
			StringBuffer restricoesConsumoEnergiaServ=new StringBuffer();
			i=0;
			while (i<numServers){
				//consumoEnergiaServidor_6 = cargaInicial_6 + consumoEnergiaCore_6*coresAlocados_6;
				restricoesConsumoEnergiaServ.append("consumoEnergiaServidor_" + S_ARQUIVO[i][I_SERVER] + " = " +
						"cargaInicialServidor_" + S_ARQUIVO[i][I_SERVER] +
						" * " +
						"existeCargaInicial_" + S_ARQUIVO[i][I_SERVER] +
						" + " + 
						"consumoCore_" + S_ARQUIVO[i][I_SERVER] +
						" * " +
						"coresAlocados_" + S_ARQUIVO[i][I_SERVER] + ";\n");				
				i++;
				System.out.print(i + " ");
			}//fim while

			System.out.println("\nPasso6: Consumo de energia de servidores: Restricoes tendem a gerar carga inicial para o consumo de energia");
			StringBuffer restricoesExisteCargaInicialServ=new StringBuffer();
			i=0;
			j=0;			
			while (i<numServers){
				j=0;
				restricoesExisteCargaInicialServ.append("[restricao_right_" + "existeCargaInicial_" + S_ARQUIVO[i][I_SERVER] + "] ");
				restricoesExisteCargaInicialServ.append("( ");
				while (j<numVMs){
					//Le-se: Exemplo: carga inicial de energia dos servidores 6 e 7;
					//(a_0_6 + a_1_6 + a_2_6) - (numVMs*existeCargaInicial_6) <= 0;
					//(a_0_7 + a_1_7 + a_2_7) - (numVMs*existeCargaInicial_7) <= 0;
					restricoesExisteCargaInicialServ.append("a" + "_" + j + "_" + S_ARQUIVO[i][I_SERVER]);
					j++;
					if(j<numVMs)
						restricoesExisteCargaInicialServ.append(" +\n");
				}//fim while
				restricoesExisteCargaInicialServ.append(") - (");
				restricoesExisteCargaInicialServ.append(numVMs + " * " + "existeCargaInicial_" + S_ARQUIVO[i][I_SERVER] + ") <= 0;\n");
				i++;
				System.out.print(i + " ");
			}//fim while

			System.out.println("\nPasso7: Consumo de energia de servidores: Constantes para o consumo de energia: consumo por cpu / numero de cores (em Watts)");
			StringBuffer constantesConsumoEnergiaCore=new StringBuffer();
			i=0;
			while (i<numServers){
				constantesConsumoEnergiaCore.append("consumoCore_" + S_ARQUIVO[i][I_SERVER] + " = 80/4" + ";\n");				
				i++;
				System.out.print(i + " ");
			}//fim while					

			System.out.println("\nPasso8: Consumo de energia de servidores: Constantes para a carga inicial de consumo de energia (em Watts)");
			StringBuffer constantesCargaInicialServ=new StringBuffer();
			i=0;
			while (i<numServers){
				constantesCargaInicialServ.append("cargaInicialServidor_" + S_ARQUIVO[i][I_SERVER] + " = 90" + ";\n");
				i++;
				System.out.print(i + " ");
			}//fim while

			System.out.println("\nPasso9: Consumo de energia de servidores: Constantes da requisicao de cores pelas VMs");
			StringBuffer constantesReqCores=new StringBuffer();
			i=0;
			while (i<numVMs){
				constantesReqCores.append("cores_" + i + " = " + VM_ARQUIVO[i][I_CLOUD_VM_CPU] + ";\n");
				i++;
				System.out.print(i + " ");
			}//fim while

			//---Para as alocacoes sem as funcoes de consumo de energia

			//Restricoes inteiras
			System.out.println("\nPasso10: Restricoes inteiras");
			StringBuffer restricoesInteiras = new StringBuffer();
			i=0;
			j=0;
			while (i<numVMs){

				j=0;
				while (j<numServers){

					restricoesInteiras.append("a" + "_" + i + "_" + S_ARQUIVO[j][I_SERVER]);
					j++;
					if(j<numServers)
						restricoesInteiras.append(" +\n");
				}//fim while
				restricoesInteiras.append(" = 1;\n");
				i++;
				System.out.print(i + " ");
			}//fim while

			//Restricoes de capacidade de CPU
			System.out.println("\nPasso11: Restricoes de capacidade de CPU");
			StringBuffer restricoesCapacidadeCPU=new StringBuffer();
			i=0;
			j=0;			
			while (i<numServers){
				j=0;
				restricoesCapacidadeCPU.append("[restricao_right_" + "cap_cpu_" + S_ARQUIVO[i][I_SERVER] + "] ");
				while (j<numVMs){
					//Le-se: requisito para alocacao da VM 0;
					//r_cpu_0 * a00 + r_cpu_1 * a10 + r_cpu_2 * a20 <= B0;
					//r_cpu_0 * a01 + r_cpu_1 * a11 + r_cpu_2 * a21 <= B1;
					restricoesCapacidadeCPU.append("r_cpu_" + j + " * " + "a" + "_" + j + "_" + S_ARQUIVO[i][I_SERVER]);
					j++;
					if(j<numVMs)
						restricoesCapacidadeCPU.append(" +\n");
				}//fim while
				restricoesCapacidadeCPU.append(" <= cap_cpu_" + S_ARQUIVO[i][I_SERVER] + ";\n");
				//restricoesCapacidadeCPU.append(" <= " + cap_cpu_medio + ";\n");
				i++;
				System.out.print(i + " ");
			}//fim while		

			//Restricoes de capacidade de Banda
			System.out.println("\nPasso12: Restricoes de capacidade de Banda");
			StringBuffer restricoesCapacidadeBanda=new StringBuffer();
			i=0;
			j=0;			
			while (i<numServers){
				j=0;
				restricoesCapacidadeBanda.append("[restricao_right_" + "cap_bw_" + S_ARQUIVO[i][I_SERVER] + "] ");
				while (j<numVMs){
					//r_bw_0 * a00 + r_bw_1 * a10 + r_bw_2 * a20 <= cap_bw_0;
					//r_bw_0 * a01 + r_bw_1 * a11 + r_bw_2 * a21 <= cap_bw_1;
					restricoesCapacidadeBanda.append("r_bw_" + j + " * " + "a" + "_" + j + "_" + S_ARQUIVO[i][I_SERVER]);
					j++;
					if(j<numVMs)
						restricoesCapacidadeBanda.append(" +\n");
				}//fim while
				restricoesCapacidadeBanda.append(" <= cap_bw_" + S_ARQUIVO[i][I_SERVER] + ";\n");
				i++;
				System.out.print(i + " ");
			}//fim while

			System.out.println("\nPasso13: Restricoes de capacidade de banda dos roteadores de acesso");
			//(pronto na geracao do modelo brite)											

			//Para a rede
			BRITETopologyPO topologiaRede = new BRITETopologyPO(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK);
			Edge [] edges = topologiaRede.getEdges();

			//Restricoes de continuidade
			System.out.println("\nPasso14: Restricoes de Continuidade");
			i=0;
			j=0;
			//Adquire todos os nodos source para cada nodo
			//listaSourceNode = [ 0 | 2 3 4 ... ]
			//                  [ 1 | 1 3 ]
			//                  ...		
			Node [] nodes = topologiaRede.getNodes();
			StringBuffer [][] listaChegaNode = new StringBuffer[nodes.length][1];
			while (i<nodes.length){
				listaChegaNode[i][0]=new StringBuffer();
				j=0;
				//Para cada nodo, varre todas as arestas
				while (j<edges.length){
					//Exemplo:
					//ID: 1
					//(Source) 0 --> 1 (Destination)
					//Se o destino da aresta eh o nodo atual
					//guarda o nodo de origem da aresta
					if (edges[j].getDestination()==nodes[i].getID()){
						listaChegaNode[i][0].append(edges[j].getSource() + " ");
						//System.out.println("Chega no Node" + i + ": [" + listaChegaNode[i][0] + "]");
					}//fim if
					j++;
				}//fim while			
				i++;
				System.out.print(i + " ");
			}//fim while		

			//Adquire todos os nodos com destino para cada nodo
			//listaDestinationNode = [ 0 | 2 3 4 ... ]
			//                       [ 1 | 1 3 ]
			//                       ...		
			StringBuffer [][] listaSaiNode = new StringBuffer[nodes.length][1];
			i=0;
			while (i<nodes.length){
				listaSaiNode[i][0]=new StringBuffer();
				j=0;
				//Para cada nodo, varre todas as arestas
				while (j<edges.length){
					//Exemplo:
					//ID: 0
					//(Source) 0 --> 1 (Destination)
					//Se a origem da aresta eh o nodo atual
					//guarda o nodo de destino da aresta
					if (edges[j].getSource()==nodes[i].getID()){
						listaSaiNode[i][0].append(edges[j].getDestination() + " ");
						//System.out.println("Sai do Node" + i + ": [" + listaSaiNode[i][0] + "]");
					}//fim if
					j++;
				}//fim while			
				i++;
				System.out.print(i + " ");
			}//fim while

			//Para cada nodo intermediario, a soma dos fluxos de entrada menos a soma dos fluxos de saida eh 0 (zero)
			i=0;
			StringTokenizer listaDestino;
			StringBuffer [] restricoesContinuidadeDestino = new StringBuffer[nodes.length];
			while(i<nodes.length){
				restricoesContinuidadeDestino[i]=new StringBuffer();
				//listaDestino armazena cada token
				//listaDestino = 2 3 4 ...
				//Ex.: 0 --> 2 3 4 ...
				if(listaSaiNode[i][0]!=null){
					listaDestino = new StringTokenizer(listaSaiNode[i][0].toString());
					while (listaDestino.hasMoreTokens()) {
						restricoesContinuidadeDestino[i].append(" f" + "_" + i + "_" + listaDestino.nextToken() + " ");

						if(listaDestino.hasMoreTokens())
							restricoesContinuidadeDestino[i].append(" +\n");
					}//fim while
				}//fim if
				i++;
				System.out.print(i + " ");
			}//fim while

			i=0;
			StringTokenizer listaOrigem;
			StringBuffer [] restricoesContinuidadeOrigem = new StringBuffer[nodes.length];
			while(i<nodes.length){
				restricoesContinuidadeOrigem[i]=new StringBuffer();
				//listaOrigem armazena cada token
				//listaOrigem = 2 3 4 ...
				//Ex.: 0 --> 2 3 4 ...
				if(listaChegaNode[i][0]!=null){			
					listaOrigem = new StringTokenizer(listaChegaNode[i][0].toString());			
					while (listaOrigem.hasMoreTokens()) {
						restricoesContinuidadeOrigem[i].append(" f" + "_" + listaOrigem.nextToken() + "_" + i + " ");

						if(listaOrigem.hasMoreTokens())
							restricoesContinuidadeOrigem[i].append(" +\n");
					}//fim while
				}//fim if
				i++;
				System.out.print(i + " ");
			}//fim while

			//Monta as restricoes
			i=0;
			StringBuffer restricoesContinuidade = new StringBuffer();
			while(i<nodes.length){
				if ( (restricoesContinuidadeOrigem[i]!=null&&restricoesContinuidadeDestino[i]!=null) &&
						(!restricoesContinuidadeOrigem[i].toString().equals("")&&!restricoesContinuidadeDestino[i].toString().equals("")))
					restricoesContinuidade.append("(" + restricoesContinuidadeOrigem[i] + ") - (" + restricoesContinuidadeDestino[i] + ") = 0;\n");
				i++;
				System.out.print(i + " ");
			}//fim while

			//---Para as alocacoes com as funcoes de consumo de energia de roteadores
			//Semelhante as restricoes de continuidade anteriores
			System.out.println("\nPasso1: Consumo de energia de routers: Restricoes de fluxos");			
			StringBuffer restricoesFluxoRoteadores = new StringBuffer();
			int indiceRouter=0;
			i=0;
			while(i<nodes.length){
				if ( (restricoesContinuidadeOrigem[i]!=null&&restricoesContinuidadeDestino[i]!=null) &&
						(!restricoesContinuidadeOrigem[i].toString().equals("")&&!restricoesContinuidadeDestino[i].toString().equals(""))){

					//Faz um parser de restricoesContinuidadeOrigem
					//restricoesContinuidadeOrigem[i] = [f_0_9 + \n + f_14_9 ...]
					//Quero o segundo indice do f_0_9, que indica o nodo de destino (um router)
					linha=restricoesContinuidadeOrigem[i].toString();

					String REGEX = "";
					Matcher matcher;
					Pattern pattern;

					REGEX = "f_(.*)_(.*) (.*)";
					pattern = Pattern.compile(REGEX);
					matcher = pattern.matcher(linha);
					if (matcher.find()){
						//System.out.println("----------------------");
						//System.out.println("["+matcher.group(2)+"]");
						indiceRouter=Integer.parseInt(removerEspacos(matcher.group(2)));

						restricoesFluxoRoteadores.append("(" + restricoesContinuidadeOrigem[i] + ") - (" + 
								"cap_router_" + indiceRouter + 
								" * " +
								"existeFluxoRouter_" + indiceRouter + ") <= 0;\n");

					}//fim if
				}//fim if
				i++;
				System.out.print(i + " ");
			}//fim while

			System.out.println("\nPasso2: Consumo de energia routers: restricoes de consumo de energia (em Watts)");
			StringBuffer restricoesConsumoEnergiaRouter=new StringBuffer();

			i=0;
			while(i<numNodes){

				//NODES_ARQUIVO = [id, type]
				if (!NODES_ARQUIVO[i][1].equals("SERVER")){

					restricoesConsumoEnergiaRouter.append("consumoEnergiaRouter_" + i + " = " +
							"cargaInicialRouter_" + i +
							" * " +
							"existeFluxoRouter_" + i + ";\n");
				} 
				//Proxima linha
				i++;
			}//fim while

			System.out.println("\nPasso3.1: Consumo de energia routers: constantes para capacidade de fluxo dos roteadores");
			StringBuffer constantesCapRouter=new StringBuffer();
			StringBuffer listaRouters = new StringBuffer();
			indiceRouter=0;
			i=0;
			while(i<numEdges){

				//EDGES_ARQUIVO = [from, to, bw, type]
				if (!EDGES_ARQUIVO[i][3].equals("SERVER")){

					//Nodo origem
					indiceRouter = Integer.parseInt(EDGES_ARQUIVO[i][0]);
					if(!repetidoRouter(indiceRouter,listaRouters)){
						//Se nao eh repetido, guarda em uma lista
						listaRouters.append(indiceRouter+ " ");
						constantesCapRouter.append("cap_router_" + indiceRouter + " = " +
								EDGES_ARQUIVO[i][2] + ";\n");

					}//fim if	
					//Nodo destino
					indiceRouter = Integer.parseInt(EDGES_ARQUIVO[i][1]);
					if(!repetidoRouter(indiceRouter,listaRouters)){
						//Se nao eh repetido, guarda em uma lista
						listaRouters.append(indiceRouter+ " ");
						constantesCapRouter.append("cap_router_" + indiceRouter + " = " +
								EDGES_ARQUIVO[i][2] + ";\n");
					}//fim if

				}//fim if
				i++;
			}//fim while

			System.out.println("\nPasso3.2: Consumo de energia routers: constantes para a carga inicial dos routers");
			StringBuffer constantesCargaInicialRouter=new StringBuffer();

			i=0;
			while(i<numNodes){

				//NODES_ARQUIVO = [id, type]
				if (!NODES_ARQUIVO[i][1].equals("SERVER")){

					constantesCargaInicialRouter.append("cargaInicialRouter_" + i + " = 1;\n");
				} 
				//Proxima linha
				i++;
			}//fim while

			//Restricao dos fluxos para o nodo destino
			//Adquire todos os fluxos que chegam no nodo destino
			StringBuffer restricaoDestino = new StringBuffer();
			if(listaChegaNode[nodoDestino][0]!=null){
				System.out.println("\nPasso15: Restricao dos Fluxos para o Nodo Destino");				
				//Fluxos que chegam no nodo destino
				//System.out.println("["+listaChegaNode[nodoDestino][0]+"]");
				token = new StringTokenizer(listaChegaNode[nodoDestino][0].toString(), " ");
				restricaoDestino.append("( ");
				restricaoDestino.append("f_" + token.nextToken() + "_" + nodoDestino);
				while (token.hasMoreTokens()){
					restricaoDestino.append(" +\n");
					restricaoDestino.append("f_" + token.nextToken() + "_" + nodoDestino);
				}//fim while
				//Fluxos gerados pelas VMs
				restricaoDestino.append(" ) - ( ");
				i=0;
				restricaoDestino.append("g_fluxo_" + i);
				i++;
				while (i<numVMs){
					restricaoDestino.append(" +\n");
					restricaoDestino.append("g_fluxo_" + i);
					System.out.print(i + " ");
					i++;
				}//fim while
				restricaoDestino.append(" ) = 0;\n");				
			}//fim if

			//Relacao entre Fluxo gerado nos servidores e fluxo gerado pelas VMs
			System.out.println("\nPasso16: Restricoes entre o fluxo gerado nos servidores e fluxo gerado pelas VMs");
			//Para cada servidor
			StringBuffer restricoesFluxoServidoresVMs=new StringBuffer();
			i=0;
			boolean achouRouter=false;
			while(i<numServers){
				//Busca o indice do router no qual o servidor se conecta
				j=0;
				int k=0;
				achouRouter=false;
				while(j<edges.length&&!achouRouter){
					//Se descobriu uma aresta com origem em um servidor
					if ((edges[j].getSource()+"").equals(S_ARQUIVO[i][I_SERVER])){
						achouRouter=true;
						//Servidores em uma LAN que se ligam a um unico roteador
						restricoesFluxoServidoresVMs.append("f_" + S_ARQUIVO[i][I_SERVER] + "_" + edges[j].getDestination() + " - ( ");
						//Fluxo gerado por todas as VMs alocadas no servidor
						k=0;
						restricoesFluxoServidoresVMs.append(
								//Ex.: g_fluxo_6 * a_6_5 (Le-se: fluxo gerado VM 6 * alocacao VM 6 no servidor 5)
								"g_fluxo_" + VM_ARQUIVO[k][I_CLOUD_VM] + " * a_" + VM_ARQUIVO[k][I_CLOUD_VM] + "_" + S_ARQUIVO[i][I_SERVER]);
						k++;
						while(k<numVMs){
							restricoesFluxoServidoresVMs.append(" +\n");
							restricoesFluxoServidoresVMs.append(
									//Ex.: g_fluxo_6 * a_6_5 (Le-se: fluxo gerado VM 6 * alocacao VM 6 no servidor 5)
									"g_fluxo_" + VM_ARQUIVO[k][I_CLOUD_VM] + " * a_" + VM_ARQUIVO[k][I_CLOUD_VM] + "_" + S_ARQUIVO[i][I_SERVER]);						
							k++;					
						}//fim while
						restricoesFluxoServidoresVMs.append(" ) = 0;\n");
					}//fim if					
					j++;
				}//fim while
				i++;
			}//fim while

			System.out.println("\nPasso17: Constante para capacidade do link do border router");
			//(pronto na geracao do modelo brite)

			System.out.println("\nPasso18: Custos de CPU");
			StringBuffer custosCPU=new StringBuffer();
			i=0;
			while (i<numServers){
				custosCPU.append("[restricao_Cost_cpu_" + S_ARQUIVO[i][I_SERVER] + "] ");
				custosCPU.append("Cost_cpu_" + S_ARQUIVO[i][I_SERVER] + " = " + S_ARQUIVO[i][I_SERVER_COST_CPU] + ";\n");
				i++;
				System.out.print(i + " ");
			}//fim while

			System.out.println("\nPasso19: Capacidade de CPU dos servidores");
			StringBuffer capCPU=new StringBuffer();
			i=0;
			while (i<numServers){
				capCPU.append("[restricao_Cap_cpu_" + S_ARQUIVO[i][I_SERVER] + "] ");
				capCPU.append("Cap_cpu_" + S_ARQUIVO[i][I_SERVER] + " = " + S_ARQUIVO[i][I_SERVER_CPU] + ";\n");
				i++;
				System.out.print(i + " ");
			}//fim while

			System.out.println("\nPasso20: Capacidade de Banda dos servidores");
			StringBuffer capBanda=new StringBuffer();
			i=0;
			while (i<numServers){
				capBanda.append("[restricao_Cap_bw_" + S_ARQUIVO[i][I_SERVER] + "] ");
				capBanda.append("Cap_bw_" + S_ARQUIVO[i][I_SERVER] + " = " + S_ARQUIVO[i][I_SERVER_BW] + ";\n");
				i++;
				System.out.print(i + " ");
			}//fim while

			System.out.println("\nPasso21: CPU requisitada pelas VMs");
			StringBuffer reqCPU=new StringBuffer();
			i=0;
			while (i<numVMs){
				reqCPU.append("[restricao_r_cpu_" + i + "] ");
				reqCPU.append("r_cpu_" + i + " = " + VM_ARQUIVO[i][I_CLOUD_VM_CPU] + ";\n");
				i++;
				System.out.print(i + " ");
			}//fim while

			System.out.println("\nPasso22: Banda requisitada pelas VMs");
			StringBuffer reqBanda=new StringBuffer();
			i=0;
			while (i<numVMs){
				reqBanda.append("[restricao_r_bw_" + i + "] ");
				reqBanda.append("r_bw_" + i + " = " + VM_ARQUIVO[i][I_CLOUD_VM_BW] + ";\n");
				i++;
				System.out.print(i + " ");
			}//fim while

			//Adquire a topologia do datacenter 
			System.out.println("\nPasso23: Custos dos Links");
			StringBuffer custosLinks=new StringBuffer();
			i=0;
			while (i<edges.length) {	
				//custosLinks.append("cr_" + edges[i].getSource() + "_" + edges[i].getDestination() + " = " + (1/edges[i].getBW()) + ";\n");
				custosLinks.append("[restricao_cr_" + edges[i].getSource() + "_" + edges[i].getDestination() + " ] ");
				//O custo inicial do link eh a propria largura de banda (evolui posteriormente na execucao do AG)
				custosLinks.append("cr_" + edges[i].getSource() + "_" + edges[i].getDestination() + " <= " + edges[i].getLength() + ";\n");
				i++;
				System.out.print(i + " ");
			}//fim while

			System.out.println("\nPasso24: Fluxo gerado pelas VMs");
			StringBuffer fluxoGeradoVMs=new StringBuffer();
			i=0;
			while (i<numVMs){
				fluxoGeradoVMs.append("[restricao_g_fluxo" + "_" + i + "] ");
				fluxoGeradoVMs.append("g_fluxo" + "_" + i + " = " + VM_ARQUIVO[i][I_CLOUD_VM_FLUXO] + ";\n");
				i++;
				System.out.print(i + " ");
			}//fim while




			System.out.println("\nDefinicao das variaveis inteiras");
			StringBuffer variaveisInteiras=new StringBuffer();
			i=0;
			while (i<numServers){
				j=0;
				while (j<numVMs){
					variaveisInteiras.append("@BIN(" + "a" + "_" + j + "_" + S_ARQUIVO[i][I_SERVER] + ");\n");
					j++;
				}//fim while
				i++;
				System.out.print(i + " ");
			}//fim while


			System.out.println("\nFuncao Objetivo");
			StringBuffer funcaoObjetivo=new StringBuffer("[Funcao_Objetivo] MIN=\n");			

			i=0;
			while(i<numServers){

				//Custo de CPU
				//funcaoObjetivo.append("Cost_cpu_" + S_ARQUIVO[i][I_SERVER] + " * ( ");
				funcaoObjetivo.append("( ");
				//VMs que podem ser alocadas nesse servidor
				j=0;
				while (j<numVMs){

					funcaoObjetivo.append("a" + "_" + j + "_" + S_ARQUIVO[i][I_SERVER]);
					j++;
					if (j<numVMs)
						funcaoObjetivo.append(" +\n");
				}//fim while

				funcaoObjetivo.append(" ) ");
				//Proximo servidor
				i++;
				if (i<numServers)
					funcaoObjetivo.append(" +\n");
				System.out.print(i + " ");
			}//fim while

			//Adiciona ah funcao objetivo os custos dos links * fluxo gerado por cada nodo
			i=0;
			StringBuffer relacaoCustoFluxo=new StringBuffer();
			while (i<edges.length) {	
				relacaoCustoFluxo.append(
						"cr_" + edges[i].getSource() + "_" + edges[i].getDestination() + 
						" * " + 
						"f_" + edges[i].getSource() + "_" + edges[i].getDestination());
				i++;
				if (i<edges.length)
					relacaoCustoFluxo.append(" +\n");
			}//fim while

			funcaoObjetivo.append(" +\n" + relacaoCustoFluxo);		

			//Adiciona ah funcao objetivo o consumo de energia por servidor
			StringBuffer consumoEnergiaServidor=new StringBuffer();
			i=0;
			while (i<numServers) {	
				consumoEnergiaServidor.append(
						//consumoEnergiaServidor_6 = cargaInicial_6*existeCargaInicial_6 + consumoEnergiaCore_6*coresAlocados_6;
						"consumoEnergiaServidor_" + S_ARQUIVO[i][I_SERVER]);						
				i++;
				if (i<numServers)
					consumoEnergiaServidor.append(" +\n");
			}//fim while
			funcaoObjetivo.append(" +\n" + consumoEnergiaServidor + "\n");

			//Adiciona ah funcao objetivo o consumo de energia por router
			StringBuffer consumoEnergiaRouter=new StringBuffer();
			i=0;
			if (!NODES_ARQUIVO[i][1].equals("SERVER"))
				consumoEnergiaRouter.append("consumoEnergiaRouter_" + i + "\n");	
			i++;
			while(i<NODES_ARQUIVO.length){

				//NODES_ARQUIVO = [id, type]
				if (!NODES_ARQUIVO[i][1].equals("SERVER")){

					consumoEnergiaRouter.append("+ consumoEnergiaRouter_" + i + "\n");	
					i++;

				} else
					i++;

			}//fim while

			funcaoObjetivo.append(" +\n" + consumoEnergiaRouter + ";\n");            

			System.out.println("\nPasso25: Criacao do modelo LINGO");
			modeloLingo.append("MODEL:\n" + 

					"!Funcao objetivo;" + "\n" +
					funcaoObjetivo + "\n" +

					"!---Passo1: Inicio Consumo energia roteadores: Restricoes de fluxo nos roteadores---;\n" +
					restricoesFluxoRoteadores + 
					"!---Passo1: Fim Consumo energia roteadores: Restricoes de fluxo nos roteadores---;\n\n" +

					"!---Passo2: Inicio Consumo energia roteadores: Restricoes do consumo de energia dos roteadores---;\n" +
					restricoesConsumoEnergiaRouter + 
					"!---Passo2: Fim Consumo energia roteadores: Restricoes do consumo de energia dos roteadores---;\n\n" +

					"!---Passo3.1: Inicio Consumo energia roteadores: Constantes da capacidade de fluxos nos roteadores---;\n" +
					constantesCapRouter + 
					"!---Passo3.1: Fim Consumo energia roteadores: Constantes da capacidade de fluxos nos roteadores---;\n\n" +

					"!---Passo3.2: Inicio Consumo energia roteadores: Constantes para a carga inicial dos roteadores---;\n" +
					constantesCargaInicialRouter +
					"!---Passo3.2: Fim Consumo energia roteadores: Constantes para a carga inicial dos roteadores---;\n\n" +

					"!---Passo4: Inicio Consumo energia servidores: Restricoes para a capacidade de cores de cpu alocados---;\n" +
					restricoesConsumoEnergiaCore +				
					"!---Passo4: Fim Consumo energia servidores: Restricoes para a capacidade de cores de cpu alocados---;\n\n" +

					"!---Passo5: Inicio Consumo energia servidores: Restricoes de consumo de energia (em Watts)---;\n" +
					restricoesConsumoEnergiaServ + 
					"!---Passo5: Fim Consumo energia servidores: Restricoes de consumo de energia (em Watts)---;\n\n" +

					"!---Passo6: Inicio Consumo energia servidores: Restricoes tendem a gerar carga inicial para o consumo de energia---;\n" +
					restricoesExisteCargaInicialServ + 					
					"!---Passo6: Fim Consumo energia servidores: Restricoes tendem a gerar carga inicial para o consumo de energia---;\n\n" +

					"!---Passo7: Inicio Consumo energia servidores: Constantes do consumo de energia por cpu / numero cores (em Watts)---;\n" +
					constantesConsumoEnergiaCore +
					"!---Passo7: Fim Consumo energia servidores: Constantes do consumo de energia por cpu / numero cores (em Watts)---;\n\n" +

					"!---Passo8: Inicio Consumo energia servidores: Constantes para a carga inicial de consumo de energia (em Watts)---;\n" +
					constantesCargaInicialServ + 
					"!---Passo8: Inicio Consumo energia servidores: Constantes para a carga inicial de consumo de energia (em Watts)---;\n\n" +

					"!---Passo9: Inicio Consumo energia servidores: Constantes da requisicao de cores por VMs---;\n" +
					constantesReqCores + 
					"!---Passo9: Inicio Consumo energia servidores: Constantes da requisicao de cores por VMs---;\n\n" +

					"!---Passo10: Inicio Restricoes Inteiras---;\n" +
					restricoesInteiras + 
					"!---Passo10: Fim Restricoes Inteiras---;\n\n" +

					"!---Passo11: Inicio Restricoes Capacidade CPU---;\n" +
					restricoesCapacidadeCPU +
					"!---Passo11: Fim Restricoes Capacidade CPU---;\n\n" +

					"!---Passo12: Inicio Restricoes Capacidade Banda---;\n" +
					restricoesCapacidadeBanda +
					"!---Passo12: Fim Restricoes Capacidade Banda---;\n\n" +

					"!---Passo13: Inicio Restricoes Banda Routers Acesso---;\n" +
					"!Definido na geracao do modelo brite;\n" +
					//restricoesBandaRoutersAcesso + "\n" +
					"!---Passo13: Fim Restricoes Banda Routers Acesso---;\n\n" +

					"!---Passo14: Inicio Restricoes de Continuidade---;\n" +
					restricoesContinuidade +
					"!---Passo14: Fim Restricoes de Continuidade---;\n\n" +					

					"!---Passo15: Inicio Restricao dos Fluxos para o Nodo Destino---;\n" +
					restricaoDestino + 					
					"!---Passo15: Fim Restricao dos Fluxos para o Nodo Destino---;\n\n" +

					"!---Passo16: Inicio Restricoes entre o fluxo gerado nos servidores e fluxo gerado pelas VMs---;\n" +
					restricoesFluxoServidoresVMs + 
					"!---Passo16: Fim Restricoes entre o fluxo gerado nos servidores e fluxo gerado pelas VMs---;\n\n" +

					"!---Passo17: Inicio Constante para capacidade do link do border router---;\n" +
					"!Definido na geracao do modelo brite;\n" +
					"!---Passo17: Fim Constante para capacidade do link do border router---;\n\n" +

					"!---Passo18: Inicio Custos de CPU dos servidores---;\n" +
					custosCPU + 
					"!---Passo18: Fim Custos de CPU dos servidores---;\n\n" +

					"!---Passo19: Inicio Capacidade de CPU dos servidores---;\n" +
					capCPU + 
					"!---Passo19: Fim Capacidade de CPU dos servidores---;\n\n" +

					"!---Passo20: Inicio Capacidade de Banda dos servidores---;\n" +
					capBanda +
					"!---Passo20: Fim Capacidade de Banda dos servidores---;\n\n" +

					"!---Passo21: Inicio Requisito de CPU das VMs---;\n" +
					reqCPU + 
					"!---Passo21: Fim Requisito de CPU das VMs---;\n\n" +

					"!---Passo22: Inicio Requisito de Banda das VMs---;\n" +
					reqBanda + 
					"!---Passo22: Fim Requisito de Banda das VMs---;\n\n" +

					"!---Passo23: Inicio Custo dos enlaces---;\n" +
					custosLinks + 
					"!---Passo23: Fim Custo dos enlaces---;\n\n" +

					"!---Passo24: Inicio Fluxo gerado pelas VMs---;\n" +
					fluxoGeradoVMs + 		
					"!---Passo24: Fim Fluxo gerado pelas VMs---;\n\n" +

					//Para a analise de sensibilidade, esse trecho deve ser comentado
					//"!Variaveis de alocacao inteiras;" + "\n" +
					//variaveisInteiras + "\n" +		

					"END\n" +		

					"! Terse output mode;" + "\n" +
					"SET TERSEO 1" + "\n" +
					"! Solve the model;" + "\n" +
					"GO" + "\n" +

					"! Sensitivity Analisys;" + "\n" +					
					"DIVERT " + ARQUIVO_RANGE_LINGO+"_"+RANK + "\n" +
					"RANGE" + "\n" +

					"! Open a file;" + "\n" +
					"DIVERT " + ARQUIVO_RESULT_LINGO+"_"+RANK + "\n" +
					"! Send solution to the file;" + "\n" +
					"SOLUTION" + "\n" +
					"! Close solution file;" + "\n" +
					"RVRT" + "\n" +
					"! Quit LINGO;" + "\n" +
					"QUIT" + "\n");

			//if (DEBUG_PROGRAM)
			//	System.out.println(modeloLingo.toString());

			//Grava em arquivo o modelo Lingo
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_MODELO_LINGO+"_"+RANK,false));			
				out.write(modeloLingo.toString());
				out.close();
			} catch(Exception e){
				System.out.println("Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Excecao 14 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK + "\n" + e.getMessage());
		}//fim catch

	}//fim gerarModeloLingo
	
	public boolean repetidoRouter(int indiceRouter, StringBuffer listaRouters){

		boolean achou=false;

		StringTokenizer token = new StringTokenizer(listaRouters.toString(), " ");
		while(token.hasMoreTokens()&&!achou){
			if (indiceRouter==Integer.parseInt(token.nextToken().toString()))
				achou=true;			
		}//fim while

		return achou;

	}//fim repetidoRouter
	
	public void executarModeloLingo(){

		System.out.println("Processo["+RANK+"]: "+"--Executar modelo no Lingo--");

		/*//Muda a data para executar o Lingo				
		try{
			String comando = "/home/lucio/mirror/mudarDataParaLingo.sh";
			Process p = Runtime.getRuntime().exec(comando);
			p.waitFor();			
		} catch (Exception e){
			System.out.println("Processo["+RANK+"]: "+"Excecao ao mudar a data do servidor para o Lingo.");
		}//fim catch
		 */

		String comando = "/home/lucio/mirror/executarModeloLingo.sh " + ARQUIVO_MODELO_LINGO+"_"+RANK;

		try {
			String line;
			Process p = Runtime.getRuntime().exec(comando);
			BufferedReader bri = new BufferedReader
			(new InputStreamReader(p.getInputStream()));
			BufferedReader bre = new BufferedReader
			(new InputStreamReader(p.getErrorStream()));
			while ((line = bri.readLine()) != null) {
				System.out.println(line);
			}
			bri.close();
			while ((line = bre.readLine()) != null) {
				System.out.println(line);
			}
			bre.close();
			p.waitFor();
			System.out.println("Processo["+RANK+"]: "+"Fim da execucao do modelo Lingo.");
		} catch (Exception e){
			System.out.println("Processo["+RANK+"]: "+"Excecao: Erro ao executar o programa Lingo.");
		}//fim catch

	}//fim executarModeloLingo
	
	public void realizarParserResultLingo(){

		String REGEX = "";
		Matcher matcher;
		Pattern pattern;

		//O indice dos servidores comeca a partir do ultimo indice dos nodos da topologia original
		//Utilizo apenas o campo Servers do arquivo

		try
		{
			//Guarda as informacoes do arquivo
			StringBuffer info = new StringBuffer();
			//Linhas do arquivo
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK));
			//Cabecalho
			linha=file.readLine();			
			StringTokenizer token = new StringTokenizer(linha, "( )");
			token.nextToken();
			//System.out.println("\nnumNodesOriginal: "+Integer.parseInt(token.nextToken()));			
			int numNodesOriginal = Integer.parseInt(token.nextToken());
			//Salta as proximas linhas do cabecalho
			for (int i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for

			//get the number of nodes
			linha = file.readLine();
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numNodes = Integer.parseInt(token.nextToken());
			//Guarda todas as informacoes dos nodos
			//System.out.println("\nnumNodes: " + numNodes);
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");
			for (int i=0; i<numNodes; i++){				
				info.append(file.readLine()+"\n");
			}//fim while

			//Adquire o numero de Edges
			for (int i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for			
			linha = file.readLine();
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numEdges = Integer.parseInt(token.nextToken());
			//Guarda todas as informacoes dos Edges
			//System.out.println("numEdges: " + numEdges);
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");
			for (int i=0; i<numEdges; i++){				
				info.append(file.readLine()+"\n");
			}//fim while

			//Adquire o numero de datacenters do arquivo
			for (int i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for			
			linha = file.readLine();
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numDatacenters = Integer.parseInt(token.nextToken());
			//System.out.println("numDatacenters: " + numDatacenters);
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");
			//Salta o campo Datacenters
			for (int i=0; i<numDatacenters; i++){				
				info.append(file.readLine()+"\n");
			}//fim while

			//Adquire o numero de servidores do arquivo
			for (int i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for			
			linha = file.readLine();			
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numServers = Integer.parseInt(token.nextToken());
			//System.out.println("numServers: " + numServers);

			//Guarda todas as informacoes dos servidores
			int numCamposServ=13;
			String [][] S_ARQUIVO = new String[numServers][numCamposServ];
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");			
			for(int i=0; i<numServers; i++){
				linha = file.readLine();
				info.append(linha+"\n");
				token = new StringTokenizer(linha, " ");
				for(int j=0; j<numCamposServ; j++){
					S_ARQUIVO[i][j]=token.nextToken()+"";
				}//fim for
			}//fim for

			//exibir(S_ARQUIVO);

			//Adquire o numero de VMs do arquivo
			for (int i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for						
			linha = file.readLine();			
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numVMs = Integer.parseInt(token.nextToken());
			//System.out.println("numVMs: " + numVMs);

			//Guarda todas as informacoes das VMs
			int numCamposVMs=10;
			String [][] VM_ARQUIVO = new String[numVMs][numCamposVMs];
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");
			for(int i=0; i<numVMs; i++){
				linha = file.readLine();
				info.append(linha+"\n");
				token = new StringTokenizer(linha, " ");
				for(int j=0; j<numCamposVMs; j++){
					VM_ARQUIVO[i][j]=token.nextToken()+"";
				}//fim for
			}//fim for

			//exibir(VM_ARQUIVO);

			//Adquire o nodo destino do arquivo
			for (int i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for						
			linha = file.readLine();			
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int nodoDestino = Integer.parseInt(token.nextToken());
			//System.out.println("nodoDestino: " + nodoDestino);

			//Cria o vetor para			
			//Guardar a quantidade de alocacoes por indice de servidor
			//Como o indice do servidor pode nao comecar em 0,
			//utilizo o primeiroIndice+NumServers+NumDatacenters			
			//System.out.println("primeiroIndice: " + S_ARQUIVO[0][0]);
			//int [][] result = new int[Integer.parseInt((S_ARQUIVO[0][I_SERVER]))+numServers+numDatacenters][1];
			int [][] result = new int[Integer.parseInt((S_ARQUIVO[0][I_SERVER]))+numServers*numDatacenters][1];

			//Inicializa a matriz
			inicializarMatriz(result);

			//System.out.println(result.length);

			int totalAlocacoes=0;			
			try {
				//Abre o arquivo com o resultado Lingo			
				BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO+"_"+RANK));
				String linhaResult=arquivoResult.readLine();
				StringTokenizer t1;
				StringTokenizer t2;
				String alocacao="";
				double valorAlocacao;
				String primeiroElemento="";
				String t2_vm="";
				String t2_server="";				
				while (linhaResult!=null){
					//Ex.:  linhaResult = "A_218_131       0.000000           3.200000"
					t1 = new StringTokenizer(linhaResult, " ");
					if (t1.hasMoreTokens()){
						primeiroElemento = t1.nextToken();
						//System.out.println(primeiroElemento);
						REGEX = "A\\_(.*)\\_(.*)";
						pattern = Pattern.compile(REGEX);
						matcher = pattern.matcher(primeiroElemento);
						if (matcher.find()){
							//System.out.println("["+linhaResult+"]");
							t2 = new StringTokenizer(primeiroElemento, "_");
							t2.nextToken();
							t2_vm = t2.nextToken();
							t2_server = t2.nextToken();
							//System.out.println("[VM: "+t2_vm+"]");
							//System.out.println("[Server: "+t2_server+"]");						

							alocacao = t1.nextToken();

							valorAlocacao=Double.parseDouble(alocacao);
							//if (alocacao.equals("1.000000")){
							if (valorAlocacao>0.5){ //Por conta de nao ser mais um modelo com variaveis binarias para a alocacao
								//Incrementa no mesmo indice do servidor
								result[Integer.parseInt(t2_server)][0]++;
								totalAlocacoes++;
							}//fim if

						}//fim if

					}//fim if

					linhaResult=arquivoResult.readLine();

				}//fim while

				//close the file
				arquivoResult.close();			
			} catch(Exception e){
				System.out.println("Processo["+RANK+"]: "+"Excecao 1 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO+"_"+RANK + "\n" + e.getMessage());
			}//fim catch

			//Busca o consumo de energia por servidor
			//Cria o vetor para			
			//Guardar o consumo de energia por indice de servidor
			//Como o indice do servidor pode nao comecar em 0,
			//utilizo o primeiroIndice+NumServers+NumDatacenters			
			//System.out.println("primeiroIndice: " + S_ARQUIVO[0][0]);
			//String [][] resultConsumoEnergia = new String[Integer.parseInt((S_ARQUIVO[0][I_SERVER]))+numServers+numDatacenters][1];
			String [][] resultConsumoEnergia = new String[Integer.parseInt((S_ARQUIVO[0][I_SERVER]))+numServers*numDatacenters][1];

			//Inicializa a matriz
			int i=0;
			int j=0;
			while(i<resultConsumoEnergia.length){
				j=0;
				while(j<resultConsumoEnergia[i].length){
					resultConsumoEnergia[i][j]="0";
					j++;
				}//fim while
				i++;
			}//fim while		

			//Consumo de energia dos servidores
			CONSUMO_ENERGIA_SERVIDORES=0;		
			try {
				//Abre o arquivo com o resultado Lingo			
				BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO+"_"+RANK));
				String linhaResult=arquivoResult.readLine();
				StringTokenizer t1;
				StringTokenizer t2;
				String consumoEnergia="";				
				String primeiroElemento="";
				String t2_server="";				
				while (linhaResult!=null){
					//Ex.:  linhaResult = "CONSUMOENERGIASERVIDOR_131       90.000000           0.000000"
					t1 = new StringTokenizer(linhaResult, " ");
					if (t1.hasMoreTokens()){
						primeiroElemento = t1.nextToken();
						//Adquire o primeiro elemento da linha
						//System.out.println(primeiroElemento);
						REGEX = "CONSUMOENERGIASERVIDOR\\_(.*)";
						pattern = Pattern.compile(REGEX);
						matcher = pattern.matcher(primeiroElemento);
						if (matcher.find()){
							//System.out.println("["+linhaResult+"]");
							t2 = new StringTokenizer(primeiroElemento, "_");
							//string CONSUMOENERGIASERVIDOR
							t2.nextToken();		
							//string com o indice do servidor
							t2_server = t2.nextToken();
							//System.out.println("[Server: "+t2_server+"]");						
							//Segundo elemento da linha
							consumoEnergia = t1.nextToken();							
							//Insere no mesmo indice do servidor
							resultConsumoEnergia[Integer.parseInt(t2_server)][0]=String.format("%1$,.2f", Double.parseDouble(consumoEnergia));

							CONSUMO_ENERGIA_SERVIDORES+=Double.parseDouble(consumoEnergia);

						}//fim if

					}//fim if

					linhaResult=arquivoResult.readLine();

				}//fim while

				//close the file
				arquivoResult.close();			
			} catch(Exception e){
				System.out.println("Processo["+RANK+"]: "+"Excecao 9 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO+"_"+RANK + "\n" + e.getMessage());
			}//fim catch

			//Consumo de energia dos roteadores
			//numRouters<=numEdges
			String [] resultConsumoEnergiaRouter = new String[numEdges];
			CONSUMO_ENERGIA_ROUTERS=0;
			try {
				//Abre o arquivo com o resultado Lingo			
				BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO+"_"+RANK));
				String linhaResult=arquivoResult.readLine();
				StringTokenizer t1;
				StringTokenizer t2;
				String consumoEnergia="";				
				String primeiroElemento="";
				String t2_router="";				
				while (linhaResult!=null){
					//Ex.:  linhaResult = "CONSUMOENERGIAROUTER_131       90.000000           0.000000"
					t1 = new StringTokenizer(linhaResult, " ");
					if (t1.hasMoreTokens()){
						primeiroElemento = t1.nextToken();
						//Adquire o primeiro elemento da linha
						//System.out.println(primeiroElemento);
						REGEX = "CONSUMOENERGIAROUTER\\_(.*)";
						pattern = Pattern.compile(REGEX);
						matcher = pattern.matcher(primeiroElemento);
						if (matcher.find()){
							//System.out.println("["+linhaResult+"]");
							t2 = new StringTokenizer(primeiroElemento, "_");
							//string CONSUMOENERGIAROUTER
							t2.nextToken();		
							//string com o indice do router
							t2_router = t2.nextToken();
							//System.out.println("[Server: "+t2_server+"]");						
							//Segundo elemento da linha
							consumoEnergia = t1.nextToken();	
							//Insere no mesmo indice do router
							if(Double.parseDouble(consumoEnergia) > 0){
								resultConsumoEnergiaRouter[Integer.parseInt(t2_router)]=1+"";
								CONSUMO_ENERGIA_ROUTERS++;
							}
							else
								resultConsumoEnergiaRouter[Integer.parseInt(t2_router)]=0+"";

						}//fim if

					}//fim if

					linhaResult=arquivoResult.readLine();

				}//fim while

				//close the file
				arquivoResult.close();			
			} catch(Exception e){
				System.out.println("Processo["+RANK+"]: Excecao 10 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO+"_"+RANK + "\n" + e.getMessage());
			}//fim catch
			
			//Grava os resultados
			try{
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_RESULT_QTDE_SERV+"_"+RANK,false));
				out.write("Parser do arquivo: " + ARQUIVO_RESULT_LINGO+"_"+RANK + "\n");
				out.write("Arquivo da topologia: " + ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK + "\n");
				out.write("---\n");
				//out.write("Quantidade de iteracoes: " + quantidadeIteracoes + "\n");
				out.write("---\n");
				//out.write("Tempo de execucao (s): " + (tempoFim-tempoInicio)/1000 + "\n");
				out.write("---\n");
				//out.write("Tamanho da populacao: " + TAM_POPULACAO + "\n");
				out.write("---\n");				
				out.write("ConfiguracoesGlobais\n");
				out.write("---\n");
				out.write("NodoDestino: " + nodoDestino + "\n");
				out.write("NumeroNodos: " + numNodes + "\n");
				out.write("NumeroEdges: " + numEdges + "\n");
				out.write("NumeroDatacenters: " + numDatacenters + "\n");
				out.write("NumeroServidores: " + numServers + "\n");
				out.write("NumeroVMs: " + numVMs + "\n");				
				out.write("---\n");
				out.write("NumeroVMsAlocadas: " + totalAlocacoes + "\n");
				System.out.println("Processo["+RANK+"]: "+"NumeroVMsAlocadas: " + totalAlocacoes + "\n");
				/*out.write("---\n");
				out.write("ConfiguracaoOriginalServidores\n");
				out.write("---\n");
				out.write("CPU: " + SERVER_HUGE_CPU + "\n");
				out.write("RAM: " + SERVER_HUGE_RAM + "\n");
				out.write("DISK: " + SERVER_HUGE_DISK + "\n");
				out.write("BW: " + SERVER_HUGE_BW + "\n");
				out.write("CostCPU: " + SERVER_COST_HUGE_CPU + "\n");
				out.write("CostRAM: " + SERVER_COST_HUGE_RAM + "\n");
				out.write("CostDISK: " + SERVER_COST_HUGE_DISK + "\n");
				out.write("CostBW: " + SERVER_COST_HUGE_BW + "\n");
				out.write("---\n");
				out.write("ConfiguracaoOriginalVMs\n");
				out.write("---\n");
				out.write("CPU: " + VM_SMALL_CPU + "\n");
				out.write("RAM: " + VM_SMALL_RAM + "\n");
				out.write("DISK: " + VM_SMALL_DISK + "\n");
				out.write("BW: " + VM_SMALL_BW + "\n");
				out.write("---\n");
				*/
				
				i=0;
				int numRoutersLigados=0;
				StringBuffer camposRouters = new StringBuffer();
				//Como numRouters<=numEdges, nem todos os campos podem ter sido preenchidos
				while (i<resultConsumoEnergiaRouter.length){
					if(resultConsumoEnergiaRouter[i]!=null){
						camposRouters.append(i + " " + resultConsumoEnergiaRouter[i] + "\n");
						if (resultConsumoEnergiaRouter[i].equals("1"))
							numRoutersLigados++;
					}
					else
						camposRouters.append(i + " " + 0 + "\n");
					i++;
				}//fim while

				out.write("---\n");				
				out.write("numRouters numRoutersLigados consumoEnergiaRouters\n");
				//Um border router por datacenter
				out.write(numNodesOriginal+numDatacenters + " " + numRoutersLigados + " " + CONSUMO_ENERGIA_ROUTERS + "\n");

				out.write("---\n");
				out.write("consumoEnergiaServidores\n");
				out.write(CONSUMO_ENERGIA_SERVIDORES + "\n");


				out.write("---\n");
				out.write("indiceRouter ConsumoEnergiaRouters\n");
				out.write(camposRouters.toString());

				out.write("---\n");
				out.write("IndiceDatacenter IndiceServidor NumeroVMsAlocadasPorServidor ConsumoEnergiaServ\n");
				i=0;
				while (i<result.length){
					out.write(buscarIndiceDatacenter(S_ARQUIVO,i) + " " + i + " " + result[i][0] + " " + resultConsumoEnergia[i][0] + "\n");
					i++;
				}//fim while				

				out.write("---\n");
				out.write("Conteudo do Arquivo da Topologia\n");
				out.write("---\n");
				out.write(info.toString());
				out.write("---\n");

				//Fecha o arquivo
				out.close();
			} catch(Exception e){
				System.out.println("Processo["+RANK+"]: "+"Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Processo["+RANK+"]: "+"Excecao 2 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK + "\n" + e.getMessage());
		}//fim catch

	}//fim parserResultLingo
	
	public void gerarModeloNS2(){	

		System.out.println("Processo["+RANK+"]: "+"\n--Gerar Modelo NS2--");

		//Le o conteudo do arquivo da topologia
		String REGEX = "";
		Matcher matcher;
		Pattern pattern;

		//O indice dos servidores comeca a partir do ultimo indice dos nodos da topologia original
		//Utilizo apenas o campo Servers do arquivo

		try
		{
			//Guarda as informacoes do arquivo
			int i=0;
			int j=0;
			StringBuffer info = new StringBuffer();

			//Guardo as informacoes da banda atribuida para cada link
			StringBuffer banda = new StringBuffer();
			banda.append("From--To Bandwidth\n");

			//Linhas do arquivo
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK));
			//Cabecalho
			linha = file.readLine();
			StringTokenizer token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numNodosOriginal = Integer.parseInt(token.nextToken());
			//System.out.println("NumNodosOriginal: "+ numNodosOriginal);
			token.nextToken();
			int numEdgesOriginal = Integer.parseInt(token.nextToken());
			//System.out.println("NumEdgesOriginal: " + numEdgesOriginal);
			info.append(linha+"\n");

			for (i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for

			//get the number of nodes
			linha = file.readLine();
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numNodes = Integer.parseInt(token.nextToken());
			//Guarda todas as informacoes dos nodos
			//System.out.println("numNodes: " + numNodes);
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");
			for (i=0; i<numNodes; i++){				
				info.append(file.readLine()+"\n");
			}//fim while

			//Adquire o numero de Edges
			for (i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for			
			linha = file.readLine();
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numEdges = Integer.parseInt(token.nextToken());
			//Guarda todas as informacoes dos Edges
			//System.out.println("numEdges: " + numEdges);
			//EDGE[0][EdgeId From To Length Delay Bandwidth ASfrom ASto Type Other]
			//    [1][EdgeId From To Length Delay Bandwidth ASfrom ASto Type Other]
			//    ...
			int campoEdgeId=0;
			int campoEdgeFrom=1;
			int campoEdgeTo=2;
			int campoEdgeLength=3;
			int campoEdgeDelay=4;
			int campoEdgeBw=5;
			int campoEdgeAsFrom=6;
			int campoEdgeAsTo=7;
			int campoEdgeType=8;
			int campoEdgeOther=9;

			String [][] EDGES_ARQUIVO = new String[NUM_EDGES][10];
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");			
			for (i=0; i<NUM_EDGES; i++){
				j=0;

				linha = file.readLine();

				token = new StringTokenizer(linha,"\t");
				//id (0)
				EDGES_ARQUIVO[i][j++]=token.nextToken();
				//from (1)
				EDGES_ARQUIVO[i][j++]=token.nextToken();
				//to (2)
				EDGES_ARQUIVO[i][j++]=token.nextToken();
				//Length (3) 
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Delay (4)
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Bandwidth (5) (Nao posso alterar a bandwidth pq ela jah foi contratada)
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//ASfrom (6)
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Asto (7)
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Type (8)
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Other (9)
				EDGES_ARQUIVO[i][j++] = token.nextToken();

			}//fim while
			//Atualiza as informacoes dos custos dos enlaces
			i=0;
			j=0;
			linha="";
			while(i<EDGES_ARQUIVO.length){
				j=0;				
				while(j<EDGES_ARQUIVO[i].length){
					linha+=EDGES_ARQUIVO[i][j]+"\t";
					j++;
				}//fim while
				linha+="\n";
				i++;
			}//fim while
			info.append(linha); //sem "\n" aqui
			/*			//Para as demais edges
			while (i<numEdges){
				info.append(file.readLine()+"\n");
				i++;
			}//fim while
			 */
			//Adquire o numero de datacenters do arquivo
			for (i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for			
			linha = file.readLine();
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numDatacenters = Integer.parseInt(token.nextToken());
			//System.out.println("numDatacenters: " + numDatacenters);
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");
			//Salta o campo Datacenters
			for (i=0; i<numDatacenters; i++){				
				info.append(file.readLine()+"\n");
			}//fim while

			//Adquire o numero de servidores do arquivo
			for (i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for			
			linha = file.readLine();			
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numServers = Integer.parseInt(token.nextToken());
			//System.out.println("numServers: " + numServers);

			//Guarda todas as informacoes dos servidores
			int numCamposServ=13;
			String [][] S_ARQUIVO = new String[numServers][numCamposServ];
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");			
			for(i=0; i<numServers; i++){
				linha = file.readLine();
				info.append(linha+"\n");
				token = new StringTokenizer(linha, " ");
				for(j=0; j<numCamposServ; j++){
					S_ARQUIVO[i][j]=token.nextToken()+"";
				}//fim for
			}//fim for

			//exibir(S_ARQUIVO);

			//Adquire o numero de VMs do arquivo
			for (i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for						
			linha = file.readLine();			
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numVMs = Integer.parseInt(token.nextToken());
			//System.out.println("numVMs: " + numVMs);

			//Guarda todas as informacoes das VMs
			int numCamposVMs=10;
			String [][] VM_ARQUIVO = new String[numVMs][numCamposVMs];
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");
			for(i=0; i<numVMs; i++){
				linha = file.readLine();
				info.append(linha+"\n");
				token = new StringTokenizer(linha, " ");
				for(j=0; j<numCamposVMs; j++){
					VM_ARQUIVO[i][j]=token.nextToken()+"";
				}//fim for
			}//fim for

			//exibir(VM_ARQUIVO);

			//Adquire o nodo destino do arquivo
			for (i=0; i<2; i++) {
				info.append(file.readLine()+"\n");
			}//fim for						
			linha = file.readLine();			
			info.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int nodoDestino = Integer.parseInt(token.nextToken());
			//System.out.println("nodoDestino: " + nodoDestino);

			int indiceNodosNS2=0;

			StringBuffer conteudoNS2 = new StringBuffer();
			conteudoNS2.append("\n#---Modelo NS2---");
			conteudoNS2.append("\nset NODO_DESTINO ");
			conteudoNS2.append(nodoDestino);
			conteudoNS2.append("\nset NUM_NODES_ORIGINAL ");
			conteudoNS2.append(numNodosOriginal);
			conteudoNS2.append("\n#set NUM_NODES ");
			conteudoNS2.append(numNodes);
			conteudoNS2.append("\nset NUM_DATACENTERS ");
			conteudoNS2.append(numDatacenters);
			conteudoNS2.append("\nset NUM_SERVERS ");
			conteudoNS2.append(numServers);
			conteudoNS2.append("\nset NUM_VMS ");
			conteudoNS2.append(numVMs);
			conteudoNS2.append("\n\n#Cria um objeto para a simulacao");
			conteudoNS2.append("\nset ns [new Simulator]");
			conteudoNS2.append("\n#Abre o arquivo de rastreamento do NAM");
			conteudoNS2.append("\nset nf [open " + ARQUIVO_NAM_NS2+"_"+RANK + " w]");
			conteudoNS2.append("\n$ns namtrace-all $nf");

			conteudoNS2.append("\n\n#Abre o arquivo de rastreamento para guardar os eventos");
			conteudoNS2.append("\nset nd [open " + ARQUIVO_RESULT_NS2+"_"+RANK + " w]");
			conteudoNS2.append("\n$ns trace-all $nd");
			conteudoNS2.append("\n");

			conteudoNS2.append("\n#Define um procedimento de finish");
			conteudoNS2.append("\nproc finish {} {");
			conteudoNS2.append("\n   global ns nf nd");
			conteudoNS2.append("\n   $ns flush-trace");			
			conteudoNS2.append("\n   close $nf");
			conteudoNS2.append("\n   close $nd");
			conteudoNS2.append("\n   #exec nam " + ARQUIVO_NAM_NS2+"_"+RANK + " &");
			conteudoNS2.append("\n   exit 0");
			conteudoNS2.append("\n}");			
			conteudoNS2.append("\n");
			conteudoNS2.append("\n#Cria os nodos da topologia da rede original");
			conteudoNS2.append("\nfor {set j 0} {$j < $NUM_NODES_ORIGINAL} {incr j} {");
			conteudoNS2.append("\n   set n($j) [$ns node]");
			//Por conta do laco para a criacao de nodos
			indiceNodosNS2=numNodosOriginal;
			//conteudoNS2.append("\n   $n($j) color black");
			conteudoNS2.append("\n}");			
			conteudoNS2.append("\n");
			conteudoNS2.append("\n#Cria a topologia da rede");
			//Le as primeiras entradas do campo edges do arquivo de topologia			
			i=0;
			while (i<EDGES_ARQUIVO.length){

				//Crio apenas os links que nao forem servidores (nodos originais + borderRouters)
				//System.out.println("EdgeType: " + EDGES[i][3]);
				//n(0) -- n(1) -- n(111) -- ...
				if (!EDGES_ARQUIVO[i][campoEdgeType].equals("SERVER")){

					conteudoNS2.append("\nputs \"Link n("+EDGES_ARQUIVO[i][campoEdgeFrom]+")--n("+EDGES_ARQUIVO[i][campoEdgeTo]+")"+"\";flush stdout;");
					//Com a adicao de roteadores de borda e servidores
					//eh necessario acrescentar nodos ah topologia original
					//Campo indice
					if (Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeFrom])>=numNodosOriginal){
						conteudoNS2.append("\n   #IndiceNS2: n("+ indiceNodosNS2 +")");
						conteudoNS2.append("\n   set n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") [$ns node]");						
						conteudoNS2.append("\n$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") color blue");
						conteudoNS2.append("\n$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") shape hexagon");
						indiceNodosNS2++;

					}//fim if
					conteudoNS2.append("\n$ns duplex-link " +
							//From
							"$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") " +
							//To
							"$n("+EDGES_ARQUIVO[i][campoEdgeTo]+") " + 
							//Bandwidth
							EDGES_ARQUIVO[i][campoEdgeBw] + "mb " +
							//Outros parametros
					"10ms DropTail");					

					//Monitora a fila nos links (padrao: 20 pacotes)
					conteudoNS2.append("\n$ns duplex-link-op " +
							//From
							"$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") " +
							//To
							"$n("+EDGES_ARQUIVO[i][campoEdgeTo]+") " +
					"queuePos 0.5");

					//Adiciona o custo do enlace
					conteudoNS2.append("\n$ns cost " +
							//From
							"$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") " +
							//To
							"$n("+EDGES_ARQUIVO[i][campoEdgeTo]+") " +
							EDGES_ARQUIVO[i][campoEdgeLength]);

					//Guarda as informacoes da banda atribuida ao link (para gravar em arquivo)
					//Campo Type
					if (EDGES_ARQUIVO[i][campoEdgeType].equals("BORDER_ROUTER"))
						//banda.append("BorderRouter_"+(indiceNodosNS2+1)+"--"+EDGES[i][1]+" "+EDGES[i][2]+"\n");
						banda.append("BorderRouter_"+i+"--"+EDGES_ARQUIVO[i][campoEdgeTo]+" "+EDGES_ARQUIVO[i][campoEdgeBw]+"\n");
					else
						banda.append(EDGES_ARQUIVO[i][campoEdgeFrom]+"--"+EDGES_ARQUIVO[i][campoEdgeTo]+" "+EDGES_ARQUIVO[i][campoEdgeBw]+"\n");					

				}//fim if
				i++;
			}//fim while

			//Cria o nodo destino
			conteudoNS2.append("\n\n#Nodo sink (destino)");
			conteudoNS2.append("\nset sink [new Agent/Null]");
			conteudoNS2.append("\n$ns attach-agent $n($NODO_DESTINO) $sink");			


			//Faz o parser do resultado da alocacao
			try {
				//Abre o arquivo com o resultado Lingo			
				BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO+"_"+RANK));
				String linhaResult=arquivoResult.readLine();
				StringTokenizer t1;
				StringTokenizer t2;
				String alocacao="";
				String primeiroElemento="";
				String t2_vm="";
				String t2_server="";

				//Agrupa os fluxos gerados pela VM
				int [] fluxoGeradoAglomeradoVMs = new int[numNodes];
				int fluxoGeradoVM=0;				
				boolean achouVM=false;
				boolean achouServer=false;

				while (linhaResult!=null){
					//Ex.:  linhaResult = "A_218_131       0.000000           3.200000"
					t1 = new StringTokenizer(linhaResult, " ");
					if (t1.hasMoreTokens()){
						primeiroElemento = t1.nextToken();
						//System.out.println(primeiroElemento);
						REGEX = "A\\_(.*)\\_(.*)";
						pattern = Pattern.compile(REGEX);
						matcher = pattern.matcher(primeiroElemento);
						if (matcher.find()){
							//System.out.println("["+linhaResult+"]");
							t2 = new StringTokenizer(primeiroElemento, "_");
							t2.nextToken();
							t2_vm = t2.nextToken();
							t2_server = t2.nextToken();
							//System.out.println("[VM: "+t2_vm+"]");
							//System.out.println("[Server: "+t2_server+"]");						

							alocacao = t1.nextToken();
							//System.out.println("[Alocacao: "+alocacao+"]");

							if (alocacao.equals("1.000000")){

								//Consulta no arquivo da topologia do datacenter
								//qual border router estah associado ah alocacao da VM
								//
								//Ex.: a_2_100	1.000000
								//t2_vm = 2
								//t2_server = 100
								//
								//vm 2 alocada no servidor 100

								//Adquire o fluxo gerado pela VM
								i=0;
								achouVM=false;
								while(i<VM_ARQUIVO.length&&!achouVM){
									//vmid
									if (VM_ARQUIVO[i][0].equals(t2_vm)){
										achouVM=true;
										fluxoGeradoVM=Integer.parseInt(VM_ARQUIVO[i][6]);
									}//fim if
									i++;
								}//fim while

								//t2_server=100 estah ligado a um border router
								//Varre a lista de arestas para descobrir em qual border router t2_server=100 estah conectado
								i=0;
								achouServer=false;
								while(i<EDGES_ARQUIVO.length&&!achouServer){

									//EDGES = [0][from to bw     type]
									//        [1][100  60  10240  SERVER]   <-- Servidor 100 estah conectado ao border router 60
									//        ...
									//
									//achou o servidor (campo 'from')
									if (EDGES_ARQUIVO[i][campoEdgeFrom].equals(t2_server)){

										//A ideia eh criar um link posterior com o somatorio 
										//do fluxo gerado pelas VMs associadas ao nodo 'to'
										//
										//Ex.:fluxoGeradoAglomeradoVMs['to']+=fluxoGeradoVM
										//
										////O indice do aglomeradoVMs corresponde ao indice do border router
										fluxoGeradoAglomeradoVMs[Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeTo])]+=fluxoGeradoVM;

										achouServer=true;
									}//fim if

									i++;
								}//fim while

							}//fim if

						}//fim if

					}//fim if

					linhaResult=arquivoResult.readLine();

				}//fim while

				String nodosComAglomerados="";

				//Cria o link entre o aglomerado de VMs e os roteadores de borda
				conteudoNS2.append("\n\n#Link aglomeradoVms com roteadores de borda");
				i=0;
				while(i<fluxoGeradoAglomeradoVMs.length){

					if(fluxoGeradoAglomeradoVMs[i]!=0){
						//n(0) -- n(1) -- n(111) -- ...
						//O indice do aglomeradoVMs corresponde ao indice do border router
						conteudoNS2.append("\n\nputs \"Link n("+indiceNodosNS2+")--n("+i+")"+"\";flush stdout;");
						//Cria a relacao entre o aglomerado de VMs e o roteador de borda
						conteudoNS2.append("\nset n("+indiceNodosNS2+") [$ns node]");
						conteudoNS2.append("\n$n("+indiceNodosNS2+") color black");
						conteudoNS2.append("\n$n("+indiceNodosNS2+") shape square");
						conteudoNS2.append("\n$ns duplex-link $n("+indiceNodosNS2+") " +
								"$n("+i+") " +
								fluxoGeradoAglomeradoVMs[i] + "mb 10ms DropTail");
						//Guarda a informacao da banda atribuida ao link (para guardar em arquivo)
						banda.append("n_"+(indiceNodosNS2)+"--"+"__"+" "+fluxoGeradoAglomeradoVMs[i]+"\n");											
						//nodosComAglomerados += i + " ";						

						conteudoNS2.append("   \n#Cria um agente UDP e atribui ao aglomerado");
						conteudoNS2.append("   \nset udp("+i+") [new Agent/UDP]");
						conteudoNS2.append("   \n$ns attach-agent $n("+indiceNodosNS2+") $udp("+i+")");
						conteudoNS2.append("   \n#Cria um trafego CBR e atribui ao agent UDP");
						conteudoNS2.append("   \nset cbr("+i+") [new Application/Traffic/CBR]");
						conteudoNS2.append("   \n$cbr("+i+") set packetSize_ 1500");
						conteudoNS2.append("   \n#Fluxo ficticio para ocupar todo o link");
						conteudoNS2.append("   \n$cbr("+i+") set rate_ " + fluxoGeradoAglomeradoVMs[i] + "mb");
						conteudoNS2.append("   \n$cbr("+i+") attach-agent $udp("+i+")");
						conteudoNS2.append("   \n#Conecta o nodo ao agente sink do nodo destino");
						conteudoNS2.append("   \n$ns connect $udp("+i+") $sink");
						conteudoNS2.append("   \n#Identifica o fluxo do aglomeradoVMs");
						conteudoNS2.append("   \n$udp("+i+") set fid_ "+i+"");	
						//Cor do trafego (passo1)
						conteudoNS2.append("   \n$udp("+i+") set class_ 1");
						conteudoNS2.append("\n#Inicio da submissao do trafego");
						conteudoNS2.append("\n$ns at 0.0 \"$cbr("+i+") start\"");

						//Um novo nodo foi criado no NS2
						indiceNodosNS2++;

					}//fim if

					i++;
				}//fim while				

				//close the file
				arquivoResult.close();			
			} catch(Exception e){
				System.out.println("Processo["+RANK+"]: "+"Excecao 3 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO+"_"+RANK + "\n" + e.getMessage());
			}//fim catch

			//Roteamento dinamico (estado do enlace)
			//conteudoNS2.append("\n\n$ns rtproto LS");
			//Cor do trafego (passo2)
			conteudoNS2.append("\n\n$ns color 1 Green");
			conteudoNS2.append("\n\n$ns at 1.0 \"finish\"");			
			conteudoNS2.append("\n$ns run\n");

			//System.out.println(conteudoNS2);

			//Grava o conteudo do modelo para o NS2
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_MODELO_NS2+"_"+RANK,false));			
				out.write(conteudoNS2.toString());
				out.close();
			} catch(Exception e){
				System.out.println("Processo["+RANK+"]: "+"Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

			//Grava a banda dos links em arquivo
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_PARSER_BANDA_LINKS+"_"+RANK,false));			
				out.write(banda.toString());
				out.close();
			} catch(Exception e){
				System.out.println("Processo["+RANK+"]: "+"Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch			

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Processo["+RANK+"]: "+"Excecao 5 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER+"_"+RANK + "\n" + e.getMessage());
		}//fim catch

	}//fim gerarModeloNS2
	
	public void executarModeloNS2(){

		System.out.println("Processo["+RANK+"]: "+"\n--Executar o modelo no NS2--");

		String comando = "/home/lucio/mirror/executarModeloNS2.sh " + ARQUIVO_MODELO_NS2+"_"+RANK;

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
			System.out.println("Processo["+RANK+"]: "+"Fim da execucao do modelo no NS2.");
		} catch (Exception e){
			System.out.println("Processo["+RANK+"]: "+"Excecao: Erro ao executar o modelo NS2. " + e.getMessage());
		}//fim catch

	}//fim executarModeloNS2
	
	public String buscarIndiceDatacenter(String [][] S_ARQUIVO, int indiceServidor){

		//Busca o indice do servidor 
		String result="-";
		boolean achou=false;
		int i=0;
		while (i<S_ARQUIVO.length&&!achou){
			//O indice do servidor precisa ser buscado
			//Note que o indice pode nao comecar em zero
			//Exemplo: S_ARQUIVO[0][serverid]
			//                     [datacenterid]
			//                     ...
			if (S_ARQUIVO[i][I_SERVER].equals(indiceServidor+"")){
				achou=true;
				result=S_ARQUIVO[i][I_SERVER_DATACENTER];
			}//fim if			
			i++;
			//System.out.print(i + " ");
		}//fim while		
		return result;

	}//fim buscarIndiceDatacenter
	
	public String dec2bin( int dec, int NUM_BITS ) {  
		String result = "";  

		int count=0;
		while( dec > 0 ) {  
			result = (dec & 1) + result;  
			dec >>= 1;
			count++;
		}//fim while
		//Incrementa o numero de bits
		String aux="";
		while(count<NUM_BITS){
			aux += "0";
			count++;
		}//fim while
		result = aux + result;     

		return result;  
	}//fim dec2bin

	public int bin2dec( String bin ) {  
		int i, result = 0;  

		for( i = 0; i < bin.length(); i++ ) {  
			result <<= 1;  
			if( bin.charAt( i ) == '1' ) result++;  
		}//fim for

		return result;  
	}//fim bin2dec	
	
	public void inicializarMatriz(int [][] result){

		int i=0;
		while (i<result.length){
			result[i][0]=0;
			i++;
		}//fim while

	}//fim inicializarMatriz
	
	public String removerEspacos(String s) {
		StringTokenizer st = new StringTokenizer(s," ",false);
		String t="";
		while (st.hasMoreElements()) t += st.nextElement();
		return t;
	}
	
	public static void main(String[] args) {

		//Esse eh o tempo por processo pq todo
		//esse codigo sera executado por um unico processo
		Date d1 = new Date();
		long tempoInicio = (long) d1.getTime();
		
		//Utilizo para identificar o processo
		RANK=Integer.parseInt(args[0]);
		new MapReduceAG();
		
		Date d2 = new Date();
		long tempoFim = (long) d2.getTime();		

	}//fim main

	//-------------------------------------------------------------------
	//Classe interna
	public class BRITETopologyPO {

		/**
		 * The number of nodes to be represented by vertices.
		 */
		private int numNodes = 0;

		/**
		 * The number of links to be represented by edges.
		 */
		private int numEdges = 0;

		/**
		 * Array of nodes.
		 */
		private Node nodes[] = null;

		/**
		 * Array of edges.
		 */
		private Edge edges[] = null;	

		public Edge[] getEdges(){
			return edges;
		}//fim getEdges
		
		public Node[] getNodes(){
			return nodes;
		}//fim getNodes
		
		public BRITETopologyPO(String filePath) {

			try
			{
				//try to open the file.
				//BufferedReader file = new BufferedReader(new FileReader("topology.brite"));
				BufferedReader file = new BufferedReader(new FileReader(filePath));
				//build the visualization
				//discard the first 3 lines
				for (int i=0; i<3; i++)
				{
					file.readLine();
				}

				//get the number of nodes
				StringTokenizer t1 = new StringTokenizer(file.readLine(), "( )");
				t1.nextToken();
				numNodes = Integer.parseInt(t1.nextToken());
				//System.out.println("numNodes: " + numNodes);
				nodes = new Node[numNodes];

				//Descricao dos campos
				file.readLine();
				//for the number of nodes
				for (int i=0; i<nodes.length; i++)
				{
					//construct each node
					//
					//Aqui recupera a posicao
					nodes[i] = new Node(file.readLine());
				}

				//discard the two blank lines
				file.readLine( );
				file.readLine( );

				//get the number of edges
				t1 = new StringTokenizer(file.readLine(), "( )");
				t1.nextToken();
				numEdges= Integer.parseInt(t1.nextToken());
				//System.out.println("numEdges: " + numEdges);
				edges = new Edge[numEdges];

				//Descricao dos campos
				file.readLine();
				//for the number of edges
				for (int i=0; i<edges.length; i++)
				{
					//construct each edge
					edges[i] = new Edge(file.readLine());
					//System.out.println("["+i+"][BW:"+edges[i].getBW()+"]");
					//System.out.println("["+i+"][Source:"+edges[i].getSource()+"]");
					//System.out.println("["+i+"][Destination:"+edges[i].getDestination()+"]");
				}

				//close the file
				file.close();
			}

			catch (Exception e)
			{
				e.printStackTrace();
			}
		}//fim construtor

	}//fim classe interna
	
	/**
	 * Representation of an edge from a BRITE topology.
	 */
	public class Edge
	{

		private int edgeID=1;
		
		/**
		 * The source node id.
		 */
		private int sourceID = -1;

		/**
		 * The destination node ID.
		 */
		private int destinationID = -1;

		private double length = -1;
		
		private double delay = -1;
		
		private int asFromNode = -1;
		
		private int asToNode = -1;
		
		/**
		 * The destination Bandwidth.
		 */
		private double bandwidth = -1;	


		/**
		 * Does this link connect between ASs?
		 */
		private boolean connector = false;

		public double getEdgeID(){
			return edgeID;
		}//fim getBW
		
		public int getSource(){
			return sourceID;
		}//fim getSource
		
		public int getDestination(){
			return destinationID;
		}//fim getDestination
		
		public double getLength(){
			return length;
		}//fim getLength
		
		public double getDelay(){
			return delay;
		}//fim getDelay
		
		public double getBW(){
			return bandwidth;
		}//fim getBW
		
		/**
		 * Default constructor
		 */
		public Edge(final String str)
		{
			StringTokenizer t = new StringTokenizer(str);
			//Edge ID
			edgeID = Integer.parseInt(t.nextToken());
			//fromNodeID
			sourceID = Integer.parseInt(t.nextToken());
			//toNodeID
			destinationID = Integer.parseInt(t.nextToken());
			//Length
			length = Double.parseDouble(t.nextToken());
			//Delay
			delay = Double.parseDouble(t.nextToken());
			//Bandwidth
			bandwidth = Double.parseDouble(t.nextToken());
			//ASFromNodeID
			asFromNode = Integer.parseInt(t.nextToken());
			//ASToNodeID
			asToNode = Integer.parseInt(t.nextToken());
			connector = !(asFromNode == asToNode);
		}

		/**
		 * Draw the edge
		 *
		 * @param g Graphics object.
		 * @param nodes The nodes to draw from and to.
		 */
		public void paint(Graphics g, final Node nodes[])
		{
			if (connector)
			{
				g.setColor(Color.red);
			}
			else
			{
				g.setColor(Color.black);
			}
			g.drawLine((int)nodes[sourceID].getX(),
					(int)nodes[sourceID].getY(),
					(int)nodes[destinationID].getX(),
					(int)nodes[destinationID].getY());
			//Fonte: http://stackoverflow.com/questions/5394364/creating-labels-along-with-drawlines
			g.drawString("[bw:" + bandwidth + "]",
					(int)(nodes[sourceID].getX() + nodes[destinationID].getX()) / 2, (int)(nodes[sourceID].getY() + nodes[destinationID].getY()) / 2);
			
		}
	}//fim classe interna
	
	/**
	 * Representation of a node from a BRITE topology.
	 */
	public class Node
	{
		/**
		 * The id of the node.
		 */
		private int id = -1;

		/**
		 * The x coordinate.
		 */
		private double x = -1.0;

		/**
		 * The y coordinate.
		 */
		private double y = -1.0;

		/**
		 * Which Autonimous System (AS) does this belong to?
		 */
		private int AS = -1;	

		/**
		 * Draw different colours for different AS groups.
		 */
		public final Color colours[] = {Color.black, Color.blue, Color.cyan, Color.gray, Color.green, Color.magenta, Color.orange, Color.pink, Color.red, Color.yellow};

		/**
		 * Default constructor
		 */
		public Node(final String str)
		{
			StringTokenizer t = new StringTokenizer(str);
			id = Integer.parseInt(t.nextToken());
			x = Double.parseDouble(t.nextToken());
			y = Double.parseDouble(t.nextToken());
			//discard the in and out degree.
			t.nextToken();
			t.nextToken();
			AS = Integer.parseInt(t.nextToken());
		}

		/**
		 * Paint this node's position
		 *
		 * @param g Graphics object.
		 */
		public void paint(Graphics g)
		{
			//g.setColor(colours[AS % colours.length]);
			g.setColor(Color.BLACK);
			g.fillOval((int)x - 2, (int)y - 2, 5, 5);
		}

		/**
		 * Return the x value.
		 *
		 * @return x;
		 */
		public double getX()
		{
			return x;
		}

		/**
		 * Return the y value.
		 *
		 * @return y;
		 */
		public double getY()
		{
			return y;
		}
		
		/**
		 * Return the ID value.
		 *
		 * @return id;
		 */
		public double getID()
		{
			return id;
		}
	}//fim classe interna
	
}//fim classe
