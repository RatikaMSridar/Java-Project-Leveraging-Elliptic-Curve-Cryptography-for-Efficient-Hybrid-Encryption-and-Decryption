package org.cloudbus.cloudsim;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

import org.cloudbus.cloudsim.brite.Visualizer.BRITETopologyPO;
import org.cloudbus.cloudsim.brite.Visualizer.Edge;
import org.cloudbus.cloudsim.brite.Visualizer.Node;

/** 
 * Gerador de Workload para a melhor distribuicao do REALcloudSim 
 * 
 * @author Lucio Agostinho Rocha
 * Ultima atualizacao: 11/09/2012
 *
 */

public class A_Workload {

	public static int NUM_AMOSTRAS=0;
	public static int AMOSTRA_ATUAL=0;

	public static String PATH;

	private static String ARQUIVO_RESULT_LINGO;

	private static String ARQUIVO_MODELO_NS2;
	private static String ARQUIVO_NAM_NS2;
	private static String ARQUIVO_RESULT_NS2;
	private static String ARQUIVO_VAZAO_NS2;
	
	private static String ARQUIVO_WORKLOAD_VAZAO_NS2;

	//Arquivo da topologia
	private static String ARQUIVO_TOPOLOGIA_ORIGINAL;
	//Arquivo de saida com a topologia do datacenter
	private static String ARQUIVO_TOPOLOGIA_DATACENTER;	

	//numero de datacenters
	public static int NUM_DATACENTERS;	
	//numero de servidores
	public static int NUM_SERVIDORES;
	//numeroVMs inicialmente requisitas por servidor
	public static int NUM_VMS;

	public A_Workload(){

		AMOSTRA_ATUAL=0;
		while (AMOSTRA_ATUAL<NUM_AMOSTRAS){

			//IMPORTANTE: Fazer backup dos arquivos finais gerados pelo REALcloudSim antes
			//            de fazer os testes de workload

			//Como a topologia da rede e o resultado final do Lingo nao mudam, 
			//posso gerar quantas simulacoes de modelos de rede forem necessarias
			
			//Como no final da execucao do REALcloudSim eh repetida a execucao do melhor resultado
			//os arquivos finais resultantes sao os da melhor distribuicao

			//Objetivo: determinar o tamanho da amostra necessario para
			//estimar a vazao media para diferentes workloads submetidos ah rede

			System.out.println("\n------------------\nAMOSTRA " + AMOSTRA_ATUAL + "\n------------------\n");			
			
			gerarModeloNS2_workload();

			executarModeloNS2_workload();

			//Grava o resultado da vazao obtida em cada link
			gravar_workload();

			//O tamanho da amostra sera, para esse workload,
			//o maior numero de amostras necessario para descobrir a vazao media,
			//uma vez que eh feito o calculo da vazao media em cada link.			
			AMOSTRA_ATUAL++;

		}//fim while

		System.out.println("\n------------------\nFIM DA AMOSTRAGEM\n------------------\n");
		
	}//fim construtor	

