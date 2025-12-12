package org.cloudbus.cloudsim;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import java.awt.Event.*;

import org.cloudbus.cloudsim.brite.Visualizer.BRITETopologyPO;
import org.cloudbus.cloudsim.brite.Visualizer.Edge;
import org.cloudbus.cloudsim.brite.Visualizer.Node;

/** 
 * Simulador para Alocacao Sob Demanda de VMs em Servidores com:
 * 
 * @author Lucio Agostinho Rocha
 * Ultima atualizacao: 17/03/2013
 *
 */

public class A_FF_Policy { 

	//Para a fronteira de Pareto
	//0: sem limite do consumo de energia
	//private static String LIMITE_W1="0";

	//private static double ALFA;

	//private static double BETA;

	private static boolean FF=false;	
	private static String LOG_FF="";

	private static int TOTAL_CPU=0;

	private static boolean CUSTOS_UNITARIOS=false;	

	private static double FATOR_CORRECAO=0;
	private static double LIMITE_SUPERIOR_LINK=0;	

	//Porcentagem de tipos de servidores por datacenter
	private static int NUM_SERVIDORES_SMALL=0;
	private static int NUM_SERVIDORES_LARGE=0;
	private static int NUM_SERVIDORES_HUGE=0;

	//Tipo de aplicacao (CBR/UDP, FTP/TCP Tahoe, Exponential/UDP)
	private static String TIPO_APLICACAO_REDE="";	

	//Fator de reducao/aumento para a analise de sensibilidade
	private static boolean SELECIONAR_ALEATORIO=false;

	//true: cria um novo conjunto de datacenters, servidores e vms (a partir da topologia original)
	private static boolean NOVA_TOPOLOGIA=false;

	//true: cria novo modelo Lingo
	private static boolean GERAR_MODELO_LINGO=false;	
	//true: executa o modelo Lingo
	private static boolean EXECUTAR_MODELO_LINGO=false;
	//true: realiza o parser do resultado gerado pelo Lingo
	private static boolean REALIZAR_PARSER_RESULT_LINGO=false;

	//true: gera o modelo NS2
	private static boolean GERAR_MODELO_NS2=false;
	//true: executa o modelo no NS2
	private static boolean EXECUTAR_MODELO_NS2=false;
	//true: gera parser vazao
	private static boolean REALIZAR_PARSER_VAZAO=false;	
	//true: gera parser perda
	private static boolean REALIZAR_PARSER_PERDA=false;
	//true: gera parser perda
	private static boolean REALIZAR_PARSER_ATRASOMEDIO=false;
	//true: para cada datacenter, adquire os ranges de seus links com o roteador de borda
	private static boolean EXECUTAR_ENGINE_SENSIBILIDADE=false;	

	//Depuracao
	public boolean DEBUG_PROGRAM=true;

	//Caminho principal
	private static String PATH;	

	//Arquivo da topologia
	private static String ARQUIVO_TOPOLOGIA_ORIGINAL;
	//Arquivo de saida com a topologia do datacenter
	private static String ARQUIVO_TOPOLOGIA_DATACENTER;
	//private static String ARQUIVO_TOPOLOGIA_THREAD;
	//Arquivos de saida
	private static String ARQUIVO_MODELO_LINGO;
	private static String ARQUIVO_RESULT_LINGO;
	private static String ARQUIVO_RANGE_LINGO;
	private static String ARQUIVO_RESULT_QTDE_SERV;
	private static String ARQUIVO_POPULACAO_PARALELA;
	private static String ARQUIVO_FITNESS_AMOSTRA;

	private static String ARQUIVO_MODELO_NS2;
	private static String ARQUIVO_NAM_NS2;
	private static String ARQUIVO_RESULT_NS2;		
	private static String ARQUIVO_VAZAO_NS2;	
	private static String ARQUIVO_PERDA_NS2;
	private static String ARQUIVO_ATRASOMEDIO_NS2;
	private static String ARQUIVO_PARSER_BANDA_LINKS;

	//Guarda a evolucao do fitness
	private static String ARQUIVO_EVOLUCAO_FITNESS;
	//Guarda a evolucao do fitness
	private static String ARQUIVO_EVOLUCAO_FITNESS_POPULACAO;
	//Guarda a evolucao da vazao
	private static String ARQUIVO_EVOLUCAO_VAZAO;
	//Guarda a evolucao da perda
	private static String ARQUIVO_EVOLUCAO_PERDA;
	//Guarda a evolucao do atraso
	private static String ARQUIVO_EVOLUCAO_ATRASO;	

	//indice do nodo destino
	public static int NODO_DESTINO;	
	//numero de datacenters
	public static int NUM_DATACENTERS;	
	//numero de servidores
	public static int NUM_SERVIDORES;
	//numeroVMs inicialmente requisitas por servidor
	public static int NUM_VMS;		

	//Link de Conexao do datacenter com a topologia Internet
	private static double DATACENTER_LINK_BACKBONE;

	//Estrutura dos Servidores	
	private String [][] S = new String[NUM_DATACENTERS*NUM_SERVIDORES][11];

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

	//Cluster grouped in this amount of servers
	private int SERVER_CLUSTER=10;

	//Capacidades do servidor (similar ah Amazon EC2)
	//Unidades
	private static int SERVER_SMALL_CPU;
	private static int SERVER_LARGE_CPU;
	private static int SERVER_HUGE_CPU;	

	//GB
	private static int SERVER_SMALL_RAM;
	private static int SERVER_LARGE_RAM;
	private static int SERVER_HUGE_RAM;

	//GB
	private static int SERVER_SMALL_DISK;
	private static int SERVER_LARGE_DISK;
	private static int SERVER_HUGE_DISK;

	//GB
	private static int SERVER_SMALL_BW;
	private static int SERVER_LARGE_BW;
	private static int SERVER_HUGE_BW;

	//Custos de CPU
	private static int SERVER_COST_SMALL_CPU;
	private static int SERVER_COST_LARGE_CPU;
	private static int SERVER_COST_HUGE_CPU;

	//Custos de RAM
	private static int SERVER_COST_SMALL_RAM;
	private static int SERVER_COST_LARGE_RAM;
	private static int SERVER_COST_HUGE_RAM;

	//Custos de Disk
	private static int SERVER_COST_SMALL_DISK;
	private static int SERVER_COST_LARGE_DISK;
	private static int SERVER_COST_HUGE_DISK;

	//Custos de Link
	private static int SERVER_COST_SMALL_BW;
	private static int SERVER_COST_LARGE_BW;
	private static int SERVER_COST_HUGE_BW;	

	//Estrutura da Cloud
	private String [][] C = new String[NUM_VMS][8];
	//Indice da matriz C (cloud)
	private int I_CLOUD_VM=0;
	private int I_CLOUD_SERVER_INDEX=1;
	private int I_CLOUD_VM_CPU=2;
	private int I_CLOUD_VM_RAM=3;
	private int I_CLOUD_VM_DISK=4;
	private int I_CLOUD_VM_BW=5;
	private int I_CLOUD_VM_FLUXO=6;	
	private int I_CLOUD_VM_VIRTUALIZER=7;

	//Capacidades das VMs
	//~metade do que eh oferecido por cada servidor
	private static int VM_SMALL_CPU;
	private static int VM_LARGE_CPU;
	private static int VM_HUGE_CPU;

	//GB
	//~metade do que eh oferecido por cada servidor
	private static int VM_SMALL_RAM;
	private static int VM_LARGE_RAM;
	private static int VM_HUGE_RAM;

	//GB
	//~metade do que eh oferecido por cada servidor
	private static int VM_SMALL_DISK;
	private static int VM_LARGE_DISK;
	private static int VM_HUGE_DISK;

	//GB
	//~metade do que eh oferecido por cada servidor
	private static int VM_SMALL_BW;
	private static int VM_LARGE_BW;
	private static int VM_HUGE_BW;

	//GB
	private static double VM_SMALL_FLUXO;
	private static int VM_LARGE_FLUXO;
	private static int VM_HUGE_FLUXO;	

	//private static double LINK_VMS_SERVIDORES;	

	//private static int PERDA_PACOTES=0;

	//Tempo de inicio
	//java.util.Date d1;
	//Tempo de fim
	//java.util.Date d2;
	//Tempo no qual o melhor fitness foi obtido
	//java.util.Date d3;
	public static long endTime, beginTime;

	//---Variaveis de instancia para o AG---
	private static int TAM_POPULACAO;
	private static int NUM_BITS;
	private static int MAX_DECIMAL_CROMOSSOMO;	
	private static int NUM_NODES;
	private static int NUM_EDGES;
	private static String [][] EDGES_ARQUIVO;
	//private static int CAMPO_;
	private static String [][] P;	
	private static String [][] P2;
	//Melhor combinacao de custos
	private static String [][] P3;
	//private static double MELHOR_FITNESS=100000;
	//Numero inicial de iteracoes
	private static int contadorIteracoes=1;
	//Numero de iteracoes do algoritmo genetico
	private static int MAX_ITERACOES=0;
	private static int ITERACOES_SEM_MELHORA=0;

	//---Para a execucao em paralelo
	//true: execucao paralela no cluster
	private static boolean EXECUTAR_PARALELO=false;

	//---Lista Tabu: evita os nodos onde ocorreu perda anterior de pacotes
	private static int[][] LISTA_TABU;

	//Comeca com a perda de pacotes=100000 para iniciar o while (~10% da vazao)
	private static int PERDA_PACOTES_TOTAL=100000;
	private static double VAZAO_TOTAL_VMs=0;

	//Consumo de energia da rede
	private static int CONSUMO_ENERGIA_ROUTERS=0;
	private static double CONSUMO_ENERGIA_SERVIDORES=0;

	//Para o trafego Exponential
	private static int NET_BUFFER;
	private static int [] NET_PACKET_SIZE;
	private static int [] NET_BURST_TIME;
	private static int [] NET_IDLE_TIME;
	private static int [] NET_RATE;

	private static int NUM_GERACAO=0;
	private static int NUM_ITERACAO=0;

