/**
 * Conversor of BRITE topology to avoid cycles in the network topology
 * 
 * Procedure: conversorBRITEGraphViz -> conversorGraphVizAcyclic -> conversorGraphVizBRITE 
 * 
 */

package org.cloudbus.cloudsim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cloudbus.cloudsim.brite.Visualizer.Edge;
import org.cloudbus.cloudsim.brite.Visualizer.Node;

public class A_ConversorBRITEGraphViz {

	public static String PATH;

	public static String ARQUIVO_BRITE;

	public static String ARQUIVO_BRITE_OUTPUT;

	public static String ARQUIVO_GRAPHVIZ;

	public static String ARQUIVO_GRAPHVIZ_ACYCLIC;

	public static String ARQUIVO_GRAPHVIZ_PNG;

	public static int NUM_NODES;

	public static int NUM_EDGES;

	public A_ConversorBRITEGraphViz(){

		conversorBRITEGraphViz();

		conversorGraphVizAcyclic();

		conversorGraphVizBRITE();	

	}//end constructor

	public void conversorBRITEGraphViz(){

		//Open the BRITE file
		try
		{
			int i=0;
			int j=0;

			StringBuffer modeloBRITE = new StringBuffer(); 
			
			String linha = new String();			
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_BRITE));
			//Cabecalho
			linha=file.readLine();		
			modeloBRITE.append(linha+"\n");
			StringTokenizer token = new StringTokenizer(linha, "( )");
			token.nextToken();
			//System.out.println("\nnumNodesOriginal: "+Integer.parseInt(token.nextToken()));			
			int numNodesOriginal = Integer.parseInt(token.nextToken());
			//Salta as proximas linhas do cabecalho
			for (i=0; i<2; i++) {
				linha=file.readLine();
				modeloBRITE.append(linha+"\n");
			}//fim for

			//get the number of nodes
			linha = file.readLine();			
			modeloBRITE.append(linha+"\n");
			token = new StringTokenizer(linha, "( )");
			token.nextToken();
			int numNodes = Integer.parseInt(token.nextToken());
			//System.out.println("numNodes: " + numNodes);
			//Salta o campo Nodes (unnecessary for original BRITE file)
			//file.readLine();
			//Guarda todas as informacoes dos Nodes
			//NODES_ARQUIVO	[0][id, type]
			//    			[1][id, type]
			//    ...
			String [][] NODES_ARQUIVO = new String[numNodes][2];
			for (i=0; i<numNodes; i++){				
				linha=file.readLine();
				modeloBRITE.append(linha+"\n");
				//System.out.println(linha);
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

			//Adquire o numero de Edges
			for (i=0; i<2; i++) {
				linha=file.readLine();
				modeloBRITE.append(linha+"\n");
			}//fim for			
			linha = file.readLine();
			modeloBRITE.append(linha+"\n");
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
			//Descricao dos campos (unnecessary for original BRITE file)
			//linha = file.readLine();
			for (i=0; i<NUM_EDGES; i++){
				j=0;

				linha = file.readLine();
				modeloBRITE.append(linha+"\n");
				//System.out.println(linha);

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

			System.out.println("\n\nOriginal BRITE topology:\n");
			System.out.println(modeloBRITE.toString());
			
			//Generate GraphViz contents
			StringBuffer graphvizGraph = new StringBuffer();

			graphvizGraph.append("Digraph G {\n");
			i=0;
			while (i<NUM_EDGES){
				graphvizGraph.append(EDGES_ARQUIVO[i][campoEdgeFrom] + " -> " + EDGES_ARQUIVO[i][campoEdgeTo] + "\n");
				i++;
			}//end while
			graphvizGraph.append("}\n");

			//System.out.println(graphvizGraph.toString());

			//Write graphviz graph in a file
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_GRAPHVIZ,false));			
				out.write(graphvizGraph.toString());
				out.close();

