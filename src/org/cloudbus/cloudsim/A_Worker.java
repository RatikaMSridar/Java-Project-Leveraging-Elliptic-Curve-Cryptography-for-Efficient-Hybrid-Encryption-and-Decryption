package org.cloudbus.cloudsim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cloudbus.cloudsim.brite.Visualizer.BRITETopologyPO;
import org.cloudbus.cloudsim.brite.Visualizer.Edge;
import org.cloudbus.cloudsim.brite.Visualizer.Node;

public class A_Worker {

	private static String SUBSCRIBE_BASE_URL="http://10.1.2.10:8080/EventChannel/";
	//private static String SUBSCRIBE_BASE_URL="http://127.0.0.1:8080/EventChannel/";
	private static String SUBSCRIBE_KEY="task";

	private static String PUBLISH_BASE_URL="http://10.1.2.10:8080/EventChannel/";
	//private static String PUBLISH_BASE_URL="http://127.0.0.1:8080/EventChannel/";
	private static String PUBLISH_KEY="result";
	private static String PUBLISH_CONTENT_TYPE="Content-Type: application/json";
	private static String PUBLISH_VALUE="";

	private int NUM_SERVIDORES_SMALL=0;
	private int NUM_SERVIDORES_LARGE=0;
	private int NUM_SERVIDORES_HUGE=0;	
	private double CAPACIDADE_MAXIMA_LINK=0;
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
	/*	private int [] NET_PACKET_SIZE;
	private int [] NET_BURST_TIME;
	private int [] NET_IDLE_TIME;
	private int [] NET_RATE;
	 */	
	private double DATACENTER_LINK_BACKBONE=0;

	//To acquire MILP flows to generate NS-2 routes
	private static String [][] MILP_FLOWS;

	private String [] P2_subscribe=null;
	private int rank=0;	

	public A_Worker(){

		System.out.println("Waiting for jobs...");

		//Tempo de espera
		Thread ticks = new Thread();
		String conteudoGET="";

		//Espera ocupada enquanto existirem tarefas a serem processadas
		while(true){			

			conteudoGET = requestJob();

			if (!conteudoGET.equals("")){

				processar(conteudoGET);					

				//Publica o resultado
				publishResultJob();

				//System.exit(0);

			}//fim if

			try{
				ticks.sleep(100);
			} catch (Exception e){
				System.out.println("Excecao no tempo de espera: ");
				e.printStackTrace();
			}//fim catch

		}//fim while

	}//fim construtor

	public String requestJob(){

		String conteudoGET="";

		try{

			String SUBSCRIBE_URL = SUBSCRIBE_BASE_URL + SUBSCRIBE_KEY;

			//Mantem a URL de insercao do conteudo
			URL url = new URL(SUBSCRIBE_URL);
			HttpURLConnection request = (HttpURLConnection)url.openConnection();

			request.setUseCaches(false);
			request.setDoOutput(true);
			request.setDoInput(true);

			request.setFollowRedirects(false);
			request.setInstanceFollowRedirects(false);

			request.setRequestMethod("GET");

			int retcode = request.getResponseCode();
			//System.out.println("Ret Code: " + retcode);

			if(retcode == 200) {
				BufferedReader in = 
					new BufferedReader(new InputStreamReader(request.getInputStream()));
				String inputLine="";
				while ((inputLine = in.readLine()) != null) {
					conteudoGET += inputLine + "\n";
				}//fim while
				System.out.println("Content: " + conteudoGET);
				in.close();
				//Close stream
				request.getInputStream().close();
			} else {
				//System.out.println("Waiting jobs...");
			}
			//Close stream and close socket			
			request.disconnect();

		} catch (Exception e){
			System.out.println("Excecao no worker ao realizar GET:");
			e.printStackTrace();
		}

		return conteudoGET;

	}//fim requestJob

	public void publishResultJob(){

		String PUBLISH_URL = PUBLISH_BASE_URL + PUBLISH_KEY;

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
			PUBLISH_VALUE = "{\"" + rank + "\": [ ";
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
				CAPACIDADE_MAXIMA_LINK + " " +					
				FATOR_CORRECAO + " " +		
				TIPO_APLICACAO_REDE + " " +
				NET_BUFFER + " " +
				//NET_PACKET_SIZE + " " +
				//NET_RATE + " " +
				//NET_IDLE_TIME + " " +
				//NET_BURST_TIME + " " +
				DATACENTER_LINK_BACKBONE + " ";

			int i=0;
			while (i<P2_subscribe.length){
				PUBLISH_VALUE += P2_subscribe[i] + " ";					
				i++;
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
			System.out.println("Ret Code: " + retcode);

			if(retcode == 200) {
				BufferedReader in = 
					new BufferedReader(new InputStreamReader(request.getInputStream()));
				String inputLine="";
				String content="";
				while ((inputLine = in.readLine()) != null) {
					content += inputLine;
				}//fim while
				System.out.println("Content:" + content);
				in.close();					
			}//fim if				

			//Fecha a conexao (gera estouro de buffer)
			//request.disconnect();

		} catch (IOException e) {
			e.printStackTrace();
		}//fim catch

	}//fim publishResultJob