	//Para o paralelismo com Publish/Subscribe
	//
	//Execucao em paralelo com modelo Publish/Subscribe
	private static boolean EXECUTAR_PUBLISH_SUBSCRIBE=false;

	private static String PUBLISH_BASE_URL="";
	private static String PUBLISH_KEY="";
	private static String PUBLISH_CONTENT_TYPE="";
	private static String PUBLISH_VALUE="";
	
	private static double somatorioPorcentagemPerda=0;
	private static double somatorioAtrasoMedio=0;

	public A_FF_Policy(){  		

		if (NOVA_TOPOLOGIA){
			//Cria os Servidores
			criarServidores();

			//Cria a cloud de VMs
			createDemand();

			//Criar topologia de datacenter, servidores e rede
			criarTopologiaDatacenter();
			
			totalCPU();

		}//fim if

		if (FF){			
			clearWriteLogFF();

			//Number of bag-of-demands
			NUM_ITERACAO=0;
			//Initial log
			writeLogFF();
			NUM_ITERACAO++;
			while(NUM_ITERACAO<50){
				createDemand();
				beginTime = System.currentTimeMillis();
				startFF();
				FF_gerarModeloNS2();
				
				if (EXECUTAR_MODELO_NS2)					
					executarModeloNS2();

				if (REALIZAR_PARSER_PERDA)
					somatorioPorcentagemPerda=realizarParserPerda();

				//O atraso medio exige um buffer enorme -> fazer apos a simulacao
				if (REALIZAR_PARSER_ATRASOMEDIO)
					somatorioAtrasoMedio=realizarParserAtrasoMedio();
				
				endTime = System.currentTimeMillis();
				writeLogFF();
				
				NUM_ITERACAO++;
			}//end while

		}//end if

		System.exit(0);

	}//fim construtor

