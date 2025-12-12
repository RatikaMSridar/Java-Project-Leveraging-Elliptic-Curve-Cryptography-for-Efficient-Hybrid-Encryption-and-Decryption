package org.cloudbus.cloudsim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cloudbus.cloudsim.brite.Visualizer.BRITETopologyPO;
import org.cloudbus.cloudsim.brite.Visualizer.Edge;
import org.cloudbus.cloudsim.brite.Visualizer.Node;

public class A_Thread extends Thread {

	private int NUM_SERVIDORES_SMALL=0;
	private int NUM_SERVIDORES_LARGE=0;
	private int NUM_SERVIDORES_HUGE=0;
	private double LIMITE_SUPERIOR_LINK=0;
	private double FATOR_CORRECAO=0;	
	private int VM_SMALL_CPU=0;
	private int VM_SMALL_BW=0;
	private int SERVER_SMALL_BW=0;	

	public int NUM_NODES=0;
	public int NUM_EDGES=0;
	public int NUM_DATACENTERS=0;
	public int NUM_SERVIDORES=0;
	public int NUM_VMS=0;
	public int NODO_DESTINO=0;

	//---Lista Tabu: evita os nodos onde ocorreu perda anterior de pacotes
	private int[][] LISTA_TABU;	

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

	private int I_CLOUD_VM=0;
	private int I_CLOUD_SERVER_INDEX=1;
	private int I_CLOUD_VM_CPU=2;
	private int I_CLOUD_VM_RAM=3;
	private int I_CLOUD_VM_DISK=4;
	private int I_CLOUD_VM_BW=5;
	private int I_CLOUD_VM_FLUXO=6;	
	private int I_CLOUD_VM_VIRTUALIZER=7;		

	private String PATH="";
	private String ARQUIVO_MODELO_LINGO = "";
	private String ARQUIVO_RESULT_LINGO = "";
	private String ARQUIVO_RANGE_LINGO = "";
	private String ARQUIVO_RESULT_QTDE_SERV = "";
	private String ARQUIVO_PARSER_BANDA_LINKS = "";
	private String ARQUIVO_EVOLUCAO_FITNESS = "";
	private String ARQUIVO_FITNESS_AMOSTRA = "";
	private String ARQUIVO_TOPOLOGIA_ORIGINAL = "";
	private String ARQUIVO_TOPOLOGIA_DATACENTER = "";
	private String ARQUIVO_MODELO_NS2 = "";
	private String ARQUIVO_NAM_NS2 = "";
	private String ARQUIVO_RESULT_NS2 = "";
	private String ARQUIVO_VAZAO_NS2 = "";		
	private String ARQUIVO_PERDA_NS2 = "";		
	private String ARQUIVO_ATRASOMEDIO_NS2 = "";	
	private String ARQUIVO_EVOLUCAO_VAZAO = "";
	private String ARQUIVO_EVOLUCAO_PERDA = "";
	private String ARQUIVO_EVOLUCAO_ATRASO = "";	
	
	private String TIPO_APLICACAO_REDE="";
	private int NET_BUFFER;
	private int [] NET_PACKET_SIZE;
	private int [] NET_BURST_TIME;
	private int [] NET_IDLE_TIME;
	private int [] NET_RATE;
	private double DATACENTER_LINK_BACKBONE=0;

	//Consumo de energia da rede
	private int CONSUMO_ENERGIA_ROUTERS=0;
	private double CONSUMO_ENERGIA_SERVIDORES=0;

	public double VAZAO_TOTAL_VMs=0;