	public void processar(String content){

		//Extrai os dados 
		StringTokenizer t = new StringTokenizer(content, "\"{[]}: ");

		//
		rank = Integer.parseInt(t.nextToken());		
		NUM_DATACENTERS = Integer.parseInt(t.nextToken());
		NUM_SERVIDORES = Integer.parseInt(t.nextToken());
		NUM_VMS = Integer.parseInt(t.nextToken());
		VM_SMALL_CPU = Integer.parseInt(t.nextToken());
		VM_SMALL_BW = Integer.parseInt(t.nextToken());
		SERVER_SMALL_BW = Integer.parseInt(t.nextToken());
		NUM_SERVIDORES_SMALL = Integer.parseInt(t.nextToken());
		NUM_SERVIDORES_LARGE = Integer.parseInt(t.nextToken());
		NUM_SERVIDORES_HUGE = Integer.parseInt(t.nextToken());
		CAPACIDADE_MAXIMA_LINK = Double.parseDouble(t.nextToken());
		FATOR_CORRECAO = Double.parseDouble(t.nextToken());
		TIPO_APLICACAO_REDE = t.nextToken(); 
		NET_BUFFER = Integer.parseInt(t.nextToken());
		/*NET_PACKET_SIZE = Integer.parseInt(t.nextToken());
		NET_RATE = Integer.parseInt(t.nextToken());
		NET_IDLE_TIME = Integer.parseInt(t.nextToken());
		NET_BURST_TIME = Integer.parseInt(t.nextToken());
		 */
		DATACENTER_LINK_BACKBONE = Double.parseDouble(t.nextToken());		

		//Cria o cromossomo: 
		//Ex.: "{\"1\": [ NUM_DATACENTERS NUM_SERVIDORES NUM_VMS ... ... 1 10 4 7 ]}"
		//t.coutTokens conta os tokens restantes (nao considera o 
		//ultimo token do 'content' que eh um caracter especial vindo do GET
		System.out.println("Quantidade Tokens: " + (t.countTokens()-1));
		P2_subscribe = new String[t.countTokens()-1];

		System.out.println("Rank: " + rank);
		System.out.print("P2_subscribe: ");
		String elemento="";
		int i=0;
		while(i<P2_subscribe.length){
			elemento = t.nextToken();
			P2_subscribe[i] = elemento;
			i++;
			System.out.print("["+elemento+"]" + " ");			
		}//fim while

		//-----------------------------------
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

		System.out.println("\n---workerPublishSubscribe---");		

		//Cria uma copia do arquivo base com o indice do rank 
		//(para evitar que 2 ou mais workers acessem o mesmo arquivo)
		inicializarArquivos(rank);

		double fitness=0;		
		int capacidadeLink=0;
		double alelo=0;
		double somatorioCapLinks=0;
		double porcentagemPerdaTotal=0;
		double somatorioAtrasoMedio=0;		

		boolean factivel=true;

		//Atualiza a topologia do datacenter com os dados do cromossomo atual
		atualizarTopologiaSubscribe(rank, P2_subscribe);

		//Gera o modelo lingo com os parametros da populacao do AG

		//Para cada individuo gera o modelo lingo
		//Transforma o conteudo do arquivo.datacenter em Linguagem Lingo
		gerarModeloLingoCompactoSubscribe(rank);

		//Resolver Modelo Lingo
		executarModeloLingoSubscribe(rank);

		//Calcula a quantidade de alocacoes por servidor
		factivel = realizarParserResultLingoSubscribe(rank);

		int j=0;

		if (factivel){

			//gerarModeloNS2Subscribe(rank);
			gerarModeloNS2Subscribe_milpFlows(rank);

			executarModeloNS2Subscribe(rank);

			//VAZAO_TOTAL_VMs=realizarParserVazaoThread(rank);
			//desativa os links e routers sem vazao
			//desativarLinksRoutersThread(rank);


			//Executa novamente para verificar se ao desativar os links
			//nao ocorre perda de pacotes
			//executarModeloNS2Thread(rank);

			porcentagemPerdaTotal=realizarParserPerdaSubscribe(rank);

			somatorioAtrasoMedio=realizarParserAtrasoMedioSubscribe(rank);		

			//Fitness = somatorio das capacidades dos links de rede +
			//			consumo de energia dos routers +
			//			consumo de energia dos servidores +
			//          somatorio da porcentagem da perda de pacotes +
			//          somatorio do atraso medio dos links
			j=0;
			somatorioCapLinks=0;
			capacidadeLink=0;			
			double capacidadeLink_original=0;
			//double ka=2;
			double delta=0.5*CAPACIDADE_MAXIMA_LINK;
			while(j<P2_subscribe.length-1){
				alelo=Integer.parseInt(P2_subscribe[j]);
				
				capacidadeLink_original = (CAPACIDADE_MAXIMA_LINK * alelo) / FATOR_CORRECAO;
				//capacidadeLink = (int) (CAPACIDADE_MAXIMA_LINK+delta*((8-alelo)/7));
				//capacidadeLink = (int) ((ka*((15-alelo)/15)*CAPACIDADE_MAXIMA_LINK)+delta);
				
				somatorioCapLinks += capacidadeLink_original;
				j++;				
			}//fim while

			fitness=
				realizarParserConsumoEnergiaServidoresSubscribe(rank)+
				realizarParserConsumoEnergiaRoteadoresSubscribe(rank) +
				porcentagemPerdaTotal;
			//somatorioAtrasoMedio;
			
			/*System.out.println("\nSomatorioCapLink: " + somatorioCapLinks);
			System.out.println("\nrealizarParserConsumoEnergiaServidores: " + realizarParserConsumoEnergiaServidoresSubscribe(rank));
			System.out.println("\nrealizarParserConsumoEnergiaRouters: " + realizarParserConsumoEnergiaRoteadoresSubscribe(rank));
			System.out.println("\nsomatorioPorcentagemPerda: " + somatorioPorcentagemPerda);
			System.out.println("\nsomatorioAtrasoMedio: " + somatorioAtrasoMedio);
			System.out.println("\nfitness: " + fitness);			
			System.exit(0);
			*/
			
		} else
			//Penaliza o individuo que nao possui solucao factivel
			fitness=888888;

		P2_subscribe[P2_subscribe.length-1]=fitness+"";		

	}//fim processar

	public void parserLingoFluxoSubscribe(int rank){

		//Acquire flows from Lingo result file.lgr 
		//
		//Open file result
		try {
			//File lines
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO+"_"+rank));
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
			while(linha!=null){
				//Ex.: F( 1, 2)    18.0000     0.0000
				REGEX = "F\\( (.*), (.*)\\)\\s(.*)\\s(.*)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linha);
				if (matcher.find()){
					//System.out.println("From: " + matcher.group(1) + " To: " + matcher.group(2) + " Flow: " + removerEspacos(matcher.group(3)));					
					//Write with NS-2 index, i.e., index-1
					from = Integer.parseInt(matcher.group(1))-1;
					to = Integer.parseInt(matcher.group(2))-1;
					flows = Double.parseDouble(matcher.group(3));
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

	}//end parserLingoFluxoSubscribe

	public void gerarModeloNS2Subscribe_milpFlows(int rank){	

		System.out.println("\n---Gerar Modelo NS2 (roteamento MILP): Worker["+rank+"]---");

		//Extract routes from Lingo result file.lgr
		parserLingoFluxoSubscribe(rank);

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
			//Cab+rankecalho
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

				//Mantem a mesma largura de banda nas simulacoes de rede
				//As variacoes de capacidade existem apenas no modelo MILP
				linkBandwidth = Double.parseDouble(EDGES_ARQUIVO[i][campoEdgeBw]);

				//linkBandwidth=(CAPACIDADE_MAXIMA_LINK * Double.parseDouble(EDGES_ARQUIVO[i][campoEdgeCapLink])) / FATOR_CORRECAO;
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
						if(!existeSinkSubscribe(listSink, sink)){

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
			
			double idleTime=100;
			double burstTime=400;
			
			while(p<MILP_FLOWS.length){
				q=0;
				while(q<MILP_FLOWS[p].length){

					//If exist flow
					if(Double.parseDouble(MILP_FLOWS[p][q])>0){

						flow = ((idleTime+burstTime)/burstTime) * Double.parseDouble(MILP_FLOWS[p][q]);
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
									conteudoNS2.append("   \n$exp("+from+") set idle_time_ "+idleTime+"ms");
									conteudoNS2.append("   \n$exp("+from+") set burst_time_ "+burstTime+"ms");
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

	}//fim gerarModeloNS2_milpFlows

	public boolean existeSinkSubscribe(StringBuffer listSink, int sink){

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

	/*	public void gerarModeloNS2Subscribe(int rank){	

		System.out.println("\n---Gerar Modelo NS2: Worker["+rank+"]---");

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
						//Para as demais edges
			//	while (i<numEdges){
			//		info.append(file.readLine()+"\n");
			//		i++;
			//	}//fim while

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

						indiceLigacao = adquirirIndiceLigacaoSubscribe(EDGES_ARQUIVO, i);

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
									conteudoNS2.append("   \n$exp("+indiceLigacao+") set packetSize_ 1500");
									conteudoNS2.append("   \n#Fluxo ficticio para ocupar todo o link");
									//conteudoNS2.append("   \n$exp("+indiceLigacao+") set rate_ " + (fluxoGeradoAglomeradoVMs[i]+30*((IDLE_TIME_+BURST_TIME_)/BURST_TIME_)) + "kb");
									conteudoNS2.append("   \n$exp("+indiceLigacao+") set rate_ " + (fluxoGeradoAglomeradoVMs[i]) + "kb");
									conteudoNS2.append("   \n$exp("+indiceLigacao+") set idle_time_ 100ms");
									conteudoNS2.append("   \n$exp("+indiceLigacao+") set idle_time_ 500ms");
									//conteudoNS2.append("   \n$exp("+indiceLigacao+") set idle_time_ " + NET_IDLE_TIME[i] + "ms");
									//conteudoNS2.append("   \n$exp("+indiceLigacao+") set burst_time_ " + NET_BURST_TIME[i] + "ms");									
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

	}//fim gerarModeloNS2Subscribe
	 */
	public void executarModeloNS2Subscribe(int rank){

		System.out.println("\n---Executar o modelo no NS2: Worker["+rank+"]---");

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
			System.out.println("Fim da execucao do modelo no NS2: Worker["+rank+"]");
		} catch (Exception e){
			System.out.println("Excecao: Erro ao executar o modelo NS2: Worker["+rank+"]");
		}//fim catch

	}//fim executarModeloNS2Subscribe

	public double realizarParserPerdaSubscribe(int rank){

		double porcentagemPerdaTotal=0;
		double somatorioPacotesEnviados=0;
		double somatorioPacotesDescartados=0;

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
							//LISTA_TABU[Integer.parseInt(t[2].toString())][Integer.parseInt(t[3].toString())]++;					

						}//fim if

					} catch(Exception e){
						System.out.println("Aviso 1: Encontrada linha fora do padrao de envio/recepcao de pacotes");						
					}//fim catch
					linhaResult = fileResult.readLine();															

				}//fim while				

				//Resultados dos links				
				i=0;
				j=0;
				double porcentagemPerdaLink=0;
				while(i<packetsSendLink.length){
					j=0;
					while(j<packetsSendLink[i].length){
						if (packetsSendLink[i][j]!=0){
							//System.out.println(i + "--" + j + " " + packetsSendLink[i][j] + " " + packetsDropLink[i][j]);
							porcentagemPerdaLink = ( packetsDropLink[i][j] * 100 ) / packetsSendLink[i][j];
							parserPerda.append("\n" + i + "--" + j + " " + packetsSendLink[i][j] + " " + packetsDropLink[i][j] + " " + porcentagemPerdaLink);							

							somatorioPacotesEnviados += packetsSendLink[i][j];
							somatorioPacotesDescartados += packetsDropLink[i][j];
							
							//Ao desativar links pacotes sao perdidos, mas
							//quero saber se ocorre perda nos links que permaneceram ativos
							//somatorioPorcentagemPerda+=porcentagemPerdaLink;

						}//fim if
						j++;
					}//fim while 
					i++;
				}//fim while

				porcentagemPerdaTotal = ( somatorioPacotesDescartados * 100 ) / somatorioPacotesEnviados;
				
				//Grava o resultado em arquivo
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_PERDA_NS2+"_"+rank,false));			
					out.write(parserPerda.toString());
					out.close();
				} catch(Exception e){
					System.out.println("11Excecao ao gravar no arquivo." + e.getMessage());
				}//fim catch

