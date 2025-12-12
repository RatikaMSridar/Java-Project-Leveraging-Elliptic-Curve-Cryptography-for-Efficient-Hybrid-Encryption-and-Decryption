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
 * Simulador para Alocacao de VMs em Servidores com:
 * - Pesquisa Operacional
 * - Simulador de rede
 * - Algoritmo Genetico
 * 
 * para tratar o problema da alocacao e escalonamento em nuvens
 * 
 * Nota: os servidores sao atribuidos a novos nodos na topologia.
 * 
 * Ex.:        
 *             9     
 *             |                 
 *         3---0---4
 *         |   |   |
 *         5--(6)--7
 *         |   |   |
 *         1---8---2
 *         |       |
 *        10      11    
 * 
 * Nesse novo modelo, servidores nao sao representados na topologia 
 * 
 * Cada roteador possui um indice proprio, que incrementa a partir dos nodos
 * da topologia de rede original. Eh importante considerar que os links sao
 * orientados na descricao (ou seja, 6--7 eh diferente de 7--6)
 * 
 * Nesse exemplo, os roteadores 9, 10 e 11 sao os roteadores de borda dos datacenters
 * O roteador de nucleo central representa um roteador para um backbone com a Internet
 * 
 * @author Lucio Agostinho Rocha
 * Ultima atualizacao: 01/01/2013
 *
 */

public class A_PLI_AG_NS2_milpFlow { 

	//Para a fronteira de Pareto
	//0: sem limite do consumo de energia
	//private static String LIMITE_W1="0";

	//private static double ALFA;

	//private static double BETA;

	//Executa apenas uma iteracao (para fins de depuracao)
	private static boolean UMA_ITERACAO=false;

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

	//numero de datacenters
	public static int NUM_DATACENTERS;	
	//numero de servidores
	public static int NUM_SERVIDORES;
	//numeroVMs inicialmente requisitas por servidor
	public static int NUM_VMS;
	//each datacenter has one destination node (backbone)
	public static int [] NODO_DESTINO;

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
	public static long tempoFim, tempoInicio;

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

	//To acquire MILP flows to generate NS-2 routes
	private static String [][] MILP_FLOWS;

	public A_PLI_AG_NS2_milpFlow(){  

		parserLingoFluxo();

/*---		gerarModeloNS2_milpFlows();
		
		executarModeloNS2();
		
		realizarParserVazao();
		
		realizarParserPerda();
---*/		

		/*tempoInicio = System.currentTimeMillis();

		if (NOVA_TOPOLOGIA){
			//Cria os Servidores
			criarServidores();

			//Cria a cloud de VMs
			criarCloud();

			//Criar topologia de datacenter, servidores e rede
			criarTopologiaDatacenter();

		}//fim if

		//Algoritmo genetico gera populacao inicial com base na topologia do datacenter 
		inicializarPopulacao();

		//Inicializa apenas uma vez os arquivos para as threads
		if (EXECUTAR_PARALELO)
			inicializarArquivosThreads();

		//Avalia os individuos gerados (Para testar se a populacao inicial eh factivel)
		//?????????DESCOMENTAR AO TERMINAR PUBLISH/SUBSCRIBE_avaliar("P");

		exibirPopulacao(P);

		//Ordena os individuos de P de acordo com o fitness
		//Os individuos com maiores fitness sao listados primeiro
		int campoFitness=P[0].length-1;
		for (int pass=1; pass<TAM_POPULACAO; pass++)//passagens
			for(int i=0; i<TAM_POPULACAO-1; i++)//uma passagem
				if (Double.parseDouble(P[i][campoFitness])<Double.parseDouble(P[i+1][campoFitness]))
					troca(i,i+1);		

		//System.exit(0);

		//System.exit(0);

		//Lista as rotas (Para o MPLS)
		//listarRotas();


		//boolean melhorouFitness=true;
		//double vazaoTotalVMs=0;
		double perdaPacotesTotal=1000; //taxa de perda minima (unidades)
		int contadorListaTabu=0;

		int i=1;		
		int iteracoesSemMelhora=0;		

		double melhorFitnessAntes=0;
		double melhorFitnessP3=0;
		double melhorFitnessP2=0;
		double fitnessCromossomo=0;
		int p=0;
		int q=0;
		double fitnessMedio=0;
		//double pacotesPerdidosVMs=1000; //taxa de perda minima (unidades)

		//A cada iteracao avalia toda a populacao
		while(i<=MAX_ITERACOES ){ 
			//iteracoesSemMelhora<=5 ){ //Se ao final dessas iteracoes nao melhorar o fitness, nova geracao eh iniciada

			//while(i<=MAX_ITERACOES&&PERDA_PACOTES_TOTAL>0){

			//P2 = seleciona (P)
			if (SELECIONAR_ALEATORIO)
				selecionarAleatorio();
			else
				selecionar();			

			//P2 = reproduz (P2)
			reproduzir();

			//P2 = varia (P2)
			variar();

			//P2 = avaliar (P2)
			if (EXECUTAR_PARALELO){
				melhorFitnessP3=avaliarParalelo();												
				//exibir(P2);				
			} else 
				if (EXECUTAR_PUBLISH_SUBSCRIBE){
					melhorFitnessP3=avaliarParaleloPublishSubscribe();
				} else {
					//Avaliacao sequencial
					melhorFitnessP3=avaliar("P2");
				}//fim else

			//Adquire o melhor fitness anterior, antes de renovar a populacao
			melhorFitnessAntes=Double.parseDouble(P[P.length-1][P[P.length-1].length-1]);

			if(melhorFitnessP3<melhorFitnessAntes){
				System.out.println("\nOcorreu melhora no fitness: melhorFitnessAntes:" + melhorFitnessAntes + " melhorFitness: " + melhorFitnessP3);
				melhorFitnessAntes=melhorFitnessP3;
				iteracoesSemMelhora=0;
			} else{
				System.out.println("\nNao ocorreu melhora no fitness: melhorFitnessAntes:" + melhorFitnessAntes + " melhorFitness: " + melhorFitnessP3);
				iteracoesSemMelhora++;
			}//fim else

			//fim da epoca -> renova populacao
			if(UMA_ITERACAO){
				tempoFim = System.currentTimeMillis();
				System.out.println("Tempo de execucao (ms): " + (tempoFim-tempoInicio));
				System.out.println("Tempo de execucao (s): " + (tempoFim-tempoInicio)/1000);				
				System.out.println("\n\nFim de 1 iteracao\n\n");
				System.exit(0);				
			} else
				renovarPopulacao();

			//Grava o fitness medio de todos os individuos ao renovar a populacao, no arquivo .evolucaoFitnessPopulacao
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_EVOLUCAO_FITNESS_POPULACAO,true));					
				//out.write("\n"+P[p][P[p].length-1]);
				q=0;
				fitnessMedio=0;
				while(q<P.length){
					fitnessMedio+=Double.parseDouble(P[q][P[q].length-1]);						
					q++;
				}//end while
				fitnessMedio = fitnessMedio/P.length;
				out.write("\nIteracao[" + i + "] MelhorFitness: " + P3[0][P3[0].length-1] + " FitnessMedio: " + fitnessMedio);
				out.close();
			} catch(Exception e){
				System.out.println("1Excecao ao gravar no arquivo." + e.getMessage());			
			}//fim catch


			//Grava o melhor fitness (Para acompanhar a evolucao a cada iteracao)
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_EVOLUCAO_FITNESS,true));					
				out.write("\nIteracao["+i+"] Custos: "); 
				p=0;
				while(p<P3[0].length-1){
					out.write(bin2dec(P3[0][p]) + " ");
					p++;
				}//fim while
				//O campo fitness nao deve ser convertido
				out.write(P3[0][p] + " ");				
				out.close();
			} catch(Exception e){
				System.out.println("1Excecao ao gravar no arquivo." + e.getMessage());			
			}//fim catch

			//Fim das iteracoes		
			tempoFim = System.currentTimeMillis();			

			//Executa novamente o melhor resultado, 
			//para obter os dados da melhor execucao
			//perdaPacotesTotal=avaliarMelhorResultado();

			System.out.println("\n---\nIteracao: " + i + "\n---");

			//Proxima geracao
			//Guarda a quantidade de iteracoes
			contadorIteracoes=i;
			i++;

			try{
				Thread t = new Thread(); 
				System.out.println("Fim da iteracao principal. Iteracoes sem melhora: " + iteracoesSemMelhora);
				//t.sleep(5000);
			} catch(Exception e){}

			NUM_GERACAO++;
			NUM_ITERACAO=0;

			//System.exit(0);

		}//fim while

		//Grava os custos da melhor alocacao ao final (appendResult=true)
		//Executa novamente o melhor resultado, 
		//para obter os dados da melhor execucao
		avaliar("P3");

		//Grava o melhor fitness da amostra (appendResult=true)
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_FITNESS_AMOSTRA,true));			
			out.write("\nMelhorFitnessAmostra: " + P3[0][P3[0].length-1] + " TempoExecucao: " + (tempoFim-tempoInicio)/1000);						
			out.close();
		} catch(Exception e){
			System.out.println("1Excecao ao gravar no arquivo." + e.getMessage());			
		}//fim catch		

		//Gravar em base de dados
		//gravarNodesBD();
		//gravarEdgesBD();
		//gravarServersBD();
		//gravarVMsBD();


		//Exibe a populacao final
		System.out.println("\n---\nPopulacao final:\n");
		exibirPopulacao(P3);
		System.out.println("\n---");


		//pacotesPerdidosVMs=adquirirPerdaPacotesVMs();
		//System.out.println("Vazao total: " + VAZAO_TOTAL_VMs);
		//System.out.println("Perda de pacotes total: " + perdaPacotesTotal);

		System.out.println("Tempo de execucao (ms): " + (tempoFim-tempoInicio));
		System.out.println("Tempo de execucao (s): " + (tempoFim-tempoInicio)/1000);		

		System.out.println("\n---\nFim da execucao.\n---");
		 */
		System.exit(0);

	}//fim construtor

	public void parserLingoFluxo(){

		//Acquire flows from Lingo result file.lgr 
		//
		//Open file result
		try {
			//File lines
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO));
			//Line parser
			String REGEX = "";
			Matcher matcher;
			Pattern pattern;

			//Count the number of flows
			int counter=0; 
			//Store flows
			StringBuffer listFlows = new StringBuffer();
			int from=0;
			int to=0;
			double flows=0;
			StringTokenizer token;
			while(linha!=null){				
				//Twho possible cases:
				//F( 1, 2)    18.0000    0.0000 
				//F( 1, 2)    18.0000
				REGEX = "F\\( (.*), (.*)\\)\\s(.*)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linha);
				if (matcher.find()){
					//In some cases, the matcher.group(3) will have more elements
					//F( 1, 2)    18.0000    0.0000 
					//F( 1, 2)    18.0000
					token = new StringTokenizer(matcher.group(3)," ");
					flows = Double.parseDouble(removerEspacos(token.nextToken()));
					System.out.println("From: " + matcher.group(1) + " To: " + matcher.group(2) + " Flow: " + flows);					
					//Write with NS-2 index, i.e., index-1
					from = Integer.parseInt(matcher.group(1))-1;
					to = Integer.parseInt(matcher.group(2))-1;
					
					listFlows.append(from + " " + to + " " + flows + " "); 
					counter++;
				}//end if
				
				linha=file.readLine();				
			}//end while

			//Store MILP flows to perform specific routing with NS-2
			//
			//Write 'from' and 'to' in the same MILP_FLOWS index
			//Ex.: From=1, To=2, Flow: 44 -> MILP_FLOWS[From][To]=44
			MILP_FLOWS = new String[counter][counter];
			//Initialize MILP_FLOWS
			int i=0;
			int j=0;
			while(i<MILP_FLOWS.length){
				j=0;
				while(j<MILP_FLOWS[i].length){					
					MILP_FLOWS[i][j]="0";
					j++;
				}//end while
				i++;
			}//end while

			StringTokenizer t = new StringTokenizer(listFlows.toString(), " ");
			while(t.hasMoreElements()){

				from = Integer.parseInt(t.nextToken());
				to = Integer.parseInt(t.nextToken());
				flows = Double.parseDouble(t.nextToken());				
				MILP_FLOWS[from][to]=flows+"";

			}//end while