	//Assumo que String [] P2_thread eh passada por referencia, ou seja,
	//alteracoes feitas para P2_thread aqui sao visiveis fora desse metodo
	public void avaliarThread(
			int rank, 
			String [] P2_thread, 
			int numData, 
			int numServ, 
			int numVMs, 
			int vmSmallCPU,
			int vmSmallBW,
			int serverSmallBW,
			int numServSmall,
			int numServLarge,
			int numServHuge,
			double limiteSuperiorLink, 
			double fatorCorrecao,
			String tipoAplicacaoRede,
			int netBuffer,
			int [] netPacketSize,
			int [] netBurstTime,
			int [] netIdleTime,
			int [] netRate,
			double datacenterLinkBackbone){

		//Forma novamente o nome dos arquivos
		NUM_DATACENTERS = numData;
		NUM_SERVIDORES = numServ;
		NUM_VMS = numVMs;		
		VM_SMALL_CPU=vmSmallCPU;
		VM_SMALL_BW=vmSmallBW;
		SERVER_SMALL_BW=serverSmallBW;
		NUM_SERVIDORES_SMALL=numServSmall;
		NUM_SERVIDORES_LARGE=numServLarge;
		NUM_SERVIDORES_HUGE=numServHuge;
		LIMITE_SUPERIOR_LINK=limiteSuperiorLink;
		FATOR_CORRECAO=fatorCorrecao;
		TIPO_APLICACAO_REDE=tipoAplicacaoRede;
		
		int i=0;

		NET_BUFFER=netBuffer;
		
		NET_PACKET_SIZE = new int[NUM_DATACENTERS];
		NET_BURST_TIME = new int[NUM_DATACENTERS];
		NET_IDLE_TIME = new int[NUM_DATACENTERS];
		NET_RATE = new int[NUM_DATACENTERS];
		while(i<NUM_DATACENTERS){
			NET_PACKET_SIZE[i]=netPacketSize[i];
			NET_BURST_TIME[i]=netBurstTime[i];
			NET_IDLE_TIME[i]=netIdleTime[i];
			NET_RATE[i]=netRate[i];
			i++;
		}//fim while		
		
		DATACENTER_LINK_BACKBONE=datacenterLinkBackbone;
		
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

		System.out.println("\n---avaliarThread---");

		//1milisegundo = 0,001segundos
		long tempoInicio = System.currentTimeMillis();		

		//Para cada individuo da populacao P2
		double vazaoTotalVMs=0;
		int campoFitness;
		double custoRede=0;
		double fitness=0;		
		int alelo=0;

		double somatorioCapLinks=0;
		double somatorioPorcentagemPerda=0;
		double somatorioAtrasoMedio=0;
		
		boolean factivel=true;

		//Atualiza a topologia do datacenter com os dados do cromossomo atual
		atualizarTopologiaThread(rank, P2_thread);

		//Gera o modelo lingo com os parametros da populacao do AG

		//Para cada individuo gera o modelo lingo
		//Transforma o conteudo do arquivo.datacenter em Linguagem Lingo
		gerarModeloLingoCompactoThread(rank);

		//Resolver Modelo Lingo
		executarModeloLingoThread(rank);

		//Calcula a quantidade de alocacoes por servidor
		factivel = realizarParserResultLingoThread(rank);

		int j=0;
		
		if (factivel){

			gerarModeloNS2Thread(rank);

			executarModeloNS2Thread(rank);

			//VAZAO_TOTAL_VMs=realizarParserVazaoThread(rank);
			//desativa os links e routers sem vazao
			//desativarLinksRoutersThread(rank);


			//Executa novamente para verificar se ao desativar os links
			//nao ocorre perda de pacotes
			//executarModeloNS2Thread(rank);

			somatorioPorcentagemPerda=realizarParserPerdaThread(rank);
			
			somatorioAtrasoMedio=realizarParserAtrasoMedioThread(rank);		

			//Fitness = somatorio das capacidades dos links de rede +
			//			consumo de energia dos routers +
			//			consumo de energia dos servidores +
			//          somatorio da porcentagem da perda de pacotes +
			//          somatorio do atraso medio dos links
			j=0;
			somatorioCapLinks=0;
			while(j<P2_thread.length-1){
				alelo=bin2decThread(P2_thread[j]);
				somatorioCapLinks += (LIMITE_SUPERIOR_LINK * alelo) / FATOR_CORRECAO;
				j++;
			}//fim while

			fitness=somatorioCapLinks+
			realizarParserConsumoEnergiaServidoresThread(rank)+
			realizarParserConsumoEnergiaRoutersThread(rank) +
			somatorioPorcentagemPerda+
			somatorioAtrasoMedio;

		} else
			//Penaliza o individuo que nao possui solucao factivel
			fitness=888888;

		P2_thread[j]=fitness+"";


		/*try{
			Thread t = new Thread();
			System.out.println("\nPerda pacotes total: " + perdaPacotesTotal);
			//t.sleep(10000);
		} catch (Exception e){}

		long tempoFim = System.currentTimeMillis();
		System.out.println("Tempo de execucao (ms): " + (tempoFim-tempoInicio));
		System.out.println("Tempo de execucao (s): " + (tempoFim-tempoInicio)/1000);
		 */

	}//fim avaliarThread	