				//Grava o resultado em arquivo
				/*try {
					BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_EVOLUCAO_PERDA,true));			
					out.write(somatorioPorcentagemPerda+"\n");
					out.close();
				} catch(Exception e){
					System.out.println("11Excecao ao gravar no arquivo: " + ARQUIVO_EVOLUCAO_PERDA + ": " + e.getMessage());
				}//fim catch
				*/

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

		return porcentagemPerdaTotal;

	}//fim realizarParserPerdaSubscribe

	public void gerarModeloLingoCompactoSubscribe(int rank){

		System.out.println("\n---Gerar Modelo Lingo Compacto: Worker["+rank+"]---\n");

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


			//---Inicio do Modelo Lingo---
			i=0;
			j=0;

			//Blocos do arquivo do modelo Lingo
			StringBuffer [] passo = new StringBuffer[18];
			int indice=0;

			//Passo0: sets			
			passo[indice] = new StringBuffer();
			passo[indice].append("\nMODEL:");
			passo[indice].append("\n\nSETS:");
			passo[indice].append("\n\n!VMS;");
			//			if (INSIDE_AG)
			//Inside AG runs optimization for all VMs
			//				passo[indice].append("\nVMS /1.." + TOTAL_AMOUNT_ALLOCS + "/: VMCPU, VMBW;");				
			//			else				
			//if (!ULTIMA_ITERACAO)
			//Ex.: 10VMs...10VMs...
			passo[indice].append("\nVMS /1.." + numVMs + "/: VMCPU, VMBW;");
			/*else
					//Last iteration
					passo[indice].append("\nVMS /1.." + TOTAL_AMOUNT_ALLOCS + "/: VMCPU, VMBW;");
			 */

			passo[indice].append("\n\n!SERVERS: CPU, BANDWIDTH, ENERGY, X (ON/OFF);");
			passo[indice].append("\nSERVERS /1.." + numServers + "/: SCPU, SBW, SE, X, COST, AMOUNT_ALLOCS;");
			passo[indice].append("\n\n!ALLOCATION;");
			passo[indice].append("\nALLOCS(VMS, SERVERS): A;");
			passo[indice].append("\n\n!ROUTERS: FLOW IN, ENERGY Y (ON/OFF);");
			passo[indice].append("\nROUTERS /1.." + (numNodesOriginal+numDatacenters) + "/: RE, FIN, Y, ROUTERS_CONSUME;");
			passo[indice].append("\n\n!LINKS;");
			passo[indice].append("\nLINKS(ROUTERS, ROUTERS): L, F, C;");
			passo[indice].append("\n\nENDSETS");
			indice++; 
			//System.out.println(passo[0].toString());

			//System.out.println(passo[1].toString());

			//Gera a matriz de links APENAS para os routers internos			
			passo[indice] = new StringBuffer();
			//Global 

				i=0;
				j=0;
				//double limiar=8;
				double alelo=0;
				int capacidadeLink=0;
				double capacidadeLink_original=0;
				int from=0;
				int to=0;

				double delta=0.5*CAPACIDADE_MAXIMA_LINK;
				//double ka=2;

				while(i<EDGES_ARQUIVO.length){

					from = Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeFrom])+1;
					to = Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeTo])+1;

					if(Integer.parseInt(EDGES_ARQUIVO[i][campoEdgeFrom])>=numNodesOriginal){

						passo[indice].append("L("+ from + "," + to + ") = " + DATACENTER_LINK_BACKBONE + ";\n");

					} else {

						alelo = Double.parseDouble(EDGES_ARQUIVO[i][campoEdgeCapLink]);

						//capacidadeLink = (int) (CAPACIDADE_MAXIMA_LINK+delta*((8-alelo)/7));						
						capacidadeLink_original = (CAPACIDADE_MAXIMA_LINK * alelo) / FATOR_CORRECAO;

						passo[indice].append("L("+ from + "," + to + ") = " + capacidadeLink_original + ";\n");

					}//end else

					/*//For on demand allocations, reduce from previous amount flows in links
					capacidadeLink -= AMOUNT_FLOWS[from][to];
					if (capacidadeLink < 0)
						capacidadeLink = 1;
					 */

					i++;
				}//fim while

			indice++; 

			passo[indice] = new StringBuffer();			
			//Nao pode ser apenas COST(J)*X(J) porque senao a funcao objetivo eh sempre a mesma
			passo[indice].append("\n[OBJ] MIN = @SUM(SERVERS(J): COST(J)*X(J)) + @SUM(ROUTERS(J):FIN(J)*RE(J));");			
			//////////////////////////////
			//Insert variables to on demand 
			i=0;
			int limiteInferior=0;
			int limiteSuperior=0;
			while(i<numDatacenters){
				limiteInferior = limiteSuperior;
				limiteSuperior += NUM_SERVIDORES_SMALL;
				passo[indice].append("\n\n!COST SMALL SERVERS ON DATACENTER "+i+";");
				passo[indice].append("\n@FOR(SERVERS(J) | J #GT# " + limiteInferior + " #AND# J #LE# " + limiteSuperior + ":");
				passo[indice].append("\nCOST(J)=1+0.01*J);");

				limiteInferior = limiteSuperior;
				limiteSuperior += NUM_SERVIDORES_LARGE;
				passo[indice].append("\n\n!COST LARGE SERVERS ON DATACENTER "+i+";");
				passo[indice].append("\n@FOR(SERVERS(J) | J #GT# " + limiteInferior + " #AND# J #LE# " + limiteSuperior + ":");
				passo[indice].append("\nCOST(J)=3+0.01*J);");

				limiteInferior = limiteSuperior;
				limiteSuperior += NUM_SERVIDORES_HUGE;
				passo[indice].append("\n\n!COST HUGE SERVERS ON DATACENTER "+i+";");
				passo[indice].append("\n@FOR(SERVERS(J) | J #GT# " + limiteInferior + " #AND# J #LE# " + limiteSuperior + ":");
				passo[indice].append("\nCOST(J)=9+0.01*J);");

				//Next datacenter
				i++;
			}//fim while
			indice++; 

			//////////////////////////////

			passo[indice] = new StringBuffer();
			//passo[indice].append("\n\nENERGY_SERVERS = @SUM(ALLOCS(I,J): A(I,J) * SE(J)/SCPU(J));");
			//passo[indice].append("\n\nENERGY_ROUTERS = @SUM(ROUTERS(J): FIN(J) * RE(J));");
			indice++; 

			passo[indice] = new StringBuffer();
			passo[indice].append("\n!AMOUNT OF VMS ALLOCATED IN SERVERS;");
			passo[indice].append("\n@FOR(SERVERS(J):");
			passo[indice].append("\nAMOUNT_ALLOCS(J) = @SUM(ALLOCS(I,J): A(I,J)));");
			indice++; 
			//System.out.println(passo[3].toString());

			/*passo[indice] = new StringBuffer();
			passo[indice].append("\n!ENERGY CONSUMPTION FOR ROUTERS;");
			passo[indice].append("\n@FOR(ROUTERS(K):");
			passo[indice].append("\nROUTERS_CONSUME(K) = RE(K)*Y(K));");
			indice++;
			 */			

			limiteInferior = 0;
			limiteSuperior = 0;
			passo[indice] = new StringBuffer();
			//passo[indice].append("\n\n!SET NUMBER OF CPUS FOR SERVERS;");
			//Note: On demand needs to informe individual capacities for servers -> + slow
			//      MILP_GROUP==1 (Global): use 'for' to inform capacities for servers -> + fast

			/*if (!MILP_SOB_DEMANDA){

				i=0;
				while(i<numDatacenters){
					limiteInferior = limiteSuperior;
					limiteSuperior += NUM_SERVIDORES_SMALL;
					passo[indice].append("\n@FOR(SERVERS(J)| J #GT# " + limiteInferior + " #AND# J #LE# " + limiteSuperior + ":");
					passo[indice].append("\nSCPU(J) = "+ SERVER_SMALL_CPU +");");

					limiteInferior = limiteSuperior;
					limiteSuperior += NUM_SERVIDORES_LARGE;
					passo[indice].append("\n@FOR(SERVERS(J)| J #GT# " + limiteInferior + " #AND# J #LE# " + limiteSuperior + ":");
					passo[indice].append("\nSCPU(J) = " + SERVER_LARGE_CPU +");");

					limiteInferior = limiteSuperior;
					limiteSuperior += NUM_SERVIDORES_HUGE;
					passo[indice].append("\n@FOR(SERVERS(J)| J #GT# " + limiteInferior + " #AND# J #LE# " + limiteSuperior + ":");
					passo[indice].append("\nSCPU(J) = " + SERVER_HUGE_CPU +");");

					i++;
				}//fim while	
				indice++; 

			} else {				
				//Insert variables to on demand 
				i=0;
				//Index in Lingo begin at 1
				j=1;
				while(i<numDatacenters){
					limiteInferior = limiteSuperior;
					limiteSuperior += NUM_SERVIDORES_SMALL;
					passo[indice].append("\n\n!SERVERS ON DATACENTER "+i+";");
					while(j<=limiteSuperior){
						//Update from previous allocation
						//+1 because Lingo do not run with SCPU(i,j)=0 
						passo[indice].append("\nSCPU("+j+") = " + (SERVER_SMALL_CPU - (AMOUNT_ALLOCS[j]*VM_SMALL_CPU)+1) + ";");
						j++;
					}//end while

					limiteInferior = limiteSuperior;
					limiteSuperior += NUM_SERVIDORES_LARGE;
					while(j<=limiteSuperior){
						//+1 because Lingo do not run with 0 value
						passo[indice].append("\nSCPU("+j+") = " + (SERVER_LARGE_CPU - (AMOUNT_ALLOCS[j]*VM_SMALL_CPU)+1) + ";");
						j++;
					}//end while

					limiteInferior = limiteSuperior;
					limiteSuperior += NUM_SERVIDORES_HUGE;
					while(j<=limiteSuperior){
						//+1 because Lingo do not run with 0 value
						passo[indice].append("\nSCPU("+j+") = " + (SERVER_HUGE_CPU - (AMOUNT_ALLOCS[j]*VM_SMALL_CPU)+1) + ";");	
						j++;
					}//end while	

					//Next datacenter
					i++;
				}//fim while
				indice++; 
			}//end else
			//System.out.println(passo[4].toString());
			 */
			//Use SCPU as a matrix in DATA section
			indice++;

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
			indice++; 
			//System.out.println(passo[5].toString());

			double [] fluxoResidualDatacenter = new double [NUM_DATACENTERS];
			//Initialize
			i=0;
			while(i<fluxoResidualDatacenter.length){
				fluxoResidualDatacenter[i]=0;
				i++;
			}//end while			

			//if (!MILP_SOB_DEMANDA){
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
					passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I))) = " + getLinkFonteTrafegoDatacenter_PublishSubscribe(EDGES_ARQUIVO,i) + ";");				
					i++;
				}//fim while
				indice++; 
				//System.out.println(passo[6].toString());
			/*} else {
				//To MILP_SOB_DEMANDA 
				passo[indice] = new StringBuffer();
				passo[indice].append("\n!FLOW RESTRICTIONS;");
				passo[indice].append("\n!FLOWS PRODUCED BY DATACENTERS;");

				//To run with residual flow 
				//Initialize 
				//Iterate datacenter index
				int p=0;
				//Iterate server index inside datacenter (q=1 because Lingo begins at index 1)
				int q=1;
				//Iterate server index inside datacenter (continuously) (r=1 because Lingo begins at index 1)
				int r=1;
				while(p<NUM_DATACENTERS){
					//fluxoResidualDatacenter[p]=0;
					q=1;
					while(q<=NUM_SERVIDORES){
						//AMOUNT_ALLOCS increases at each iteration
						//AMOUNT_ALLOCS keeps all amount of allocations in servers
						if(!ULTIMA_ITERACAO)
							fluxoResidualDatacenter[p] += AMOUNT_ALLOCS[r];
						else
							fluxoResidualDatacenter[p] += AMOUNT_ALLOCS_ANTES[r];
						//Iterate server index inside datacenter
						q++;
						//Iterate server index inside datacenter (continuously)
						r++;
					}//end while
					//Iterate datacenter index
					fluxoResidualDatacenter[p] *= VM_SMALL_FLUXO;
					p++;
				}//end while
				i=0;
				limiteInferior=0;
				limiteSuperior=0;
				while(i<numDatacenters){

					limiteInferior = limiteSuperior;
					limiteSuperior += numServers/numDatacenters;
					passo[indice].append("\n@SUM(SERVERS(J)| J #GT# " + limiteInferior + " #AND# J #LE# " + limiteSuperior + ":");
					passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I))) + " +  
							fluxoResidualDatacenter[i] + " = " + 
							getLinkFonteTrafegoDatacenter(EDGES_ARQUIVO,i) + ";");				
					i++;
				}//fim while
				indice++; 
			}//end else				 
*/
			/*	limiteInferior=0;
				limiteSuperior=0;
				while(i<numDatacenters){

					limiteInferior = limiteSuperior;
					limiteSuperior += numServers/numDatacenters;
					passo[indice].append("\n@SUM(SERVERS(J)| J #GT# " + limiteInferior + " #AND# J #LE# " + limiteSuperior + ":");
					passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I))) = " +  
							getLinkFonteTrafegoDatacenter(EDGES_ARQUIVO,i) + ";");				
					i++;
				}//fim while
				indice++; 
			}//end else			
			 */
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
			indice++; 
			//System.out.println(passo[7].toString());

			passo[indice] = new StringBuffer();