	public void executarModeloNS2(){

		System.out.println("\n-- Iteracao: " + NUM_ITERACAO + " --");

		System.out.println("\n--Executar modelo no NS2--");

		String comando = "ns " + ARQUIVO_MODELO_NS2;

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
			System.out.println("Fim da execucao do modelo no NS2.");
		} catch (Exception e){
			System.out.println("Excecao: Erro ao executar o modelo NS2.");
		}//fim catch

	}//fim executarModeloNS2
		
	public void FF_gerarModeloNS2(){

		//Read network topology

		System.out.println("\n--Generate Network Model--");

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
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER));
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
			//Modificado aqui porque o MILP atribuia NUM_EDGES antes (em A_PLI_AG_NS2)
			NUM_EDGES = Integer.parseInt(token.nextToken());
			//Guarda todas as informacoes dos Edges
			//System.out.println("numEdges: " + numEdges);
			//EDGE[0][EdgeId From To Length Delay Bandwidth QueueLimit ASto Type Other]
			//    [1][EdgeId From To Length Delay Bandwidth QueueLimit ASto Type Other]
			//    ...
			int campoEdgeId=0;
			int campoEdgeFrom=1;
			int campoEdgeTo=2;
			int campoEdgeCost=3;
			int campoEdgeDelay=4;
			int campoEdgeBw=5;
			int campoEdgeQueueLimit=6;
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
				//QueueLimit (6)
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
			/*//Para as demais edges
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
			conteudoNS2.append("\nset nf [open " + ARQUIVO_NAM_NS2 + " w]");
			conteudoNS2.append("\n$ns namtrace-all $nf");

			conteudoNS2.append("\n\n#Abre o arquivo de rastreamento para guardar os eventos");
			conteudoNS2.append("\nset nd [open " + ARQUIVO_RESULT_NS2 + " w]");
			conteudoNS2.append("\n$ns trace-all $nd");
			conteudoNS2.append("\n");

			conteudoNS2.append("\n#Define um procedimento de finish");
			conteudoNS2.append("\nproc finish {} {");
			conteudoNS2.append("\n   global ns nf nd");
			conteudoNS2.append("\n   $ns flush-trace");			
			conteudoNS2.append("\n   close $nf");
			conteudoNS2.append("\n   close $nd");
			conteudoNS2.append("\n   #exec nam " + ARQUIVO_NAM_NS2 + " &");
			conteudoNS2.append("\n   exit 0");
			conteudoNS2.append("\n}");			
			conteudoNS2.append("\n");
			conteudoNS2.append("\n#Cria os nodos da topologia da rede original");
			conteudoNS2.append("\n$ns node-config -MPLS ON");
			conteudoNS2.append("\nfor {set j 0} {$j < $NUM_NODES_ORIGINAL} {incr j} {");
			conteudoNS2.append("\n   set n($j) [$ns node]");
			conteudoNS2.append("\n#$n($j) label \"$j\"");
			//Por conta do laco para a criacao de nodos
			indiceNodosNS2=numNodosOriginal;
			
			//conteudoNS2.append("\n   $n($j) color black");
			conteudoNS2.append("\n}");			
			conteudoNS2.append("\n$ns node-config -MPLS OFF");

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

						//Cria o nodo
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
							EDGES_ARQUIVO[i][campoEdgeBw] + "kb " +
							//Outros parametros
							"10ms DropTail");					

					//Monitora a fila nos links (padrao: 20 pacotes)
					conteudoNS2.append("\n$ns duplex-link-op " +
							//From
							"$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") " +
							//To
							"$n("+EDGES_ARQUIVO[i][campoEdgeTo]+") " +
							"queuePos 0.5");
					//Tamanho do buffer nos roteadores
					conteudoNS2.append("\n$ns queue-limit " +
							//From
							"$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") " +
							//To
							"$n("+EDGES_ARQUIVO[i][campoEdgeTo]+") " +
							//Recupera informacao do buffer do arquivo
							//EDGES_ARQUIVO[i][campoEdgeQueueLimit]);
							//Recupera informacao do buffer do simulador
							NET_BUFFER);

					//Adiciona o custo do enlace (lembrar que o link eh full-duplex)
					conteudoNS2.append("\n$ns cost " +
							//From
							"$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") " +
							//To
							"$n("+EDGES_ARQUIVO[i][campoEdgeTo]+") " +
							EDGES_ARQUIVO[i][campoEdgeCost]);
					conteudoNS2.append("\n$ns cost " +
							//From
							"$n("+EDGES_ARQUIVO[i][campoEdgeTo]+") " +
							//To
							"$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") " +
							EDGES_ARQUIVO[i][campoEdgeCost]);

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
			
			//conteudoNS2.append("\n\n"+indiceNodosNS2);
			//Cria os LDP-peers
			//LDP (Label Distribution Protocol) eh o protocolo
			//utilizado pelo MPLS para mapear rotulos na rede
			conteudoNS2.append("\n\n#Cada par de LSR troca informacoes de roteamento por rotulos entre si");
			conteudoNS2.append("\nfor {set i 0} {$i < $NUM_NODES_ORIGINAL} {incr i} {");
			conteudoNS2.append("\n   for {set j 0} {$j < $NUM_NODES_ORIGINAL} {incr j} {");
			conteudoNS2.append("\n      if {$i != $j} {");
			conteudoNS2.append("\n         set a n($i)");
			conteudoNS2.append("\n         set b n($j)");
			conteudoNS2.append("\n         eval $ns LDP-peer $$a $$b");
			conteudoNS2.append("\n         #puts \"$a $b\"");
			conteudoNS2.append("\n      };#fim if");
			conteudoNS2.append("\n   };#fim for");
			conteudoNS2.append("\n};#fim for");

			conteudoNS2.append("\n\n#Atribui cores ah mensagens do protocolo LDP");
			conteudoNS2.append("\n$ns ldp-request-color 		blue");
			conteudoNS2.append("\n$ns ldp-mapping-color 		red");
			conteudoNS2.append("\n$ns ldp-withdraw-color 		magenta");
			conteudoNS2.append("\n$ns ldp-release-color 		orange");
			conteudoNS2.append("\n$ns ldp-notification-color 	yellow");


			//Cria o nodo destino
			conteudoNS2.append("\n\n#Nodo sink (destino)");
			if (TIPO_APLICACAO_REDE.equals("CBR/UDP"))
				conteudoNS2.append("\nset sink [new Agent/Null]");
			else
				if (TIPO_APLICACAO_REDE.equals("FTP/TCP"))
					conteudoNS2.append("\nset sink [new Agent/TCPSink]");
				else
					if (TIPO_APLICACAO_REDE.equals("Exp/UDP"))
						conteudoNS2.append("\nset sink [new Agent/Null]");

			conteudoNS2.append("\n$ns attach-agent $n($NODO_DESTINO) $sink");
			
			//Faz o parser do resultado da alocacao

			//Iteract with servers
			int p=0;
			//Iteract with datacenter index
			int q=0;
			//Count amount of server in each datacenter
			int r=0;
			//Group the flow generated by VMs are linked with numDatacenters
			double [] fluxoGeradoAglomeradoVMs = new double[numDatacenters];
			//Servers in each datacenter
			while(p<S.length){

				//I_SERVER_COST_CPU is used here to inform the number of VM allocations in this server
				fluxoGeradoAglomeradoVMs[q]+=Integer.parseInt(S[p][I_SERVER_COST_CPU]);
				//End of servers in this datacenter
				if(r>numServers){
					r=0;
					//Amount of traffic generated by this aggregated traffic
					fluxoGeradoAglomeradoVMs[q] *= VM_SMALL_FLUXO;
					//Next datacenter and fluxoGeradoAglomeradoVMs
					q++;
				}//end if

				//Next datacenter server index (0 at numServer)
				r++;
				//Next server (0 at S.length)
				p++;
			}//end while

			//conteudoNS2.append("\n\n"+indiceNodosNS2);

			//Ajusta o indice do NS2 para criar novos nodos a partir 
			//do ultimo indice dos roteadores de borda
			//indiceNodosNS2=numNodes;

			//Cria o link entre o aglomerado de VMs e os roteadores de borda
			conteudoNS2.append("\n\n#Link aglomeradoVms com roteadores de borda");
			int indiceLigacao=0;
			i=0;
			while(i<fluxoGeradoAglomeradoVMs.length){
				
				//o indice do fluxoAglomeradoVMs eh o mesmo do seu datacenter
				if(fluxoGeradoAglomeradoVMs[i]!=0){

					indiceLigacao = adquirirIndiceLigacao(EDGES_ARQUIVO, i);

					conteudoNS2.append("\n\n#fluxoGeradoAglomeradoVMs["+i+"]="+fluxoGeradoAglomeradoVMs[i]);
					//O indice do aglomeradoVMs corresponde ao indice do border router
					conteudoNS2.append("\nputs \"Link n("+ indiceLigacao +")--n("+ indiceNodosNS2 + ")\";flush stdout;");
					//Cria a relacao entre o aglomerado de VMs e o roteador de borda

					//Cria o nodo para o aglomeradoVMs
					conteudoNS2.append("\nset n("+indiceNodosNS2+") [$ns node]");
					conteudoNS2.append("\n$n("+indiceNodosNS2+") color black");
					conteudoNS2.append("\n$n("+indiceNodosNS2+") shape square");
					//Incremento o indiceNodosNS2 mais abaixo, pq ainda irei precisar dele							

					conteudoNS2.append("\n$ns duplex-link $n("+indiceLigacao+") " +
							"$n("+indiceNodosNS2+") " +								
							DATACENTER_LINK_BACKBONE + "kb 10ms DropTail");					
					
					//Custos
					conteudoNS2.append("\n$ns cost $n("+indiceLigacao+") $n("+indiceNodosNS2+") 1");
					conteudoNS2.append("\n$ns cost $n("+indiceNodosNS2+") $n("+indiceLigacao+") 1");

					//Guarda a informacao da banda atribuida ao link (para guardar em arquivo)
					banda.append(indiceLigacao+"--"+"__"+" "+"n_"+indiceNodosNS2+"\n");											
					//nodosComAglomerados += i + " ";						

					if (TIPO_APLICACAO_REDE.equals("CBR/UDP")){
						conteudoNS2.append("   \n#Cria um agente UDP e atribui ao aglomerado");
						conteudoNS2.append("   \nset udp("+indiceLigacao+") [new Agent/UDP]");
						//Fluxo comeca nos servidores
						conteudoNS2.append("   \n$ns attach-agent $n("+indiceNodosNS2+") $udp("+indiceLigacao+")");
						//Fluxo comeca nos roteadores de nucleo
						//conteudoNS2.append("   \n$ns attach-agent $n("+adquirirLigacaoCoreRouter(EDGES_ARQUIVO,indiceLigacao)+") $udp("+indiceLigacao+")");
						conteudoNS2.append("   \n#Cria um trafego CBR e atribui ao agent UDP");
						conteudoNS2.append("   \nset cbr("+indiceLigacao+") [new Application/Traffic/CBR]");
						conteudoNS2.append("   \n$cbr("+indiceLigacao+") set packetSize_ 1500");
						conteudoNS2.append("   \n#Fluxo ficticio para ocupar todo o link");
						conteudoNS2.append("   \n$cbr("+indiceLigacao+") set rate_ " + (fluxoGeradoAglomeradoVMs[i]+100) + "kb");
						conteudoNS2.append("   \n$cbr("+indiceLigacao+") attach-agent $udp("+indiceLigacao+")");
						conteudoNS2.append("   \n#Conecta o nodo ao agente sink do nodo destino");
						conteudoNS2.append("   \n$ns connect $udp("+indiceLigacao+") $sink");
						conteudoNS2.append("   \n#Identifica o fluxo do aglomeradoVMs");
						conteudoNS2.append("   \n$udp("+indiceLigacao+") set fid_ "+indiceLigacao+"");
						//Cor do trafego (passo1)
						conteudoNS2.append("   \n$udp("+indiceLigacao+") set class_ 1");
						conteudoNS2.append("\n#Inicio da submissao do trafego");
						conteudoNS2.append("\n$ns at 0.0 \"$cbr("+indiceLigacao+") start\"");
					} else 
						if (TIPO_APLICACAO_REDE.equals("FTP/TCP")){
							conteudoNS2.append("   \n#Cria um agente TCP e atribui ao aglomerado");
							conteudoNS2.append("   \nset tcp("+indiceLigacao+") [new Agent/TCP]");
							conteudoNS2.append("   \n$ns attach-agent $n("+indiceNodosNS2+") $udp("+indiceLigacao+")");
							conteudoNS2.append("   \n#Cria um trafego FTP e atribui ao agent TCP");
							conteudoNS2.append("   \nset ftp("+indiceLigacao+") [new Application/FTP]");
							conteudoNS2.append("   \n$ftp("+indiceLigacao+") set packetSize_ 1500");
							conteudoNS2.append("   \n#Fluxo ficticio para ocupar todo o link");
							conteudoNS2.append("   \n$ftp("+indiceLigacao+") set rate_ " + (fluxoGeradoAglomeradoVMs[i]+100) + "kb");
							conteudoNS2.append("   \n$ftp("+indiceLigacao+") attach-agent $tcp("+indiceLigacao+")");
							conteudoNS2.append("   \n#Conecta o nodo ao agente sink do nodo destino");
							conteudoNS2.append("   \n$ns connect $tcp("+indiceLigacao+") $sink");
							conteudoNS2.append("   \n#Identifica o fluxo do aglomeradoVMs");
							conteudoNS2.append("   \n$tcp("+indiceLigacao+") set fid_ "+indiceLigacao+"");
							//Cor do trafego (passo1)
							conteudoNS2.append("   \n$tcp("+indiceLigacao+") set class_ 1");
							conteudoNS2.append("\n#Inicio da submissao do trafego");
							conteudoNS2.append("\n$ns at 0.0 \"$ftp("+indiceLigacao+") start\"");
						} else 
							if (TIPO_APLICACAO_REDE.equals("Exp/UDP")){							
								
								conteudoNS2.append("   \n#Cria um agente UDP e atribui ao aglomerado");
								conteudoNS2.append("   \nset udp("+indiceLigacao+") [new Agent/UDP]");
								conteudoNS2.append("   \n$ns attach-agent $n("+indiceNodosNS2+") $udp("+indiceLigacao+")");
								conteudoNS2.append("   \n#Cria um trafego CBR e atribui ao agent UDP");
								conteudoNS2.append("   \nset exp("+indiceLigacao+") [new Application/Traffic/Exponential]");
								conteudoNS2.append("   \n$exp("+indiceLigacao+") set packetSize_ " + NET_PACKET_SIZE[i]);
								conteudoNS2.append("   \n#Fluxo ficticio para ocupar todo o link");
								//conteudoNS2.append("   \n$exp("+indiceLigacao+") set rate_ " + (fluxoGeradoAglomeradoVMs[i]+30*((IDLE_TIME_+BURST_TIME_)/BURST_TIME_)) + "kb");
								conteudoNS2.append("   \n$exp("+indiceLigacao+") set rate_ " + NET_RATE[i] + "kb");
								conteudoNS2.append("   \n$exp("+indiceLigacao+") set idle_time_ " + NET_IDLE_TIME[i] + "ms");
								conteudoNS2.append("   \n$exp("+indiceLigacao+") set burst_time_ " + NET_BURST_TIME[i] + "ms");									
								conteudoNS2.append("   \n$exp("+indiceLigacao+") attach-agent $udp("+indiceLigacao+")");
								conteudoNS2.append("   \n#Conecta o nodo ao agente sink do nodo destino");
								conteudoNS2.append("   \n$ns connect $udp("+indiceLigacao+") $sink");
								conteudoNS2.append("   \n#Identifica o fluxo do aglomeradoVMs");
								conteudoNS2.append("   \n$udp("+indiceLigacao+") set fid_ "+indiceLigacao+"");
								//Cor do trafego (passo1)
								conteudoNS2.append("   \n$udp("+indiceLigacao+") set class_ 1");
								conteudoNS2.append("\n#Inicio da submissao do trafego");
								conteudoNS2.append("\n$ns at 0.0 \"$exp("+indiceLigacao+") start\"");
							}//fim if

					indiceNodosNS2++;

				}//fim if

				i++;
			}//fim while				
			
			conteudoNS2.append("\n\n#A partir dos novos links formados");
			conteudoNS2.append("\n#cada LSR atualiza as suas tabelas de rotulos para envaminhar pacotes");
			conteudoNS2.append("\nfor {set i 0} {$i < $NUM_NODES_ORIGINAL} {incr i} {");
			conteudoNS2.append("\n   set a n($i)");
			conteudoNS2.append("\n   set m [eval $$a get-module \"MPLS\"]");
			conteudoNS2.append("\n   eval set n($i) $m");
			conteudoNS2.append("\n};#fim for");

			//Cria as rotas explicitas 
			conteudoNS2.append("\n\n#Cria as rotas explicitas");
			conteudoNS2.append("\n#ER-LSP entre LSR(0) e LSR(6) com LSPID 3000");
			conteudoNS2.append("\n#$ns at 0.5 \"$n(0) make-explicit-route 6 0_3_5_6 3000 -1\"");
			conteudoNS2.append("\n#Atribui o fluxo para alcancar o nodo destino (FEC) ao LSP");
			conteudoNS2.append("\n#$ns at 0.7 \"$n(0) flow-erlsp-install 6 -1 3000\"");


			//Roteamento dinamico (estado do enlace)
			//conteudoNS2.append("\n\n$ns rtproto LS");
			//Cor do trafego (passo2)
			conteudoNS2.append("\n\n$ns color 1 Green");
			conteudoNS2.append("\n\n$ns at 60.0 \"finish\"");			
			conteudoNS2.append("\n$ns run\n");

			//System.out.println(conteudoNS2);

			//Grava o conteudo do modelo para o NS2
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_MODELO_NS2,false));			
				out.write(conteudoNS2.toString());
				out.close();
			} catch(Exception e){
				System.out.println("15Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

			//Grava a banda dos links em arquivo
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_PARSER_BANDA_LINKS,false));			
				out.write(banda.toString());
				out.close();
			} catch(Exception e){
				System.out.println("16Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch			

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Excecao 18 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER + "\n" + e.getMessage());
		}//fim catch

	}//end FF_gerarModeloNS2

	public double realizarParserPerda(){

		double somatorioPorcentagemPerda=0;

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
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER));
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
			//numNodes+numEdges por causa dos indices (tenho indices para os roteadores de borda tb)
			//numNodes+numEdges eh mais do que suficiente (um novo link pode ter um router jah usado anteriormente) 
			int [][] packetsSendLink = new int[numNodes+numEdges][numNodes+numEdges];
			int i=0;
			int j=0;
			while(i<packetsSendLink.length){
				j=0;
				while(j<packetsSendLink[i].length){
					packetsSendLink[i][j]=0;
					j++;
				}//fim while
				i++;
			}//fim while

			//Pacotes enviados pela origem
			//int [] packetsSend = new int[numNodes+numEdges];

			//Pacotes descartados apenas no link
			i=0;
			j=0;
			int [][] packetsDropLink = new int[numNodes+numEdges][numNodes+numEdges];			
			while(i<packetsDropLink.length){
				j=0;
				while(j<packetsDropLink[i].length){
					packetsDropLink[i][j]=0;
					j++;
				}//fim while
				i++;
			}//fim while
			//Pacotes descartados gerados pela VM
			//int [] packetsDrop = new int[numNodes+numEdges];

			//iniciaMatriz(packetsSend);
			//iniciaVetor(packetsDrop);

			String [] t = new String[8];
			StringBuffer parserPerda = new StringBuffer("From-To PacketsSend PacketsDrop PercentOfDrop");
			//System.out.println("\nFrom-To PacketsSend PacketsDrop");

			try {
				//try to open the file.
				BufferedReader fileResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_NS2));
				String linhaResult = fileResult.readLine();				
				while (linhaResult != null) {

					//Ignora as linhas que nao respeitam o padrao (linhas com link-down)
					try {
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
							//Adiciona os links com descarte de pacotes na lista tabu
							LISTA_TABU[Integer.parseInt(t[2].toString())][Integer.parseInt(t[3].toString())]++;					
							LISTA_TABU[Integer.parseInt(t[3].toString())][Integer.parseInt(t[2].toString())]++;

						}//fim if

					} catch(Exception e){
						//Quando os links sao desativados, o arquivo de log do NS2 (*.tr) fica com 
						//um formato diferente. Disparo esse warning para exibir essa informacao
						//
						//System.out.println("Aviso 1: Encontrada linha fora do padrao de envio/recepcao de pacotes");						
					}//fim catch
					linhaResult = fileResult.readLine();															

				}//fim while				

				//Resultados dos links				
				i=0;
				j=0;
				double porcentagemPerda=0;
				while(i<packetsSendLink.length){
					j=0;
					while(j<packetsSendLink[i].length){
						if (packetsSendLink[i][j]!=0){
							//System.out.println(i + "--" + j + " " + packetsSendLink[i][j] + " " + packetsDropLink[i][j]);
							porcentagemPerda = ( packetsDropLink[i][j] * 100 ) / packetsSendLink[i][j];
							parserPerda.append("\n" + i + "--" + j + " " + packetsSendLink[i][j] + " " + packetsDropLink[i][j] + " " + porcentagemPerda);							

							//Ao desativar links pacotes sao perdidos, mas
							//quero saber se ocorre perda nos links que permaneceram ativos
							somatorioPorcentagemPerda+=porcentagemPerda;
							//somatorioPorcentagemPerda+=packetsDropLink[i][j];

						}//fim if
						j++;
					}//fim while 
					i++;
				}//fim while

				//Grava o resultado em arquivo
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_PERDA_NS2,false));			
					out.write(parserPerda.toString());
					out.close();
				} catch(Exception e){
					System.out.println("11Excecao ao gravar no arquivo." + e.getMessage());
				}//fim catch

				//Grava o resultado em arquivo
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_EVOLUCAO_PERDA,true));			
					out.write(somatorioPorcentagemPerda+"\n");
					out.close();
				} catch(Exception e){
					System.out.println("11Excecao ao gravar no arquivo: " + ARQUIVO_EVOLUCAO_PERDA + ": " + e.getMessage());
				}//fim catch

				//Resultados da fonte de trafego
				/*System.out.println("\n\nSource PacketsSend PacketsDrop");
				parserPerda.append("\n\nSource PacketsSend PacketsDrop");
				i=0;
				while(i<packetsSendLink.length){
					System.out.println(i + " " + packetsSendLink[i] + " " + packetsDropLink[i]); 
					parserPerda.append("\n" + i + " " + packetsSendLink[i] + " " + packetsDropLink[i]);
					i++;
				}//fim while
				 */				

				//close the file			
				fileResult.close();	
			} catch (Exception e){
				System.out.println("Excecao 12: Erro ao ler o arquivo: " + ARQUIVO_RESULT_NS2 + " Excecao: " + e.getMessage());
			}//fim catch

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Excecao 13 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER + "\n" + e.getMessage());
		}//fim catch

		return somatorioPorcentagemPerda;

	}//fim realizarParserPerda
	
	public double realizarParserAtrasoMedio(){

		double somatorioAtrasoMedio=0;

		//Para cada aresta, calcula o atraso medio
		//
		String [][] EDGES=null;		

		//
		//Adquire as arestas do arquivo
		try
		{
			//Guarda as informacoes do arquivo
			StringBuffer info = new StringBuffer();
			//Linhas do arquivo
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER));
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

			//
			//Le o conteudo do arquivo resultante da simulacao no NS2
			//Para cada aresta
			int i=0;
			String [][] fromNode = new String[numNodes+numEdges][numNodes+numEdges];
			String [][] toNode = new String[numNodes+numEdges][numNodes+numEdges];
			int [][] numSamples = new int[numNodes+numEdges][numNodes+numEdges];
			double [][] totalDelay = new double[numNodes+numEdges][numNodes+numEdges];

			//Inicializa as matrizes
			int p=0;
			int q=0;
			while(p<numSamples.length){
				q=0;
				while(q<numSamples[p].length){
					numSamples[p][q]=0;
					q++;
				}//fim while
				p++;
			}//fim while

			p=0;
			q=0;
			while(p<totalDelay.length){
				q=0;
				while(q<totalDelay[p].length){
					totalDelay[p][q]=0;
					q++;
				}//fim while
				p++;
			}//fim while

			String [] t = new String[12];

			StringBuffer parserAtrasoMedio = new StringBuffer("From-To AtrasoMedio");
			//System.out.println("From-To AtrasoMedio");

			int maiorPID=1;
			try {
				//try to open the file.
				BufferedReader fileResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_NS2));
				String linhaResult = fileResult.readLine();
				//Preciso saber quantos PIDs tenho no arquivo, porque
				//o calculo do delay eh baseado no PID
				while (linhaResult != null) {

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

					if (Integer.parseInt(t[11].toString())>maiorPID){
						maiorPID = Integer.parseInt(t[11].toString());
					}//fim if

					linhaResult = fileResult.readLine();

				}//fim while

				//close the file			
				fileResult.close();	
			} catch (Exception e){
				System.out.println("Excecao 20: Erro ao ler o arquivo: " + ARQUIVO_RESULT_NS2 + " : " + e.getMessage());
			}//fim catch

			//System.out.println("MaiorPID: " + maiorPID);

			//Abre o arquivo novamente para o calculo do atraso
			double [] t_arr = new double[maiorPID+1];				

			try {
				//try to open the file.
				BufferedReader fileResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_NS2));
				String linhaResult = fileResult.readLine();

				while (linhaResult != null) {

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
						t_arr[Integer.parseInt(t[11].toString())]=Double.parseDouble(t[1].toString());
					}//fim if

					if (t[0].equals("r")){
						if (t_arr[Integer.parseInt(t[11].toString())]>0){
							//Incrementa o numero de amostras para o atraso no link
							numSamples[Integer.parseInt(t[2].toString())][Integer.parseInt(t[3].toString())]++;
							//Atraso medio no link
							totalDelay[Integer.parseInt(t[2].toString())][Integer.parseInt(t[3].toString())]+=(Double.parseDouble(t[1].toString()) - t_arr[Integer.parseInt(t[11].toString())]);							
						}//fim if

					}//fim if

					linhaResult = fileResult.readLine();

				}//fim while

				//close the file			
				fileResult.close();	
			} catch (Exception e){
				System.out.println("Excecao 21: Erro ao ler o arquivo: " + ARQUIVO_RESULT_NS2 + " : " + e.getMessage());
			}//fim catch


			//System.out.println("Passei por aqui");

			/////////////

			//Resultados
			double [][] avgDelay = new double[numNodes+numEdges][numNodes+numEdges];
			//Inicializa
			p=0;
			q=0;
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
			int j=0;
			while (i<numNodes+numEdges){
				j=0;
				while (j<numNodes+numEdges){						
					//avgDelay = totalDelay / numSamples
					if (numSamples[i][j]!=0){
						avgDelay[i][j]=totalDelay[i][j]/numSamples[i][j];							
						//System.out.println(i + "--" + j + " " + String.format("%1$,.7f",avgDelay[i][j]));
						conteudoAtrasoMedio.append("\n" + i + "--" + j + " " + String.format("%1$,.7f",avgDelay[i][j]));
						somatorioAtrasoMedio+=avgDelay[i][j];
					}//fim if
					j++;
				}//fim while					
				i++;
			}//fim while

			//Grava o resultado em arquivo
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_ATRASOMEDIO_NS2,false));			
				out.write(conteudoAtrasoMedio.toString());
				out.close();
			} catch(Exception e){
				System.out.println("Excecao 22 ao gravar no arquivo." + e.getMessage());
			}//fim catch

			//Grava o resultado em arquivo
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_EVOLUCAO_ATRASO,true));			
				out.write(somatorioAtrasoMedio+"\n");
				out.close();
			} catch(Exception e){
				System.out.println("Excecao 22 ao gravar no arquivo: " + ARQUIVO_EVOLUCAO_ATRASO + ": " + e.getMessage());
			}//fim catch

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Excecao 23 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER + "\n" + e.getMessage());
		}//fim catch

		return somatorioAtrasoMedio;

	}//fim realizarParserAtrasoMedio
	
	public int adquirirIndiceLigacao(String [][] EDGES_ARQUIVO, int indiceDatacenter){

		int indiceLigacao=0;

		int campoEdgeId=0;
		int campoEdgeFrom=1;
		int campoEdgeTo=2;
		int campoEdgeCost=3;
		int campoEdgeDelay=4;
		int campoEdgeBw=5;
		int campoEdgeQueueLimit=6;
		int campoEdgeAsTo=7;
		int campoEdgeType=8;
		int campoEdgeOther=9;

		int p=0;
		boolean achou=false;
		while(p<EDGES_ARQUIVO.length && !achou){
			if (	EDGES_ARQUIVO[p][campoEdgeType].equals("BORDER_ROUTER") &&
					EDGES_ARQUIVO[p][campoEdgeTo].equals(indiceDatacenter+"")){
				achou=true;
				indiceLigacao = Integer.parseInt(EDGES_ARQUIVO[p][campoEdgeFrom]);
			}//fim if
			p++;
		}//fim while

		return indiceLigacao;		

	}//fim adquirirIndiceLigacao

	public void createDemand(){

		//Cloud de VMs
		//
		//C = [
		//VM, 
		//CPU da VM consumida, 
		//RAM da VM consumida, 
		//DISK da VM consumida,
		//BW da VM consumida,
		//I_CLOUD_SERVER_INDEX]		

		//Cria as instancias de servidores e VMs
		int i=0;
		int j=0;
		int k=0;
		int servidor=0;
		//Cria as instancias de VMs
		while (j<NUM_VMS){         
			C[k][I_CLOUD_VM]=k+"";			

			//Tipo da VM (1-small, 2-large, 3-huge)
			//Tipo aleatorio de VMs
			//int tipoVM = (int) Math.round(1 + Math.random() * 2);

			//Tipo fixo de VMs
			int tipoVM = 1;
			switch (tipoVM){
			//1-small
			case 1 :  
				C[k][I_CLOUD_VM_CPU]=VM_SMALL_CPU+"";
				C[k][I_CLOUD_VM_RAM]=VM_SMALL_RAM+"";
				C[k][I_CLOUD_VM_DISK]=VM_SMALL_DISK+"";
				C[k][I_CLOUD_VM_BW]=VM_SMALL_BW+"";
				C[k][I_CLOUD_VM_FLUXO]=VM_SMALL_FLUXO+"";
				C[k][I_CLOUD_VM_VIRTUALIZER]="KVM";
				break;
				//2-large
			case 2 : 
				C[k][I_CLOUD_VM_CPU]=VM_LARGE_CPU+"";
				C[k][I_CLOUD_VM_RAM]=VM_LARGE_RAM+"";
				C[k][I_CLOUD_VM_DISK]=VM_LARGE_DISK+"";
				C[k][I_CLOUD_VM_BW]=VM_LARGE_BW+"";		
				C[k][I_CLOUD_VM_FLUXO]=VM_LARGE_FLUXO+"";
				C[k][I_CLOUD_VM_VIRTUALIZER]="KVM";
				break;
				//3-huge
			case 3 : 
				C[k][I_CLOUD_VM_CPU]=VM_HUGE_CPU+"";
				C[k][I_CLOUD_VM_RAM]=VM_HUGE_RAM+"";
				C[k][I_CLOUD_VM_DISK]=VM_HUGE_DISK+"";
				C[k][I_CLOUD_VM_BW]=VM_HUGE_BW+"";
				C[k][I_CLOUD_VM_FLUXO]=VM_HUGE_FLUXO+"";
				C[k][I_CLOUD_VM_VIRTUALIZER]="KVM";
				break;
			}//fim switch
			//Busca alocacao inicial em um servidor
			servidor=(int) Math.round(0 + Math.random() * (NUM_SERVIDORES-1));
			C[k][I_CLOUD_SERVER_INDEX]=servidor+"";
			j=j+1;
			k=k+1;
		}//fim while

		if (DEBUG_PROGRAM){
			//Nota: para a tranformacao em Lingo, a alocacao inicial em um servidor nao interessa
			System.out.println("\nCloud: [VM_index, InitialServer, CPU_request, RAM_request, DISK_request, BW_request, Traffic, Virtualizer]");	
			exibir(C);
		}//fim if

	}//end createDemand	

	public void startFF(){

		//Iteract with cloud vms
		int i=0;
		//Iteract with servers
		int j=0;
		boolean allocated=false;
		int serverCPU=0;
		int vmCPU=0;
		int serverRemainCPU=0;
		while(i<C.length){

			allocated=false;
			//Try allocation in all servers
			j=0;
			while(j<S.length && !allocated){

				/*System.out.println("Here!!! i: " + i + " j: " + j);
				try{
					Thread t = new Thread();
					t.sleep(1000);
				} catch (Exception e){}
				 */

				//Amount of VM CPU request lesser than Server CPU
				serverCPU=Integer.parseInt(S[j][I_SERVER_CPU]);
				vmCPU=Integer.parseInt(C[i][I_CLOUD_VM_CPU]);
				if(serverCPU >= vmCPU){
					allocated=true;
					serverRemainCPU=serverCPU-vmCPU;
					S[j][I_SERVER_CPU]=serverRemainCPU+"";

					//Update TOTAL_CPU
					TOTAL_CPU-=vmCPU;

					//Uses I_SERVER_COST_CPU to indicate the amount of VM allocations
					S[j][I_SERVER_COST_CPU]=(Integer.parseInt(S[j][I_SERVER_COST_CPU])+1)+"";

				}//end if
				j++;
			}//end while
			//Next VM request
			i++;
		}//end while		

	}//end startFF

	public void clearWriteLogFF(){

		try {
			//Overwrite previous log file
			BufferedWriter out = new BufferedWriter(new FileWriter(LOG_FF,false));					
			out.write("");						
			out.close();
		} catch(Exception e){
			System.out.println("1Exception in clear " + LOG_FF + "\n" + e.getMessage());			
		}//fim catch

	}//end clearWriteLogFF

	public void writeLogFF(){

		try {
			//Append previous log file
			BufferedWriter out = new BufferedWriter(new FileWriter(LOG_FF,true));

			int amountServersAllocated=0;
			int amountVMAllocations=0;
			int amountClusterAllocations=0;

			int i=0;
			//To indicate server clusters
			int j=0;
			//Write index of current demand
			out.write("\nDemandIndex: " + NUM_ITERACAO + " ");			
			//Write all server CPU that remains
			out.write("VMAllocationsInServers: ");
			while(i<S.length){			
				out.write(S[i][I_SERVER_COST_CPU] + " ");
				//Verify if server is allocated
				if(Integer.parseInt(S[i][I_SERVER_COST_CPU])!=0)
					amountServersAllocated++;

				amountVMAllocations += Integer.parseInt(S[i][I_SERVER_COST_CPU]);
				//Cluster
				/*if (j>SERVER_CLUSTER){
					j=0;
					amountClusterAllocations++;					
				}//end if
				 */
				//Next server
				i++;
				//Next iteration to discover cluster
				j++;
			}//end while

			//Allocation time
			out.write("Time: " + (endTime-beginTime)/1000 + " ");

			//Amount of TOTAL_CPU that remains
			out.write("TOTAL_CPU_Remains: " + TOTAL_CPU + " ");

			//Amount of Servers allocated
			out.write("ServersAllocated: " + amountServersAllocated + " ");

			//Amount of Cluster allocations
			//out.write("ClustersAllocated: " + amountClusterAllocations + " ");

			//Amount of VM allocations
			out.write("VMsAllocated: " + amountVMAllocations + " ");
			
			//Percentage of Packets Drop
			out.write("%PacketsDropped: " + somatorioPorcentagemPerda + " ");
			
			//Sum of end-to-end delay
			out.write("SumEndToEndDelay: " + somatorioAtrasoMedio + " ");

			out.close();
		} catch(Exception e){
			System.out.println("1Exception in clear " + LOG_FF + "\n" + e.getMessage());			
		}//fim catch		

	}//end writeLog

	public void criarServidores(){

		//Servidores
		//S = [
		//indiceDatacenter,
		//indice, 
		//CPU disponivel, 
		//RAM disponivel, 
		//DISK disponivel,
		//BW disponivel...]

		//Cria as instancias de servidores
		int i=0; //datacenter
		int j=0; //linha
		int k=0; //iterar num_servidores
		int m=0; //indice dos servidores
		while (i<NUM_DATACENTERS){

			//Cria o percentual de servidores do tipo small
			////////////
			k=0;		
			//m=0; <-- 	esse eh o elemento para iniciar 
			//			os indices dos servidores em 0 para cada datacenter
			while (k<NUM_SERVIDORES_SMALL&&k<NUM_SERVIDORES){

				//S = [ datacenterId serverId cap_cpu ...
				// 0 0 ...
				// 0 1 ...
				// ...
				// 1 100 ...
				// 1 101 ...

				//System.out.println("iteracao: " + i + " j: " + j);
				S[j][I_SERVER_DATACENTER]=i+"";

				S[j][I_SERVER]=m+"";

				//Tipo do servidor (small) 

				int tipoServidor = 1;

				switch (tipoServidor){
				//1-small
				case 1 : 
					S[j][I_SERVER_CPU]=SERVER_SMALL_CPU+"";			
					S[j][I_SERVER_RAM]=SERVER_SMALL_RAM+"";
					S[j][I_SERVER_DISK]=SERVER_SMALL_DISK+"";
					S[j][I_SERVER_BW]=SERVER_SMALL_BW+"";
					S[j][I_SERVER_VIRTUALIZER]="KVM";
					S[j][I_SERVER_COST_CPU]=SERVER_COST_SMALL_CPU+"";
					S[j][I_SERVER_COST_RAM]=SERVER_COST_SMALL_RAM+"";
					S[j][I_SERVER_COST_DISK]=SERVER_COST_SMALL_DISK+"";
					S[j][I_SERVER_COST_BW]=SERVER_COST_SMALL_BW+"";
					break;
				}//fim switch
				//Proxima linha
				j++;
				//Proxima iteracao
				k++;
				//Proximo indice do servidor
				m++;
			}//fim while
			///////////

			//Cria o percentual de servidores do tipo large
			k=0;
			while (k<NUM_SERVIDORES_LARGE&&k<NUM_SERVIDORES){

				//S = [ datacenterId serverId cap_cpu ...
				// 0 0 ...
				// 0 1 ...
				// ...
				// 1 100 ...
				// 1 101 ...

				S[j][I_SERVER_DATACENTER]=i+"";

				S[j][I_SERVER]=m+"";

				//Tipo do servidor (large) 

				int tipoServidor = 2;

				switch (tipoServidor){
				//2-large
				case 2 : 
					S[j][I_SERVER_CPU]=SERVER_LARGE_CPU+"";
					S[j][I_SERVER_RAM]=SERVER_LARGE_RAM+"";
					S[j][I_SERVER_DISK]=SERVER_LARGE_DISK+"";
					S[j][I_SERVER_BW]=SERVER_LARGE_BW+"";
					S[j][I_SERVER_VIRTUALIZER]="KVM";
					S[j][I_SERVER_COST_CPU]=SERVER_COST_LARGE_CPU+"";
					S[j][I_SERVER_COST_RAM]=SERVER_COST_LARGE_RAM+"";
					S[j][I_SERVER_COST_DISK]=SERVER_COST_LARGE_DISK+"";
					S[j][I_SERVER_COST_BW]=SERVER_COST_LARGE_BW+"";				
					break;
				}//fim switch
				//Proxima linha
				j++;
				//Proxima iteracao
				k++;
				//Proximo indice do servidor
				m++;
			}//fim while

			//Cria o percentual de servidores do tipo huge
			//Nota: caso o somatorio das porcentagens nao seja normalizado, 
			//os ultimos servidores serao do tipo huge (para completar)
			k=0;
			while (k<NUM_SERVIDORES_HUGE && k<NUM_SERVIDORES){

				//S = [ datacenterId serverId cap_cpu ...
				// 0 0 ...
				// 0 1 ...
				// ...
				// 1 0 ...
				// 1 1 ...

				S[j][I_SERVER_DATACENTER]=i+"";

				S[j][I_SERVER]=m+"";

				//Tipo do servidor (huge) 

				int tipoServidor = 3;

				switch (tipoServidor){
				//3-huge
				case 3 : 
					S[j][I_SERVER_CPU]=SERVER_HUGE_CPU+"";
					S[j][I_SERVER_RAM]=SERVER_HUGE_RAM+"";
					S[j][I_SERVER_DISK]=SERVER_HUGE_DISK+"";
					S[j][I_SERVER_BW]=SERVER_HUGE_BW+"";
					S[j][I_SERVER_VIRTUALIZER]="KVM";
					S[j][I_SERVER_COST_CPU]=SERVER_COST_HUGE_CPU+"";
					S[j][I_SERVER_COST_RAM]=SERVER_COST_HUGE_RAM+"";
					S[j][I_SERVER_COST_DISK]=SERVER_COST_HUGE_DISK+"";
					S[j][I_SERVER_COST_BW]=SERVER_COST_HUGE_BW+"";				
					break;
				}//fim switch
				//Proxima linha
				j++;
				//Proxima iteracao
				k++;
				//Proximo indice do servidor
				m++;
			}//fim while						

			//Proximo datacenter
			i++;			
		}//fim while

		if (DEBUG_PROGRAM){
			System.out.println("\nServer: [Datacenter_Index, Server_index, CPU_available, RAM_available, DISK_available, BW_available, Virtualizer, Cost CPU, Cost RAM, Cost Disk, Cost Link]"); 
			exibir(S);
		}

	}//fim criarServidores

	public void criarCloud(){

		//Cloud de VMs
		//
		//C = [
		//VM, 
		//CPU da VM consumida, 
		//RAM da VM consumida, 
		//DISK da VM consumida,
		//BW da VM consumida,
		//I_CLOUD_SERVER_INDEX]		

		//Cria as instancias de servidores e VMs
		int i=0;
		int j=0;
		int k=0;
		int servidor=0;
		//Cria as instancias de VMs
		while (j<NUM_VMS){         
			C[k][I_CLOUD_VM]=k+"";			

			//Tipo da VM (1-small, 2-large, 3-huge)
			//Tipo aleatorio de VMs
			//int tipoVM = (int) Math.round(1 + Math.random() * 2);

			//Tipo fixo de VMs
			int tipoVM = 1;
			switch (tipoVM){
			//1-small
			case 1 :  
				C[k][I_CLOUD_VM_CPU]=VM_SMALL_CPU+"";
				C[k][I_CLOUD_VM_RAM]=VM_SMALL_RAM+"";
				C[k][I_CLOUD_VM_DISK]=VM_SMALL_DISK+"";
				C[k][I_CLOUD_VM_BW]=VM_SMALL_BW+"";
				C[k][I_CLOUD_VM_FLUXO]=VM_SMALL_FLUXO+"";
				C[k][I_CLOUD_VM_VIRTUALIZER]="KVM";
				break;
				//2-large
			case 2 : 
				C[k][I_CLOUD_VM_CPU]=VM_LARGE_CPU+"";
				C[k][I_CLOUD_VM_RAM]=VM_LARGE_RAM+"";
				C[k][I_CLOUD_VM_DISK]=VM_LARGE_DISK+"";
				C[k][I_CLOUD_VM_BW]=VM_LARGE_BW+"";		
				C[k][I_CLOUD_VM_FLUXO]=VM_LARGE_FLUXO+"";
				C[k][I_CLOUD_VM_VIRTUALIZER]="KVM";
				break;
				//3-huge
			case 3 : 
				C[k][I_CLOUD_VM_CPU]=VM_HUGE_CPU+"";
				C[k][I_CLOUD_VM_RAM]=VM_HUGE_RAM+"";
				C[k][I_CLOUD_VM_DISK]=VM_HUGE_DISK+"";
				C[k][I_CLOUD_VM_BW]=VM_HUGE_BW+"";
				C[k][I_CLOUD_VM_FLUXO]=VM_HUGE_FLUXO+"";
				C[k][I_CLOUD_VM_VIRTUALIZER]="KVM";
				break;
			}//fim switch
			//Busca alocacao inicial em um servidor
			servidor=(int) Math.round(0 + Math.random() * (NUM_SERVIDORES-1));
			C[k][I_CLOUD_SERVER_INDEX]=servidor+"";
			j=j+1;
			k=k+1;
		}//fim while

		if (DEBUG_PROGRAM){
			//Nota: para a tranformacao em Lingo, a alocacao inicial em um servidor nao interessa
			System.out.println("\nCloud: [VM_index, InitialServer, CPU_request, RAM_request, DISK_request, BW_request, Traffic, Virtualizer]");	
			exibir(C);
		}//fim if

	}//fim criarCloud
	
	public void criarTopologiaDatacenter(){

		//Para cada novo datacenter, atualiza o arquivo do modelo da topologia
		//Adquire o ultimo indice da topologia
		//System.out.println("\n\n");
		StringBuffer modeloDatacenter = new StringBuffer();

		//Escreve apos o campo nodos
		try
		{
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_ORIGINAL));
			for (int i=0; i<3; i++) {
				modeloDatacenter.append(file.readLine()+"\n");
			}//fim for

			//get the number of nodes
			linha = file.readLine();
			//Nao guarda a linha com o numero antigo de nodos
			//Armazena os proximos nodos
			StringBuffer aux1 = new StringBuffer();
			//Descricao dos campos
			aux1.append("#NodeId xpos ypos indegree outdegree ASid type\n");
			StringTokenizer t1 = new StringTokenizer(linha, "( )");
			t1.nextToken();
			int numNodes = Integer.parseInt(t1.nextToken());
			//System.out.println("numNodes: " + numNodes);
			Node [] nodes = new Node[numNodes];

			//for the number of nodes
			int i=0;
			for (i=0; i<nodes.length; i++){
				//construct each node
				//
				//Aqui recupera a posicao
				linha = file.readLine();
				aux1.append(linha+"\n");
				nodes[i] = new Node(linha);
			}

			//Preciso dos indices dos novos nodos para criar as arestas de conexao
			//Acrescenta os nodos para router e servidores
			String [][] nodoDatacenter = new String[NUM_DATACENTERS][2];

			i=0;
			int j=nodes.length;
			//int k=0;

			//Linhas de nodoDatacenter
			int m=0;
			//Primeiro indice para cada datacenter
			//int m_inicial=0;

			//Para a atualizacao do indice da topologia com o indice do servidor
			//int p=0;

			while (i<NUM_DATACENTERS){

				//Ex.: nodoDatacenter = [indice conectadoAoNodo]
				//                		[100    0  ] <-- router conectado ao nodo 0
				//                		[101    100] <-- servidor1 conectado ao router
				//                		[102    100] <-- servidor2 conectado ao router
				//                		...
				//Router
				//Indice do nodo
				//m_inicial=m;
				nodoDatacenter[m][0]=j+"";
				//Conexao com o nodo da topologia (Datacenter 0 conecta-se ao nodo 0, e assim por diante)
				nodoDatacenter[m][1]=i+"";  
				aux1.append(nodoDatacenter[m][0] + "	" + nodoDatacenter[m][1] + "	1000	0	0	0	BORDER_ROUTER\n");
				//Proximo indice
				j++;			
				//k para iteracao com NUM_SERVIDORES
				//k=0;
				//Proxima linha de nodoDatacenter
				m++;
				/*//Servidores
				while(k<NUM_SERVIDORES){
					//Indice do servidor
					nodoDatacenter[m][0]=j+"";
					//Servidor conectado ao roteador
					nodoDatacenter[m][1]=nodoDatacenter[m_inicial][0]; //Primeiro elemento do datacenter eh o router
					aux1.append(nodoDatacenter[m][0] + "	" + nodoDatacenter[m][1] + "	0	0	0	0	SERVER\n");

					//Atualiza o indice do servidor
					S[p][I_SERVER]=nodoDatacenter[m][0]=j+"";

					//Proximo indice
					j++;
					//Proxima iteracao
					k++;
					//Proxima linha de nodoDatacenter
					m++;
					//Proximo servidor
					p++;
				}//fim while
				 */
				//Proximo datacenter
				i++;
			}//fim while

			//Atualiza a informacao sobre o novo numero de nodos
			int novoNumNodos=numNodes+NUM_DATACENTERS;
			modeloDatacenter.append("Nodes: ( " + novoNumNodos + " )\n");
			modeloDatacenter.append(aux1);

			//two blank lines
			file.readLine( );
			file.readLine( );
			modeloDatacenter.append("\n\n");

			//get the number of edges
			linha = file.readLine();
			//Nao guarda a linha com o numero antigo de nodos
			//Armazena os proximos nodos
			StringBuffer aux2 = new StringBuffer();
			aux2.append("#EdgeId From 	To 	CapLink	Delay	Bandwidth QueueLimit ASto Type Other\n");

			t1 = new StringTokenizer(linha, "( )");
			t1.nextToken();
			int numEdges= Integer.parseInt(t1.nextToken());
			//System.out.println("numEdges: " + numEdges);
			Edge [] edges = new Edge[numEdges];

			//for the number of edges
			for (i=0; i<edges.length; i++){
				//construct each edge
				linha = file.readLine();
				aux2.append(linha+"\n");
				edges[i] = new Edge(linha);
			}//fim for

			//Datacenter: adiciona as arestas (respeitando os indices)
			i=edges.length;	
			j=0;				
			//k=0;
			while (j<nodoDatacenter.length){
				//largura de banda do router (primeiro elemento)
				aux2.append( i + "	" +
						//Source
						nodoDatacenter[j][0] + "	" +
						//Destination
						nodoDatacenter[j][1] + "	" +
						//Custo do enlace
						"1" + "	" +
						//Delay
						"0" + "	" +
						//Bandwidth
						DATACENTER_LINK_BACKBONE + "	" +
						//QueueLimit
						"1000"+
						//Outros campos
				"	0	BORDER_ROUTER	U\n");
				//Proximo indice
				i++;					
				//Para linha do nodoDatacenter
				j++;
				//iterador para num_servidores
				//k=0;
				//Largura de banda dos demais servidores
				/*while(k<NUM_SERVIDORES){

					aux2.append( i + "	" +
							//Source
							nodoDatacenter[j][0] + "	" +
							//Destination
							nodoDatacenter[j][1] + "	" +
							//Custo do enlace
							"1" + "	" +
							//Delay
							"0.1" + "	" +
							//Adquire a bandwidth a partir do indice (atualizado) do servidor
							getServBandwidth(nodoDatacenter[j][0]) + "	" +
					"0	0	SERVER	U\n");

					//Proximo indice
					i++;					
					//Para linha do nodoDatacenter
					j++;
					//proximo iterador para num_servidores
					k++;
				}//fim while
				 */
			}//fim while

			//Atualiza o numero de arestas
			int novoNumArestas = edges.length+nodoDatacenter.length;
			modeloDatacenter.append("Edges: ( " + novoNumArestas + " )\n");
			modeloDatacenter.append(aux2);

			//close the file
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}//fim catch

		//Burst

		//Campo para datacenters
		modeloDatacenter.append("\n\nDatacenters: ( " + NUM_DATACENTERS + " )\n");
		modeloDatacenter.append("#DatacenterId Bandwidth Burst_time Idle_time Rate\n");
		int i=0;
		while(i<NUM_DATACENTERS){
			//indice linkDatacenter
			modeloDatacenter.append(i + " " + DATACENTER_LINK_BACKBONE + " " + NET_BURST_TIME[i] + " " + NET_IDLE_TIME[i] + " " + NET_RATE[i] + " " + "\n");
			i++;
		}//fim while

		//Campo para servidores	
		modeloDatacenter.append("\n\nServers: ( " + S.length + " )\n");
		modeloDatacenter.append("#datacenterid serverid cap_cpu cap_ram cap_disk cap_bw virtualizer cost_cpu cost_ram cost_disk cost_bw xpos ypos\n");
		i=0;
		while (i<S.length){
			//datacenterid
			modeloDatacenter.append(S[i][I_SERVER_DATACENTER]+" ");
			//serverid
			modeloDatacenter.append(S[i][I_SERVER]+" ");			
			//cap_cpu
			modeloDatacenter.append(S[i][I_SERVER_CPU]+" ");
			//cap_ram
			modeloDatacenter.append(S[i][I_SERVER_RAM]+" ");
			//cap_disk
			modeloDatacenter.append(S[i][I_SERVER_DISK]+" ");
			//cap_bw
			modeloDatacenter.append(S[i][I_SERVER_BW]+" ");
			//virtualizer
			modeloDatacenter.append(S[i][I_SERVER_VIRTUALIZER]+" ");
			//cost_cpu
			modeloDatacenter.append(S[i][I_SERVER_COST_CPU]+" ");
			//cost_ram
			modeloDatacenter.append(S[i][I_SERVER_COST_RAM]+" ");
			//cost_disk
			modeloDatacenter.append(S[i][I_SERVER_COST_DISK]+" ");
			//cost_bw
			modeloDatacenter.append(S[i][I_SERVER_COST_BW]+" ");
			//xpos
			modeloDatacenter.append(0 + " ");
			//ypos
			modeloDatacenter.append(0 + " ");

			modeloDatacenter.append("\n");
			i++;
		}//fim while

		//Campo para VMs
		modeloDatacenter.append("\n\nVMs: ( " + C.length + " )\n");
		modeloDatacenter.append("#vmid initialserver cpu_req ram_req disk_req bw_req fluxo_gerado virtualizer xpos ypos\n");
		i=0;
		while (i<C.length){
			//vmid
			modeloDatacenter.append(C[i][I_CLOUD_VM]+" ");
			//initialserver
			modeloDatacenter.append(C[i][I_CLOUD_SERVER_INDEX]+" ");
			//cpu_req
			modeloDatacenter.append(C[i][I_CLOUD_VM_CPU]+" ");
			//ram_req
			modeloDatacenter.append(C[i][I_CLOUD_VM_RAM]+" ");
			//disk_req
			modeloDatacenter.append(C[i][I_CLOUD_VM_DISK]+" ");
			//bw_req
			modeloDatacenter.append(C[i][I_CLOUD_VM_BW]+" ");
			//fluxo_gerado
			modeloDatacenter.append(C[i][I_CLOUD_VM_FLUXO]+" ");
			//virtualizer
			modeloDatacenter.append(C[i][I_CLOUD_VM_VIRTUALIZER] + " ");
			//xpos
			modeloDatacenter.append(0 + " ");
			//ypos
			modeloDatacenter.append(0 + " ");

			modeloDatacenter.append("\n");
			i++;
		}//fim while		

		//Indice do nodo destino
		modeloDatacenter.append("\n\nNodoDestino: ( " + NODO_DESTINO + " )\n");

		//System.out.println(modeloDatacenter.toString());

		//Grava a configuracao em arquivo
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_TOPOLOGIA_DATACENTER,false));			
			out.write(modeloDatacenter.toString());
			out.close();
		} catch(Exception e){
			System.out.println("12Excecao ao gravar no arquivo." + e.getMessage());
		}//fim catch

	}//fim criarTopologiaDatacenter

	public void totalCPU(){

		int i=0;
		while(i<S.length){

			TOTAL_CPU+=Integer.parseInt(S[i][I_SERVER_CPU]);

			i++;
		}//end while

	}//end totalCPU

	//Exibe os valores sem alteracao
	public void exibir(String [][] A){

		System.out.println("---");

		int i=0;
		int j=0;
		while (i<A.length){

			while (j<A[i].length){

				System.out.print(A[i][j] + " ");
				j++;

			}//fim while

			//Salta uma linha
			System.out.println();

			i++;
			j=0;
		}//fim while		

	}//fim exibir

	public static void main(String[] args) {		

		//Nota: Para cada mudanca no numero de 'datacenters' 
		//      eh necessario ter uma topologia com, no minimo, a quantidade de nos para os 'datacenters'
		//      que se ligam ah topologia gerada pelo simulador REALcloudSim
		//Nota: Eh muito importante informar os custos dos links
		//      na topologia

		//Indice do nodo destino
		NODO_DESTINO=6;

		//Numero de datacenters homogeneos (mesma quantidade de servidores, com as mesmas capacidades)
		NUM_DATACENTERS = 3;

		//Numero de Servidores por datacenter 
		//(Nota: esse valor corresponde a um multiplo de 10)
		NUM_SERVIDORES = 100;

		//Numero de VMs
		NUM_VMS = 1;

		//Numero maximo de iteracoes
		MAX_ITERACOES=1;  //20 iteracoes sao suficientes para estimar a evolucao 

		//Execute allocation policy FF
		FF=true;


		//true: execucao paralela com threads
		EXECUTAR_PARALELO=false;

		//true: execucao paralela com metodo publish/subscribe
		EXECUTAR_PUBLISH_SUBSCRIBE=false;

		PUBLISH_BASE_URL="http://127.0.0.1:8080/EventChannel/";
		PUBLISH_KEY="task";
		PUBLISH_CONTENT_TYPE="Content-Type: application/json";
		PUBLISH_VALUE="";

		//Tipo de aplicacao de rede (CBR/UDP, FTP/TCP, Exponential/UDP)
		TIPO_APLICACAO_REDE = "Exp/UDP";

		//Para a definicao de limites para gerar a Fronteira de Pareto
		//"0" = sem limite do consumo de energia
		//LIMITE_W1="0";

		//ALFA=5;
		//BETA=10-ALFA;

		//---Para o AG---
		//TAM_POPULACAO TEM que ser potencia de 2
		TAM_POPULACAO=4; //(8)
		//Para representar valores binarios nos campos dos cromossomos
		NUM_BITS=4;
		//Limite superior de valores em decimal, para inicializarPopulacao (todos os bits preenchidos)
		MAX_DECIMAL_CROMOSSOMO=14;
		//---------------

		//Custos dos links (false: para execuçao com variacao dos alelos dos cromossomos)
		CUSTOS_UNITARIOS=true;

		//Para gerar o modelo Lingo
		FATOR_CORRECAO=10;   //Fixo
		LIMITE_SUPERIOR_LINK = 300; //Estimativa da vazao maxima do link
		//(LIMITE_SUPERIOR_LINK * alelo) / FATOR_CORRECAO

		//true: cria um novo conjunto de datacenters, servidores e vms (a partir da topologia original)
		//false: le diretamente do arquivo
		NOVA_TOPOLOGIA=true;		

		//true: cria novo modelo Lingo
		GERAR_MODELO_LINGO=true;

		//true: executa o modelo Lingo
		EXECUTAR_MODELO_LINGO=true;

		//Para verificar se o modelo eh factivel ou nao
		//true: realiza novo parser do resultado: 
		//numero de VMs alocadas por servidor, energia consumida por servidor
		REALIZAR_PARSER_RESULT_LINGO=true;

		//true: selecao aleatoria de individuos
		SELECIONAR_ALEATORIO=false;

		//Nota: modelagem da alocacao de VMs DEPENDE de EXECUTAR_MODELO_LINGO
		//Tambem grava a banda atribuida para os links 		
		//true: gera o modelo NS2
		GERAR_MODELO_NS2=true;
		//true: executa o modelo no NS2
		EXECUTAR_MODELO_NS2=true;
		//true: gera parser da vazao
		REALIZAR_PARSER_VAZAO=true;
		//true: gera parser da perda de pacotes
		REALIZAR_PARSER_PERDA=true;
		//true: gera parser do atraso medio
		REALIZAR_PARSER_ATRASOMEDIO=true;		 

		//Executa um laco com todos os passos anteriores 
		//true: para cada datacenter, adquire os ranges de seus links com o roteador de borda
		//EXECUTAR_ENGINE_SENSIBILIDADE=true;

		//Arquivos
		PATH = "/home/lucio/";			

		//Para o Lingo
		String arquivo = PATH + "modeloLingo_"+
				NUM_DATACENTERS+"data_"+
				NUM_SERVIDORES+"serv_"+
				NUM_VMS+"vm";
		ARQUIVO_MODELO_LINGO = arquivo + ".lg4";
		ARQUIVO_RESULT_LINGO = arquivo + ".lgr";
		ARQUIVO_RANGE_LINGO = arquivo + ".range";
		ARQUIVO_RESULT_QTDE_SERV = arquivo + ".parser";
		ARQUIVO_PARSER_BANDA_LINKS = arquivo + ".banda";
		ARQUIVO_EVOLUCAO_FITNESS = arquivo + ".evolucaoFitness";
		ARQUIVO_EVOLUCAO_FITNESS_POPULACAO = arquivo + ".evolucaoFitnessPopulacao";		
		ARQUIVO_POPULACAO_PARALELA = PATH + "/mirror/cromossomo.txt";
		ARQUIVO_FITNESS_AMOSTRA = arquivo + ".amostras";

		//Para a Topologia
		String topologia = PATH + "modeloLingo_"+
				NUM_DATACENTERS+"data_"+
				NUM_SERVIDORES+"serv_"+
				NUM_VMS+"vm";		
		ARQUIVO_TOPOLOGIA_ORIGINAL = topologia + ".brite";
		ARQUIVO_TOPOLOGIA_DATACENTER = topologia + ".datacenter";

		//Para o NS2
		String modeloNS2 = PATH + "modeloNS2_" + 
				NUM_DATACENTERS+"data_"+
				NUM_SERVIDORES+"serv_"+
				NUM_VMS+"vm";
		ARQUIVO_MODELO_NS2 = modeloNS2 + ".tcl";
		ARQUIVO_NAM_NS2 = modeloNS2 + ".nam";
		ARQUIVO_RESULT_NS2 = modeloNS2 + ".tr";
		ARQUIVO_VAZAO_NS2 = modeloNS2 + ".vazao";		
		ARQUIVO_PERDA_NS2 = modeloNS2 + ".perda";		
		ARQUIVO_ATRASOMEDIO_NS2 = modeloNS2 + ".atrasoMedio";
		ARQUIVO_EVOLUCAO_VAZAO = modeloNS2 + ".evolucaoVazao";
		ARQUIVO_EVOLUCAO_PERDA = modeloNS2 + ".evolucaoPerda";		
		ARQUIVO_EVOLUCAO_ATRASO = modeloNS2 + ".evolucaoAtraso";		

		//To FF allocation policy
		LOG_FF = PATH + "modeloFF_" + 
				NUM_DATACENTERS+"data_" +
				NUM_SERVIDORES+"serv_" +
				NUM_VMS+"vm" +
				".log";


		//Porcentagem para cada tipo de servidor nos datacenters
		//Nota: se as porcentagens nao forem normalizadas, o restante fica como huge
		//Para o caso de se querer gerar proporcoes pequenas 
		//Ex.: (1 servidor por datacenter: 3data_1serv_...), incluir a linha abaixo
		NUM_SERVIDORES_SMALL=(int)(NUM_SERVIDORES*0.6);
		//NUM_SERVIDORES_SMALL=(int)(NUM_SERVIDORES*0.6);
		NUM_SERVIDORES_LARGE=(int)(NUM_SERVIDORES*0.3);
		NUM_SERVIDORES_HUGE=(int)(NUM_SERVIDORES*0.1);

		System.out.println(NUM_SERVIDORES + " " + NUM_SERVIDORES_SMALL + " " + NUM_SERVIDORES_LARGE + " " + NUM_SERVIDORES_HUGE);
		//System.exit(0);

		//Configuracoes dos Servidores
		//
		//CPU (unidades)
		SERVER_SMALL_CPU=4;
		SERVER_LARGE_CPU=8;
		SERVER_HUGE_CPU=32;	

		//RAM (unidades)
		SERVER_SMALL_RAM=20;
		SERVER_LARGE_RAM=80;
		SERVER_HUGE_RAM=1600;

		//Disk (unidades)
		SERVER_SMALL_DISK=160;
		SERVER_LARGE_DISK=800;
		SERVER_HUGE_DISK=16000;

		//Bandwidth (unidades)
		SERVER_SMALL_BW=1000;
		SERVER_LARGE_BW=1000;
		SERVER_HUGE_BW=1000;

		//Link do datacenter com a topologia
		DATACENTER_LINK_BACKBONE = NUM_SERVIDORES*SERVER_HUGE_BW;
		//DATACENTER_LINK_BACKBONE = 10000;

		//Custos de CPU
		SERVER_COST_SMALL_CPU=0;
		SERVER_COST_LARGE_CPU=0;
		SERVER_COST_HUGE_CPU=0;

		//Custos de RAM

		//SERVER_COST_SMALL_RAM=1;
		//SERVER_COST_LARGE_RAM=2;
		SERVER_COST_HUGE_RAM=1;

		//Custos de Disk
		//SERVER_COST_SMALL_DISK=1;
		//SERVER_COST_LARGE_DISK=2;
		SERVER_COST_HUGE_DISK=1;

		//Custos de Link
		//SERVER_COST_SMALL_BW=1;
		//SERVER_COST_LARGE_BW=2;
		SERVER_COST_HUGE_BW=1;

		//---Requisicoes das VMs		
		VM_SMALL_CPU=4;
		//VM_LARGE_CPU=2;
		//VM_HUGE_CPU=4;

		//GB		
		VM_SMALL_RAM=10;
		//VM_LARGE_RAM=40;
		//VM_HUGE_RAM=80;

		//GB
		VM_SMALL_DISK=80;
		//VM_LARGE_DISK=400;
		//VM_HUGE_DISK=800;

		//GB		
		VM_SMALL_BW=2;
		//VM_LARGE_BW=5;
		//VM_HUGE_BW=10;

		//GB
		VM_SMALL_FLUXO=2;
		//VM_LARGE_FLUXO=5;
		//VM_HUGE_FLUXO=10;

		//Buffer dos roteadores
		NET_BUFFER = 10;

		//Para todos os datacenters: Burst_time, idle_time e rate por datacenter
		int i=0;
		NET_PACKET_SIZE = new int[NUM_DATACENTERS];
		NET_BURST_TIME = new int[NUM_DATACENTERS];
		NET_IDLE_TIME = new int[NUM_DATACENTERS];
		NET_RATE = new int[NUM_DATACENTERS];
		while(i<NUM_DATACENTERS){
			NET_PACKET_SIZE[i] = 1500;
			NET_BURST_TIME[i] = 400;
			NET_IDLE_TIME[i] = 100;
			NET_RATE[i] = 1000;
			i++;
		}//fim while
		//--Configuracoes especificas por datacenter sao feitas aqui
		//Datacenter0
		NET_PACKET_SIZE[0] = 1500;
		NET_BURST_TIME[0]= 400;
		NET_IDLE_TIME[0]=100;
		NET_RATE[0]=1050;

		//Datacenter1
		NET_PACKET_SIZE[1] = 1500;
		NET_BURST_TIME[1]= 400;
		NET_IDLE_TIME[1]=100;
		NET_RATE[1]=1038;

		//Datacenter2
		NET_PACKET_SIZE[2] = 1500;
		NET_BURST_TIME[2]= 400;
		NET_IDLE_TIME[2]=100;
		NET_RATE[2]=1038;
		
		new A_FF_Policy();

	}//fim main

}//fim classe