	public double realizarParserConsumoEnergiaServidoresThread(int rank){

		double consumoEnergiaServidores=0;

		String REGEX = "";
		Matcher matcher;
		Pattern pattern;

		try {
			//Abre o arquivo com o resultado Lingo			
			BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO + "_" + rank));
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
			System.out.println("Excecao 16 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO + "_" + rank + "\n" + e.getMessage());
			e.printStackTrace();
		}//fim catch

		return consumoEnergiaServidores;

	}//fim realizarParserConsumoEnergiaServidoresThread

	public double realizarParserConsumoEnergiaRoutersThread(int rank){

		double consumoEnergiaRouters=0;

		String REGEX = "";
		Matcher matcher;
		Pattern pattern;

		try {
			//Abre o arquivo com o resultado Lingo			
			BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO + "_" + rank));
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
			System.out.println("Excecao 16 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO + "_" + rank + "\n" + e.getMessage());
			e.printStackTrace();
		}//fim catch

		return consumoEnergiaRouters;

	}//fim realizarParserConsumoEnergiaRoutersThread

	public void atualizarTopologiaThread(int rank, String [] P2_thread){

		//Recupera as informacoes do arquivo da topologia:
		//NUM_NODES,NUM_EDGES,NUM_DATACENTERS,NUM_SERVIDORES,NUM_VMS,NODO_DESTINO
		//Todas as informacoes das arestas: EDGES_ARQUIVO
		//Todas as informacoes dos servidores: S_ARQUIVO
		//Todas as informacoes das VMs: VM_ARQUIVO

		System.out.println("\n---Atualizar Topologia: Thread["+rank+"]---");		

		//Adquire as informacoes do arquivo
		try
		{
			int i=0;
			int j=0;

			String linha = new String();
			StringBuffer info = new StringBuffer();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank));

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
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");

			String id;
			String from;
			String to;
			String delay;
			String bw;
			String asfrom;
			String asto;
			String type;
			String other;

			//Inicializa a lista tabu
			//numNodes+numEdges por causa dos indices (tenho indices para os roteadores de borda tb)
			LISTA_TABU = new int[NUM_NODES+NUM_EDGES][NUM_NODES+NUM_EDGES];
			renovarListaTabuThread();

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
				//se o link nao pertence ah lista tabu, adiciona custo 0					
				/*if (pertenceListaTabuThread(Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeFrom]),Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeTo])))
					EDGES_ARQUIVO[i][j++] = "999999999";
				else
				 */
				//Aqui estah certo, pq P2_thread estah no mesmo indice de P2[indiceCromossomo][i]
				EDGES_ARQUIVO[i][j++] = bin2decThread(P2_thread[i]+"")+"";			

				//EDGES_ARQUIVO[i][j++] = bin2dec(P2[indiceCromossomo][i]+"")+"";
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

				//ASfrom (6)
				asfrom=token.nextToken();
				//System.out.print(asfrom + " ");
				EDGES_ARQUIVO[i][j++] = asfrom;

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
			NODO_DESTINO = Integer.parseInt(token.nextToken());
			//System.out.println("nodoDestino: " + NODO_DESTINO);

			//close the file			
			file.close();

			//Grava a atualizacao de custos no arquivo de topologia do datacenter
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank,false));			
				out.write(info.toString());
				out.close();
			} catch(Exception e){
				System.out.println("7Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

		} catch(Exception e){
			System.out.println("Excecao 4 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank + "\n" + e.getMessage());
		}//fim catch

	}//fim atualizarTopologiaThread

	public void gerarModeloLingoCompactoThread(int rank){

		System.out.println("\n---Gerar Modelo Lingo Compacto: Thread["+rank+"]---\n");

		//Adquire as informacoes do arquivo
		try
		{
			int i=0;
			int j=0;

			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank));
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
			int campoEdgeCost=3;
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
				//Length (3) (Atualizar no campo Length o custo ficticio do enlace)
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Delay (4) 
				EDGES_ARQUIVO[i][j++] = token.nextToken();
				//Bandwidth (5) (Nao posso alterar a bandwidth pq ela jah foi contratada)
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
			int nodoDestino = Integer.parseInt(token.nextToken());
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

			passo[indice].append("\nRE = 0.06;");
			passo[indice].append("\nENDDATA\n");
			indice++; //2
			//System.out.println(passo[1].toString());

			//Gera a matriz de links APENAS para os routers internos			
			passo[indice] = new StringBuffer();
			i=0;
			j=0;
			double alelo=0;
			double capacidadeLink=0;
			while(i<(numNodesOriginal)){
				j=0;
				while(j<(numNodesOriginal)){

					//Se existe custo para o link (soh existe custo se o link existe)
					if (recuperarValorCustoThread(EDGES_ARQUIVO, i, j)!=0){
						//O custo eh o valor do campo do cromossomo no arquivo
						alelo = recuperarValorCustoThread(EDGES_ARQUIVO, i, j);
						if (alelo>10)
							alelo=10;
						capacidadeLink = (LIMITE_SUPERIOR_LINK * alelo) / FATOR_CORRECAO;
						passo[indice].append("L("+ (i+1) + "," + (j+1) + ") = " + capacidadeLink + ";\n");
					}//fim if
					j++;
				}//fim while
				i++;
			}//fim while
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
				passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I))) = " + adquirirFonteTrafegoDatacenterThread(EDGES_ARQUIVO,i) + ";");				
				i++;
			}//fim while
			indice++; //9
			//System.out.println(passo[6].toString());

			passo[indice] = new StringBuffer();
			passo[indice].append("\n\n!CONSERVATION ON ROUTERS;");
			//Para a rede
			//Forma uma estrutura de conexao a partir dos links do arquivo da topologia
			BRITETopologyPO topologiaRede = new BRITETopologyPO(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank);
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
			passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I))) = " + adquirirFluxosEntradaThread(EDGES_ARQUIVO,nodoDestino) + ";");
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
				passo[indice].append("\nFIN("+(i+1)+") = " + adquirirFluxosEntradaThread(EDGES_ARQUIVO,i) + ";");
				i++;
			}//fim while
			indice++; //13
			//System.out.println(passo[10].toString());

			passo[indice] = new StringBuffer();
			passo[indice].append("\n!X: SLACK;");
			passo[indice].append("\n@FOR(SERVERS(J):");
			passo[indice].append("\n@SUM(VMS(I): A(I,J)) + X(J) = SCPU(J));");
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
					"\n!DIVERT " + ARQUIVO_RANGE_LINGO+"_"+rank + 
					"\n!RANGE" + 

					"\n! Open a file;" + 
					"\nDIVERT " + ARQUIVO_RESULT_LINGO+"_"+rank + 
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
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_MODELO_LINGO+"_"+rank,false));			
				out.write(modeloLingo.toString());
				out.close();
			} catch(Exception e){
				System.out.println("13Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Excecao 14 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER + "_" + rank + "\n" + e.getMessage());
			e.printStackTrace();
		}//fim catch

	}//fim gerarModeloLingoCompactoThread

	public String adquirirFluxosEntradaThread(String [][] EDGES_ARQUIVO, int nodoDestino){

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
				(Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeFrom])+1) + "," + 
				(Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeTo])+1) + ")";
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

	}//fim adquirirFluxosEntradaThread

	public double recuperarValorCustoThread(String [][] EDGES_ARQUIVO, int i, int j){

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

	}//fim recuperarValorCustoThread

	public void executarModeloLingoThread(int rank){

		System.out.println("---Executar modelo no Lingo: Thread["+rank+"]---");

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
		String comando = PATH + "/executarModeloLingo.sh " + ARQUIVO_MODELO_LINGO + "_" + rank;

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
			System.out.println("Fim da execucao do modelo Lingo: Thread ["+rank+"]");
		} catch (Exception e){
			System.out.println("Excecao: Erro ao executar o programa Lingo: Thread ["+rank+"]");
		}//fim catch

	}//fim executarModeloLingoThread

	public boolean realizarParserResultLingoThread(int rank){

		boolean factivel=true;

		String REGEX = "";
		Matcher matcher;
		Pattern pattern;

		//Primeiro verifica se a solucao eh factivel
		try {
			//Abre o arquivo com o resultado Lingo			
			BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO + "_" + rank));
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
			System.out.println("Excecao 16 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO + "_" + rank + "\n" + e.getMessage());
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
				BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER + "_" + rank));
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

				//close the file			
				file.close();			
			} catch(Exception e){
				System.out.println("Excecao 2 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER + "_" + rank + "\n" + e.getMessage());
			}//fim catch

		}//fim if

		return factivel;

	}//fim realizarParserResultLingoThread

	public void gerarModeloNS2Thread(int rank){	

		System.out.println("\n---Gerar Modelo NS2: Thread["+rank+"]---");

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
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank));
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
			conteudoNS2.append("\nset nf [open " + ARQUIVO_NAM_NS2+"_"+rank + " w]");
			conteudoNS2.append("\n$ns namtrace-all $nf");

			conteudoNS2.append("\n\n#Abre o arquivo de rastreamento para guardar os eventos");
			conteudoNS2.append("\nset nd [open " + ARQUIVO_RESULT_NS2+"_"+rank + " w]");
			conteudoNS2.append("\n$ns trace-all $nd");
			conteudoNS2.append("\n");

			conteudoNS2.append("\n#Define um procedimento de finish");
			conteudoNS2.append("\nproc finish {} {");
			conteudoNS2.append("\n   global ns nf nd");
			conteudoNS2.append("\n   $ns flush-trace");			
			conteudoNS2.append("\n   close $nf");
			conteudoNS2.append("\n   close $nd");
			conteudoNS2.append("\n   #exec nam " + ARQUIVO_NAM_NS2+"_"+rank + " &");
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
			try {
				//Abre o arquivo com o resultado Lingo			
				BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO+"_"+rank));
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

						indiceLigacao = adquirirIndiceLigacaoThread(EDGES_ARQUIVO, i);

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

				//close the file
				arquivoResult.close();			
			} catch(Exception e){
				System.out.println("Excecao 17 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO+"_"+rank + "\n" + e.getMessage());
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
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_MODELO_NS2+"_"+rank,false));			
				out.write(conteudoNS2.toString());
				out.close();
			} catch(Exception e){
				System.out.println("15Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

			//Grava a banda dos links em arquivo
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_PARSER_BANDA_LINKS+"_"+rank,false));			
				out.write(banda.toString());
				out.close();
			} catch(Exception e){
				System.out.println("16Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch			

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Excecao 18 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank + "\n" + e.getMessage());
		}//fim catch

	}//fim gerarModeloNS2Thread

	public void executarModeloNS2Thread(int rank){

		System.out.println("\n---Executar o modelo no NS2: Thread["+rank+"]---");

		String comando = "ns " + ARQUIVO_MODELO_NS2 + "_"+rank;

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
			System.out.println("Fim da execucao do modelo no NS2: Thread["+rank+"]");
		} catch (Exception e){
			System.out.println("Excecao: Erro ao executar o modelo NS2: Thread["+rank+"]");
		}//fim catch

	}//fim executarModeloNS2
	
	public int adquirirIndiceLigacaoThread(String [][] EDGES_ARQUIVO, int indiceDatacenter){

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

	}//fim adquirirIndiceLigacaoThread

	public String adquirirFonteTrafegoDatacenterThread(String [][] EDGES_ARQUIVO, int i){

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

	}//adquirirFonteTrafegoDatacenterThread

	public double realizarParserVazaoThread(int rank){

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
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank));
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
				BufferedReader fileResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_NS2+"_"+rank));
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
				System.out.println("Excecao 8: Erro ao ler o arquivo: " + ARQUIVO_RESULT_NS2+"_"+rank);
			}//fim catch

			//Grava o resultado em arquivo
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_VAZAO_NS2+"_"+rank,false));			
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
			System.out.println("Excecao 9 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank + "\n" + e.getMessage());
		}//fim catch

		return vazaoTotalVMs;

	}//fim realizarParserVazaoThread

	public void desativarLinksRoutersThread(int rank){

		//Remonta o arquivo para o NS2, apos a analise da vazao
		//Os links sem vazao sao desligados
		System.out.println("\n--Gerar Modelo NS2 Thread (Para reduzir consumo de energia)--");

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
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank));
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

			/*//Consumo de energia dos roteadores
			CONSUMO_ENERGIA_ROUTERS = new int[numNodes+numEdges];
			//(com isso posso saber quantos roteadores foram criados, quantos estao ligados/desligados)
			//inicializa: -1 indice do router nao alocado
			//             1 indice do router ligado
			//             0 indice do router desligado 
			int p=0;
			while(p<CONSUMO_ENERGIA_ROUTERS.length){
				CONSUMO_ENERGIA_ROUTERS[p]=-1;
				p++;
			}//fim while						
			 */

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
			conteudoNS2.append("\nset nf [open " + ARQUIVO_NAM_NS2+"_"+rank + " w]");
			conteudoNS2.append("\n$ns namtrace-all $nf");

			conteudoNS2.append("\n\n#Abre o arquivo de rastreamento para guardar os eventos");
			conteudoNS2.append("\nset nd [open " + ARQUIVO_RESULT_NS2+"_"+rank + " w]");
			conteudoNS2.append("\n$ns trace-all $nd");
			conteudoNS2.append("\n");

			conteudoNS2.append("\n#Define um procedimento de finish");
			conteudoNS2.append("\nproc finish {} {");
			conteudoNS2.append("\n   global ns nf nd");
			conteudoNS2.append("\n   $ns flush-trace");			
			conteudoNS2.append("\n   close $nf");
			conteudoNS2.append("\n   close $nd");
			conteudoNS2.append("\n   #exec nam " + ARQUIVO_NAM_NS2+"_"+rank + " &");
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
						conteudoNS2.append("\n   #IndiceNS2: n("+ indiceNodosNS2 +")");
						conteudoNS2.append("\n   set n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") [$ns node]");
						conteudoNS2.append("\n$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") color blue");
						conteudoNS2.append("\n$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") shape hexagon");
					}//fim if
					if (Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeTo])>=numNodosOriginal){
						conteudoNS2.append("\n   set n("+EDGES_ARQUIVO[i][campoEdgeTo]+") [$ns node]");
						conteudoNS2.append("\n$n("+EDGES_ARQUIVO[i][campoEdgeTo]+") color blue");
						conteudoNS2.append("\n$n("+EDGES_ARQUIVO[i][campoEdgeTo]+") shape hexagon");
					}

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
					if (EDGES_ARQUIVO[i][campoEdgeType].equals("BORDER_ROUTER"))
						//banda.append("BorderRouter_"+(indiceNodosNS2+1)+"--"+EDGES[i][1]+" "+EDGES[i][2]+"\n");
						banda.append("BorderRouter_"+i+"--"+EDGES_ARQUIVO[i][campoEdgeTo]+" "+EDGES_ARQUIVO[i][campoEdgeBw]+"\n");
					else
						banda.append(EDGES_ARQUIVO[i][campoEdgeFrom]+"--"+EDGES_ARQUIVO[i][campoEdgeTo]+" "+EDGES_ARQUIVO[i][campoEdgeBw]+"\n");					

					//Verifica se existe vazao no link					
					if (Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeFrom])>=numNodosOriginal){
						if(!linkComVazaoThread(rank, indiceNodosNS2+"", EDGES_ARQUIVO[i][campoEdgeTo]))
							//Desabilita o link	
							conteudoNS2.append("\n$ns rtmodel-at 0.1 down " + "$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") " + "$n("+EDGES_ARQUIVO[i][campoEdgeTo]+")");
						//Desabilita o router
						if(!routerComVazaoThread(rank, indiceNodosNS2+"")){
							conteudoNS2.append("\n   $n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") color red");
							//CONSUMO_ENERGIA_ROUTERS[indiceNodosNS2]=0;
						} //else
						//CONSUMO_ENERGIA_ROUTERS[indiceNodosNS2]=1;

						if(!routerComVazaoThread(rank, EDGES_ARQUIVO[i][campoEdgeTo])){
							conteudoNS2.append("\n   $n("+EDGES_ARQUIVO[i][campoEdgeTo]+") color red");
							//CONSUMO_ENERGIA_ROUTERS[campoEdgeTo]=0;
						} //else
						//CONSUMO_ENERGIA_ROUTERS[campoEdgeTo]=1;

						//Agora posso incrementar o indice
						indiceNodosNS2++;
					} else {
						if(!linkComVazaoThread(rank, EDGES_ARQUIVO[i][campoEdgeFrom], EDGES_ARQUIVO[i][campoEdgeTo]))
							//Desabilita o link	
							conteudoNS2.append("\n$ns rtmodel-at 0.1 down " + "$n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") " + "$n("+EDGES_ARQUIVO[i][campoEdgeTo]+")");
						if(!routerComVazaoThread(rank, EDGES_ARQUIVO[i][campoEdgeFrom]+"")){
							//Desabilita o router
							conteudoNS2.append("\n   $n("+EDGES_ARQUIVO[i][campoEdgeFrom]+") color red");
							//CONSUMO_ENERGIA_ROUTERS[campoEdgeFrom]=0;
						} //else
						//CONSUMO_ENERGIA_ROUTERS[campoEdgeFrom]=1;

						if(!routerComVazaoThread(rank, EDGES_ARQUIVO[i][campoEdgeTo])){
							//Desabilita o router
							conteudoNS2.append("\n   $n("+EDGES_ARQUIVO[i][campoEdgeTo]+") color red");
							//CONSUMO_ENERGIA_ROUTERS[campoEdgeTo]=0;
						} //else
						//CONSUMO_ENERGIA_ROUTERS[campoEdgeTo]=1;

					}//fim else
				}//fim if
				i++;
			}//fim while

			//Gera as variaveis globais 
			//(eh necessario fazer aqui porque a gravacao do parser tambem ocorre sem desativar os links)
			/*NUM_ROUTERS=0;
			NUM_ROUTERS_LIGADOS=0;
			p=0;
			while(p<CONSUMO_ENERGIA_ROUTERS.length){
				if (CONSUMO_ENERGIA_ROUTERS[p]!=-1)
					NUM_ROUTERS++;
				if (CONSUMO_ENERGIA_ROUTERS[p]==1)
					NUM_ROUTERS_LIGADOS++;
				p++;
			}//fim while
			 */		

			//Cria o nodo destino
			conteudoNS2.append("\n\n#Nodo sink (destino)");
			conteudoNS2.append("\nset sink [new Agent/Null]");
			conteudoNS2.append("\n$ns attach-agent $n($NODO_DESTINO) $sink");			


			//Faz o parser do resultado da alocacao
			try {
				//Abre o arquivo com o resultado Lingo			
				BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO+"_"+rank));
				String linhaResult=arquivoResult.readLine();
				StringTokenizer t1;
				StringTokenizer t2;
				String alocacao="";
				double valorAlocacao;
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

							valorAlocacao=Double.parseDouble(alocacao);
							//if (alocacao.equals("1.000000")){
							if (valorAlocacao>0.5){ //Por conta de nao ser mais um modelo com variaveis binarias para a alocacao

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
				System.out.println("Excecao 3 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO+"_"+rank + "\n" + e.getMessage());
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
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_MODELO_NS2+"_"+rank,false));			
				out.write(conteudoNS2.toString());
				out.close();
			} catch(Exception e){
				System.out.println("5Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch

			//Grava a banda dos links em arquivo
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_PARSER_BANDA_LINKS+"_"+rank,false));			
				out.write(banda.toString());
				out.close();
			} catch(Exception e){
				System.out.println("6Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch			

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Excecao 5 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank + "\n" + e.getMessage());
		}//fim catch

	}//fim desativarLinksRoutersThread

	public boolean routerComVazaoThread(int rank, String nodo1){

		boolean result=false;

		//Abre o arquivo com o resultado da vazao
		//Caso o router nao seja encontrado, nao ocorre fluxo no router

		String nodoOrigem2="";
		String nodoDestino2="";	

		try
		{
			//Guarda as informacoes do arquivo
			StringBuffer info = new StringBuffer();

			//Linhas do arquivo
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_VAZAO_NS2+"_"+rank));

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

					//Verifica se existe vazao no router
					if(nodo1.equals(nodoOrigem2)||nodo1.equals(nodoDestino2))
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
			System.out.println("Excecao 10 ao abrir o arquivo: " + ARQUIVO_VAZAO_NS2+"_"+rank + "\n" + e.getMessage());
		}//fim catch		

		return result;

	}//fim routerComVazaoThread

	public boolean linkComVazaoThread(int rank, String nodoOrigem1, String nodoDestino1){

		boolean result=false;

		//Abre o arquivo com o resultado da vazao
		//Caso o link nao seja encontrado, nao ocorre fluxo no link
		//(Eh necessario verificar se a ordem inversa entre 
		//nodoOrigem e nodoDestino ocorre porque o canal eh duplex,
		//ou seja, 3--6 eh equivalente a 6--3)

		String nodoOrigem2="";
		String nodoDestino2="";	

		try
		{
			//Guarda as informacoes do arquivo
			StringBuffer info = new StringBuffer();

			//Linhas do arquivo
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_VAZAO_NS2+"_"+rank));

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

					//Verifica se existe vazao no link 
					//3--6 eh equivalente ah 6--3 para canal duplex
					if(nodoOrigem1.equals(nodoOrigem2)&&nodoDestino1.equals(nodoDestino2)||
							nodoOrigem1.equals(nodoDestino2)&&nodoDestino1.equals(nodoOrigem2)){
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
			System.out.println("Excecao 11 ao abrir o arquivo: " + ARQUIVO_VAZAO_NS2+"_"+rank + "\n" + e.getMessage());
		}//fim catch		

		return result;

	}//fim linkComVazaoThread

	public double realizarParserPerdaThread(int rank){

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
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank));
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
			StringBuffer parserPerda = new StringBuffer("From-To PacketsSend PacketsDrop");
			//System.out.println("\nFrom-To PacketsSend PacketsDrop");

			try {
				//try to open the file.
				BufferedReader fileResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_NS2+"_"+rank));
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

						}//fim if

					} catch(Exception e){
						System.out.println("Aviso 1: Encontrada linha fora do padrao de envio/recepcao de pacotes");						
					}//fim catch
					linhaResult = fileResult.readLine();															

				}//fim while				

				//Resultados dos links				
				i=0;
				j=0;
				while(i<packetsSendLink.length){
					j=0;
					while(j<packetsSendLink[i].length){
						if (packetsSendLink[i][j]!=0){
							//System.out.println(i + "--" + j + " " + packetsSendLink[i][j] + " " + packetsDropLink[i][j]);			
							parserPerda.append("\n" + i + "--" + j + " " + packetsSendLink[i][j] + " " + packetsDropLink[i][j]);							

							//Ao desativar links pacotes sao perdidos, mas
							//quero saber se ocorre perda nos links que permaneceram ativos
							somatorioPorcentagemPerda+=packetsDropLink[i][j];

						}//fim if
						j++;
					}//fim while 
					i++;
				}//fim while

				//Grava o resultado em arquivo
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_PERDA_NS2+"_"+rank,false));			
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
				System.out.println("Excecao 12: Erro ao ler o arquivo: " + ARQUIVO_RESULT_NS2+"_"+rank + " Excecao: " + e.getMessage());
			}//fim catch

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Excecao 13 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank + "\n" + e.getMessage());
		}//fim catch

		return somatorioPorcentagemPerda;

	}//fim realizarParserPerdaThread

	public String buscarIndiceDatacenterThread(String [][] S_ARQUIVO, int indiceServidor){

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

	}//fim buscarIndiceDatacenterThread

	public void inicializarMatrizThread(int [][] result){

		int i=0;
		while (i<result.length){
			result[i][0]=0;
			i++;
		}//fim while

	}//fim inicializarMatrizThread

	public boolean pertenceListaTabuThread(int indiceOrigem, int indiceDestino){

		boolean result=false;

		if(LISTA_TABU[indiceOrigem][indiceDestino]>0)
			result=true;

		return result;

	}//fim pertenceListaTabuThread

	public void renovarListaTabuThread(){

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

	}//fim renovarListaTabuThread

	public String dec2binThread( int dec, int NUM_BITS ) {  
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
	}//fim dec2binThread

	public int bin2decThread( String bin ) {  
		int i, result = 0;  

		for( i = 0; i < bin.length(); i++ ) {  
			result <<= 1;  
			if( bin.charAt( i ) == '1' ) result++;  
		}//fim for

		return result;  
	}//fim bin2decThread

	public String removerEspacosThread(String s) {
		StringTokenizer st = new StringTokenizer(s," ",false);
		String t="";
		while (st.hasMoreElements()) t += st.nextElement();
		return t;
	}//fim removerEspacosThread

	public boolean repetidoRouterThread(int indiceRouter, StringBuffer listaRouters){

		boolean achou=false;

		StringTokenizer token = new StringTokenizer(listaRouters.toString(), " ");
		while(token.hasMoreTokens()&&!achou){
			if (indiceRouter==Integer.parseInt(token.nextToken().toString()))
				achou=true;			
		}//fim while

		return achou;

	}//fim repetidoRouterThread
	
	public double realizarParserAtrasoMedioThread(int rank){

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
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank));
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
				BufferedReader fileResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_NS2+"_"+rank));
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
				System.out.println("Excecao 20: Erro ao ler o arquivo: " + ARQUIVO_RESULT_NS2+"_"+rank + " : " + e.getMessage());
			}//fim catch

			//System.out.println("MaiorPID: " + maiorPID);

			//Abre o arquivo novamente para o calculo do atraso
			double [] t_arr = new double[maiorPID+1];				

			try {
				//try to open the file.
				BufferedReader fileResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_NS2+"_"+rank));
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
				System.out.println("Excecao 21: Erro ao ler o arquivo: " + ARQUIVO_RESULT_NS2+"_"+rank + " : " + e.getMessage());
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
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_ATRASOMEDIO_NS2+"_"+rank,false));			
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
			System.out.println("Excecao 23 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER +"_"+rank+ "\n" + e.getMessage());
		}//fim catch
		
		return somatorioAtrasoMedio;

	}//fim realizarParserAtrasoMedioThread

}//fim classe