/*			String todosFluxosResiduais="";
			i=0;
			while(i<fluxoResidualDatacenter.length){
				todosFluxosResiduais += " + " + fluxoResidualDatacenter[i];
				i++;
			}//end while				
			//To run with residual flows 
			passo[indice].append("\n\n!CONSERVATION ON SINK;");
			passo[indice].append("\n@SUM(SERVERS(J):");
			passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I))) " +
					todosFluxosResiduais + 
					" = " + adquirirFluxosEntrada(EDGES_ARQUIVO, (NODO_DESTINO[0]-1)) + ";");
			//Multiple destinations
			//passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I))) = " + adquirirTodosFluxosDestino(EDGES_ARQUIVO) + ";");
			 */
			indice++; 
			//System.out.println(passo[8].toString());

			/*passo[indice] = new StringBuffer();
			passo[indice].append("\n\n!CONSERVATION ON SINK;");
			passo[indice].append("\n@SUM(SERVERS(J):");
			passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I))) " + 
					" = " + adquirirFluxosEntrada(EDGES_ARQUIVO, (NODO_DESTINO[0]-1)) + ";");
			//Multiple destinations
			//passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I))) = " + adquirirTodosFluxosDestino(EDGES_ARQUIVO) + ";");
			indice++; //11
			 */

			//Usa a tabela de links
			passo[indice] = new StringBuffer();
			passo[indice].append("\n\n!LINK CAPACITY;");			
			passo[indice].append("\n@FOR(LINKS(I,J):"); 
			passo[indice].append("\nF(I,J) <= L(I,J));");
			indice++; 
			//System.out.println(passo[9].toString());

			passo[indice] = new StringBuffer();
			passo[indice].append("\n\n!FLOW IN FOR ROUTERS;");
			i=0;
			while(i<numNodesOriginal){
				passo[indice].append("\n!"+(i+1)+";");
				passo[indice].append("\nFIN("+(i+1)+") = " + adquirirFluxosEntrada_PublishSubscribe(EDGES_ARQUIVO,i) + ";");
				i++;
			}//fim while
			indice++; 
			//System.out.println(passo[10].toString());

			passo[indice] = new StringBuffer();
			passo[indice].append("\n!X: SLACK;");
			passo[indice].append("\n@FOR(SERVERS(J):");
			passo[indice].append("\n@SUM(VMS(I): A(I,J)*VMBW(I)) + X(J) = SCPU(J));");
			indice++; 
			//System.out.println(passo[11].toString());

			passo[indice] = new StringBuffer();
			//passo[indice].append("\n!ON/OFF ROUTERS;");
			//passo[indice].append("\n@FOR(ROUTERS(J):");
			//passo[indice].append("\nFIN(J) - " + CAPACIDADE_MAXIMA_LINK + "*Y(J) <= 0);");
			indice++; 

			//Relaxamento para nao usar variaveis binarias			
			passo[indice] = new StringBuffer();
			//			if(MILP_SOB_DEMANDA){
			passo[indice].append("\n\n!BINARY VARIABLES;");
			passo[indice].append("\n@FOR(SERVERS(J):");
			passo[indice].append("\n@FOR(VMS(I): ");
			//passo[indice].append("\n@BIN(A(I,J))));");
			passo[indice].append("\nA(I,J) <= 1;"); //Nao dah certo usar binario na alocacao global, exceto na ultima iteracao
			passo[indice].append("\nA(I,J) >= 0));");
			/*			} else 
				//Global allocation
				if(!ULTIMA_ITERACAO){
					passo[indice].append("\n@FOR(SERVERS(J):");
					passo[indice].append("\n@FOR(VMS(I): ");
					passo[indice].append("\nA(I,J) <= 1;");
					passo[indice].append("\nA(I,J) >= 0));");
					//System.out.println("-----Passei por aqui: SEM BIN nao ultimo-----");
				} else 
					//Last iteration with binary variables
					if(LAST_BIN){									
						passo[indice].append("\n\n!BINARY VARIABLES;");
						passo[indice].append("\n@FOR(SERVERS(J):");
						passo[indice].append("\n@FOR(VMS(I): ");
						passo[indice].append("\n@BIN(A(I,J))));");
						//System.out.println("-----Passei por aqui: BIN-----");
					} else {
						//Last iteration without binary variables
						passo[indice].append("\n@FOR(SERVERS(J):");
						passo[indice].append("\n@FOR(VMS(I): ");
						passo[indice].append("\nA(I,J) <= 1;");
						passo[indice].append("\nA(I,J) >= 0));");
						//System.out.println("-----Passei por aqui: SEM BIN-----");
					}//end else	
			 */
			/*if(MILP_SOB_DEMANDA){
					passo[indice].append("\n\n!PREVIOUS ALLOCATIONS;");
					i=0;
					j=0;
					while(i<A.length){
						j=0;
						while(j<A[i].length){
							if (A[i][j]!=0)
								passo[indice].append("\nA("+i+","+j+")="+A[i][j]+";");
							j++;
						}//end while
						i++;
					}//end while				
				}//end if
			 */
			indice++;

			//DATA Section
			passo[indice] = new StringBuffer();
			passo[indice].append("\nDATA:");
			passo[indice].append("\nVMCPU, VMBW = " + VM_SMALL_CPU + ", " + VM_SMALL_BW + ";");
			passo[indice].append("\nSE = 90;");

			//Update SBW 
			//To on demand LP and global allocation				

			limiteInferior = 0;
			limiteSuperior = 0;

			//Insert variables to on demand 
			i=0;
			//Index in Lingo begin at 1
			j=1;
			//Only to improve legibility
			int k=0;
			int matrixSize=10;
			passo[indice].append("\n\n!BANDWIDTH OF SERVERS;");
			passo[indice].append("\nSBW = ");
			while(i<numDatacenters){
				passo[indice].append("\n\n");
				k=0;
				limiteInferior = limiteSuperior;
				limiteSuperior += NUM_SERVIDORES_SMALL;					
				while(j<=limiteSuperior){
/*					if(MILP_SOB_DEMANDA){
						//Update from previous allocation
						if (!ULTIMA_ITERACAO)
							passo[indice].append((SERVER_SMALL_BW - (AMOUNT_ALLOCS[j]*VM_SMALL_BW)) + " ");
						else
							passo[indice].append((SERVER_SMALL_BW - (AMOUNT_ALLOCS_ANTES[j]*VM_SMALL_BW)) + " ");
					} else
*/					
						//Global allocation
						passo[indice].append(SERVER_SMALL_BW + " ");

					//Because last iteration can or not consume all resources							
					//passo[indice].append(SERVER_SMALL_BW + " ");
					j++;
					if(k>matrixSize){
						passo[indice].append("\n");
						k=0;
					}//end if
					k++;
				}//end while

				passo[indice].append("\n");
				k=0;
				limiteInferior = limiteSuperior;
				limiteSuperior += NUM_SERVIDORES_LARGE;
				while(j<=limiteSuperior){
/*					if(MILP_SOB_DEMANDA){	
						if (!ULTIMA_ITERACAO)
							passo[indice].append((SERVER_LARGE_BW - (AMOUNT_ALLOCS[j]*VM_LARGE_BW)) + " ");
						else
							passo[indice].append((SERVER_LARGE_BW - (AMOUNT_ALLOCS_ANTES[j]*VM_LARGE_BW)) + " ");
					} else
*/					
						//Global allocation
						passo[indice].append(SERVER_SMALL_BW + " ");
					//Because last iteration can or not consume all resources
					//passo[indice].append(SERVER_LARGE_BW + " ");
					j++;
					if(k>matrixSize){
						passo[indice].append("\n");
						k=0;
					}//end if
					k++;
				}//end while

				passo[indice].append("\n");
				k=0;
				limiteInferior = limiteSuperior;
				limiteSuperior += NUM_SERVIDORES_HUGE;
				while(j<=limiteSuperior){
/*					if (MILP_SOB_DEMANDA){
						if (!ULTIMA_ITERACAO)
							passo[indice].append((SERVER_HUGE_BW - (AMOUNT_ALLOCS[j]*VM_HUGE_BW)) + " ");
						else
							passo[indice].append((SERVER_HUGE_BW - (AMOUNT_ALLOCS_ANTES[j]*VM_HUGE_BW)) + " ");
					} else
*/					
						//Global allocation
						passo[indice].append(SERVER_SMALL_BW + " ");
					//Because last iteration can or not consume all resources
					//passo[indice].append(SERVER_HUGE_BW + " ");
					j++;
					if(k>matrixSize){
						passo[indice].append("\n");
						k=0;
					}//end if
					k++;
				}//end while	

				//Next datacenter
				i++;
			}//fim while

			passo[indice].append(";\n");


			passo[indice].append("\nC = 1;");


			//Update CPU core
			//To onde demand allocation and global allocation	

			limiteInferior = 0;
			limiteSuperior = 0;

			//Insert variables to on demand 
			i=0;
			//Index in Lingo begin at 1
			j=1;
			//Only to improve legibility
			k=0;
			matrixSize=10;
			passo[indice].append("\n\n!CPU CORES OF SERVERS ;");
			passo[indice].append("\nSCPU = ");
			while(i<numDatacenters){
				passo[indice].append("\n\n");
				k=0;
				limiteInferior = limiteSuperior;
				limiteSuperior += NUM_SERVIDORES_SMALL;					
				while(j<=limiteSuperior){
					//Update from previous allocation
/*					if(MILP_SOB_DEMANDA){
						if(!ULTIMA_ITERACAO)
							passo[indice].append((SERVER_SMALL_CPU - (AMOUNT_ALLOCS[j]*VM_SMALL_CPU)) + " ");
						else
							passo[indice].append((SERVER_SMALL_CPU - (AMOUNT_ALLOCS_ANTES[j]*VM_SMALL_CPU)) + " ");
					} else
*/					
						passo[indice].append("4" + " ");
					//Because last iteration can or not consume all resources							
					//passo[indice].append(SERVER_SMALL_CPU + " ");
					j++;
					if(k>matrixSize){
						passo[indice].append("\n");
						k=0;
					}//end if
					k++;
				}//end while

				passo[indice].append("\n");
				k=0;
				limiteInferior = limiteSuperior;
				limiteSuperior += NUM_SERVIDORES_LARGE;
				while(j<=limiteSuperior){
/*					if(MILP_SOB_DEMANDA){
						if(!ULTIMA_ITERACAO)
							passo[indice].append((SERVER_LARGE_CPU - (AMOUNT_ALLOCS[j]*VM_LARGE_CPU)) + " ");
						else
							passo[indice].append((SERVER_LARGE_CPU - (AMOUNT_ALLOCS_ANTES[j]*VM_LARGE_CPU)) + " ");
					} else 
*/						
						passo[indice].append("8" + " ");
					//Because last iteration can or not consume all resources							
					//passo[indice].append(SERVER_LARGE_CPU + " ");
					j++;
					if(k>matrixSize){
						passo[indice].append("\n");
						k=0;
					}//end if
					k++;
				}//end while

				passo[indice].append("\n");
				k=0;
				limiteInferior = limiteSuperior;
				limiteSuperior += NUM_SERVIDORES_HUGE;
				while(j<=limiteSuperior){
/*					if(MILP_SOB_DEMANDA){
						if(!ULTIMA_ITERACAO)
							passo[indice].append((SERVER_HUGE_CPU - (AMOUNT_ALLOCS[j]*VM_HUGE_CPU)) + " ");
						else
							passo[indice].append((SERVER_HUGE_CPU - (AMOUNT_ALLOCS_ANTES[j]*VM_HUGE_CPU)) + " ");
					} else
*/					 
						passo[indice].append("32" + " ");
					//Because last iteration can or not consume all resources							
					//passo[indice].append(SERVER_HUGE_CPU + " ");
					j++;
					if(k>matrixSize){
						passo[indice].append("\n");
						k=0;
					}//end if
					k++;
				}//end while	

				//Next datacenter
				i++;
			}//fim while

			passo[indice].append(";\n");

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
			while(i<EDGES_ARQUIVO.length){
				j=0;

					//Original
					//passo[indice].append(recuperarValorLink(EDGES_ARQUIVO,i,j) + "\t");
					//
					//
					//Novo
					//( campoCromossomo * capacidade do link ) / fatorCorrecao;					
					capacidadeLink = (recuperarValorCusto(EDGES_ARQUIVO, i, j) * recuperarValorLink(EDGES_ARQUIVO,i,j)) / 10;
					passo[indice].append(capacidadeLink + "\t");
					j++;

				passo[indice].append("\n");
				i++;
			}//fim while
			passo[indice].append(";");
			indice++;
			 */
			//Uma iteracao ou Sob demanda para execucao inicial