	public void gravar_workload(){

		//Para cada aresta, calcula a vazao
		//
		String [][] EDGES=null;		

		double vazaoTotalVMs=0;

		//
		//Adquire as arestas do arquivo
		try
		{
			int i=0;
			int j=0;

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

			//Le o conteudo do arquivo resultante da simulacao no NS2
			//Para cada aresta
			i=0;
			String [][] fromNode = new String[numNodes+numEdges][numNodes+numEdges];
			String [][] toNode = new String[numNodes+numEdges][numNodes+numEdges];
			int [][] totalBits = new int[numNodes+numEdges][numNodes+numEdges];

			String [] t = new String[7];
			double [][] timeBegin = new double[numNodes+numEdges][numNodes+numEdges];
			double [][] timeEnd = new double[numNodes+numEdges][numNodes+numEdges]; 
			double [][] duration = new double[numNodes+numEdges][numNodes+numEdges];
			double [][] throughput = new double[numNodes+numEdges][numNodes+numEdges];
			//parserVazao [from][to] = [Throughput]
			//            [from][to] = [Throughput]
			//              ...
			String [][] parserVazao = new String [numNodes+numEdges][numNodes+numEdges];
			//Inicializa a matriz
			i=0;
			j=0;
			while(i<parserVazao.length){
				j=0;
				while(j<parserVazao[i].length){
					parserVazao[i][j]="";
					j++;
				}//fim while
				i++;
			}//fim while

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
				j=0;
				while (i<numNodes+numEdges){
					j=0;
					while (j<numNodes+numEdges){
						if (totalBits[i][j]!=0){
							duration[i][j] = Math.abs(timeEnd[i][j] - timeBegin[i][j]);
							throughput[i][j] = totalBits[i][j]/duration[i][j]/1e3;
							vazaoTotalVMs += throughput[i][j];
							//System.out.println(i + "--" + j + " " + totalBits[i][j] + " " + String.format("%1$,.2f",duration[i][j]) + " " + String.format("%1$,.2f",throughput[i][j]));
							//parserVazao.append("\n" + i + "--" + j + " " + totalBits[i][j] + " " + String.format("%1$,.2f",duration[i][j]) + " " + String.format("%1$,.2f",throughput[i][j]));
							parserVazao[i][j] = String.format("%1$.2f",throughput[i][j]);							
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

			//AMOSTRA_ATUAL=0 exibe o indice dos links
			if (AMOSTRA_ATUAL==0){
				//Grava o resultado em arquivo
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_WORKLOAD_VAZAO_NS2,false));
					i=0;
					j=0;			
					while(i<parserVazao.length){
						j=0;
						while (j<parserVazao[i].length){
							if (!parserVazao[i][j].equals(""))
								out.write(i + "--" + j + " ");
							j++;
						}//fim while
						i++;
					}//fim while
					out.close();
				} catch(Exception e){
					System.out.println("10Excecao ao gravar no arquivo." + e.getMessage());
				}//fim catch

			} else {

				//AMOSTRA_ATUAL>0 (vazao nos links das outras amostras)
				//Grava o resultado em arquivo
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_WORKLOAD_VAZAO_NS2,true));
					i=0;
					j=0;	
					out.write("\n");
					while(i<parserVazao.length){
						j=0;
						while (j<parserVazao[i].length){
							if (!parserVazao[i][j].equals(""))
								out.write(parserVazao[i][j] + " ");
							j++;
						}//fim while
						i++;
					}//fim while				
					out.close();
				} catch(Exception e){
					System.out.println("11Excecao ao gravar no arquivo." + e.getMessage());
				}//fim catch

			}//fim else

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Excecao 9 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER + "\n" + e.getMessage());
		}//fim catch

	}//fim gravar_workload

	public void gerarModeloNS2_workload(){

		System.out.println("\n--Gerar Modelo NS2--");

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

			String [][] EDGES_ARQUIVO = new String[numEdges][10];
			//Descricao dos campos
			linha = file.readLine();			
			info.append(linha+"\n");			
			for (i=0; i<numEdges; i++){
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
				BufferedReader arquivoResult = new BufferedReader(new FileReader(ARQUIVO_RESULT_LINGO));
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

						//Preciso gerar diferentes submissoes de trafego,
						//para ser possivel calcular o numero de amostras necessario para
						//informar qual a vazao media nos links
						
						conteudoNS2.append("   \n#Cria um agente UDP e atribui ao aglomerado");
						conteudoNS2.append("   \nset udp("+i+") [new Agent/UDP]");
						conteudoNS2.append("   \n$ns attach-agent $n("+indiceNodosNS2+") $udp("+i+")");
						conteudoNS2.append("   \n#Cria um trafego CBR e atribui ao agent UDP");
						conteudoNS2.append("   \nset cbr("+i+") [new Application/Traffic/CBR]");
						conteudoNS2.append("   \n$cbr("+i+") set packetSize_ 1500");
						conteudoNS2.append("   \n#Fluxo ficticio para ocupar todo o link");
						//conteudoNS2.append("   \n$cbr("+i+") set rate_ " + fluxoGeradoAglomeradoVMs[i] + "mb");						
						conteudoNS2.append("   \n$cbr("+i+") set rate_ " + (1 + Math.random() * fluxoGeradoAglomeradoVMs[i]) + "mb");
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
				System.out.println("Excecao 17 ao abrir o arquivo: " + ARQUIVO_RESULT_LINGO + "\n" + e.getMessage());
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
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_MODELO_NS2,false));			
				out.write(conteudoNS2.toString());
				out.close();
			} catch(Exception e){
				System.out.println("15Excecao ao gravar no arquivo." + e.getMessage());
			}//fim catch			

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Excecao 18 ao abrir o arquivo: " + ARQUIVO_TOPOLOGIA_DATACENTER + "\n" + e.getMessage());
		}//fim catch

	}//fim gerarModeloNS_workload

	public void executarModeloNS2_workload(){

		System.out.println("\n--Executar o modelo no NS2--");

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

	}//fim executarModeloNS2_workload

	public static void main(String args[]){

		NUM_AMOSTRAS = 100;

		//Numero de datacenters homogeneos (mesma quantidade de servidores, com as mesmas capacidades)
		NUM_DATACENTERS = 5;

		//Numero de Servidores por datacenter
		NUM_SERVIDORES = 10;

		//Numero de VMs
		NUM_VMS = 100;

		//Arquivos
		PATH = "/home/lucio/";

		//Para o NS2
		String modeloNS2 = PATH + "modeloNS2_" + 
		NUM_DATACENTERS+"data_"+
		NUM_SERVIDORES+"serv_"+
		NUM_VMS+"vm";
		ARQUIVO_MODELO_NS2 = modeloNS2 + ".tcl";
		ARQUIVO_NAM_NS2 = modeloNS2 + ".nam";
		ARQUIVO_RESULT_NS2 = modeloNS2 + ".tr";
		ARQUIVO_VAZAO_NS2 = modeloNS2 + ".vazao";
		ARQUIVO_WORKLOAD_VAZAO_NS2 = modeloNS2 + ".workload";

		//Para o Lingo
		String arquivo = PATH + "modeloLingo_"+
		NUM_DATACENTERS+"data_"+
		NUM_SERVIDORES+"serv_"+
		NUM_VMS+"vm";
		ARQUIVO_RESULT_LINGO = arquivo + ".lgr";		

		//Para a Topologia
		String topologia = PATH + "modeloLingo_"+
		NUM_DATACENTERS+"data_"+
		NUM_SERVIDORES+"serv_"+
		NUM_VMS+"vm";		
		ARQUIVO_TOPOLOGIA_ORIGINAL = topologia + ".brite";
		ARQUIVO_TOPOLOGIA_DATACENTER = topologia + ".datacenter";

		new A_Workload();

	}//fim main

}//fim classe