			/*
			//Show MILP_FLOWS
			i=0;
			j=0;
			while(i<MILP_FLOWS.length){
				j=0;
				while(j<MILP_FLOWS[i].length){
					if (Double.parseDouble(MILP_FLOWS[i][j])>0)
						System.out.println("From: " + i + " To: " + j + " Flow: " + MILP_FLOWS[i][j]);
					j++;
				}//end while				
				i++;
			}//end while
			//System.exit(0);
			//System.out.println("Passei por aqui.");
			 */

		} catch (Exception e){
			System.out.println("\nException openning Lingo .lgr file: " + ARQUIVO_RESULT_LINGO + " " + e.getMessage());	
		}//end catch

	}//end parserLingoFluxo

	public void gerarModeloNS2_milpFlows(){	

		System.out.println("\n--Gerar Modelo NS2 (roteamento MILP)--");

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
			NUM_EDGES = Integer.parseInt(token.nextToken());
			//Guarda todas as informacoes dos Edges
			//System.out.println("numEdges: " + numEdges);
			//EDGE[0][EdgeId From To Length Delay Bandwidth QueueLimit ASto Type Other]
			//    [1][EdgeId From To Length Delay Bandwidth QueueLimit ASto Type Other]
			//    ...
			int campoEdgeId=0;
			int campoEdgeFrom=1;
			int campoEdgeTo=2;
			int campoEdgeCapLink=3;
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
			//conteudoNS2.append("\nset NODO_DESTINO ");
			//conteudoNS2.append(nodoDestino);
			conteudoNS2.append("\n#set NUM_NODES_ORIGINAL ");
			conteudoNS2.append(numNodosOriginal);
			conteudoNS2.append("\nset NUM_NODES ");
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
			conteudoNS2.append("\nfor {set j 0} {$j < $NUM_NODES} {incr j} {");
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
			double linkBandwidth=0;
			while (i<EDGES_ARQUIVO.length){

				conteudoNS2.append("\nputs \"Link n("+EDGES_ARQUIVO[i][campoEdgeFrom]+")--n("+EDGES_ARQUIVO[i][campoEdgeTo]+")"+"\";flush stdout;");
				//Com a adicao de roteadores de borda e servidores
				//eh necessario acrescentar nodos ah topologia original
				//Campo indice
				if (Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeFrom])>=numNodosOriginal){			

					//Cria o nodo (it is not necessary for MILP_FLOWS)
					//conteudoNS2.append("\n   set n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") [$ns node]");
					conteudoNS2.append("\n$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") color blue");
					conteudoNS2.append("\n$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") shape hexagon");

					//indiceNodosNS2++;

				}//fim if

				linkBandwidth=(LIMITE_SUPERIOR_LINK * Double.parseDouble(EDGES_ARQUIVO[i][campoEdgeCapLink])) / FATOR_CORRECAO;
				conteudoNS2.append("\n$ns duplex-link " +
						//From
						"$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") " +
						//To
						"$n("+EDGES_ARQUIVO[i][campoEdgeTo]+") " + 
						//Bandwidth
						linkBandwidth +
						//EDGES_ARQUIVO[i][campoEdgeBw] + "kb " +
						"kb " +
						//Outros parametros
				"10ms DropTail");
				
				banda.append(EDGES_ARQUIVO[i][campoEdgeFrom] + "--" + EDGES_ARQUIVO[i][campoEdgeTo] + " " + linkBandwidth+"\n");

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

				
				i++;
			}//fim while

			//conteudoNS2.append("\n\n"+indiceNodosNS2);
			//Cria os LDP-peers
			//LDP (Label Distribution Protocol) eh o protocolo
			//utilizado pelo MPLS para mapear rotulos na rede
			conteudoNS2.append("\n\n#Cada par de LSR troca informacoes de roteamento por rotulos entre si");
			conteudoNS2.append("\n#for {set i 0} {$i < $NUM_NODES} {incr i} {");
			conteudoNS2.append("\n#   for {set j 0} {$j < $NUM_NODES} {incr j} {");
			conteudoNS2.append("\n#      if {$i != $j} {");
			conteudoNS2.append("\n#         set a n($i)");
			conteudoNS2.append("\n#         set b n($j)");
			conteudoNS2.append("\n#         eval $ns LDP-peer $$a $$b");
			conteudoNS2.append("\n#         #puts \"$a $b\"");
			conteudoNS2.append("\n#      };#fim if");
			conteudoNS2.append("\n#   };#fim for");
			conteudoNS2.append("\n#};#fim for");

			conteudoNS2.append("\n\n#Atribui cores ah mensagens do protocolo LDP");
			conteudoNS2.append("\n$ns ldp-request-color 		blue");
			conteudoNS2.append("\n$ns ldp-mapping-color 		red");
			conteudoNS2.append("\n$ns ldp-withdraw-color 		magenta");
			conteudoNS2.append("\n$ns ldp-release-color 		orange");
			conteudoNS2.append("\n$ns ldp-notification-color 	yellow");


			//Create traffic pairs between each MILP_FLOWS
			conteudoNS2.append("\n\n#Destination sinks");
			//Ex.: listSink = "1 13 2 ..."
			StringBuffer listSink = new StringBuffer();
			int sink=0;
			int p=0;
			int q=0;
			while(p<MILP_FLOWS.length){
				q=0;
				while(q<MILP_FLOWS[p].length){
					//If exist flow in this link
					//System.out.println("\nMILP_FLOWS[p][q]: " + MILP_FLOWS[p][q]);
					if(Double.parseDouble(MILP_FLOWS[p][q])>0){
						//If exist flow, then define a sink for this pair of nodes
						//
						//System.out.println("\nMILP_FLOWS[p][q]: " + MILP_FLOWS[p][q]);
						sink = q;
						if(!existeSink(listSink, sink)){

							if (TIPO_APLICACAO_REDE.equals("CBR/UDP"))
								conteudoNS2.append("\nset sink" + sink + " [new Agent/Null]");
							else
								if (TIPO_APLICACAO_REDE.equals("FTP/TCP"))
									conteudoNS2.append("\nset sink" + sink + " [new Agent/TCPSink]");
								else
									if (TIPO_APLICACAO_REDE.equals("Exp/UDP"))
										conteudoNS2.append("\nset sink" + sink + " [new Agent/Null]");

							conteudoNS2.append("\n$ns attach-agent $n(" + sink + ") $sink"+sink);

							//Append new sink to listSink
							listSink.append(sink + " ");

						}//end if

					}//end if
					//else, not create sink node					
					q++;
				}//end while

				//Next destination
				p++;
			}//end while

			//System.out.println(conteudoNS2.toString());
			//System.out.println("Passei por aqui");
			//System.exit(0);

			//Create individual flows between each pair of nodes,
			//according MILP_FLOWS
			p=0;
			int to;
			int from;
			double flow=0;
			while(p<MILP_FLOWS.length){
				q=0;
				while(q<MILP_FLOWS[p].length){

					//If exist flow
					if(Double.parseDouble(MILP_FLOWS[p][q])>0){

						//+50 to simulate a full link usage 
						flow = Double.parseDouble(MILP_FLOWS[p][q])+50;
						from = p;
						to = q;
						conteudoNS2.append("\nputs \"Link n("+ from +")--n("+ to + ")\";flush stdout;");
						if (TIPO_APLICACAO_REDE.equals("CBR/UDP")){
							conteudoNS2.append("   \n#Cria um agente UDP e atribui ao aglomerado");
							conteudoNS2.append("   \nset udp("+ from +") [new Agent/UDP]");
							//Fluxo comeca nos servidores
							conteudoNS2.append("   \n$ns attach-agent $n("+ from +") $udp("+ from +")");
							//Fluxo comeca nos roteadores de nucleo
							//conteudoNS2.append("   \n$ns attach-agent $n("+adquirirLigacaoCoreRouter(EDGES_ARQUIVO,indiceLigacao)+") $udp("+indiceLigacao+")");
							conteudoNS2.append("   \n#Cria um trafego CBR e atribui ao agent UDP");
							conteudoNS2.append("   \nset cbr("+from+") [new Application/Traffic/CBR]");
							conteudoNS2.append("   \n$cbr("+from+") set packetSize_ 1500");
							conteudoNS2.append("   \n#Fluxo ficticio para ocupar todo o link");
							conteudoNS2.append("   \n$cbr("+from+") set rate_ " + flow + "kb");
							conteudoNS2.append("   \n$cbr("+from+") attach-agent $udp("+from+")");
							conteudoNS2.append("   \n#Conecta o nodo ao agente sink do nodo destino");
							conteudoNS2.append("   \n$ns connect $udp("+from+") $sink"+to);
							conteudoNS2.append("   \n#Identifica o fluxo do aglomeradoVMs");
							conteudoNS2.append("   \n$udp("+from+") set fid_ "+from+"");
							//Cor do trafego (passo1)
							conteudoNS2.append("   \n$udp("+from+") set class_ 1");
							//Costs for links with flows is 1 (full-duplex)
							conteudoNS2.append("\n$ns cost " +
									//From
									"$n("+from+") " +
									//To
									"$n("+to+") " +
									"1");
							conteudoNS2.append("\n$ns cost " +
									//To
									"$n("+to+") " +
									//From
									"$n("+from+") " +
									"1");
							conteudoNS2.append("\n#Inicio da submissao do trafego");
							conteudoNS2.append("\n$ns at 0.0 \"$cbr("+from+") start\"");
						} else 
							if (TIPO_APLICACAO_REDE.equals("FTP/TCP")){
								conteudoNS2.append("   \n#Cria um agente TCP e atribui ao aglomerado");
								conteudoNS2.append("   \nset tcp("+from+") [new Agent/TCP]");
								conteudoNS2.append("   \n$ns attach-agent $n("+from+") $udp("+from+")");
								conteudoNS2.append("   \n#Cria um trafego FTP e atribui ao agent TCP");
								conteudoNS2.append("   \nset ftp("+from+") [new Application/FTP]");
								conteudoNS2.append("   \n$ftp("+from+") set packetSize_ 1500");
								conteudoNS2.append("   \n#Fluxo ficticio para ocupar todo o link");
								conteudoNS2.append("   \n$ftp("+from+") set rate_ " + flow + "kb");
								conteudoNS2.append("   \n$ftp("+from+") attach-agent $tcp("+from+")");
								conteudoNS2.append("   \n#Conecta o nodo ao agente sink do nodo destino");
								conteudoNS2.append("   \n$ns connect $tcp("+from+") $sink"+to);
								conteudoNS2.append("   \n#Identifica o fluxo do aglomeradoVMs");
								conteudoNS2.append("   \n$tcp("+from+") set fid_ "+from+"");
								//Cor do trafego (passo1)
								conteudoNS2.append("   \n$tcp("+from+") set class_ 1");
								//Costs for links with flows is 1 (full-duplex)
								conteudoNS2.append("\n$ns cost " +
										//From
										"$n("+from+") " +
										//To
										"$n("+to+") " +
										"1");
								conteudoNS2.append("\n$ns cost " +
										//To
										"$n("+to+") " +
										//From
										"$n("+from+") " +
										"1");
								conteudoNS2.append("\n#Inicio da submissao do trafego");
								conteudoNS2.append("\n$ns at 0.0 \"$ftp("+from+") start\"");
							} else 
								if (TIPO_APLICACAO_REDE.equals("Exp/UDP")){
									conteudoNS2.append("   \n#Cria um agente UDP e atribui ao aglomerado");
									conteudoNS2.append("   \nset udp("+from+") [new Agent/UDP]");
									conteudoNS2.append("   \n$ns attach-agent $n("+from+") $udp("+from+")");
									conteudoNS2.append("   \n#Cria um trafego Exponential e atribui ao agent UDP");
									conteudoNS2.append("   \nset exp("+from+") [new Application/Traffic/Exponential]");
									conteudoNS2.append("   \n$exp("+from+") set packetSize_ 1500");
									conteudoNS2.append("   \n#Fluxo ficticio para ocupar todo o link");
									conteudoNS2.append("   \n$exp("+from+") set rate_ " + flow + "kb");
									conteudoNS2.append("   \n$exp("+from+") set idle_time_ 100ms");
									conteudoNS2.append("   \n$exp("+from+") set burst_time_ 500ms");
									//conteudoNS2.append("   \n$exp("+indiceLigacao+") set rate_ " + NET_RATE[i] + "kb");
									//conteudoNS2.append("   \n$exp("+indiceLigacao+") set idle_time_ " + NET_IDLE_TIME[i] + "ms");
									//conteudoNS2.append("   \n$exp("+indiceLigacao+") set burst_time_ " + NET_BURST_TIME[i] + "ms");									
									conteudoNS2.append("   \n$exp("+from+") attach-agent $udp("+from+")");
									conteudoNS2.append("   \n#Conecta o nodo ao agente sink do nodo destino");
									conteudoNS2.append("   \n$ns connect $udp("+from+") $sink"+to);
									conteudoNS2.append("   \n#Identifica o fluxo do aglomeradoVMs");
									conteudoNS2.append("   \n$udp("+from+") set fid_ "+from+"");
									//Cor do trafego (passo1)
									conteudoNS2.append("   \n$udp("+from+") set class_ 1");
									//Costs for links with flows is 1 (full-duplex)
									conteudoNS2.append("\n$ns cost " +
											//From
											"$n("+from+") " +
											//To
											"$n("+to+") " +
											"1");
									conteudoNS2.append("\n$ns cost " +
											//To
											"$n("+to+") " +
											//From
											"$n("+from+") " +
											"1");
									conteudoNS2.append("\n#Inicio da submissao do trafego");
									conteudoNS2.append("\n$ns at 0.0 \"$exp("+from+") start\"");
								}//fim if

					}//end if
					q++;
				}//end while								
				p++;
			}//end while				

			conteudoNS2.append("\n\n#A partir dos novos links formados");
			conteudoNS2.append("\n#cada LSR atualiza as suas tabelas de rotulos para encaminhar pacotes");
			conteudoNS2.append("\n#for {set i 0} {$i < $NUM_NODES} {incr i} {");
			conteudoNS2.append("\n#   set a n($i)");
			conteudoNS2.append("\n#   set m [eval $$a get-module \"MPLS\"]");
			conteudoNS2.append("\n#   eval set n($i) $m");
			conteudoNS2.append("\n#};#fim for");

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

	}//fim gerarModeloNS2_milpFlows

	public boolean existeSink(StringBuffer listSink, int sink){

		boolean achouSink=false;

		StringTokenizer t = new StringTokenizer(listSink.toString(), " ");
		int elementoSink=0;
		while(t.hasMoreElements()&&!achouSink){
			elementoSink = Integer.parseInt(t.nextToken());
			if (elementoSink == sink)
				achouSink=true;

		}//end while

		return achouSink;

	}//end existeSink


	private double avaliarParaleloPublishSubscribe() {

		System.out.println("\n--Geracao: " + NUM_GERACAO + " Iteracao: " + NUM_ITERACAO + " --");
		System.out.println("\n---avaliarParaleloPublishSubscribe");

		//Semelhante ao paralelismo com threads, mas
		//com workers que devem ser instanciados remotamente para
		//recolher os dados da base de tasks

		int i=0;
		int j=0;

		String PUBLISH_URL = PUBLISH_BASE_URL + PUBLISH_KEY; //Exemplo: .../EventChannel/task

		//Armazena o ID dos jobs submetidos
		LinkedList listaIDJobs = new LinkedList();
		while(i<P2.length){

			try {
				//Mantem a URL de insercao do conteudo
				URL url = new URL(PUBLISH_URL);
				HttpURLConnection request = (HttpURLConnection)url.openConnection();

				request.setUseCaches(false);
				request.setDoOutput(true);
				request.setDoInput(true);

				request.setFollowRedirects(false);
				request.setInstanceFollowRedirects(false);

				request.setRequestProperty("Content-Type", PUBLISH_CONTENT_TYPE);

				//---POST
				//Insere o conteudo dos custos dos links no 
				//payload da mensagem HTTP
				//Ex.: { "1": [ 1 2 4 ... ]}
				PUBLISH_VALUE = "{\"" + i + "\": [ ";
				PUBLISH_VALUE += 
					NUM_DATACENTERS + " " + 
					NUM_SERVIDORES + " " + 
					NUM_VMS + " " +
					VM_SMALL_CPU + " " +
					VM_SMALL_BW + " " + 
					SERVER_SMALL_BW + " " +
					NUM_SERVIDORES_SMALL + " " +
					NUM_SERVIDORES_LARGE + " " +
					NUM_SERVIDORES_HUGE + " " +
					LIMITE_SUPERIOR_LINK + " " +					
					FATOR_CORRECAO + " " +		
					TIPO_APLICACAO_REDE + " " +
					NET_BUFFER + " " +
					//NET_PACKET_SIZE + " " +
					//NET_RATE + " " +
					//NET_IDLE_TIME + " " +
					//NET_BURST_TIME + " " +
					DATACENTER_LINK_BACKBONE + " ";

				j=0;
				while (j<P2[i].length){
					PUBLISH_VALUE += bin2dec(P2[i][j]) + " ";					
					j++;
				}//fim while
				PUBLISH_VALUE += "]}";				
				//System.out.println(PUBLISH_VALUE);

				request.setRequestProperty("Content-Length", 
						String.valueOf(PUBLISH_VALUE.length()));

				request.setRequestMethod("POST");
				OutputStreamWriter post = 
					new OutputStreamWriter(request.getOutputStream());
				post.write(PUBLISH_VALUE);
				post.flush();
				post.close();

				//Eh necessario adquirir a resposta do servlet
				int retcode = request.getResponseCode();
				//System.out.println("Ret Code: " + retcode);

				if(retcode == 200) {
					BufferedReader in = 
						new BufferedReader(new InputStreamReader(request.getInputStream()));
					String inputLine="";
					String content="";
					while ((inputLine = in.readLine()) != null) {
						content += inputLine;
					}//fim while
					//System.out.println("Content:" + content);
					in.close();					
				}//fim if				

				//Fecha a conexao (gera estouro de buffer)
				//request.disconnect();

			} catch (IOException e) {
				e.printStackTrace();
			}//fim catch	

			//Proximo individuo de P2
			//
			//Adiciona o ID do job ah lista ligada
			listaIDJobs.add("job_" + i);
			i++;

		}//fim while

		//Espera ocupada enquanto existirem tarefas a serem processadas
		double melhorFitness=0;
		double fitness=0;
		int q=0;

		boolean processouTodosJobs=false;
		PUBLISH_URL = PUBLISH_BASE_URL + "result";
		int IDJob = 0;
		Thread ticks = new Thread();
		System.out.println("Jobs pending...");
		while(!listaIDJobs.isEmpty()){
			try {
				//Mantem a URL de insercao do conteudo
				//System.out.println("Passei por aqui: " + PUBLISH_URL);
				URL url = new URL(PUBLISH_URL);
				HttpURLConnection request = (HttpURLConnection)url.openConnection();
				int retcode = request.getResponseCode();
				//System.out.println("Ret Code: " + retcode);

				if(retcode == 200) {
					BufferedReader in = 
						new BufferedReader(new InputStreamReader(request.getInputStream()));
					String inputLine="";
					String content="";
					while ((inputLine = in.readLine()) != null) {
						content += inputLine + "\n";
					}//fim while
					//System.out.println("Content: " + content);
					in.close();					

					//Parser para atualizar P2 com o novo conteudo, no indice do job processado
					atualizarP2_PublishSubscribe(content);

					//Remove o job de acordo com o valor de sua chave unica
					IDJob = recuperarIDJob(content);
					listaIDJobs.remove("job_" + IDJob);

				} else {
					//System.out.println("Jobs pending...");
				}//fim else

			} catch (IOException e) {
				e.printStackTrace();
			}//fim catch

			try{
				ticks.sleep(100);
			} catch (Exception e){
				System.out.println("Excecao no tempo de espera: ");
				e.printStackTrace();
			}//fim catch

		}//fim while

		i=0;
		while(i<P2.length){
			//Adquire o melhor fitness da populacao		
			fitness=Double.parseDouble(P2[i][P2[i].length-1]);

			melhorFitness=Double.parseDouble(P3[0][P3[0].length-1]);
			System.out.println("Fitness: "+fitness + " MelhorFitness:" + melhorFitness);

			//Melhor individuo eh o que possui o menor fitness(da populacao renovada P)
			if (fitness<melhorFitness){

				melhorFitness=fitness;				

				//Guarda a melhor sequencia
				System.out.println("Melhor fitness encontrado: ");
				q=0;
				while(q<P2[i].length){
					P3[0][q]=P2[i][q];
					System.out.print(P3[0][q]+" ");
					q++;
				}//fim while

			}//fim if

			//Next chromossome
			i++;
		}//end while
		System.out.println("Populacao P2 atualizada pelos workers:");
		exibirPopulacao(P2);

		//System.exit(0);

		return melhorFitness;

	}//fim avaliarParaleloPublishSubscribe

	public void atualizarP2_PublishSubscribe(String content){

		//Extrai dados do conteudo processado
		StringTokenizer t = new StringTokenizer(content, "\"{[]}: ");

		int IDJob = Integer.parseInt(t.nextToken());
		//Salta os campos desnecessarios
		int i=0;
		int j=0;
		while(i<14){
			t.nextToken();
			i++;
		}//fim while

		//Atualiza P2 no mesmo indice do job processado
		i=0;
		j=0;
		//-1 por conta do campo de fitness
		//System.out.println("Tamanho de P2: " + (P2[IDJob].length-1));
		while(j<P2[IDJob].length-1){
			P2[IDJob][j]=dec2bin(Integer.parseInt(t.nextToken()),NUM_BITS);
			j++;
		}//fim while
		//Campo fitness
		P2[IDJob][j]=t.nextToken();

	}//fim atualizarP2_PublishSubscribe

	public int recuperarIDJob(String content){

		int IDJob=0;

		StringTokenizer t = new StringTokenizer(content, "\"{[]}: ");
		IDJob = Integer.parseInt(t.nextToken());

		return IDJob;

	}//fim recuperarIDJob

	//  Avaliar original
	public double avaliar(String tipo){

		//3 tipos de avaliacao sao possiveis: para P, P2 e P3
		String [][] T = null;

		//Copia antes
		int i=0;
		int j=0;
		if (tipo.equals("P")){
			T = new String[P.length][P[0].length];
			//Copia os dados para T
			while(i<P.length){
				j=0;
				while(j<P[i].length) {
					T[i][j]=P[i][j];
					j++;
				}//fim while				
				i++;
			}//fim while			
		} else
			if(tipo.equals("P2")){
				T = new String[P2.length][P2[0].length];
				//Copia os dados para T
				while(i<P2.length){
					j=0;
					while(j<P2[i].length) {
						T[i][j]=P2[i][j];
						j++;
					}//fim while				
					i++;
				}//fim while
			} else
				if(tipo.equals("P3")) {
					T = new String[P3.length][P3[0].length];
					//Copia os dados para T
					while(i<P3.length){
						j=0;
						while(j<P3[i].length) {
							T[i][j]=P3[i][j];
							j++;
						}//fim while				
						i++;
					}//fim while
				}//fim if

		double melhorFitness=0;

		System.out.println("\n---avaliar---");		

		//Para cada individuo da populacao P2
		double somatorioCapLinks=0;
		double somatorioPorcentagemPerda=0;
		double somatorioAtrasoMedio=0;
		double vazaoTotalVMs=0;		
		int campoFitness;
		int alelo=0;
		double fitness=0;
		i=0;
		j=0;
		int p=0;
		int q=0;

		//consumoEnergia[0] = consumoEnergiaServers
		//consumoEnergia[1] = consumoEnergiaRouters
		//double [] consumoEnergia = new double[2];

		//TEM que executar todos os individuos, para recolher aquele com o melhor fitness
		int iteracoes=0;
		if(UMA_ITERACAO)
			iteracoes=1;
		else
			iteracoes=T.length;

		while (i<iteracoes){	

			//Atualiza a topologia do datacenter com os dados do cromossomo atual
			//Nota: a funcao objetivo pode nao variar quando o custo for atribuido
			//      a um link sem fluxo;
			//Nota: observar todos os campos do cromossomo no arquivo .datacenter, para
			//      verificar a atualizacao
			//tipo: indica que a atualizacao ocorre para os individuos de P ou P2 ou P3
			atualizarTopologia(i,tipo);			

			System.out.println("\nAvaliar com dados do cromossomo de indice: [" + i + "]\n");
			System.out.println("\nPopulacao atual:\n");
			p=0;
			q=0;
			while(p<T.length){
				System.out.print("["+p+"] ");
				q=0;
				while(q<T[p].length-1){
					System.out.print(bin2dec(T[p][q])+" ");
					q++;
				}//fim while
				//Campo de fitness (double)
				System.out.print(T[p][q]+"\n");
				p++;
			}//fim while			

			//Exibe os custos dos links
			//System.out.println("\nCustos dos links do cromossomo de indice: [" + i + "]\n");
			//System.out.print("["+i+"] ");			
			//q=0;
			//while(q<P2[i].length){
			//	System.out.print(bin2dec(P2[i][q]+"")%4+" ");
			//	q++;
			//}//fim while
			//System.out.print("\n");


			try{
				Thread t1 = new Thread();
				//t1.sleep(5000);
			} catch (Exception e){}

			//Gera o modelo lingo com os parametros da populacao do AG

			//Para cada individuo gera o modelo lingo
			if (GERAR_MODELO_LINGO)
				//Transforma o conteudo do arquivo.datacenter em Linguagem Lingo
				//gerarModeloLingo();
				gerarModeloLingoCompacto(i);

			//Resolver Modelo Lingo
			if (EXECUTAR_MODELO_LINGO)
				executarModeloLingo();

			//Calcula a quantidade de alocacoes por servidor
			//Indica se a execucao do modelo Lingo eh factivel
			//Valor obtido no parserResultLingo
			boolean factivel=true;
			if (REALIZAR_PARSER_RESULT_LINGO){
				//Inicializa o consumoEnergia
				//consumoEnergia[0]=0;
				//consumoEnergia[1]=0;
				factivel = realizarParserResultLingo();					
				System.out.println("Factivel: " + factivel);
				System.out.println("Consumo servers: " + realizarParserConsumoEnergiaServidores());
				System.out.println("Consumo routers: " + realizarParserConsumoEnergiaRouters());
			}//fim if

			if (factivel){

				if (GERAR_MODELO_NS2)
					gerarModeloNS2_milpFlows();

				if (EXECUTAR_MODELO_NS2)
					executarModeloNS2();

				if (REALIZAR_PARSER_VAZAO)
					VAZAO_TOTAL_VMs=realizarParserVazao();

				if (REALIZAR_PARSER_PERDA)
					somatorioPorcentagemPerda=realizarParserPerda();
				//pacotesPerdidosVMs=adquirirPerdaPacotesVMs();

				//System.exit(0);

				//O atraso medio exige um buffer enorme -> fazer apos a simulacao
				if (REALIZAR_PARSER_ATRASOMEDIO)
					somatorioAtrasoMedio=realizarParserAtrasoMedio();

				//Atualiza o fitness do cromossomo			
				//campoFitness=(T[i].length)-1;		

				j=0;
				somatorioCapLinks=0;
				while(j<T[i].length-1){
					alelo=bin2dec(T[i][j]);				
					somatorioCapLinks += (LIMITE_SUPERIOR_LINK * alelo) / FATOR_CORRECAO;
					j++;
				}//fim while

				/*System.out.println("\nSomatorioCapLink: " + somatorioCapLinks);
				System.out.println("\nrealizarParserConsumoEnergiaServidores: " + realizarParserConsumoEnergiaServidores());
				System.out.println("\nrealizarParserConsumoEnergiaRouters: " + realizarParserConsumoEnergiaRouters());
				System.out.println("\nsomatorioPorcentagemPerda: " + somatorioPorcentagemPerda);
				System.out.println("\nsomatorioAtrasoMedio: " + somatorioAtrasoMedio);
				System.out.println("\nfitness: " + fitness);
				 */

				fitness=somatorioCapLinks+
				realizarParserConsumoEnergiaServidores()+
				realizarParserConsumoEnergiaRouters() + 
				somatorioPorcentagemPerda +
				somatorioAtrasoMedio;

			} else
				//Penaliza o individuo que nao possui solucao factivel
				fitness=888888;

			T[i][T[i].length-1]=fitness+"";

			//Grava o fitness de todos os individuos no arquivo .evolucaoFitness
			/*			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_EVOLUCAO_FITNESS,true));				
				out.write("\n"+fitness);
				out.close();
			} catch(Exception e){
				System.out.println("1Excecao ao gravar no arquivo." + e.getMessage());			
			}//fim catch
			 */

			melhorFitness=Double.parseDouble(P3[0][P3[0].length-1]);
			System.out.println("Fitness: "+fitness + " MelhorFitness:" + melhorFitness);

			//Melhor individuo eh o que possui o menor fitness(da populacao renovada P)
			if (fitness<melhorFitness){

				melhorFitness=fitness;				

				//Guarda a melhor sequencia
				q=0;
				while(q<T[i].length){
					P3[0][q]=T[i][q];
					q++;
				}//fim while

			}//fim if

			//Proximo cromossomo
			i++;

			try{
				Thread t = new Thread();
				//System.out.println("\nPerda pacotes total do individuo: " + perdaPacotesTotal);
				//System.out.println("\nPerda pacotes VMs: " + pacotesPerdidosVMs);
				//t.sleep(5000);
			} catch (Exception e){}

			NUM_ITERACAO++;

		}//fim while

		//Copia depois
		i=0;
		j=0;
		if (tipo.equals("P")){			
			//Copia os dados para T
			while(i<P.length){
				j=0;
				while(j<P[i].length) {
					P[i][j]=T[i][j];
					j++;
				}//fim while				
				i++;
			}//fim while			
		} else
			if(tipo.equals("P2")){
				//Copia os dados para T
				while(i<P2.length){
					j=0;
					while(j<P2[i].length) {
						P2[i][j]=T[i][j];
						j++;
					}//fim while				
					i++;
				}//fim while
			} else
				if(tipo.equals("P3")) {
					//Copia os dados para T
					while(i<P3.length){
						j=0;
						while(j<P3[i].length) {
							P3[i][j]=T[i][j];
							j++;
						}//fim while				
						i++;
					}//fim while
				}//fim if

		return melhorFitness;

	}//fim avaliar	

	/* Avaliar sem NS-2 e sem Lingo
 	public double avaliar(){

		System.out.println("\n---avaliar---");		

		//Para cada individuo da populacao P2
		double vazaoTotalVMs=0;
		double perdaPacotesTotal=1000;
		int campoFitness;
		double custoRede=0;
		double fitness=0;
		int i=0;
		int j=0;
		int p=0;
		int q=0;

		//consumoEnergia[0] = consumoEnergiaServers
		//consumoEnergia[1] = consumoEnergiaRouters
		double [] consumoEnergia = new double[2];

		double melhorFitness=0;

		//TEM que executar todos os individuos, para recolher aquele com o melhor fitness
		while (i<P2.length){	

			//Atualiza a topologia do datacenter com os dados do cromossomo atual
			//Nota: a funcao objetivo pode nao variar quando o custo for atribuido
			//      a um link sem fluxo;
			//Nota: observar todos os campos do cromossomo no arquivo .datacenter, para
			//      verificar a atualizacao
			//false: indica que nao eh o melhor resultado final
			atualizarTopologia(i,false);			
			System.out.println("\nAvaliar com dados do cromossomo de indice: [" + i + "]\n");
			System.out.println("\nPopulacao atual:\n");
			p=0;
			q=0;
			while(p<P2.length){
				System.out.print("["+p+"] ");
				q=0;
				while(q<P2[p].length-1){
					System.out.print(bin2dec(P2[p][q])+" ");
					q++;
				}//fim while
				//Campo de fitness (double)
				System.out.print(P2[p][q]+"\n");
				p++;
			}//fim while			

			//Exibe os custos dos links
			//System.out.println("\nCustos dos links do cromossomo de indice: [" + i + "]\n");
			//System.out.print("["+i+"] ");			
			//q=0;
			//while(q<P2[i].length){
			//	System.out.print(bin2dec(P2[i][q]+"")%4+" ");
			//	q++;
			//}//fim while
			//System.out.print("\n");


			try{
				Thread t1 = new Thread();
				//t1.sleep(5000);
			} catch (Exception e){}

			//Gera o modelo lingo com os parametros da populacao do AG

			//Para cada individuo gera o modelo lingo			

			//Atualiza o fitness do cromossomo			
			campoFitness=(P2[i].length)-1;		

			//Fitness = somatorio dos custos de rede do cromossomo +
			//			consumo de energia dos routers +
			//			consumo de energia dos servidores +
			//          somatorio da perda de pacotes
			j=0;
			while(j<P2[i].length){
				custoRede+=bin2dec(P2[i][j]);
				j++;
			}//fim while

			//Quer-se a rede com menor custo de links, menor consumo de energia, e menor perda de pacotes
			fitness=custoRede;

			P2[i][campoFitness]=fitness+"";

			//Grava o fitness de todos os individuos
			//try {
			//	BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_EVOLUCAO_FITNESS,true));				
			//	out.write("\n"+P2[0][P2[0].length-1]);
			//	out.close();
			//} catch(Exception e){
			//	System.out.println("1Excecao ao gravar no arquivo." + e.getMessage());			
			//}//fim catch						

			melhorFitness=Double.parseDouble(P3[0][P3[0].length-1]);
			System.out.println("Fitness: "+fitness + " MelhorFitness:" + melhorFitness);

			//Melhor individuo eh o que possui o menor fitness(da populacao renovada P)
			if (fitness<melhorFitness){

				melhorFitness=fitness;				

				//Guarda a melhor sequencia
				q=0;
				while(q<P2[i].length){
					P3[0][q]=P2[i][q];
					q++;
				}//fim while

			}//fim if

			i++;

		}//fim while

		return melhorFitness;

	}//fim avaliar	
	 */	

	public double avaliarParalelo(){

		double melhorFitness=0;
		double fitness=0;

		int i=0;
		int q=0;

		System.out.println("\n---avaliarParalelo---");		

		//Para cada individuo representado por um cromossomo da populacao P2
		//executa o passo de 'avaliar' em uma thread
		//retorna a perdaPacotesTotal ao final de cada thread

		try {
			//Cria uma lista de threads
			ExecutarThread [] t = new ExecutarThread[P2.length];
			i=0;
			while(i<P2.length){

				//Cria nova instancia da classe
				t[i] = new ExecutarThread();
				t[i].setPopulacao(i,P2);
				t[i].start();

				//Proximo cromossomo
				i++;			
			}//fim while 

			//Monitoramento das threads
			//
			//Enquanto as threads nao terminarem, ou
			//nao existir uma thread que indique que para ela a perda de pacotes eh zero
			System.out.println("Espera o fim da execucao das threads...");			
			//Espera a finalizacao de cada thread
			Thread t_sleep = new Thread();
			i=0;
			while(i<P2.length){
				while(t[i].isAlive()){
					//System.out.println("Monitor: " + "thread["+i+"] is alive. Entering in sleep mode...");
					try{
						t_sleep.sleep(50);
					} catch(Exception e){
						System.out.println("Excecao no sleep da thread ["+i+"]");
					}//fim catch
				}//fim while
				i++;
			}//fim while										
			System.out.println("Fim de execucao das threads.");

			//---
			//Recupera o que foi processado pelas threads
			//System.out.println("P2 antes:\n");
			//exibir(P2);			
			i=0;
			while(i<P2.length){
				//System.out.println("thread["+i+"]: " + t[i].getPerdaPacotesTotal());
				//perdaPacotesTotal = t[i].getPerdaPacotesTotal();

				//Atualiza P2 com P2_thread que foi processada na thread
				P2[i] = t[i].getP2Thread();

				fitness=Double.parseDouble(P2[i][P2[i].length-1]);

				melhorFitness=Double.parseDouble(P3[0][P3[0].length-1]);
				System.out.println("Fitness: "+fitness + " MelhorFitness:" + melhorFitness);

				//Melhor individuo eh o que possui o menor fitness(da populacao renovada P)
				if (fitness<melhorFitness){

					melhorFitness=fitness;				

					//Guarda a melhor sequencia
					q=0;
					while(q<P2[i].length){
						P3[0][q]=P2[i][q];
						q++;
					}//fim while

				}//fim if

				i++;
			}//fim while

		} catch (Exception e){
			System.out.println("Excecao thread: " + e.getMessage());
		}//fim try


		//long tempoFim = System.currentTimeMillis();
		//System.out.println("Tempo de execucao (ms): " + (tempoFim-tempoInicio));
		//System.out.println("Tempo de execucao (s): " + (tempoFim-tempoInicio)/1000);

		return melhorFitness;

	}//fim avaliarParalelo

	public double adquirirPerdaPacotesVMs(){

		double pacotesPerdidosVMs=0;

		try {
			//Linhas do arquivo
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_PERDA_NS2));

			//Salta o cabecalho
			linha=file.readLine();

			linha=file.readLine();
			while (linha!=null) {

				//System.out.println("Passei por aqui: " + linha);

				//Faz o parser da linha
				String REGEX = "";
				Matcher matcher;
				Pattern pattern;

				REGEX = "(.*)--(.*) (.*) (.*) (.*)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linha);
				if (matcher.find()){
					pacotesPerdidosVMs+=Double.parseDouble(matcher.group(4));
				}//fim if

				linha=file.readLine();
			}//fim while

			//close the file			
			file.close();

		} catch (Exception e){
			System.out.println("Excecao 20 ao abrir o arquivo: " + ARQUIVO_PERDA_NS2 + "\n" + e.getMessage());
		}//fim catch

		//System.exit(0);

		return pacotesPerdidosVMs;

	}//fim adquirirPerdaPacotesVMs

	public void renovarListaTabu(){

		int i=0;
		int j=0;
		while (i<LISTA_TABU.length){
			j=0;
			while(j<LISTA_TABU[i].length){
				LISTA_TABU[i][j]=0;
				j++;
			}//fim while
			i++;
		}//fim while		

	}//fim renovarListaTabu

	public void recuperarPopulacaoParalela(){

		try{
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_POPULACAO_PARALELA));
			StringBuffer linha = new StringBuffer();
			int i=0;
			int j=0;
			while(i<P2.length){
				linha.append(file.readLine());
				//Faz o parser da linha e atribui os campos ah estrutura P2
				StringTokenizer token = new StringTokenizer(linha.toString(), " ");
				j=0;
				while(j<P2[i].length){
					P2[i][j]=token.nextToken()+"";
					j++;
				}//fim while				
				i++;
			}//fim while

			//Fecha o descritor do arquivo
			file.close();

		} catch(Exception e){
			System.out.println("Excecao 19: Excecao ao ler o arquivo: " + e.getMessage());
		}//fim catch

	}//fim recuperarPopulacaoParalela

	public void renovarPopulacao(){

		System.out.println("\n---renovarPopulacao---");

		if(DEBUG_PROGRAM){
			System.out.println("P antes");
			exibirPopulacao(P);
			System.out.println("P2:");
			exibirPopulacao(P2);
		}//fim if		

		int campoFitness=P[0].length-1;

		//Substitui os individuos de P que possuem os maiores fitness
		//ou seja, que sao listados primeiro
		int i=0;
		int j=0;
		while (i<P2.length){
			j=0;
			while(j<P2[i].length){
				P[i][j]=P2[i][j];
				j++;
			}//fim while			
			i++;
		}//fim while

		//Ordena os individuos de P de acordo com o fitness
		//Os individuos com maiores fitness sao listados primeiro
		for (int pass=1; pass<TAM_POPULACAO; pass++)//passagens
			for(i=0; i<TAM_POPULACAO-1; i++)//uma passagem
				if (Double.parseDouble(P[i][campoFitness])<Double.parseDouble(P[i+1][campoFitness]))
					troca(i,i+1);		

		if(DEBUG_PROGRAM){
			System.out.println("P depois");
			exibirPopulacao(P);
		}//fim if

		//System.exit(0);

	}//fim renovarPopulacao

	public void troca(int i1, int i2){

		int numCampos=P[0].length;

		String aux[] = new String[numCampos]; 

		int i=0;
		while(i<numCampos){
			aux[i]=P[i1][i];
			i++;
		}//fim while

		i=0;
		while(i<numCampos){
			P[i1][i]=P[i2][i];
			i++;
		}//fim while

		i=0;
		while(i<numCampos){
			P[i2][i]=aux[i];
			i++;
		}//fim while

	}//fim troca

	public void selecionarAleatorio(){

		System.out.println("\n---selecionarAleatorio---");

		int i=0;
		int j=0;
		int p=0;
		int cromoSelecionado=0;
		while (i<TAM_POPULACAO/2){
			//while (i<TAM_POPULACAO){
			//Probabilidade da cromo ser selecionada
			p = (int) Math.round(0 + Math.random() * (TAM_POPULACAO-1));

			cromoSelecionado = p;

			//Copia a cromo selecionada para
			//a matriz de cromos para reproducao
			j=0; 
			////(NUM_DATACENTERS*NUM_SERVIDORES)+NUM_EDGES+fitness
			while (j<P[cromoSelecionado].length){
				P2[i][j] = P[cromoSelecionado][j]+"";
				j++;
			}//fim while

			i++;
		}//fim while

		if (DEBUG_PROGRAM){
			System.out.println("P2");
			exibirPopulacao(P2);
		}//fim if

	}//fim selecionarAleatorio

	public void selecionar(){

		System.out.println("\n---selecionar---");

		String [] roleta = new String[TAM_POPULACAO];
		//O vetor eh passado por referencia. Entao,
		//o que for feito no metodo para o vetor
		//sera atribuido aqui tambem
		gerarRoletaInvertida(roleta);

		int i=0;
		int j=0;
		double p=0;
		int cromoSelecionado=0;
		while (i<TAM_POPULACAO/2){
			//while (i<TAM_POPULACAO){
			//Probabilidade da cromo ser selecionada
			p = (double) Math.random();

			cromoSelecionado = individuoRoletaInvertida(roleta,p);

			//Copia a cromo selecionada para
			//a matriz de cromos para reproducao
			j=0; 
			////(NUM_DATACENTERS*NUM_SERVIDORES)+NUM_EDGES+fitness
			while (j<P[cromoSelecionado].length){
				P2[i][j] = P[cromoSelecionado][j]+"";
				j++;
			}//fim while

			i++;
		}//fim while

		if (DEBUG_PROGRAM){
			System.out.println("P2");
			exibirPopulacao(P2);
		}//fim if

	}//fim selecionar

	public void reproduzir(){

		System.out.println("\n---reproduzir---");

		//P antes:
		//P = | custo_serv_a custo_serv_b ... fit |
		//P = | 000 000 000 000 000 | 5 |
		//    | 111 111 111 111 111 | 6 |
		//...
		//ex.: ponto de crossover: indice 3
		//
		//P depois:
		//P = | 000 000 000 111 111 | 5 |
		//    | 111 111 111 000 000 | 6 |

		int pontoCrossover=0;

		//Faz o cruzamento das regras, 2 a 2
		//
		//Cada par possui o seu proprio ponto de crossover
		int i=0;
		int j=0;
		int k=0;
		String [] auxA;
		String [] auxB;

		String [] auxC;
		String [] auxD;

		while (i<P2.length){

			//Ex.: P2[i].length=4
			//     random gera valores entre [0,3]
			//mas 3 eh o proprio indice do fitness
			//entao reduzo 2 unidades
			pontoCrossover = (int) Math.round(0 + Math.random() * (P2[i].length-2));
			//System.out.println("Ponto de crossover: " + pontoCrossover);
			//P = | custo_serv_a custo_serv_b ... fit |
			//P = | 000 000 000 111 111 | 5 |
			//    | 111 111 111 000 000 | 6 |			
			//...
			//Supondo um ponto de crossover no alelo 3

			auxA=new String[pontoCrossover];
			auxB=new String[(P2[i].length-1)-pontoCrossover];
			//auxA: [000] (guarda do inicio ao ponto de crossover)
			j=0;
			k=0;
			while (j<pontoCrossover){
				auxA[k]=P2[i][j];
				//System.out.println("auxA: " +auxA[k]);
				k++;
				j++;
			}//fim while
			//auxB: [11] (guarda do ponto de crossover para frente)
			k=0;
			while (k<(P2[i].length-1)-pontoCrossover){
				auxB[k]=P2[i][j];			
				//System.out.println("auxB: " +auxB[k]);
				k++;
				j++;
			}//fim while

			//---
			auxC=new String[pontoCrossover];
			auxD=new String[(P2[i].length-1)-pontoCrossover];
			//auxC: [111] (guarda do inicio ao ponto de crossover)
			j=0;
			k=0;
			while (j<pontoCrossover)
				auxC[k++]=P2[i+1][j++];
			//auxD: [00] (guarda do ponto de crossover para frente)
			k=0;
			while (k<(P2[i].length-1)-pontoCrossover)
				auxD[k++]=P2[i+1][j++];

			//Cromossomo 1
			j=0;
			k=0;
			while (j<pontoCrossover)
				P2[i][j++] = auxA[k++];
			k=0;
			while (k<(P2[i].length-1)-pontoCrossover)
				P2[i][j++] = auxD[k++];

			//Cromossomo 2
			j=0;
			k=0;
			while (j<pontoCrossover)
				P2[i+1][j++] = auxC[k++];
			k=0;
			while (k<(P2[i].length-1)-pontoCrossover)
				P2[i+1][j++] = auxB[k++];			

			i+=2;
		}//fim while

		if (DEBUG_PROGRAM){
			System.out.println("P2");
			exibirPopulacao(P2);
		}//fim if

	}//fim reproduzir

	public void variar(){

		System.out.println("\n---variar---");

		double p_mutacao;
		double p_populacao;
		int indiceAlelo;
		StringBuilder conteudoBinarioAlelo;
		int ponto1;
		char bit1;
		int ponto2;
		char bit2;
		String aux;

		int valorDecimal=0;

		int i=0;
		while (i<P2.length){
			//Probabilidade de mutacao no cromossomo			
			p_mutacao = (double)1/TAM_POPULACAO;
			p_populacao = (double) Math.random();

			//Faz a mutacao no cromossomo
			//Seleciona o alelo
			//Ex.: P2 = [ 00000011 ... 10101010 fitness]
			//          [  ...                         ]
			//
			//Ex.: P2[i].length=4
			//     random gera valores entre [0,3]
			//mas 3 eh o proprio indice do fitness
			//entao reduzo 2 unidades			
			indiceAlelo = (int) Math.round(0 + Math.random() * (P2[i].length-2));			

			//Atualiza a informacao no cromossomo
			P2[i][indiceAlelo]=dec2bin((int)Math.round(1 + Math.random() * MAX_DECIMAL_CROMOSSOMO),NUM_BITS);

			//System.out.println("Indice do alelo: " + indiceAlelo + " Ponto1: " + ponto1 + " Bit1: " + bit1 + " Ponto2: " + ponto2 + " Bit2: " + bit2);
			//System.out.println("Depois: " + P2[i][indiceAlelo]);			
			//System.exit(0);

			//Proximo cromossomo
			i++;			
		}//fim while

		if (DEBUG_PROGRAM){
			System.out.println("P2");
			exibirPopulacao(P2);
		}//fim if

	}//fim variar

	/*  //Variar original
	 * 	public void variar(){

		System.out.println("\n---variar---");

		double p_mutacao;
		double p_populacao;
		int indiceAlelo;
		StringBuilder conteudoBinarioAlelo;
		int ponto1;
		char bit1;
		int ponto2;
		char bit2;
		String aux;

		int i=0;
		while (i<P2.length){
			//Probabilidade de mutacao no cromossomo			
			p_mutacao = (double)1/TAM_POPULACAO;
			p_populacao = (double) Math.random();

			//Faz a mutacao no cromossomo
			//Seleciona o alelo
			//Ex.: P2 = [ 00000011 ... 10101010 fitness]
			//          [  ...                         ]
			//
			//Ex.: P2[i].length=4
			//     random gera valores entre [0,3]
			//mas 3 eh o proprio indice do fitness
			//entao reduzo 2 unidades			
			indiceAlelo = (int) Math.round(0 + Math.random() * (P2[i].length-2));			

			//System.out.println("Indice Alelo: " + indiceAlelo + " P2[i].length: "+P2[i].length);

			//Abre ele
			//conteudoBinarioAlelo = 10101010
			conteudoBinarioAlelo = new StringBuilder(P2[i][indiceAlelo]);
			//System.out.println("conteudoBinarioAlelo: " + conteudoBinarioAlelo);
			//Seleciona bits (aqui nao tem problema reduzir 1 unidade)
			ponto1 = (int) Math.round(0 + Math.random() * (NUM_BITS-1));
			ponto2 = (int) Math.round(0 + Math.random() * (NUM_BITS-1));			
			bit1 = conteudoBinarioAlelo.charAt(ponto1);
			bit2 = conteudoBinarioAlelo.charAt(ponto2);			
			//Troca bits dentro alelo
			conteudoBinarioAlelo.setCharAt(ponto1, bit2);
			conteudoBinarioAlelo.setCharAt(ponto2, bit1);
			//Atualiza a informacao no cromossomo
			P2[i][indiceAlelo]=conteudoBinarioAlelo.toString();

			//System.out.println("Indice do alelo: " + indiceAlelo + " Ponto1: " + ponto1 + " Bit1: " + bit1 + " Ponto2: " + ponto2 + " Bit2: " + bit2);
			//System.out.println("Depois: " + P2[i][indiceAlelo]);			
			//System.exit(0);

			//Proximo cromossomo
			i++;			
		}//fim while

		if (DEBUG_PROGRAM){
			System.out.println("P2");
			exibirPopulacao(P2);
		}//fim if

	}//fim variar
	 */


	public boolean routerComVazao(String nodo1){

		boolean result=false;

		//Abre o arquivo com o resultado da vazao
		//Caso o router nao seja encontrado, nao ocorre fluxo no router

		String nodoOrigem2="";
		String nodoDestino2="";	
		String vazao="";

		try
		{
			//Guarda as informacoes do arquivo
			StringBuffer info = new StringBuffer();

			//Linhas do arquivo
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_VAZAO_NS2));

			linha=file.readLine();
			while (linha!=null) {				
				//Faz o parser da linha
				String REGEX = "";
				Matcher matcher;
				Pattern pattern;

				REGEX = "(.*)--(.*) (.*) (.*) (.*)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linha);
				if (matcher.find()){
					nodoOrigem2=matcher.group(1);
					nodoDestino2=matcher.group(2);

					//Eh necessario verificar se existe informaçao
					//ah mais daquela provida pelo protocolo de roteamento
					vazao=matcher.group(5);

					//Verifica se existe vazao no router
					if(Double.parseDouble(vazao)>1000&&
							(nodo1.equals(nodoOrigem2)||nodo1.equals(nodoDestino2)))
						result=true;				

				}//fim if

				//Proxima linha
				linha=file.readLine();
			}//fim for			

			//close the file			
			file.close();	

			//Thread t = new Thread();
			//t.sleep(100000000);			

		} catch(Exception e){
			System.out.println("Excecao 10 ao abrir o arquivo: " + ARQUIVO_VAZAO_NS2 + "\n" + e.getMessage());
		}//fim catch		

		return result;

	}//fim routerComVazao

	public boolean linkComVazao(String nodoOrigem1, String nodoDestino1){

		boolean result=false;

		//Abre o arquivo com o resultado da vazao
		//Caso o link nao seja encontrado, nao ocorre fluxo no link
		//(Eh necessario verificar se a ordem inversa entre 
		//nodoOrigem e nodoDestino ocorre porque o canal eh duplex,
		//ou seja, 3--6 eh equivalente a 6--3)

		String nodoOrigem2="";
		String nodoDestino2="";	
		String vazao="";

		try
		{
			//Guarda as informacoes do arquivo
			StringBuffer info = new StringBuffer();

			//Linhas do arquivo
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_VAZAO_NS2));

			linha=file.readLine();
			while (linha!=null) {				
				//Faz o parser da linha
				String REGEX = "";
				Matcher matcher;
				Pattern pattern;

				REGEX = "(.*)--(.*) (.*) (.*) (.*)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linha);
				if (matcher.find()){
					nodoOrigem2=matcher.group(1);
					nodoDestino2=matcher.group(2);

					//Eh necessario verificar se existe informaçao
					//ah mais daquela provida pelo protocolo de roteamento
					vazao=matcher.group(5);

					//Verifica se existe vazao no link 
					//3--6 eh equivalente ah 6--3 para canal duplex
					if(Double.parseDouble(vazao)>1000&& 
							(nodoOrigem1.equals(nodoOrigem2)&&nodoDestino1.equals(nodoDestino2)||
									nodoOrigem1.equals(nodoDestino2)&&nodoDestino1.equals(nodoOrigem2))){
						result=true;

					}//fim if				

				}//fim if

				//Proxima linha
				linha=file.readLine();
			}//fim for			

			//close the file			
			file.close();	

			//Thread t = new Thread();
			//t.sleep(100000000);			

		} catch(Exception e){
			System.out.println("Excecao 11 ao abrir o arquivo: " + ARQUIVO_VAZAO_NS2 + "\n" + e.getMessage());
		}//fim catch		

		return result;

	}//fim linkComVazao

	/*	public void desativarLinksRouters(){

		//Remonta o arquivo para o NS2, apos a analise da vazao
		//Os links sem vazao sao desligados
		System.out.println("\n--Gerar Modelo NS2 (Desativa links e roteadores sem vazao)--");

		//Le o conteudo do arquivo da topologia
		String REGEX = "";
		Matcher matcher;
		Pattern pattern;

		//O indice dos servidores comeca a partir do ultimo indice dos nodos da topologia original
		//Utilizo apenas o campo Servers do arquivo

		try
		{
			int i=0;
			int j=0;

			//Guarda as informacoes do arquivo
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
			int numEdges = Integer.parseInt(token.nextToken());
			//Guarda todas as informacoes dos Edges
			//System.out.println("numEdges: " + numEdges);
			//EDGE[0][EdgeId From To Length Delay Bandwidth queueLimit ASto Type Other]
			//    [1][EdgeId From To Length Delay Bandwidth queueLimit ASto Type Other]
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

					//CONSUMO_ENERGIA_ROUTERS[campoEdgeFrom]=1;
					//CONSUMO_ENERGIA_ROUTERS[campoEdgeTo]=1;

					//Com a adicao de roteadores de borda e servidores
					//eh necessario acrescentar nodos ah topologia original
					if (Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeFrom])>=numNodosOriginal){			

						//Cria o nodo
						conteudoNS2.append("\n   set n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") [$ns node]");
						conteudoNS2.append("\n$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") color blue");
						conteudoNS2.append("\n$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") shape hexagon");

						//Nao incremento indiceNodosNS2 aqui, por conta de ter que desligar links

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
							EDGES_ARQUIVO[i][campoEdgeQueueLimit]);

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
					if (EDGES_ARQUIVO[i][campoEdgeType].equals("BORDER_ROUTER"))
						//banda.append("BorderRouter_"+(indiceNodosNS2+1)+"--"+EDGES[i][1]+" "+EDGES[i][2]+"\n");
						banda.append("BorderRouter_"+i+"--"+EDGES_ARQUIVO[i][campoEdgeTo]+" "+EDGES_ARQUIVO[i][campoEdgeBw]+"\n");
					else
						banda.append(EDGES_ARQUIVO[i][campoEdgeFrom]+"--"+EDGES_ARQUIVO[i][campoEdgeTo]+" "+EDGES_ARQUIVO[i][campoEdgeBw]+"\n");					

					//Verifica se existe vazao no link					
					if (Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeFrom])>=numNodosOriginal){
						if(!linkComVazao(indiceNodosNS2+"", EDGES_ARQUIVO[i][campoEdgeTo]))
							//Desabilita o link	
							conteudoNS2.append("\n$ns rtmodel-at 0.1 down " + "$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") " + "$n("+EDGES_ARQUIVO[i][campoEdgeTo]+")");
						//Desabilita o router
						if(!routerComVazao(indiceNodosNS2+""))
							conteudoNS2.append("\n   $n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") color red");
						if(!routerComVazao(EDGES_ARQUIVO[i][campoEdgeTo]))
							conteudoNS2.append("\n   $n("+EDGES_ARQUIVO[i][campoEdgeTo]+") color red");
						//Agora posso incrementar o indice
						indiceNodosNS2++;
					} else {
						if(!linkComVazao(EDGES_ARQUIVO[i][campoEdgeFrom], EDGES_ARQUIVO[i][campoEdgeTo]))
							//Desabilita o link	
							conteudoNS2.append("\n$ns rtmodel-at 0.1 down " + "$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") " + "$n("+EDGES_ARQUIVO[i][campoEdgeTo]+")");
						if(!routerComVazao(EDGES_ARQUIVO[i][campoEdgeFrom]+""))
							//Desabilita o router
							conteudoNS2.append("\n   $n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") color red");
						if(!routerComVazao(EDGES_ARQUIVO[i][campoEdgeTo]))
							conteudoNS2.append("\n   $n("+EDGES_ARQUIVO[i][campoEdgeTo]+") color red");						
					}//fim else


				}//fim if
				i++;
			}//fim while	

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
			//Faz o parser do resultado da alocacao
			try {
				//Abre o arquivo com o resultado Lingo			
				BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO));
				String linhaResult=arquivoResult.readLine();
				StringTokenizer t1;
				StringTokenizer t2;
				String alocacao="";
				String primeiroElemento="";
				String t2_vm="";
				String t2_server="";
				String t2_datacenter="";

				//Agrupa os fluxos gerados pelas VMs
				double [] fluxoGeradoAglomeradoVMs = new double[numNodes];

				//Para nao precisar ler o arquivo inteiro
				int alocacoesLidas=0;
				while (linhaResult!=null && alocacoesLidas<numServers){

					//Ex.:  AMOUNT_ALLOCS( 300)        1.000000            3.955556"
					REGEX = "AMOUNT\\_ALLOCS\\( (.*)\\)";
					pattern = Pattern.compile(REGEX);
					matcher = pattern.matcher(linhaResult);
					//Ideia: achou uma alocacao, verifica em qual datacenter ocorreu a alocacao
					if (matcher.find()){
						alocacoesLidas++;
						t2_server = matcher.group(1);
						//System.out.println("t2_server: " + t2_server);
						//Datacenter do servidor (-1 por conta do indice do Lingo)
						t2_datacenter = S_ARQUIVO[Integer.parseInt(t2_server)-1][I_SERVER_DATACENTER];
						//System.out.println(t2_datacenter);
						//O indice do fluxoGeradoAglomeradoVMs corresponde ao indice do border router
						//Ate aqui tenho a quantidade de VMs alocadas no datacenter
						t2 = new StringTokenizer(linhaResult, " ");
						//AMOUNT_ALLOCS(
						t2.nextToken();

						//300)
						t2.nextToken();

						//1.000000
						//O indice do fluxoGeradoAglomeradoVMs corresponde ao indice datacenter
						fluxoGeradoAglomeradoVMs[Integer.parseInt(t2_datacenter)]+=Double.parseDouble(t2.nextToken());

					}//fim if

					//Proxima linha do arquivo
					linhaResult = arquivoResult.readLine();

				}//fim while

				i=0;
				//Multiplica a quantidade de VMs alocadas pelo fluxo gerado por cada uma delas
				while(i<fluxoGeradoAglomeradoVMs.length){
					fluxoGeradoAglomeradoVMs[i] = fluxoGeradoAglomeradoVMs[i]*VM_SMALL_BW; 
					i++;
				}//fim while

				//conteudoNS2.append("\n\n"+indiceNodosNS2);

				//Ajusta o indice do NS2 para criar novos nodos a partir 
				//do ultimo indice dos roteadores de borda
				indiceNodosNS2=numNodes;

				//Cria o link entre o aglomerado de VMs e os roteadores de borda
				conteudoNS2.append("\n\n#Link aglomeradoVms com roteadores de borda");
				int indiceLigacao=0;
				i=0;
				while(i<fluxoGeradoAglomeradoVMs.length){

					//o indice do fluxoAglomeradoVMs eh o mesmo do seu datacenter
					if(fluxoGeradoAglomeradoVMs[i]!=0){

						indiceLigacao = adquirirIndiceLigacao(EDGES_ARQUIVO, i);

						//O indice do aglomeradoVMs corresponde ao indice do border router
						conteudoNS2.append("\n\nputs \"Link n("+ indiceLigacao +")--n("+ indiceNodosNS2 + ")\";flush stdout;");
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
							conteudoNS2.append("   \n$ns attach-agent $n("+indiceNodosNS2+") $udp("+indiceLigacao+")");
							conteudoNS2.append("   \n#Cria um trafego CBR e atribui ao agent UDP");
							conteudoNS2.append("   \nset cbr("+indiceLigacao+") [new Application/Traffic/CBR]");
							conteudoNS2.append("   \n$cbr("+indiceLigacao+") set packetSize_ 1500");
							conteudoNS2.append("   \n#Fluxo ficticio para ocupar todo o link");
							conteudoNS2.append("   \n$cbr("+indiceLigacao+") set rate_ " + fluxoGeradoAglomeradoVMs[i] + "kb");
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
								conteudoNS2.append("   \n$ns attach-agent $n("+indiceNodosNS2+") $tcp("+indiceLigacao+")");
								conteudoNS2.append("   \n#Cria um trafego FTP e atribui ao agent TCP");
								conteudoNS2.append("   \nset ftp("+indiceLigacao+") [new Application/FTP]");
								conteudoNS2.append("   \n$ftp("+indiceLigacao+") set packetSize_ 1500");
								conteudoNS2.append("   \n#Fluxo ficticio para ocupar todo o link");
								conteudoNS2.append("   \n$ftp("+indiceLigacao+") set rate_ " + fluxoGeradoAglomeradoVMs[i] + "kb");
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
									conteudoNS2.append("   \n$exp("+indiceLigacao+") set packetSize_ 1500");
									conteudoNS2.append("   \n#Fluxo ficticio para ocupar todo o link");
									//conteudoNS2.append("   \n$exp("+i+") set rate_ " + fluxoGeradoAglomeradoVMs[i] + "kb");
									conteudoNS2.append("   \n$exp("+indiceLigacao+") set rate_ 500kb");
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

				//close the file
				arquivoResult.close();			
			} catch(Exception e){
				System.out.println("Excecao 3 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO + "\n" + e.getMessage());
			}//fim catch

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
				System.out.println("5Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

			//Grava a banda dos links em arquivo
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_PARSER_BANDA_LINKS,false));			
				out.write(banda.toString());
				out.close();
			} catch(Exception e){
				System.out.println("6Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch			

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Excecao 5 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER + "\n" + e.getMessage());
		}//fim catch

	}//fim desativarLinksRouters
	 */

	public void atualizarTopologia(int indiceCromossomo, String tipo){

		//Recupera as informacoes do arquivo da topologia:
		//NUM_NODES,NUM_EDGES,NUM_DATACENTERS,NUM_SERVIDORES,NUM_VMS,NODO_DESTINO
		//Todas as informacoes das arestas: EDGES_ARQUIVO
		//Todas as informacoes dos servidores: S_ARQUIVO
		//Todas as informacoes das VMs: VM_ARQUIVO

		System.out.println("\n\nAtualizar Topologia");

		//Adquire as informacoes do arquivo
		try
		{
			int i=0;
			int j=0;

			String linha = new String();
			StringBuffer info = new StringBuffer();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER));
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
			//EDGE[0][EdgeId From To Length Delay Bandwidth QueueLimit ASto Type Other]
			//    [1][EdgeId From To Length Delay Bandwidth QueueLimit ASto Type Other]
			//    ...
			String [][] EDGES_ARQUIVO = new String[NUM_EDGES][10];
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
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");

			String id;
			String from;
			String to;
			String delay;
			String bw;
			String queueLimit;
			String asto;
			String type;
			String other;

			//Todos os links do arquivo .datacenter recebem
			//os custos informados pelo cromossomo
			System.out.println("\nValor decimal dos alelos do cromossomo selecionado:");
			String custoLink="0";
			for (i=0; i<NUM_EDGES; i++){
				j=0;

				linha = file.readLine();

				token = new StringTokenizer(linha,"\t");
				//id (0)
				id=token.nextToken();
				//System.out.print(id + " ");
				EDGES_ARQUIVO[i][j++]=id;
				//from (1)
				from=token.nextToken();
				//System.out.print(from + " ");
				EDGES_ARQUIVO[i][j++]=from;
				//to (2)
				to=token.nextToken();
				//System.out.print(to + " ");
				EDGES_ARQUIVO[i][j++]=to;

				//Length (3) (Atualizar no campo Length o custo ficticio do enlace)

				//if (bin2dec(P2[indiceCromossomo][i]+"")<100&&!pertenceListaTabu(Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeFrom]),Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeTo])))
				/*if (pertenceListaTabu(Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeFrom]),Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeTo]))){
					custoLink="99";
					EDGES_ARQUIVO[i][j++] = custoLink;
					//System.out.print(bin2dec(P2[indiceCromossomo][i]+"") + " ");
					System.out.print(custoLink + " ");
				}//fim if
				else{
				 */
				//Para evoluir os custos dos link
				//'i' eh o indice que varia aqui						
				//Por conta da analise de sensibilidade pequenas variacoes causam grandes mudancas
				if(CUSTOS_UNITARIOS){
					//Custos especificos dos links
					if ( 
							(from.equals("6")&&to.equals("8"))||(from.equals("8")&&to.equals("6"))||
							(from.equals("0")&&to.equals("6"))||(from.equals("6")&&to.equals("0"))						
					){
						custoLink = "2";
						EDGES_ARQUIVO[i][j++] = custoLink;
						System.out.print(custoLink + " ");
					} else {
						custoLink = "1";
						EDGES_ARQUIVO[i][j++] = custoLink;
						System.out.print(custoLink + " ");
					}//fim else
				} else 
					//Se nao eh o melhor resultado final
					if (tipo.equals("P")){

						custoLink = (bin2dec(P[indiceCromossomo][i]+""))+"";
						EDGES_ARQUIVO[i][j++] = custoLink;
						//System.out.print(bin2dec(P2[indiceCromossomo][i]+"") + " ");
						System.out.print(custoLink + " ");
					} else 
						if (tipo.equals("P2")){

							custoLink = (bin2dec(P2[indiceCromossomo][i]+""))+"";
							EDGES_ARQUIVO[i][j++] = custoLink;
							//System.out.print(bin2dec(P2[indiceCromossomo][i]+"") + " ");
							System.out.print(custoLink + " ");						
						} else 
							if (tipo.equals("P3")){
								//Original
								//custoLink = (bin2dec(P3[indiceCromossomo][i]+"")%4)+"";
								//Depois
								custoLink = (bin2dec(P3[indiceCromossomo][i]+""))+"";
								EDGES_ARQUIVO[i][j++] = custoLink;
								//System.out.print(bin2dec(P3[indiceCromossomo][i]+"") + " ");
								System.out.print(custoLink + " ");
							}//fim else						

				//}//fim else

				//Salta o campo
				token.nextToken();				
				//Delay (4)
				delay=token.nextToken();
				//System.out.print(delay + " ");
				EDGES_ARQUIVO[i][j++] = delay;

				//Bandwidth (5) (Nao posso alterar a bandwidth pq ela jah foi contratada)
				bw=token.nextToken();
				//System.out.print(bw + " ");
				EDGES_ARQUIVO[i][j++] = bw;

				//queueLimit (6)
				queueLimit=token.nextToken();
				//System.out.print(queueLimit + " ");
				EDGES_ARQUIVO[i][j++] = queueLimit;

				//Asto (7)
				asto=token.nextToken();
				//System.out.print(asto + " ");
				EDGES_ARQUIVO[i][j++] = asto;

				//Type (8)
				type=token.nextToken();
				//System.out.print(type + " ");
				EDGES_ARQUIVO[i][j++] = type;
				//Other (9)
				other=token.nextToken();
				//System.out.print(other + " ");
				EDGES_ARQUIVO[i][j++] = other;

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
			int numServidores = Integer.parseInt(token.nextToken());
			//System.out.println("numServers: " + NUM_SERVIDORES);

			//Guarda todas as informacoes dos servidores			
			linha=file.readLine();
			info.append(linha+"\n");

			int numCamposServ=13;
			String [][] S_ARQUIVO = new String[numServidores][numCamposServ];

			for(i=0; i<numServidores; i++){
				linha=file.readLine();				
				token = new StringTokenizer(linha, " ");
				for(j=0; j<numCamposServ; j++){
					//Atualiza os custos de CPU do servidor de acordo com os dados do cromossomo
					//7 eh o campo de custo da CPU no arquivo .datacenter
					//if(j==7){
					//	S_ARQUIVO[i][j]=bin2dec(P2[indiceCromossomo][i]+"")+"";
					//	token.nextToken();
					//} else
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
			//NODO_DESTINO = Integer.parseInt(token.nextToken());
			//System.out.println("nodoDestino: " + NODO_DESTINO);	

			//Grava a atualizacao de custos no arquivo de topologia do datacenter
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_TOPOLOGIA_DATACENTER,false));			
				out.write(info.toString());
				out.close();
			} catch(Exception e){
				System.out.println("7Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

			//close the file			
			file.close();

		} catch(Exception e){
			System.out.println("Excecao 4 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER + "\n" + e.getMessage());
			e.printStackTrace();
			//System.exit(0);
		}//fim catch

	}//fim atualizarTopologia

	public boolean pertenceListaTabu(int indiceOrigem, int indiceDestino){

		boolean result=false;

		//Abre o arquivo e adquire o percentual de perda de cada link
		//Mesmo tamanho da lista tabu
		String [][] PERCENTUAL_PERDA = new String[LISTA_TABU.length][LISTA_TABU[0].length];

		try
		{
			String nodoOrigem1=indiceOrigem+"";
			String nodoDestino1=indiceDestino+"";
			String nodoOrigem2="";
			String nodoDestino2="";

			//Linhas do arquivo
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_PERDA_NS2));

			linha=file.readLine();
			while (linha!=null && result==false) {
				//Faz o parser da linha
				String REGEX = "";
				Matcher matcher;
				Pattern pattern;

				REGEX = "(.*)--(.*) (.*) (.*) (.*)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linha);
				if (matcher.find()){
					nodoOrigem2=matcher.group(1);
					nodoDestino2=matcher.group(2);

					//Verifica se existe perda no link 
					//3--6 eh equivalente ah 6--3 para canal duplex
					if(nodoOrigem1.equals(nodoOrigem2)&&nodoDestino1.equals(nodoDestino2)){							
						//System.out.println(matcher.group(3));
						//System.out.println(matcher.group(4));
						PERCENTUAL_PERDA[Integer.parseInt(nodoOrigem1)][Integer.parseInt(nodoDestino1)] = matcher.group(5);
						//Perda > 1%
						if(	LISTA_TABU[indiceOrigem][indiceDestino]>0&&
								Double.parseDouble(PERCENTUAL_PERDA[indiceOrigem][indiceDestino])>0)
							result=true;
						//System.out.println("Porcentagem perda: " + PERCENTUAL_PERDA[Integer.parseInt(nodoOrigem1)][Integer.parseInt(nodoDestino1)]);
					}//fim if				
					if (nodoOrigem1.equals(nodoDestino2)&&nodoDestino1.equals(nodoOrigem2)){
						PERCENTUAL_PERDA[Integer.parseInt(nodoDestino1)][Integer.parseInt(nodoOrigem1)] = matcher.group(5);
						//Perda > 1%
						if(	LISTA_TABU[indiceDestino][indiceOrigem]>0&&
								Double.parseDouble(PERCENTUAL_PERDA[indiceDestino][indiceOrigem])>0)
							result=true;
					}//fim if

				}//fim if

				//Proxima linha
				linha=file.readLine();
			}//fim for			

			//close the file			
			file.close();	

		} catch(Exception e){
			System.out.println("Excecao 25 ao abrir o arquivo: " + ARQUIVO_PERDA_NS2 + "\n" + e.getMessage());
		}//fim catch

		/*		try {
			Thread t = new Thread();
			System.out.println("LISTA_TABU");
			t.sleep(5000);
		} catch (Exception e){}
		 */		
		return result;

	}//fim pertenceListaTabu

	public void gerarRoletaInvertida(String [] roleta){

		double soma=0;
		double energia=0;

		int i=0;
		while (i<TAM_POPULACAO){

			energia = Double.parseDouble(P[i][P[i].length-1]);
			soma += energia;

			i++;
		}//fim while

		double anterior=0;
		double probabilidade=0;
		double somatorioProbabilidades=0;

		//exibirPopulacao(P);
		System.out.print("Roleta antes: ");
		i=0;
		while (i<TAM_POPULACAO){

			energia = Double.parseDouble(P[i][P[i].length-1]);
			//probabilidade = 1/((anterior + energia)/soma);
			probabilidade = ((anterior + energia)/soma);
			roleta[i] = anterior+probabilidade + "";
			System.out.print("["+roleta[i] + "] ");

			anterior = probabilidade;			

			somatorioProbabilidades+=probabilidade;
			i++;

		}//fim while
		System.out.println("\nSomatorio Antes: " + somatorioProbabilidades);

		//Normaliza as porcentagens da roleta (ate aqui, 
		//o somatorio de porcentagens gera valores maiores que 1
		System.out.print("\nRoleta meio: ");
		double somatorioMeio=0;
		i=0;
		roleta[i]="0";		
		System.out.print("["+ roleta[i] + "] ");
		i++;
		while(i<TAM_POPULACAO){
			//roleta[i] = 1/(Double.parseDouble(roleta[i])/somatorioProbabilidades) + "";
			roleta[i] = 1/Double.parseDouble(roleta[i]) +"";
			somatorioMeio += Double.parseDouble(roleta[i]);
			System.out.print("["+ roleta[i] + "] ");
			i++;
		}//fim while		
		System.out.println("\nSomatorio Meio: " + somatorioMeio);

		//For normalization
		System.out.print("\nRoleta depois: ");
		i=0;
		double somatorioDepois=0;
		while(i<TAM_POPULACAO){
			roleta[i] = (Double.parseDouble(roleta[i])/somatorioMeio) +"";
			somatorioDepois += Double.parseDouble(roleta[i]);
			System.out.print("["+ roleta[i] + "] ");
			i++;
		}//end while		
		System.out.println("\nSomatorio Depois: " + somatorioDepois);

		/*try{
			Thread t = new Thread();			
			t.sleep(10000);
		} catch(Exception e){}
		 */

	}//fim gerarRoletaInvertida

	public void gerarRoleta_old(String [] roleta){

		double soma=0;
		double energia=0;

		int i=0;
		while (i<TAM_POPULACAO){

			energia = Double.parseDouble(P[i][P[i].length-1]);
			soma += energia;

			i++;
		}//fim while

		double anterior=0;
		double probabilidade=0;
		double somatorioProbabilidades=0;

		exibirPopulacao(P);
		System.out.print("Roleta antes: ");
		i=0;
		while (i<TAM_POPULACAO){

			energia = Double.parseDouble(P[i][P[i].length-1]);
			probabilidade = 1/((anterior + energia)/soma);
			roleta[i] = anterior+probabilidade + "";
			System.out.print("["+roleta[i] + "] ");

			anterior = probabilidade;			

			somatorioProbabilidades+=probabilidade;
			i++;

		}//fim while

		//Normaliza as porcentagens da roleta (ate aqui, 
		//o somatorio de porcentagens gera valores maiores que 1
		System.out.print("\nRoleta depois: ");
		i=0;
		while(i<TAM_POPULACAO){
			roleta[i] = (Double.parseDouble(roleta[i])/somatorioProbabilidades) + "";
			System.out.print(roleta[i] + " ");
			i++;
		}//fim while		

		/*try{
			Thread t = new Thread();			
			t.sleep(10000);
		} catch(Exception e){}
		 */

	}//fim gerarRoleta

	public int individuoRoletaInvertida(String [] roleta, double p){

		double anterior=0;
		boolean achou=false;
		int cromoSelecionado=0;

		int i=roleta.length-1;
		while (i>0 && !achou){

			if (p>=anterior && p <= Double.parseDouble(roleta[i])){
				cromoSelecionado = i;
				achou=true;
			} else
				anterior = Double.parseDouble(roleta[i]);

			i--;
		}//fim while

		return cromoSelecionado;

	}//fim individuoRoletaInvertida

	public int individuoRoleta(String [] roleta, double p){

		double anterior=0;
		boolean achou=false;
		int cromoSelecionado=0;

		int i=0;
		while (i<roleta.length && !achou){

			if (p>=anterior && p <= Double.parseDouble(roleta[i])){
				cromoSelecionado = i;
				achou=true;
			} else
				anterior = Double.parseDouble(roleta[i]);

			i++;
		}//fim while

		return cromoSelecionado;

	}//fim individuoRoleta

	public void inicializarPopulacao(){

		System.out.println("---inicializar populacao---");

		//N_BITS_SERV = calcularQtdeBits(NUM_SERVIDORES);

		//P = [custos de servidores e custos de rede]
		//Para quantidade de custos de rede, adquire a
		//quantidade de arestas do arquivo de topologia do datacenter

		try
		{
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER));
			//Cabecalho
			for (int i=0; i<3; i++) {
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
			for (int i=0; i<numNodes; i++){				
				file.readLine();
			}//fim while

			//Adquire o numero de Edges
			for (int i=0; i<2; i++) {
				file.readLine();
			}//fim for			
			linha = file.readLine();			
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numEdges = Integer.parseInt(token.nextToken());
			//System.out.println("numEdges: " + numEdges);			
			//Salta o campo Edges
			file.readLine();
			for (int i=0; i<numEdges; i++){				
				file.readLine();
			}//fim while

			//Adquire o numero de datacenters do arquivo
			for (int i=0; i<2; i++) {
				file.readLine();
			}//fim for			
			linha = file.readLine();			
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numDatacenters = Integer.parseInt(token.nextToken());
			//System.out.println("numDatacenters: " + numDatacenters);
			//close the file			
			file.close();

			//(NUM_DATACENTERS*NUM_SERVIDORES)+NUM_EDGES+fitness
			NUM_EDGES=numEdges;
			NUM_DATACENTERS=numDatacenters;
			P = new String[TAM_POPULACAO][NUM_EDGES+1];
			P2 = new String[TAM_POPULACAO/2][NUM_EDGES+1];
			//P3 guarda a melhor sequencia
			P3 = new String[1][NUM_EDGES+1];

			//Inicializa a lista tabu
			//numNodes+numEdges por causa dos indices (tenho indices para os roteadores de borda tb)
			LISTA_TABU = new int[numNodes+numEdges][numNodes+numEdges];
			renovarListaTabu();

			//Preenche a populacao
			int i=0;
			int j=0;
			while(i<P.length){
				j=0;
				while(j<(P[i].length)-1){ //Nao preenche o campo de fitness
					P[i][j]=dec2bin((int)Math.round(1 + Math.random() * MAX_DECIMAL_CROMOSSOMO),NUM_BITS);
					//Todos os alelos com 10 unidades para testar a avaliacao inicial (factivel)
					//P[i][j]=dec2bin(10,10);
					j++;
				}//fim while
				//Campo de fitness
				P[i][j]="999999.00";

				//Preencher com dados historicos
				if(i==P.length-1){
					j=0;

					//11 4 13 8 7 7 4 9 8 11 9 2 2 15 8
					/*P[i][j++] = dec2bin(11,NUM_BITS)+"";
					P[i][j++] = dec2bin(4,NUM_BITS)+"";
					P[i][j++] = dec2bin(13,NUM_BITS)+"";
					P[i][j++] = dec2bin(8,NUM_BITS)+"";
					P[i][j++] = dec2bin(7,NUM_BITS)+"";
					P[i][j++] = dec2bin(7,NUM_BITS)+"";
					P[i][j++] = dec2bin(4,NUM_BITS)+"";
					P[i][j++] = dec2bin(9,NUM_BITS)+"";
					P[i][j++] = dec2bin(8,NUM_BITS)+"";
					P[i][j++] = dec2bin(11,NUM_BITS)+"";
					P[i][j++] = dec2bin(9,NUM_BITS)+"";
					P[i][j++] = dec2bin(2,NUM_BITS)+"";
					P[i][j++] = dec2bin(2,NUM_BITS)+"";
					P[i][j++] = dec2bin(15,NUM_BITS)+"";
					P[i][j++] = dec2bin(8,NUM_BITS)+"";
					 */
				}//fim if


				i++;
			}//fim while

			//Inicializa P2
			i=0;
			while(i<P2.length){
				j=0;
				while(j<P2[i].length){
					P2[i][j]=P[i][j];
					j++;
				}//fim while
				i++;
			}//fim while

			////////////

			//Inicializa P3
			i=0;
			while(i<P3.length){
				j=0;
				while(j<P3[i].length){
					P3[i][j]=P[i][j];
					j++;
				}//fim while
				i++;
			}//fim while

			exibirPopulacao(P);

		} catch(Exception e){
			System.out.println("Excecao 6 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER + "\n" + e.getMessage());
		}//fim catch

	}//fim inicializarPopulacao	

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

	public double realizarParserFuncaoObjetivo(){

		double valorFuncaoObjetivo=0;

		String REGEX = "";
		Matcher matcher;
		Pattern pattern;

		try {
			//Abre o arquivo com o resultado Lingo			
			BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO));
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
			System.out.println("Excecao 7 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO + "\n" + e.getMessage());
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
			StringBuffer parserVazao = new StringBuffer("From-To TotalBitsTransmitted Duration Throughput(Kbps)");
			//System.out.println("From-To TotalBitsTransmitted Duration Throughput");

			try {
				//try to open the file.
				BufferedReader fileResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_NS2));
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
							parserVazao.append("\n" + i + "--" + j + " " + totalBits[i][j] + " " + String.format("%1$.2f",duration[i][j]) + " " + String.format("%1$.2f",throughput[i][j]));
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
				System.out.println("Excecao 8: Erro ao ler o arquivo: " + ARQUIVO_RESULT_NS2);
			}//fim catch

			//Grava o resultado em arquivo
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_VAZAO_NS2,false));			
				out.write(parserVazao.toString());
				out.close();
			} catch(Exception e){
				System.out.println("10Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

			//Thread t111 = new Thread();
			//t111.sleep(1000000);


			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Excecao 9 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER + "\n" + e.getMessage());
		}//fim catch

		return vazaoTotalVMs;

	}//fim realizarParserVazao

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

	public void executarModeloLingo(){

		System.out.println("\n--Geracao: " + NUM_GERACAO + " Iteracao: " + NUM_ITERACAO + " --");

		System.out.println("--Executar modelo no Lingo--");

		/*//Eh necessario mudar a data apenas uma vez, na execucao sequencial 
		//Muda a data para executar o Lingo				
		try{
			String comando = PATH + "/mudarDataParaLingo.sh";
			Process p = Runtime.getRuntime().exec(comando);
			p.waitFor();			
		} catch (Exception e){
			System.out.println("Excecao ao mudar a data do servidor para o Lingo.");
		}//fim catch		
		 */
		String comando = PATH + "/executarModeloLingo.sh " + ARQUIVO_MODELO_LINGO;

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
			System.out.println("Fim da execucao do modelo Lingo.");
		} catch (Exception e){
			System.out.println("Excecao: Erro ao executar o programa Lingo.");
		}//fim catch

	}//fim executarModeloLingo

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
		modeloDatacenter.append("\n\nNodoDestino: ( ");
		i=0;
		while(i<NODO_DESTINO.length){
			modeloDatacenter.append(NODO_DESTINO[i] + " ");
			i++;
		}//end while
		modeloDatacenter.append(")\n");

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

	public String getServBandwidth(String indice){

		String result="0";

		boolean achou=false;
		int i=0;
		while(i<S.length&&!achou){
			if (S[i][I_SERVER].equals(indice)){
				result=S[i][I_SERVER_BW];
				achou=true;
			}//fim if			
			i++;
		}//fim while

		return result;
	}//fim getServBandwidth

	/*	public void gravarNodesBD(){

		//Remove as informacoes anteriores
		A_PO_BD bd = new A_PO_BD();
		try {
			if( bd.removeAllNodesFromDB() == false )
				throw new Exception("Error to remove nodes from DB");
		} catch (Exception e){	
			System.out.println(e.getMessage());
		}//fim catch		

		BRITETopologyPO topologiaRede = new BRITETopologyPO(ARQUIVO_TOPOLOGIA_ORIGINAL);
		Node [] nodes = topologiaRede.getNodes();
		String nodeid="";
		String xpos="";
		String ypos="";
		String indegree="";
		String outdegree="";
		String asid="";
		String type="";

		int i=0;
		while (i<nodes.length){

			nodeid = nodes[i].getID()+"";
			xpos = nodes[i].getX()+"";
			ypos = nodes[i].getY()+"";

			bd.setNodeId(nodeid);
			bd.setXPos(xpos);
			bd.setYPos(ypos);
			bd.setIndegree(indegree);
			bd.setOutdegree(outdegree);
			bd.setASId(asid);
			bd.setType(type);
			i++;

			try {
				if( bd.insertNodesIntoDB() == false )
					throw new Exception("Error storing nodes into DB");
			} catch (Exception e){	
				System.out.println(e.getMessage());
			}//fim catch
		}//fim while

	}//fim gravarNodesBD

	public void gravarEdgesBD(){

		//Remove as informacoes anteriores
		A_PO_BD bd = new A_PO_BD();
		try {
			if( bd.removeAllEdgesFromDB() == false )
				throw new Exception("Error to remove edges from DB");
		} catch (Exception e){	
			System.out.println(e.getMessage());
		}//fim catch		

		BRITETopologyPO topologiaRede = new BRITETopologyPO(ARQUIVO_TOPOLOGIA_ORIGINAL);
		Edge [] edges = topologiaRede.getEdges();
		String edgeid="";
		String sourcenode="";
		String destinationnode="";
		String length="";
		String delay="";
		String bandwidth="";
		String queueLimit="";
		String asto="";
		String edgetype="";

		int i=0;
		while (i<edges.length){

			edgeid = edges[i].getEdgeID()+"";
			sourcenode = edges[i].getSource()+"";
			destinationnode = edges[i].getDestination()+"";
			length = edges[i].getLength()+"";
			delay = edges[i].getDelay()+"";
			bandwidth = edges[i].getBW()+"";

			bd.setEdgeId(edgeid);
			bd.setSourceNode(sourcenode);
			bd.setDestinationNode(destinationnode);
			bd.setLength(length);
			bd.setDelay(delay);
			bd.setBandwidth(bandwidth);
			bd.setASFrom(queueLimit); //utiliza o campo ASFrom
			bd.setASTo(asto);
			bd.setEdgeType(edgetype);
			i++;

			try {
				if( bd.insertEdgesIntoDB() == false )
					throw new Exception("Error storing edges into DB");
			} catch (Exception e){	
				System.out.println(e.getMessage());
			}//fim catch
		}//fim while

	}//fim gravarEdgesBD

	public void gravarServersBD(){

		//Remove as informacoes anteriores
		A_PO_BD bd = new A_PO_BD();
		try {
			if( bd.removeAllServersFromDB() == false )
				throw new Exception("Error to remove servers from DB");
		} catch (Exception e){	
			System.out.println(e.getMessage());
		}//fim catch		

		int i=0;
		while (i<NUM_SERVIDORES){

			bd.setServerId(S[i][I_SERVER]);
			bd.setServerDatacenterId(S[i][I_SERVER_DATACENTER]);
			bd.setServerCapCPU(S[i][I_SERVER_CPU]);
			bd.setServerCapRAM(S[i][I_SERVER_RAM]);
			bd.setServerCapDisk(S[i][I_SERVER_DISK]);
			bd.setServerCapBw(S[i][I_SERVER_BW]);
			bd.setServerVirtualizer(S[i][I_SERVER_VIRTUALIZER]);
			bd.setServerCostCPU(S[i][I_SERVER_COST_CPU]);
			bd.setServerCostRAM(S[i][I_SERVER_COST_RAM]);
			bd.setServerCostDisk(S[i][I_SERVER_COST_DISK]);
			bd.setServerCostBw(S[i][I_SERVER_COST_BW]);
			bd.setServerXPos("");
			bd.setServerYPos("");
			i++;

			try {
				if( bd.insertServersIntoDB() == false )
					throw new Exception("Error storing servers into DB");
			} catch (Exception e){	
				System.out.println(e.getMessage());
			}//fim catch
		}//fim while

	}//fim gravarServersBD	

	public void gravarVMsBD(){

		//Remove as informacoes anteriores
		A_PO_BD bd = new A_PO_BD();
		try {
			if( bd.removeAllVMsFromDB() == false )
				throw new Exception("Error to remove VMs from DB");
		} catch (Exception e){	
			System.out.println(e.getMessage());
		}//fim catch		

		int i=0;
		while (i<NUM_VMS){

			bd.setVMId(i+"");
			bd.setVMInitialServer(C[i][I_CLOUD_SERVER_INDEX]);
			bd.setVMCPUReq(C[i][I_CLOUD_VM_CPU]);
			bd.setVMRAMReq(C[i][I_CLOUD_VM_RAM]);
			bd.setVMDiskReq(C[i][I_CLOUD_VM_DISK]);
			bd.setVMBwReq(C[i][I_CLOUD_VM_BW]);
			bd.setVMFluxo(C[i][I_CLOUD_VM_FLUXO]);			
			bd.setVMVirtualizer("");
			bd.setVMXPos("");
			bd.setVMYPos("");
			i++;

			try {
				if( bd.insertVMsIntoDB() == false )
					throw new Exception("Error storing VMs into DB");
			} catch (Exception e){	
				System.out.println(e.getMessage());
			}//fim catch
		}//fim while

	}//fim gravarVMsBD
	 */
	public void gerarModeloLingoCompacto(int indiceCromossomo){

		System.out.println("\nGerar Modelo Lingo Compacto---\n");

		//Adquire as informacoes do arquivo
		try
		{
			int i=0;
			int j=0;

			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER));
			//Cabecalho
			linha=file.readLine();			
			StringTokenizer token = new StringTokenizer(linha, "( )");
			token.nextToken();
			//System.out.println("\nnumNodesOriginal: "+Integer.parseInt(token.nextToken()));			
			int numNodesOriginal = Integer.parseInt(token.nextToken());
			//Salta as proximas linhas do cabecalho
			for (i=0; i<2; i++) {
				file.readLine();
			}//fim for

			//get the number of nodes
			linha = file.readLine();			
			token = new StringTokenizer(linha, "( )");
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
			//EDGE[0][EdgeId From To Length Delay Bandwidth queueLimit ASto Type Other]
			//    [1][EdgeId From To Length Delay Bandwidth queueLimit ASto Type Other]
			//    ...
			String [][] EDGES_ARQUIVO = new String[NUM_EDGES][10];
			//Inicializa a matriz
			i=0;
			j=0;
			while(i<EDGES_ARQUIVO.length){
				j=0;
				while(j<EDGES_ARQUIVO[i].length){
					EDGES_ARQUIVO[i][j]="0";
					j++;
				}//fim while
				i++;
			}//fim while
			int campoEdgeId=0;
			int campoEdgeFrom=1;
			int campoEdgeTo=2;
			int campoEdgeCapLink=3;
			int campoEdgeDelay=4;
			int campoEdgeBw=5;
			int campoEdgeQueueLimit=6;
			int campoEdgeAsTo=7;
			int campoEdgeType=8;
			int campoEdgeOther=9;
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
				//Length (3) (capLink atribuida pelo cromossomo)
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Delay (4) 
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Bandwidth (5) 
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//queueLimit (6)
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
			//int nodoDestino = Integer.parseInt(token.nextToken());
			//System.out.println("nodoDestino: " + nodoDestino);


			//---Inicio da transformacao
			i=0;
			j=0;

			//
			StringBuffer [] passo = new StringBuffer[17];
			int indice=0;

			//Passo0: sets			
			passo[indice] = new StringBuffer();
			passo[indice].append("\nMODEL:");
			passo[indice].append("\n\nSETS:");
			passo[indice].append("\n\n!VMS;");
			passo[indice].append("\nVMS /1.." + numVMs + "/: VMCPU, VMBW;");
			passo[indice].append("\n\n!SERVERS: CPU, BANDWIDTH, ENERGY, X (ON/OFF);");
			passo[indice].append("\nSERVERS /1.." + numServers + "/: SCPU, SBW, SE, X, COST, AMOUNT_ALLOCS;");
			passo[indice].append("\n\n!ALLOCATION;");
			passo[indice].append("\nALLOCS(VMS, SERVERS): A;");
			passo[indice].append("\n\n!ROUTERS: FLOW IN, ENERGY Y (ON/OFF);");
			passo[indice].append("\nROUTERS /1.." + (numNodesOriginal+numDatacenters) + "/: RE, FIN, Y, ROUTERS_CONSUME;");
			passo[indice].append("\n\n!LINKS;");
			passo[indice].append("\nLINKS(ROUTERS, ROUTERS): L, F, C;");
			passo[indice].append("\n\nENDSETS");
			indice++; //1
			//System.out.println(passo[0].toString());

			//Nota: essas constantes 'deveriam' ser lidas do arquivo
			passo[indice] = new StringBuffer();
			passo[indice].append("\nDATA:");
			passo[indice].append("\nVMCPU, VMBW = " + VM_SMALL_CPU + ", " + VM_SMALL_BW + ";");
			passo[indice].append("\nSE = 90;");
			passo[indice].append("\nSBW = " + SERVER_SMALL_BW + ";");
			passo[indice].append("\nC = 1;");
			//Gera a matriz de custos
			/*			passo[indice].append("\nC = \n");
			i=0;
			j=0;
			while(i<(numNodesOriginal+numDatacenters)){
				j=0;
				while(j<(numNodesOriginal+numDatacenters)){					
					passo[indice].append(recuperarValorCusto(EDGES_ARQUIVO,i,j) + "\t");					
					j++;
				}//fim while
				passo[indice].append("\n");
				i++;
			}//fim while
			passo[indice].append(";\n");
			 */			

			/*passo[indice].append("\nL = \n");
			//Gera a matriz de links			
			i=0;
			j=0;
			double capacidadeLink=0;
			while(i<(numNodesOriginal+numDatacenters)){
				j=0;
				while(j<(numNodesOriginal+numDatacenters)){
					//Original
					//passo[indice].append(recuperarValorLink(EDGES_ARQUIVO,i,j) + "\t");
					//
					//
					//Novo
					//( campoCromossomo * capacidade do link ) / fatorCorrecao;					
					capacidadeLink = (recuperarValorCusto(EDGES_ARQUIVO, i, j) * recuperarValorLink(EDGES_ARQUIVO,i,j)) / 10;
					passo[indice].append(capacidadeLink + "\t");
					j++;
				}//fim while
				passo[indice].append("\n");
				i++;
			}//fim while
			passo[indice].append(";");
			 */
			if (UMA_ITERACAO){
				passo[indice].append("\nL=500;");
			}//end if

			passo[indice].append("\nRE = 0.06;");
			passo[indice].append("\nENDDATA\n");
			indice++; //2
			//System.out.println(passo[1].toString());

			//Gera a matriz de links APENAS para os routers internos			
			passo[indice] = new StringBuffer();
			if(!UMA_ITERACAO){
				i=0;
				j=0;
				double alelo=0;
				double capacidadeLink=0;
				while(i<EDGES_ARQUIVO.length){

					alelo = Double.parseDouble(EDGES_ARQUIVO[i][campoEdgeCapLink]);

					capacidadeLink = (LIMITE_SUPERIOR_LINK * alelo) / FATOR_CORRECAO;
					passo[indice].append("L("+ (Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeFrom])+1) + "," + 
							(Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeTo])+1) + ") = " + 
							capacidadeLink + ";\n");
					i++;
				}//fim while
			}//end if
			indice++; //3

			passo[indice] = new StringBuffer();			
			//Nao pode ser apenas COST(J)*X(J) porque senao a funcao objetivo eh sempre a mesma
			passo[indice].append("\nMIN = @SUM(SERVERS(J): COST(J)*X(J)) + @SUM(ROUTERS(J):FIN(J)*RE(J));");
			passo[indice].append("\n\n@FOR(SERVERS(J): COST(J) = SCPU(J) / SE(J));");
			indice++; //4
			//System.out.println(passo[2].toString());

			passo[indice] = new StringBuffer();
			passo[indice].append("\n\nENERGY_SERVERS = @SUM(ALLOCS(I,J): A(I,J) * SE(J)/SCPU(J));");
			passo[indice].append("\n\nENERGY_ROUTERS = @SUM(ROUTERS(J): FIN(J) * RE(J));");
			indice++; //5

			passo[indice] = new StringBuffer();
			passo[indice].append("\n!AMOUNT OF VMS ALLOCATED IN SERVERS;");
			passo[indice].append("\n@FOR(SERVERS(J):");
			passo[indice].append("\nAMOUNT_ALLOCS(J) = @SUM(ALLOCS(I,J): A(I,J)));");
			indice++; //6
			//System.out.println(passo[3].toString());

			/*passo[indice] = new StringBuffer();
			passo[indice].append("\n!ENERGY CONSUMPTION FOR ROUTERS;");
			passo[indice].append("\n@FOR(ROUTERS(K):");
			passo[indice].append("\nROUTERS_CONSUME(K) = RE(K)*Y(K));");
			indice++;
			 */			

			int limiteInferior = 0;
			int limiteSuperior = 0;
			passo[indice] = new StringBuffer();
			passo[indice].append("\n\n!SET NUMBER OF CPUS FOR SERVERS;");
			i=0;
			while(i<numDatacenters){
				limiteInferior = limiteSuperior;
				limiteSuperior += NUM_SERVIDORES_SMALL;
				passo[indice].append("\n@FOR(SERVERS(J)| J #GT# " + limiteInferior + " #AND# J #LE# " + limiteSuperior + ":");
				passo[indice].append("\nSCPU(J) = 4);");

				limiteInferior = limiteSuperior;
				limiteSuperior += NUM_SERVIDORES_LARGE;
				passo[indice].append("\n@FOR(SERVERS(J)| J #GT# " + limiteInferior + " #AND# J #LE# " + limiteSuperior + ":");
				passo[indice].append("\nSCPU(J) = 8);");

				limiteInferior = limiteSuperior;
				limiteSuperior += NUM_SERVIDORES_HUGE;
				passo[indice].append("\n@FOR(SERVERS(J)| J #GT# " + limiteInferior + " #AND# J #LE# " + limiteSuperior + ":");
				passo[indice].append("\nSCPU(J) = 32);");

				i++;
			}//fim while	
			indice++; //7
			//System.out.println(passo[4].toString());

			passo[indice] = new StringBuffer();
			passo[indice].append("\n\n!ALLOCATION RESTRICTIONS;");
			passo[indice].append("\n!CORES;");
			passo[indice].append("\n@FOR(SERVERS(J):");
			passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMCPU(I)) <= SCPU(J));");
			passo[indice].append("\n!BANDWIDTH;");
			passo[indice].append("\n@FOR(SERVERS(J):");
			passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I)) <= SBW(J));");
			passo[indice].append("\n!UNICITY;");
			passo[indice].append("\n@FOR(VMS(I):");
			passo[indice].append("\n@SUM(SERVERS(J): A(I,J)) = 1);");
			indice++; //8
			//System.out.println(passo[5].toString());

			passo[indice] = new StringBuffer();
			passo[indice].append("\n!FLOW RESTRICTIONS;");
			passo[indice].append("\n!FLOWS PRODUCED BY DATACENTERS;");
			i=0;
			limiteInferior=0;
			limiteSuperior=0;
			while(i<numDatacenters){
				limiteInferior = limiteSuperior;
				limiteSuperior += numServers/numDatacenters;
				passo[indice].append("\n@SUM(SERVERS(J)| J #GT# " + limiteInferior + " #AND# J #LE# " + limiteSuperior + ":");
				passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I))) = " + adquirirFonteTrafegoDatacenter(EDGES_ARQUIVO,i) + ";");				
				i++;
			}//fim while
			indice++; //9
			//System.out.println(passo[6].toString());

			passo[indice] = new StringBuffer();
			passo[indice].append("\n\n!CONSERVATION ON ROUTERS;");
			//Para a rede
			//Forma uma estrutura de conexao a partir dos links do arquivo da topologia
			BRITETopologyPO topologiaRede = new BRITETopologyPO(ARQUIVO_TOPOLOGIA_DATACENTER);
			Edge [] edges = topologiaRede.getEdges();

			//Restricoes de continuidade
			//System.out.println("\nPasso14: Restricoes de Continuidade");
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
					if (edges[j].getDestination()==nodes[i].getID() && !(edges[j].getTypeNode()).equals("SERVER")){
						listaChegaNode[i][0].append( edges[j].getSource() + " ");
						//System.out.println("Chega no Node" + i + ": [" + listaChegaNode[i][0] + "]");
					}//fim if
					j++;
				}//fim while			
				i++;
				//System.out.print(i + " ");
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
					if (edges[j].getSource()==nodes[i].getID()&&!(edges[j].getTypeNode()).equals("SERVER")){
						listaSaiNode[i][0].append( edges[j].getDestination() + " ");
						//System.out.println("Sai do Node" + i + ": [" + listaSaiNode[i][0] + "]");
					}//fim if
					j++;
				}//fim while			
				i++;
				//System.out.print(i + " ");
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
						restricoesContinuidadeDestino[i].append(" F(" + (i+1)+ "," + 
								(Integer.parseInt(listaDestino.nextToken())+1) + ") ");

						if(listaDestino.hasMoreTokens())
							restricoesContinuidadeDestino[i].append(" +\n");
					}//fim while
				}//fim if
				i++;
				//System.out.print(i + " ");
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
						restricoesContinuidadeOrigem[i].append(" F(" + 
								(Integer.parseInt(listaOrigem.nextToken())+1) + "," + (i+1) + ") ");

						if(listaOrigem.hasMoreTokens())
							restricoesContinuidadeOrigem[i].append(" +\n");
					}//fim while
				}//fim if
				i++;
				//System.out.print(i + " ");
			}//fim while

			//Monta as restricoes
			i=0;
			StringBuffer restricoesContinuidade = new StringBuffer();
			while(i<nodes.length){
				if ( (restricoesContinuidadeOrigem[i]!=null&&restricoesContinuidadeDestino[i]!=null) &&
						//Multiple destinations
						//(!restricoesContinuidadeOrigem[i].toString().equals("")&&!restricoesContinuidadeDestino[i].toString().equals("")&&!ehFluxoParaSink(restricoesContinuidadeDestino[i].toString())))
						(!restricoesContinuidadeOrigem[i].toString().equals("")&&!restricoesContinuidadeDestino[i].toString().equals("")))
					restricoesContinuidade.append("(" + restricoesContinuidadeOrigem[i] + ") - (" + restricoesContinuidadeDestino[i] + ") = 0;\n");
				i++;
				//System.out.print(i + " ");
			}//fim while

			passo[indice].append("\n"+restricoesContinuidade.toString());
			indice++; //10
			//System.out.println(passo[7].toString());

			passo[indice] = new StringBuffer();
			passo[indice].append("\n\n!CONSERVATION ON SINK;");
			passo[indice].append("\n@SUM(SERVERS(J):");
			passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I))) = " + adquirirFluxosEntrada(EDGES_ARQUIVO, NODO_DESTINO[0]) + ";");
			//Multiple destinations
			//passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I))) = " + adquirirTodosFluxosDestino(EDGES_ARQUIVO) + ";");
			indice++; //11
			//System.out.println(passo[8].toString());

			//Usa a tabela de links
			passo[indice] = new StringBuffer();
			passo[indice].append("\n\n!LINK CAPACITY;");			
			passo[indice].append("\n@FOR(LINKS(I,J):"); 
			passo[indice].append("\nF(I,J) <= L(I,J));");
			indice++; //12
			//System.out.println(passo[9].toString());

			passo[indice] = new StringBuffer();
			passo[indice].append("\n\n!FLOW IN FOR ROUTERS;");
			i=0;
			while(i<numNodesOriginal){
				passo[indice].append("\n!"+(i+1)+";");
				passo[indice].append("\nFIN("+(i+1)+") = " + adquirirFluxosEntrada(EDGES_ARQUIVO,i) + ";");
				i++;
			}//fim while
			indice++; //13
			//System.out.println(passo[10].toString());

			passo[indice] = new StringBuffer();
			passo[indice].append("\n!X: SLACK;");
			passo[indice].append("\n@FOR(SERVERS(J):");
			passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I)) + X(J) = SCPU(J));");
			indice++; //14
			//System.out.println(passo[11].toString());

			passo[indice] = new StringBuffer();
			passo[indice].append("\n!ON/OFF ROUTERS;");
			passo[indice].append("\n@FOR(ROUTERS(J):");
			passo[indice].append("\nFIN(J) - " + LIMITE_SUPERIOR_LINK + "*Y(J) <= 0);");
			indice++; //15

			//Relaxamento para nao usar variaveis binarias			
			/*passo[indice] = new StringBuffer();
			passo[indice].append("\n@FOR(SERVERS(J):");
			passo[indice].append("\n@FOR(VMS(I): ");
			passo[indice].append("\nA(I,J) <= 1;");
			passo[indice].append("\nA(I,J) >= 0));");
			 */			
			passo[indice] = new StringBuffer();
			passo[indice].append("\n@FOR(SERVERS(J):");
			passo[indice].append("\n@FOR(VMS(I): ");
			passo[indice].append("\n@BIN(A(I,J))));");			
			indice++; //16

			passo[indice] = new StringBuffer();
			passo[indice].append("\n\nEND");
			passo[indice].append("\n! Terse output mode;" +
					"\nSET TERSEO 1" + 
					"\n! Solve the model;" +
					"\nGO" + 

					"\n! Sensitivity Analisys;" + 					
					"\n!DIVERT " + ARQUIVO_RANGE_LINGO + 
					"\n!RANGE" + 

					"\n! Open a file;" + 
					"\nDIVERT " + ARQUIVO_RESULT_LINGO + 
					"\n! Send solution to the file;" + 
					"\nSOLUTION" + 
					"\n! Close solution file;" + 
					"\nRVRT" + 
					"\n! Quit LINGO;" + 
			"\nQUIT");			
			//System.out.println(passo[13].toString());

			StringBuffer modeloLingo = new StringBuffer();
			i=0;
			while(i<passo.length){
				modeloLingo.append(passo[i].toString()+"\n");				
				i++;
			}//fim while
			//	System.out.println(modeloLingo.toString());

			//Grava em arquivo o modelo Lingo
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_MODELO_LINGO,false));			
				out.write(modeloLingo.toString());
				out.close();
			} catch(Exception e){
				System.out.println("13Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Excecao 14 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER + "\n" + e.getMessage());
			e.printStackTrace();
		}//fim catch

	}//fim gerarModeloLingoCompacto

	public boolean ehFluxoParaSink(String listaFluxo){

		boolean isFlowToSink=false;

		// Verify if only destination is the sink node and
		// If exist flow that begin in sink to other destination. 
		// If yes, do not use this flow
		StringTokenizer t1 = new StringTokenizer(listaFluxo," ");
		//Ex.: F(4,6) -> sink=5, but in Lingo is index+1=6 
		//fluxo = F(4,6)
		String fluxo = t1.nextToken();
		if (!t1.hasMoreElements()){
			//Parser of destination node
			StringTokenizer t2 = new StringTokenizer(fluxo,",");
			//Source: F(4
			t2.nextToken();
			//Destination: 6)
			StringTokenizer t3 = new StringTokenizer(t2.nextToken(), ")");
			//Destination: 6
			int destination = Integer.parseInt(t3.nextToken());
			int i=0;
			//Verify if this destination is a sink node
			while(i<NODO_DESTINO.length&&!isFlowToSink){
				//Is a sink node
				if (destination==NODO_DESTINO[i])
					isFlowToSink=true;
				i++;
			}//end while

		}//end if
		System.out.println("Fluxo: " + listaFluxo + " isFlowUniqueToSink: " + isFlowToSink);

		return isFlowToSink;

	}//end ehFluxoParaSink

	public String adquirirTodosFluxosDestino(String [][] EDGES_ARQUIVO){

		String fluxos="";

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

		int i=0;
		int j=0;
		int quantidade=0;
		//Iteract NODO_DESTINO
		int p=0;

		/*		System.out.println("NODO_DESTINO.length: [" + NODO_DESTINO.length +"]");
		while(p<NODO_DESTINO.length){
			System.out.println("["+NODO_DESTINO[p]+"]");
			p++;
		}//end while
		System.exit(0);
		 */		
		while(p<NODO_DESTINO.length){
			i=0;
			j=0;
			quantidade=0;
			//Adquire todos os fluxos com destino ao nodoDestino
			while (i<EDGES_ARQUIVO.length){
				if (EDGES_ARQUIVO[i][campoEdgeTo].equals(NODO_DESTINO[p]+""))
					quantidade++;			
				i++;
			}//fim while

			String [] fluxosParaSink = new String[quantidade];
			i=0;
			j=0;
			while (i<EDGES_ARQUIVO.length){
				if (EDGES_ARQUIVO[i][campoEdgeTo].equals(NODO_DESTINO[p]+"")){
					fluxosParaSink[j] = "F(" + 
					(Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeFrom])) + "," + 
					(Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeTo])) + ")";
					j++;
				}//fim if
				i++;
			}//fim while

			i=0;
			while(i<fluxosParaSink.length){
				fluxos += fluxosParaSink[i]; 
				if (i+1<fluxosParaSink.length)
					fluxos += " +\n";
				i++;
			}//fim while

			if(p+1<NODO_DESTINO.length)
				fluxos += " +\n";
			//Next destination node
			p++;
		}//end while

		return fluxos;		

	}//fim adquirirTodosFluxosDestino

	public String adquirirFluxosEntrada(String [][] EDGES_ARQUIVO, int nodoDestino){

		String fluxos="";

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

		int i=0;
		int j=0;
		int quantidade=0;


		//Adquire todos os fluxos com destino ao nodoDestino
		while (i<EDGES_ARQUIVO.length){
			if (EDGES_ARQUIVO[i][campoEdgeTo].equals(nodoDestino+""))
				quantidade++;			
			i++;
		}//fim while

		String [] fluxosParaSink = new String[quantidade];
		i=0;
		j=0;
		while (i<EDGES_ARQUIVO.length){
			if (EDGES_ARQUIVO[i][campoEdgeTo].equals(nodoDestino+"")){
				fluxosParaSink[j] = "F(" + 
				(Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeFrom])) + "," + 
				(Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeTo])) + ")";
				j++;
			}//fim if
			i++;
		}//fim while

		i=0;
		while(i<fluxosParaSink.length){
			fluxos += fluxosParaSink[i]; 
			i++;
			if (i<fluxosParaSink.length)
				fluxos += " +\n";
		}//fim while

		return fluxos;		

	}//fim adquirirFluxosEntrada

	public String adquirirFonteTrafegoDatacenter(String [][] EDGES_ARQUIVO, int i){

		String fonteTrafego="";

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

		//Busque as arestas que sejam border routers
		int p=0;
		boolean achouFonteTrafego=false;
		while (p<EDGES_ARQUIVO.length && !achouFonteTrafego){
			if(	EDGES_ARQUIVO[p][campoEdgeType].equals("BORDER_ROUTER") &&
					EDGES_ARQUIVO[p][campoEdgeTo].equals(i+"")){

				achouFonteTrafego=true;
				//+1 por conta do indice inicial do Lingo em 1
				fonteTrafego = "F("+
				(Integer.parseInt(EDGES_ARQUIVO[p][campoEdgeFrom])+1)+","+
				(Integer.parseInt(EDGES_ARQUIVO[p][campoEdgeTo])+1)+")";

			}//fim if
			p++;
		}//fim while

		return fonteTrafego;

	}//adquirirFonteTrafegoDatacenter

	public double recuperarValorLink(String [][] EDGES_ARQUIVO, int i, int j){

		double valorLink=0;

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
		int q=0;
		boolean achou=false;
		while(p<EDGES_ARQUIVO.length && !achou){
			q=0;
			while(q<EDGES_ARQUIVO[p].length && !achou){

				//Lembrar que o link eh full-duplex (1--2 = 2--1)
				if(	(	Integer.parseInt(EDGES_ARQUIVO[p][campoEdgeFrom])==i && 
						Integer.parseInt(EDGES_ARQUIVO[p][campoEdgeTo])==j	) ||
						( 	Integer.parseInt(EDGES_ARQUIVO[p][campoEdgeFrom])==j && 
								Integer.parseInt(EDGES_ARQUIVO[p][campoEdgeTo])==i	) ){
					achou=true;
					valorLink = Double.parseDouble(EDGES_ARQUIVO[p][campoEdgeBw]);						
				}//fim if				
				q++;
			}//fim while			
			p++;
		}//fim while


		return valorLink;

	}//fim recuperarValorLink

	public double recuperarValorCusto(String [][] EDGES_ARQUIVO, int i, int j){

		double valorCusto=0;

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
		int q=0;
		boolean achou=false;
		while(p<EDGES_ARQUIVO.length && !achou){
			q=0;
			while(q<EDGES_ARQUIVO[p].length && !achou){

				//Lembrar que o link eh full-duplex (1--2 = 2--1)
				if(	(	Integer.parseInt(EDGES_ARQUIVO[p][campoEdgeFrom])==i && 
						Integer.parseInt(EDGES_ARQUIVO[p][campoEdgeTo])==j	) ||
						( 	Integer.parseInt(EDGES_ARQUIVO[p][campoEdgeFrom])==j && 
								Integer.parseInt(EDGES_ARQUIVO[p][campoEdgeTo])==i	) ){
					achou=true;
					valorCusto = Double.parseDouble(EDGES_ARQUIVO[p][campoEdgeCost]);						
				}//fim if				
				q++;
			}//fim while			
			p++;
		}//fim while


		return valorCusto;

	}//fim recuperarValorCusto

	public boolean repetidoRouter(int indiceRouter, StringBuffer listaRouters){

		boolean achou=false;

		StringTokenizer token = new StringTokenizer(listaRouters.toString(), " ");
		while(token.hasMoreTokens()&&!achou){
			if (indiceRouter==Integer.parseInt(token.nextToken().toString()))
				achou=true;			
		}//fim while

		return achou;

	}//fim repetidoRouter

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
			System.out.println("\nCloud: [VM_index, InitialServer, CPU_request, RAM_request, DISK_request, BW_request]");	
			exibir(C);
		}//fim if

	}//fim criarVMs

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

	public double realizarParserConsumoEnergiaServidores(){

		double consumoEnergiaServidores=0;

		String REGEX = "";
		Matcher matcher;
		Pattern pattern;

		try {
			//Abre o arquivo com o resultado Lingo			
			BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO));
			String linhaResult=arquivoResult.readLine();
			StringTokenizer t1;
			StringTokenizer t2;

			////////////////
			boolean achou=false;
			while (linhaResult!=null && !achou){

				//Ex.:  ENERGY_SERVERS 		1.000000            3.955556"
				REGEX = "ENERGY\\_SERVERS (.*)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linhaResult);
				//Ideia: achou uma alocacao, verifica em qual datacenter ocorreu a alocacao
				if (matcher.find()){

					achou=true;

					t2 = new StringTokenizer(linhaResult, " ");
					//ENERGY_SERVERS
					t2.nextToken();
					//1.000000
					consumoEnergiaServidores = Double.parseDouble(t2.nextToken());

					//System.out.println("Passei por aqui: " + consumoEnergiaServidores);

				}//fim if

				//Proxima linha do arquivo
				linhaResult = arquivoResult.readLine();

			}//fim while

			//close the file
			arquivoResult.close();			
		} catch(Exception e){
			System.out.println("Excecao 16 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO + "\n" + e.getMessage());
			e.printStackTrace();
		}//fim catch

		return consumoEnergiaServidores;

	}//fim realizarParserConsumoEnergiaServidores

	public double realizarParserConsumoEnergiaRouters(){

		double consumoEnergiaRouters=0;

		String REGEX = "";
		Matcher matcher;
		Pattern pattern;

		try {
			//Abre o arquivo com o resultado Lingo			
			BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO));
			String linhaResult=arquivoResult.readLine();
			StringTokenizer t1;
			StringTokenizer t2;

			////////////////
			boolean achou=false;
			while (linhaResult!=null && !achou){

				//Ex.:  ENERGY_ROUTERS 		1.000000            3.955556"
				REGEX = "ENERGY\\_ROUTERS (.*)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linhaResult);
				//Ideia: achou uma alocacao, verifica em qual datacenter ocorreu a alocacao
				if (matcher.find()){

					achou=true;

					t2 = new StringTokenizer(linhaResult, " ");
					//ENERGY_SERVERS
					t2.nextToken();
					//1.000000
					consumoEnergiaRouters = Double.parseDouble(t2.nextToken());

					//System.out.println("Passei por aqui: " + consumoEnergiaRouters);

				}//fim if

				//Proxima linha do arquivo
				linhaResult = arquivoResult.readLine();

			}//fim while

			//close the file
			arquivoResult.close();			
		} catch(Exception e){
			System.out.println("Excecao 16 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO + "\n" + e.getMessage());
			e.printStackTrace();
		}//fim catch

		return consumoEnergiaRouters;

	}//fim realizarParserConsumoEnergiaRouters

	public boolean realizarParserResultLingo(){

		boolean factivel=true;

		String REGEX = "";
		Matcher matcher;
		Pattern pattern;

		//Primeiro verifica se a solucao eh factivel
		try {
			//Abre o arquivo com o resultado Lingo			
			BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO));
			String linhaResult=arquivoResult.readLine();
			StringTokenizer t1;
			StringTokenizer t2;
			while (linhaResult!=null && factivel){

				REGEX = "Error";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linhaResult);
				if (matcher.find()){
					factivel=false;
				}//fim if
				//Proxima linha do arquivo
				linhaResult = arquivoResult.readLine();

			}//fim while

			//close the file
			arquivoResult.close();			
		} catch(Exception e){
			System.out.println("Excecao 16 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO + "\n" + e.getMessage());
		}//fim catch

		//System.out.println("Factivel: " + factivel);
		//System.exit(0);

		if (factivel){
			//O indice dos servidores comeca a partir do ultimo indice dos nodos da topologia original
			//Utilizo apenas o campo Servers do arquivo

			try
			{
				//Guarda as informacoes do arquivo
				StringBuffer info = new StringBuffer();
				//Linhas do arquivo
				String linha = new String();
				//try to open the file.
				BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER));
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

				/*				//Cria o vetor para		
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
					BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO));
					String linhaResult=arquivoResult.readLine();
					StringTokenizer t1;
					StringTokenizer t2;
					String alocacao="";
					double valorAlocacao;
					String primeiroElemento="";
					String t2_vm="";
					String t2_server="";		
					String t2_datacenter="";

					////////////////
					//Agrupa os fluxos gerados pelas VMs
					double [] fluxoGeradoAglomeradoVMs = new double[numNodes];

					//Para nao precisar ler o arquivo inteiro
					int alocacoesLidas=0;
					while (linhaResult!=null && alocacoesLidas<numServers){

						//Ex.:  AMOUNT_ALLOCS( 300)        1.000000            3.955556"
						REGEX = "AMOUNT\\_ALLOCS\\( (.*)\\)";
						pattern = Pattern.compile(REGEX);
						matcher = pattern.matcher(linhaResult);
						//Ideia: achou uma alocacao, verifica em qual datacenter ocorreu a alocacao
						if (matcher.find()){
							alocacoesLidas++;
							t2_server = matcher.group(1);
							//System.out.println("t2_server: " + t2_server);
							//Datacenter do servidor (-1 por conta do indice do Lingo)
							t2_datacenter = S_ARQUIVO[Integer.parseInt(t2_server)-1][I_SERVER_DATACENTER];
							//System.out.println(t2_datacenter);
							//O indice do fluxoGeradoAglomeradoVMs corresponde ao indice do border router
							//Ate aqui tenho a quantidade de VMs alocadas no datacenter
							t2 = new StringTokenizer(linhaResult, " ");
							//AMOUNT_ALLOCS(
							t2.nextToken();

							//300)
							t2.nextToken();

							//1.000000						
							result[Integer.parseInt(t2_server)][0]=(int)Double.parseDouble(t2.nextToken());
							totalAlocacoes+=result[Integer.parseInt(t2_server)][0];

							//Consumo de energia dos servidores eh uma estimativa, com base
							//na quantidade de alocacoes
							if (result[Integer.parseInt(t2_server)][0]>0)
								consumoEnergia[0]++;

						}//fim if

						//Proxima linha do arquivo
						linhaResult = arquivoResult.readLine();

					}//fim while

					//close the file
					arquivoResult.close();			
				} catch(Exception e){
					System.out.println("Excecao 16 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO + "\n" + e.getMessage());
				}//fim catch						

				//Consumo de energia dos roteadores
				//numRouters<=numEdges
				String [] resultConsumoEnergiaRouter = new String[numNodesOriginal];

				try {
					//Abre o arquivo com o resultado Lingo			
					BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO));
					String linhaResult=arquivoResult.readLine();
					StringTokenizer t1;
					StringTokenizer t2;				
					String primeiroElemento="";
					double t2_router_consumo=0;		

					while (linhaResult!=null){

						//Ex.:  ROUTERS_CONSUME( 300)        1.000000            3.955556"
						REGEX = "ROUTERS\\_CONSUME\\( (.*)\\)";
						pattern = Pattern.compile(REGEX);
						matcher = pattern.matcher(linhaResult);
						//Se achou a expressao regular
						if (matcher.find()){

							//Faz o parser na linha
							t2 = new StringTokenizer(linhaResult, " ");
							//ROUTERS_CONSUME(
							t2.nextToken();

							//300)
							t2.nextToken();

							//1.000000						
							t2_router_consumo = Double.parseDouble(t2.nextToken());
							//Consumo de energia dos routers eh uma estimativa
							if (t2_router_consumo>0)			
								consumoEnergia[1]++;

						}//fim if

						//Proxima linha do arquivo
						linhaResult = arquivoResult.readLine();

					}//fim while

					//close the file
					arquivoResult.close();			
				} catch(Exception e){
					System.out.println("Excecao 16_1 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO + "\n" + e.getMessage());
				}//fim catch		
				 */
				//Grava os resultados
				try{
					BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_RESULT_QTDE_SERV,false));
					out.write("Parser do arquivo: " + ARQUIVO_RESULT_LINGO + "\n");
					out.write("Arquivo da topologia: " + ARQUIVO_TOPOLOGIA_DATACENTER + "\n");
					out.write("---\n");
					out.write("Quantidade de iteracoes: " + contadorIteracoes + "\n");
					out.write("---\n");
					out.write("Tempo de execucao (s): " + (tempoFim-tempoInicio)/1000 + "\n");
					out.write("---\n");
					out.write("Tamanho da populacao: " + TAM_POPULACAO + "\n");
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
					//out.write("NumeroVMsAlocadas: " + totalAlocacoes + "\n");
					//System.out.println("NumeroVMsAlocadas: " + totalAlocacoes + "\n");
					out.write("---\n");
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

					/*				i=0;
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
					 */
					out.write("---\n");				
					out.write("numRouters numRoutersLigados consumoEnergiaRouters\n");
					//Um border router por datacenter
					//				out.write(numNodesOriginal+numDatacenters + " " + numRoutersLigados + " " + CONSUMO_ENERGIA_ROUTERS + "\n");

					out.write("---\n");
					out.write("consumoEnergiaServidores\n");
					out.write(realizarParserConsumoEnergiaServidores() + "\n");


					out.write("---\n");
					out.write("consumoEnergiaRouters\n");
					out.write(realizarParserConsumoEnergiaRouters() + "\n");

					/*					out.write("---\n");
					out.write("IndiceDatacenter IndiceServidor NumeroVMsAlocadasPorServidor\n");
					int i=0;
					while (i<result.length){
						out.write(buscarIndiceDatacenter(S_ARQUIVO,i) + " " + i + " " + result[i][0] + "\n");
						i++;
					}//fim while				
					 */
					out.write("---\n");
					out.write("Conteudo do Arquivo da Topologia\n");
					out.write("---\n");
					out.write(info.toString());
					out.write("---\n");

					//Fecha o arquivo
					out.close();
				} catch(Exception e){
					System.out.println("14Excecao ao gravar no arquivo." + e.getMessage());
				}//fim catch

				//close the file			
				file.close();			
			} catch(Exception e){
				System.out.println("Excecao 2 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER + "\n" + e.getMessage());
			}//fim catch

		}//fim if

		return factivel;

	}//fim realizarParserResultLingo

	public void gerarModeloNS2_old(){	

		System.out.println("\n--Gerar Modelo NS2 (roteamento OSPF)--");

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
			int numEdges = Integer.parseInt(token.nextToken());
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
			//conteudoNS2.append("\nset NODO_DESTINO ");
			//conteudoNS2.append(nodoDestino);
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
			i=0;
			while(i<NODO_DESTINO.length){
				if (TIPO_APLICACAO_REDE.equals("CBR/UDP"))
					conteudoNS2.append("\nset sink" + i + " [new Agent/Null]");
				else
					if (TIPO_APLICACAO_REDE.equals("FTP/TCP"))
						conteudoNS2.append("\nset sink" + i + " [new Agent/TCPSink]");
					else
						if (TIPO_APLICACAO_REDE.equals("Exp/UDP"))
							conteudoNS2.append("\nset sink" + i + " [new Agent/Null]");

				conteudoNS2.append("\n$ns attach-agent $n(" + (NODO_DESTINO[i]-1) + ") $sink"+i);
				//Next destination
				i++;
			}//end while

			//Faz o parser do resultado da alocacao
			try {
				//Abre o arquivo com o resultado Lingo			
				BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO));
				String linhaResult=arquivoResult.readLine();
				StringTokenizer t1;
				StringTokenizer t2;
				String alocacao="";
				String primeiroElemento="";
				String t2_vm="";
				String t2_server="";
				String t2_datacenter="";

				//Agrupa os fluxos gerados pelas VMs
				double [] fluxoGeradoAglomeradoVMs = new double[numNodes];

				//Para nao precisar ler o arquivo inteiro
				int alocacoesLidas=0;
				while (linhaResult!=null && alocacoesLidas<numServers){

					//Ex.:  AMOUNT_ALLOCS( 300)        1.000000            3.955556"
					REGEX = "AMOUNT\\_ALLOCS\\( (.*)\\)";
					pattern = Pattern.compile(REGEX);
					matcher = pattern.matcher(linhaResult);
					//Ideia: achou uma alocacao, verifica em qual datacenter ocorreu a alocacao
					if (matcher.find()){
						alocacoesLidas++;
						t2_server = matcher.group(1);
						//System.out.println("t2_server: " + t2_server);
						//Datacenter do servidor (-1 por conta do indice do Lingo)
						t2_datacenter = S_ARQUIVO[Integer.parseInt(t2_server)-1][I_SERVER_DATACENTER];
						//System.out.println(t2_datacenter);
						//O indice do fluxoGeradoAglomeradoVMs corresponde ao indice do border router
						//Ate aqui tenho a quantidade de VMs alocadas no datacenter
						t2 = new StringTokenizer(linhaResult, " ");
						//AMOUNT_ALLOCS(
						t2.nextToken();

						//300)
						t2.nextToken();

						//1.000000
						//O indice do fluxoGeradoAglomeradoVMs corresponde ao indice datacenter
						fluxoGeradoAglomeradoVMs[Integer.parseInt(t2_datacenter)]+=Double.parseDouble(t2.nextToken());

					}//fim if

					//Proxima linha do arquivo
					linhaResult = arquivoResult.readLine();

				}//fim while

				i=0;
				//Multiplica a quantidade de VMs alocadas pelo fluxo gerado por cada uma delas
				while(i<fluxoGeradoAglomeradoVMs.length){
					fluxoGeradoAglomeradoVMs[i] = fluxoGeradoAglomeradoVMs[i]*VM_SMALL_BW; 
					i++;
				}//fim while

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
							conteudoNS2.append("   \n$ns connect $udp("+indiceLigacao+") $sink"+i);
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
								conteudoNS2.append("   \n$ns connect $tcp("+indiceLigacao+") $sink"+i);
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
									conteudoNS2.append("   \n$exp("+indiceLigacao+") set packetSize_ 1500");
									conteudoNS2.append("   \n#Fluxo ficticio para ocupar todo o link");
									conteudoNS2.append("   \n$exp("+indiceLigacao+") set rate_ " + fluxoGeradoAglomeradoVMs[i] + "kb");
									conteudoNS2.append("   \n$exp("+indiceLigacao+") set idle_time_ 100ms");
									conteudoNS2.append("   \n$exp("+indiceLigacao+") set burst_time_ 500ms");
									//conteudoNS2.append("   \n$exp("+indiceLigacao+") set rate_ " + NET_RATE[i] + "kb");
									//conteudoNS2.append("   \n$exp("+indiceLigacao+") set idle_time_ " + NET_IDLE_TIME[i] + "ms");
									//conteudoNS2.append("   \n$exp("+indiceLigacao+") set burst_time_ " + NET_BURST_TIME[i] + "ms");									
									conteudoNS2.append("   \n$exp("+indiceLigacao+") attach-agent $udp("+indiceLigacao+")");
									conteudoNS2.append("   \n#Conecta o nodo ao agente sink do nodo destino");
									conteudoNS2.append("   \n$ns connect $udp("+indiceLigacao+") $sink"+i);
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

				//close the file
				arquivoResult.close();			
			} catch(Exception e){
				System.out.println("Excecao 17 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO + "\n" + e.getMessage());
			}//fim catch

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

	}//fim gerarModeloNS2




	public int adquirirLigacaoCoreRouter(String [][] EDGES_ARQUIVO, int indiceLigacao){

		int indiceCoreRouter=0;

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
					EDGES_ARQUIVO[p][campoEdgeFrom].equals(indiceLigacao+"")){
				achou=true;
				indiceCoreRouter = Integer.parseInt(EDGES_ARQUIVO[p][campoEdgeTo]);
			}//fim if
			p++;
		}//fim while

		return indiceCoreRouter;		


	}//fim adquirirLigacaoCoreRouter

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

	public void executarModeloNS2(){

		System.out.println("\n--Geracao: " + NUM_GERACAO + " Iteracao: " + NUM_ITERACAO + " --");

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

	public void exibirMatriz(int [][] result){

		int i=0;
		while (i<result.length){
			System.out.println("Serv[" + i +"]:[" + result[i][0]+"]");
			i++;
		}//fim while

	}//exibirMatriz

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

	public void exibirPopulacao(String [][] A){

		System.out.println("---");

		int i=0;
		int j=0;
		while (i<A.length){

			System.out.print("["+i+"] ");

			while (j<A[i].length-1){

				System.out.print(bin2dec(A[i][j]) + " ");
				j++;

			}//fim while
			//Imprime o campo de fitness (double)
			System.out.print(A[i][j] + "\n");			

			i++;
			j=0;
		}//fim while		

	}//fim exibirPopulacao

	class ExecutarThread extends Thread {

		int INDICE_CROMOSSOMO=-1;
		double PERDA_PACOTES_TOTAL_THREAD=-1;

		/*		double LIMITE_SUPERIOR_LINK=0;
		double FATOR_CORRECAO=0;
		int VM_SMALL_CPU=0;
		int VM_SMALL_BW=0; 
		int SERVER_SMALL_BW=0;
		 */
		//Para cada thread, tenho uma copia da populacao P2
		String [] P2_thread;

		/*		public void setParametros(int vm_small_cpu, int vm_small_bw, int server_small_bw, double limiteSuperiorLink, double fatorCorrecao){

			VM_SMALL_CPU=vm_small_cpu;
			VM_SMALL_BW=vm_small_bw;
			SERVER_SMALL_BW=server_small_bw;
			LIMITE_SUPERIOR_LINK = limiteSuperiorLink;
			FATOR_CORRECAO = fatorCorrecao;

		}//fim setParametros
		 */
		public void setPopulacao(int indice, String [][] P2){

			//System.out.println("Inicio: P2[indice].length: " + P2[indice].length);

			INDICE_CROMOSSOMO=indice;

			//Copio apenas o cromossomo do indice
			P2_thread = new String[P2[INDICE_CROMOSSOMO].length];

			//Atribui e exibe cada cromossomo
			int j=0;
			while (j<P2[INDICE_CROMOSSOMO].length){				
				P2_thread[j] = P2[INDICE_CROMOSSOMO][j];				
				j++;
				//System.out.println("Indice: " + INDICE_CROMOSSOMO + " Passei por aqui: " + j);
			}//fim while
			//Exibe o que foi atribuido
			j=0;
			while (j<P2[INDICE_CROMOSSOMO].length-1){
				//Imprime o campo de fitness	
				System.out.print(bin2dec(P2_thread[j]) + " ");
				j++;
			}//fim while
			System.out.print(P2_thread[j] + "\n");

			/*try{
				Thread t = new Thread();
				t.sleep(10000);
			} catch (Exception e){}
			 */

			//System.out.println("Fim: P2[indice].length: " + P2[indice].length);

		}//fim setPopulacao

		public double getPerdaPacotesTotal(){

			return PERDA_PACOTES_TOTAL_THREAD;

		}//fim getPerdaPacotesTotal

		public String [] getP2Thread(){

			return P2_thread;

		}//fim getP2Thread

		//construtor recebe o indice do cromossomo de P2
		public void run(){

			//Faz a execuçao sequencial
			A_Thread aThread = new A_Thread();

			aThread.avaliarThread(
					INDICE_CROMOSSOMO, 
					P2_thread, 
					NUM_DATACENTERS, 
					NUM_SERVIDORES, 
					NUM_VMS,
					VM_SMALL_CPU,
					VM_SMALL_BW,
					SERVER_SMALL_BW,
					NUM_SERVIDORES_SMALL,
					NUM_SERVIDORES_LARGE,
					NUM_SERVIDORES_HUGE,
					LIMITE_SUPERIOR_LINK,
					FATOR_CORRECAO,
					TIPO_APLICACAO_REDE,
					NET_BUFFER,
					NET_PACKET_SIZE,
					NET_RATE,
					NET_IDLE_TIME,
					NET_BURST_TIME,
					DATACENTER_LINK_BACKBONE);

		}//fim construtor

	}//fim classe interna

	public void inicializarArquivosThreads(){

		//1) Grava um modelo do datacenter atual para cada thread
		try {
			//Abre o arquivo do modelo de datacenter atual
			String linha = new String();
			StringBuffer info = new StringBuffer();
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER));
			while((linha=file.readLine())!=null){
				info.append(linha+"\n");
			}//fim while
			int i=0;
			while(i<P2.length){
				//Grava na base do cluster
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+i,false));
				out.write(info.toString());
				//Fecha o arquivo de escrita
				out.close();
				//Proximo cromossomo
				i++;
			}//fim while
			//Fecha o arquivo de leitura
			file.close();
		} catch(Exception e){
			System.out.println("4Excecao ao gravar no arquivo." + e.getMessage());
		}//fim catch

	}//fim inicializarArquivosThreads

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

	public static void main(String[] args) {		

		//Nota: Para cada mudanca no numero de 'datacenters' 
		//      eh necessario ter uma topologia com, no minimo, a quantidade de nos para os 'datacenters'
		//      que se ligam ah topologia gerada pelo simulador REALcloudSim
		//Nota: Eh muito importante informar os custos dos links
		//      na topologia

		//Numero de datacenters homogeneos (mesma quantidade de servidores, com as mesmas capacidades)
		NUM_DATACENTERS = 3;

		//Numero de Servidores por datacenter 
		//(Nota: esse valor corresponde a um multiplo de 10)
		NUM_SERVIDORES = 100;

		//Numero de VMs
		NUM_VMS = 10;

		//TODO: Multiple destination nodes
		//Important: one destination node for each datacenter
		//String nodosDestino = "27 13 25";
		String nodosDestino = "28 28 28 28";
		//String nodosDestino = "6 6 6 6";
		StringTokenizer t = new StringTokenizer(nodosDestino," ");
		int i=0;
		NODO_DESTINO = new int[NUM_DATACENTERS];
		while(i<NUM_DATACENTERS){			
			NODO_DESTINO[i]=Integer.parseInt(t.nextToken());
			i++;
		}//end while

		//true: executa apenas uma iteracao
		UMA_ITERACAO=false;

		//Custos dos links (false: para execuçao com variacao dos alelos dos cromossomos)
		CUSTOS_UNITARIOS=false;

		//Numero maximo de iteracoes
		MAX_ITERACOES=50;  //20 iteracoes sao suficientes para estimar a evolucao 

		//true: execucao paralela com threads
		EXECUTAR_PARALELO=false;

		//true: execucao paralela com metodo publish/subscribe
		EXECUTAR_PUBLISH_SUBSCRIBE=true;

		PUBLISH_BASE_URL="http://10.1.2.10:8080/EventChannel/";
		PUBLISH_KEY="task";
		PUBLISH_CONTENT_TYPE="Content-Type: application/json";
		PUBLISH_VALUE="";

		//Tipo de aplicacao de rede (CBR/UDP, FTP/TCP, Exponential/UDP)
		TIPO_APLICACAO_REDE = "Exp/UDP";
		//TIPO_APLICACAO_REDE = "CBR/UDP";

		//Para a definicao de limites para gerar a Fronteira de Pareto
		//"0" = sem limite do consumo de energia
		//LIMITE_W1="0";

		//ALFA=5;
		//BETA=10-ALFA;

		//---Para o AG---
		//TAM_POPULACAO TEM que ser potencia de 2
		TAM_POPULACAO=8; //(8)
		//Para representar valores binarios nos campos dos cromossomos
		NUM_BITS=4;
		//Limite superior de valores em decimal, para inicializarPopulacao (todos os bits preenchidos)
		MAX_DECIMAL_CROMOSSOMO=14;
		//---------------

		//Para gerar o modelo Lingo
		FATOR_CORRECAO=10;   //Fixo
		LIMITE_SUPERIOR_LINK = 500; //Estimativa da vazao maxima do link
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
		ARQUIVO_PARSER_BANDA_LINKS = modeloNS2 + ".banda";

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
		//SERVER_COST_SMALL_CPU=1;
		//SERVER_COST_LARGE_CPU=2;
		SERVER_COST_HUGE_CPU=1;

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
		i=0;
		NET_PACKET_SIZE = new int[NUM_DATACENTERS];
		NET_BURST_TIME = new int[NUM_DATACENTERS];
		NET_IDLE_TIME = new int[NUM_DATACENTERS];
		NET_RATE = new int[NUM_DATACENTERS];
		while(i<NUM_DATACENTERS){
			NET_PACKET_SIZE[i] = 1500;
			NET_BURST_TIME[i] = 500;
			NET_IDLE_TIME[i] = 100;
			NET_RATE[i] = 0; 
			i++;
		}//fim while
		//--Configuracoes especificas por datacenter sao feitas aqui
		//Datacenter0
		/*NET_PACKET_SIZE[0] = 1500;
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
		 */
		new A_PLI_AG_NS2_milpFlow();

	}//fim main

}//fim classe