/*			if (UMA_ITERACAO || EXECUCAO_INICIAL || ULTIMA_ITERACAO){
				passo[indice].append("\nL="+CAPACIDADE_MAXIMA_LINK+";");
			}//end if
*/
			passo[indice].append("\nRE = 0.06;");
			passo[indice].append("\nENDDATA\n");
			indice++;

			//DATA Section for result file
			String arquivoResult = ARQUIVO_RESULT_LINGO +"_"+rank;
			passo[indice] = new StringBuffer();
			passo[indice].append("\nDATA:");
			//passo[indice].append("\n\n!Status;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITE( 'Status:',@STATUS(), ");
			passo[indice].append("\n@NEWLINE(2));");

			//passo[indice].append("\n\n!Indicates if global solution was found;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITE("); 
			passo[indice].append("\n@IF( @STATUS() #EQ# 0, 'Global optimal', '**** Non-global ***'),");
			passo[indice].append("\n' solution found at iteration:', @FORMAT( @ITERS(), '14.14G'),");
			passo[indice].append("\n@NEWLINE(2));");

			//passo[indice].append("\n\n!Objective value;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITE('Objective value:', @FORMAT( OBJ, '#41.7G'),");
			passo[indice].append("\n@NEWLINE( 2));");

			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITE('Variable           Value        Reduced Cost',");
			passo[indice].append("\n@NEWLINE( 1));");

			passo[indice].append("\n\n!VMCPU;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITEFOR( VMS(I):"); 
			passo[indice].append("\n@NAME(VMCPU(I)),"); 
			passo[indice].append("\n@FORMAT( VMCPU(I), '#16.7G'),");
			passo[indice].append("\n@FORMAT( @DUAL( VMCPU(I)), '#20.6G'), @NEWLINE(1));");

			passo[indice].append("\n\n!VMBW;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITEFOR( VMS(I):"); 
			passo[indice].append("\n@NAME(VMBW(I)),"); 
			passo[indice].append("\n@FORMAT( VMBW(I), '#16.7G'),");
			passo[indice].append("\n@FORMAT( @DUAL( VMBW(I)), '#20.6G'), @NEWLINE(1));");

			passo[indice].append("\n\n!SCPU;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITEFOR( SERVERS(I):"); 
			passo[indice].append("\n@NAME(SCPU(I)),"); 
			passo[indice].append("\n@FORMAT( SCPU(I), '#16.7G'),");
			passo[indice].append("\n@FORMAT( @DUAL( SCPU(I)), '#20.6G'), @NEWLINE(1));");

			passo[indice].append("\n\n!SBW;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITEFOR( SERVERS(I):"); 
			passo[indice].append("\n@NAME(SBW(I)),"); 
			passo[indice].append("\n@FORMAT( SBW(I), '#16.7G'),");
			passo[indice].append("\n@FORMAT( @DUAL( SBW(I)), '#20.6G'), @NEWLINE(1));");

			passo[indice].append("\n\n!SE;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITEFOR( SERVERS(I):"); 
			passo[indice].append("\n@NAME(SE(I)),"); 
			passo[indice].append("\n@FORMAT( SE(I), '#16.7G'),");
			passo[indice].append("\n@FORMAT( @DUAL( SE(I)), '#20.6G'), @NEWLINE(1));");

			passo[indice].append("\n\n!X;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITEFOR( SERVERS(I):"); 
			passo[indice].append("\n@NAME(X(I)),"); 
			passo[indice].append("\n@FORMAT( X(I), '#16.7G'),");
			passo[indice].append("\n@FORMAT( @DUAL( X(I)), '#20.6G'), @NEWLINE(1));");

			passo[indice].append("\n\n!COST;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITEFOR( SERVERS(I):"); 
			passo[indice].append("\n@NAME(COST(I)),"); 
			passo[indice].append("\n@FORMAT( COST(I), '#16.7G'),");
			passo[indice].append("\n@FORMAT( @DUAL( COST(I)), '#20.6G'), @NEWLINE(1));");

			passo[indice].append("\n\n!AMOUNT_ALLOCS;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITEFOR( SERVERS(I):"); 
			passo[indice].append("\n@NAME(AMOUNT_ALLOCS(I)),"); 
			passo[indice].append("\n@FORMAT( AMOUNT_ALLOCS(I), '#16.7G'),");
			passo[indice].append("\n@FORMAT( @DUAL( AMOUNT_ALLOCS(I)), '#20.6G'), @NEWLINE(1));");

			passo[indice].append("\n\n!ALLOCS(VMS, SERVERS): A;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITEFOR( ALLOCS(I,J):"); 
			passo[indice].append("\n@NAME(A(I,J)),"); 
			passo[indice].append("\n@FORMAT( A(I,J), '#16.7G'),");
			passo[indice].append("\n@FORMAT( @DUAL( A(I,J)), '#20.6G'), @NEWLINE(1));");

			passo[indice].append("\n\n!ROUTERS");
			passo[indice].append("\n!RE;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITEFOR( ROUTERS(I):"); 
			passo[indice].append("\n@NAME(RE(I)),"); 
			passo[indice].append("\n@FORMAT( RE(I), '#16.7G'),");
			passo[indice].append("\n@FORMAT( @DUAL( RE(I)), '#20.6G'), @NEWLINE(1));");

			passo[indice].append("\n\n!FIN;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITEFOR( ROUTERS(I):"); 
			passo[indice].append("\n@NAME(FIN(I)),"); 
			passo[indice].append("\n@FORMAT( FIN(I), '#16.7G'),");
			passo[indice].append("\n@FORMAT( @DUAL( FIN(I)), '#20.6G'), @NEWLINE(1));");

			passo[indice].append("\n\n!LINKS(ROUTERS, ROUTERS): L, F;");
			passo[indice].append("\n!L;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITEFOR( LINKS(I, J):");
			passo[indice].append("\n@NAME( L(I,J)),");
			passo[indice].append("\n@FORMAT( L(I,J), '#16.7G'),");
			passo[indice].append("\n@FORMAT( @DUAL( L(I,J)), '#20.6G'), @NEWLINE(1));");

			passo[indice].append("\n\n!F;");
			passo[indice].append("\n\n@TEXT('"+arquivoResult+"') ="); 
			passo[indice].append("\n@WRITEFOR( LINKS(I, J):");
			passo[indice].append("\n@NAME( F(I,J)),");
			passo[indice].append("\n@FORMAT( F(I,J), '#16.7G'),");
			passo[indice].append("\n@FORMAT( @DUAL( F(I,J)), '#20.6G'), @NEWLINE(1));");

			passo[indice].append("\n\nENDDATA");
			indice++;


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
					"\n!DIVERT " + ARQUIVO_RESULT_LINGO+"_"+rank + 
					"\n! Send solution to the file;" + 
					"\n!SOLUTION" + 
					"\n! Close solution file;" + 
					"\n!RVRT" + 
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

			//---Fim do Modelo Lingo---
			
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

	}//fim gerarModeloLingoCompactoSubscribe 

	public void atualizarTopologiaSubscribe(int rank, String [] P2_worker){

		//Recupera as informacoes do arquivo da topologia:
		//NUM_NODES,NUM_EDGES,NUM_DATACENTERS,NUM_SERVIDORES,NUM_VMS,NODO_DESTINO
		//Todas as informacoes das arestas: EDGES_ARQUIVO
		//Todas as informacoes dos servidores: S_ARQUIVO
		//Todas as informacoes das VMs: VM_ARQUIVO

		System.out.println("\n---Atualizar Topologia: Worker["+rank+"]---");		

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
			//LISTA_TABU = new int[NUM_NODES+NUM_EDGES][NUM_NODES+NUM_EDGES];
			//renovarListaTabuThread();

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
				//Aqui estah certo, pq P2_worker estah no mesmo indice de P2[indiceCromossomo][i]
				EDGES_ARQUIVO[i][j++] = P2_worker[i];			

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

	}//fim atualizarTopologiaSubscribe

	public int bin2decSubscribe( String bin ) {  
		int i, result = 0;  

		for( i = 0; i < bin.length(); i++ ) {  
			result <<= 1;  
			if( bin.charAt( i ) == '1' ) result++;  
		}//fim for

		return result;  
	}//fim bin2decSubscribe

	public double recuperarValorCustoSubscribe(String [][] EDGES_ARQUIVO, int i, int j){

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

	}//fim recuperarValorCustoSubscribe

	public boolean realizarParserResultLingoSubscribe(int rank){

		boolean factivel=false;

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
			while (linhaResult!=null && !factivel){

				REGEX = "Status:(.*)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linhaResult);
				if (matcher.find()){
					if(matcher.group(1).equals("0"))
						factivel=true;
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

		}//end if

		return factivel;

	}//fim realizarParserResultLingoSubscribe

	public void executarModeloLingoSubscribe(int rank){

		System.out.println("---Executar modelo no Lingo: Worker["+rank+"]---");

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
			System.out.println("Fim da execucao do modelo Lingo: Worker ["+rank+"]");
		} catch (Exception e){
			System.out.println("Excecao: Erro ao executar o programa Lingo: Worker ["+rank+"]");
		}//fim catch

	}//fim executarModeloLingoSubscribe

	public String adquirirFonteTrafegoDatacenterSubscribe(String [][] EDGES_ARQUIVO, int i){

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

	}//adquirirFonteTrafegoDatacenterSubscribe

	public String adquirirFluxosEntradaSubscribe(String [][] EDGES_ARQUIVO, int nodoDestino){

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

	}//fim adquirirFluxosEntradaSubscribe

	public int adquirirIndiceLigacaoSubscribe(String [][] EDGES_ARQUIVO, int indiceDatacenter){

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

	}//fim adquirirIndiceLigacaoSubscribe

	public double realizarParserAtrasoMedioSubscribe(int rank){

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

	}//fim realizarParserAtrasoMedioSubscribe

	public double realizarParserConsumoEnergiaServidoresSubscribe(int rank){

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

			String servidor="";
			String servidorLigado="";

			int [] SERVERS = new int[NUM_SERVIDORES*NUM_DATACENTERS+1];
			int p=0;
			while(p<SERVERS.length){
				SERVERS[p]=0;
				p++;
			}//end while
			
			////////////////
			boolean achou=false;
			int i=0;
			int j=0;
			int limiteInferior=0;
			int limiteSuperior=0;		

			while (linhaResult!=null){

				//Possiveis resultados				
				//Ex.:  AMOUNT_ALLOCS( 10) 		1.000000            3.955556"   ou
				//Ex.:  AMOUNT_ALLOCS( 10) 		1.000000"  
				REGEX = "AMOUNT\\_ALLOCS\\(\\s(.*)\\)\\s(.*)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linhaResult);
				//Ideia: achou uma alocacao, verifica em qual datacenter ocorreu a alocacao
				//System.out.println(linhaResult);

				if (matcher.find()){

					servidor = matcher.group(1);

					//Como pode ou nao existir mais de um elemento na segunda parte, uso stringtokenizer

					t2 = new StringTokenizer(matcher.group(2), " ");
					//1.000000
					servidorLigado = t2.nextToken();					

					/*System.out.println("linhaResult: " + linhaResult);
					System.out.println("Servidor: " + servidor + " ServidorLigado: " + servidorLigado);
					System.exit(0);
					 */
					//Put information in the same index of AMOUNT_ALLOCS
					SERVERS[Integer.parseInt(servidor)]=(int)Double.parseDouble(servidorLigado);

				}//end if

				//Proxima linha do arquivo
				linhaResult = arquivoResult.readLine();

			}//fim while

			//Do the same of on demand allocation
			limiteInferior = limiteSuperior;
			limiteSuperior += NUM_SERVIDORES_SMALL;
			double tipoConsumo = 1;
			String tipo = "small";
			i=0;
			
			while(i<SERVERS.length){					
				//Se ha servidor com alocacoes, atribui consumo
				if (SERVERS[i]!=0){				
					consumoEnergiaServidores+=tipoConsumo;
					/*System.out.println(
							"i: " + i + " limiteInferior: " + limiteInferior + 
							" limiteSuperior: " + limiteSuperior + 
							" tipo: " + tipo + " tipoConsumo: " + tipoConsumo);
					*/
				}//end if					

				//Proximo indice of AMOUNT_ALLOCS for servers
				i++;
				limiteInferior++;
				if(i>limiteSuperior){
					if(tipo.equals("small")){
						tipo="large";
						tipoConsumo = 1.8;
						limiteSuperior += NUM_SERVIDORES_LARGE;						
					} else 
						if(tipo.equals("large")){
							tipo="huge";
							tipoConsumo = 6;
							limiteSuperior += NUM_SERVIDORES_HUGE;
						} else
							if(tipo.equals("huge")){
								tipo="small";
								tipoConsumo = 1;
								limiteSuperior += NUM_SERVIDORES_SMALL;
							}//end if					
				}//end if

			}//end while

			//close the file
			arquivoResult.close();			
		} catch(Exception e){
			System.out.println("Excecao 16 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO + "_" + rank + "\n" + e.getMessage());
			e.printStackTrace();
		}//fim catch

		return consumoEnergiaServidores;

	}//fim realizarParserConsumoEnergiaServidoresSubscribe

	public double realizarParserConsumoEnergiaRoteadoresSubscribe(int rank){

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

			String router;
			
			////////////////
			while (linhaResult!=null){

				//Possiveis resultados				
				//Ex.:  FIN( 10) 		1.000000            3.955556"   ou
				//Ex.:  FIN( 10) 		1.000000"  
				REGEX = "FIN\\(\\s(.*)\\)\\s(.*)";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linhaResult);
				//Ideia: achou uma alocacao, verifica em qual datacenter ocorreu a alocacao
				if (matcher.find()){

					router = matcher.group(1);

					//Como pode ou nao existir mais de um elemento na segunda parte, uso stringtokenizer

					t2 = new StringTokenizer(matcher.group(2), " ");
					//1.000000			
					if(Double.parseDouble(t2.nextToken())>0)
						//1 unidade de consumo para cada servidor ligado
						consumoEnergiaRouters++;

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

	}//fim realizarParserConsumoEnergiaRoteadoresSubscribe	

	public void inicializarArquivos(int rank){

		//1) Grava um modelo do datacenter atual para cada thread
		try {
			//Abre o arquivo do modelo de datacenter atual
			String linha = new String();
			StringBuffer info = new StringBuffer();
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_TOPOLOGIA_DATACENTER));
			while((linha=file.readLine())!=null){
				info.append(linha+"\n");
			}//fim while
			//Grava na base do cluster
			BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_TOPOLOGIA_DATACENTER+"_"+rank,false));
			out.write(info.toString());
			//Fecha o arquivo de escrita
			out.close();

			//Fecha o arquivo de leitura
			file.close();
		} catch(Exception e){
			System.out.println("4Excecao ao gravar no arquivo." + e.getMessage());
		}//fim catch

	}//fim inicializarArquivos

	public String adquirirFluxosEntrada_PublishSubscribe(String [][] EDGES_ARQUIVO, int nodoDestino){

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

	}//fim adquirirFluxosEntrada
	
	public String getLinkFonteTrafegoDatacenter_PublishSubscribe(String [][] EDGES_ARQUIVO, int i){

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

	}//getLinkFonteTrafegoDatacenter
	
	public static void main(String[] args) {

		new A_Worker();

	}//fim main

}//fim class