				System.out.println("Generated GraphViz file: " + ARQUIVO_GRAPHVIZ);

			} catch(Exception e){
				System.out.println("Exception at writting in file." + e.getMessage());
			}//fim catch

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Exception at openning file: " + ARQUIVO_BRITE + "\n" + e.getMessage());
			e.printStackTrace();
		}//fim catch	

	}//end conversorBRITEGraphViz

	public void conversorGraphVizAcyclic(){

		String comando = PATH + "/conversorGraphVizAcyclic.sh " + 
		ARQUIVO_GRAPHVIZ + " " + ARQUIVO_GRAPHVIZ_ACYCLIC + " " + ARQUIVO_GRAPHVIZ_PNG;

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
			System.out.println("End of convertion. Output file: " + ARQUIVO_GRAPHVIZ_ACYCLIC);
			System.out.println("PNG output file: " + ARQUIVO_GRAPHVIZ_PNG);
		} catch (Exception e){
			System.out.println("Exception in convertion to acyclic graphviz file.");
		}//fim catch

	}//end conversorGraphVizAcyclic

	public void conversorGraphVizBRITE(){

		//Extract data from acyclic graphViz file
		
		StringBuffer edgesGraphViz = new StringBuffer();
		//Number of generated graphviz edges
		int numEdgesGraphViz=0;		
		
		try
		{
			int i=0;
			int j=0;

			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_GRAPHVIZ_ACYCLIC));
			//Jump Header
			linha=file.readLine();
			linha=file.readLine();

			String REGEX = "";
			Matcher matcher;
			Pattern pattern;

			//Source node
			String fromNode;
			//Destination node
			String toNode;	
			
			//Group of edges graphViz
			//Ex.: 
			// 0 -> 1
			// 1 -> 2
			// 1 -> 3
			//
			// edgesGraphViz: 0 1 1 2 1 3 ...
			edgesGraphViz = new StringBuffer();
			
			//Verify the number of new generated edges
			//while is not the end of graphviz acyclic file
			while (!linha.equals("}")){

				REGEX = "(.*)->(.*);";
				pattern = Pattern.compile(REGEX);
				matcher = pattern.matcher(linha);
				if (matcher.find()){

					fromNode = removerEspacos(removerTabs(matcher.group(1)));
					toNode = removerEspacos(removerTabs(matcher.group(2)));
					//System.out.print("\nSource: "+Integer.parseInt(fromNode) + " Destination: " + Integer.parseInt(toNode));
					edgesGraphViz.append(fromNode + " " + toNode + " ");

				}//end if

				//Next line
				linha=file.readLine();
				numEdgesGraphViz++;
				
			}//end while
			
			//Until here, all edges are acquired

			//close the file			
			file.close();			
		} catch(Exception e){
			System.out.println("Exception at openning file: " + ARQUIVO_GRAPHVIZ_ACYCLIC + "\n" + e.getMessage());
			e.printStackTrace();
		}//fim catch
		
		//Generate new BRITE output file with previous data		
		System.out.println("\n\nGenerating newer acyclic BRITE topology file: " + ARQUIVO_BRITE_OUTPUT);
		System.out.println("\nNote: previous bandwidth IS NOT preserved.\n");
		StringBuffer modeloBRITE = new StringBuffer();

		try
		{				
			String linha = new String();
			//try to open the file.
			BufferedReader file = new BufferedReader(new FileReader(ARQUIVO_BRITE));
			for (int i=0; i<3; i++) {
				modeloBRITE.append(file.readLine()+"\n");
			}//fim for

			//get the number of nodes
			linha = file.readLine();
			modeloBRITE.append(linha+"\n");
			StringTokenizer t1 = new StringTokenizer(linha, "( )");
			t1.nextToken();
			int numNodes = Integer.parseInt(t1.nextToken());
			//System.out.println("numNodes: " + numNodes);
			Node [] nodes = new Node[numNodes];

			//for the number of nodes
			int i=0;
			for (i=0; i<nodes.length; i++){
				linha = file.readLine();
				modeloBRITE.append(linha+"\n");
				nodes[i] = new Node(linha);
			}

			//two blank lines
			file.readLine( );
			file.readLine( );
			modeloBRITE.append("\n\n");

			//Jump the original number of edges
			linha = file.readLine();
			
			//use the number of graphviz edges
			modeloBRITE.append("Edges: ( " + numEdgesGraphViz + " )\n");	

			//System.out.println(modeloBRITE.toString());
				
			//Extract values of edgesGraphViz
			StringTokenizer token = new StringTokenizer(edgesGraphViz.toString()," ");

			//Note: previous bandwidth IS NOT preserved
			i=0;
			while (i<numEdgesGraphViz) {				
				
				//construct each edge
				modeloBRITE.append(i + "	" +
						//Source
						token.nextToken() + "	" +
						//Destination
						token.nextToken() + "	" +
						//Link cost
						"1" + "	" +
						//Delay
						"0.1" + "	" +
						//Bandwidth
						"9999" + "	" +
						//QueueLimit
						"1"+
						//Other fields
						"	0	E_AS_NONE	U\n");				
				
				//Next graphviz edge
				i++;				
			}//end while
					
			//close the file
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}//fim catch

		System.out.println(modeloBRITE.toString());
		
		//Grava a configuracao em arquivo
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(ARQUIVO_BRITE_OUTPUT,false));			
			out.write(modeloBRITE.toString());
			out.close();
		} catch(Exception e){
			System.out.println("Exception at writting output file: " + ARQUIVO_BRITE_OUTPUT + "\n" + e.getMessage());
		}//fim catch
		
		System.out.println("\n\nNewer acyclic BRITE file created: " + ARQUIVO_BRITE_OUTPUT);
		System.out.println("\nNote: previous bandwidth IS NOT preserved. Replace the 9999 bandwidth value.\n");

	}//end conversorGraphVizBRITE

	public String removerTabs(String s) {
		StringTokenizer st = new StringTokenizer(s,"\t",false);
		String t="";
		while (st.hasMoreElements()) t += st.nextElement();
		return t;
	}

	public String removerEspacos(String s) {
		StringTokenizer st = new StringTokenizer(s," ",false);
		String t="";
		while (st.hasMoreElements()) t += st.nextElement();
		return t;
	}

	public static void main(String[] args) {

		NUM_NODES=50;

		NUM_EDGES=85;

		//Home Path
		PATH = "/home/lucio/";

		//Original BRITE file
		ARQUIVO_BRITE = PATH + "modeloMesh_" + 			
		NUM_NODES + "nodes_" + NUM_EDGES + "edges.brite"; 

		//BRITE -> GraphViz
		ARQUIVO_GRAPHVIZ = PATH + "modeloMesh_" + 			
		NUM_NODES + "nodes_" + NUM_EDGES + "edges.graphviz";

		//GraphViz acyclic file
		ARQUIVO_GRAPHVIZ_ACYCLIC = PATH + "modeloMesh_" + 			
		NUM_NODES + "nodes_" + NUM_EDGES + "edges.graphviz_acyclic";

		//GraphViz PNG file
		ARQUIVO_GRAPHVIZ_PNG = PATH + "modeloMesh_" + 			
		NUM_NODES + "nodes_" + NUM_EDGES + "edges.png";

		//GraphViz -> BRITE
		//(It is better generate other file, because 
		//there is not guarantee that will keep the same amount of edges
		ARQUIVO_BRITE_OUTPUT = PATH + "modeloMesh_" + 			
		NUM_NODES + "nodes_" + NUM_EDGES + "edges.brite_acyclic";

		new A_ConversorBRITEGraphViz();

	}//end main

}//end classe
