/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * VmAllocationPolicySimple is an VmAllocationPolicy that
 * chooses, as the host for a VM, the host with
 * less PEs (number of CPUs or CPU cores) in use.
 *
 * @author      Lucio Agostinho Rocha
 * @author		Rodrigo N. Calheiros
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 1.0
 */
public class VmAllocationPolicyBio_v1 extends VmAllocationPolicy {

	/** The vm table. */
	private Map<String, Host> vmTable;

	/** The used pes. */
	private Map<String, Integer> usedPes;

	/** The free pes. */
	private List<Integer> freePes;

	//Numero de iteracoes do algoritmo, por alocacao
	private int NUM_ITERACOES=1;

	//5 campos para servidores + 1 para fitness
	//private int TAM_CROMO=6;
	private int TAM_CROMO=10;

	//[serv0, serv1, ... serv4, fitness]	
	private int campoFitness=TAM_CROMO-1;	

	//tamanho da populacao
	//Deve ser igual ao numero de host no datacenter
	private int TAM_POPULACAO=1000;

	private String [][] R = new String[TAM_POPULACAO][TAM_CROMO];	
	private String [][] R2 = new String[TAM_POPULACAO][TAM_CROMO];	

	/**
	 * Creates the new VmAllocationPolicySimple object.
	 *
	 * @param list the list
	 *
	 * @pre $none
	 * @post $none
	 */
	public VmAllocationPolicyBio_v1(List<? extends Host> list) {
		super(list);

		setFreePes(new ArrayList<Integer>());
		for (Host host : getHostList()) {
			System.out.println("Host/CPUs: ["+host.getId()+"]["+host.getPesNumber()+"]");
			getFreePes().add(host.getPesNumber());

		}

		setVmTable(new HashMap<String, Host>());
		setUsedPes(new HashMap<String, Integer>());
	}

	/**
	 * Allocates a host for a given VM.
	 *
	 * @param vm VM specification
	 *
	 * @return $true if the host could be allocated; $false otherwise
	 *
	 * @pre $none
	 * @post $none
	 */
	@Override
	public boolean allocateHostForVm(Vm vm) {
		//Quantidade de processadores na VM
		int requiredPes = vm.getPesNumber(); 
		boolean result = false;
		int tries = 0;
		List<Integer> freePesTmp = new ArrayList<Integer>();
		//Obtem a lista de CPUs livres nos hosts
		for (Integer freePes : getFreePes()) {
			freePesTmp.add(freePes);
		}

		//O tamanho da populacao deve ser, no minimo,
		//igual ao numero de hosts disponiveis, para
		//pesquisar possibilidades na maior parte do
		//espaco de busca
		//setTamPopulacao(freePesTmp.size());		

		if (!getVmTable().containsKey(vm.getUid())) { //if this vm was not created

			do {//we still trying until we find a host or until we try all of them
				int moreFree = Integer.MIN_VALUE;
				int idx = -1;

				//Inicializa 
				/*				//Lista de CPUs disponiveis em cada host do datacenter
				Iterator it = freePesTmp.iterator();
				int i=1;
				while (it.hasNext()){
					Log.printLine("Free CPUs no host["+i+"]: " + it.next());
					//it.next();
					i=i+1;
				}//fim while
				 */
				//Com uma lista muito grande de hosts no datacenter 
				//(o que geralmente ocorre), nao eh viavel consultar todos os hosts

				//Inicializa
				//
				//Inicializa com a lista de hosts com CPUs disponiveis
				inicializar(freePesTmp);
				//System.out.println("Passei por aqui");
				try {
					//exibirPopulacao(R2);
					//System.out.println("---");
					//					Thread t = new Thread();			
					//					t.sleep(5000);
				} catch (Exception e){}


				int i=1;				
				while (i<=NUM_ITERACOES){

					//Seleciona 
					//
					//Seleciona para reproducao os individuos com
					//maior fitness para a alocacao atual
					selecionar();

					//Reproduz
					reproduzir();

					//Varia
					//variar();

					//Avalia
					//
					//Avalia dentre os selecteds aquele
					//com o maior fitness
					idx=avaliar(freePesTmp);

					//Troca os pais pelos filhos com os melhores fitness
					atualizarPopulacao();

					i=i+1;
				}//fim while

				//freePesTmp contem a lista de CPUs disponiveis por servidor
				//freePesTmp = [host1, pes_host1_disponiveis]
				//             [host2, pes_host2_disponiveis]
				//              ...				

				/*				//we want the host with less pes in use	
				for (i=0; i < freePesTmp.size(); i++) {
					if (freePesTmp.get(i) > moreFree) {
						moreFree = freePesTmp.get(i);
						idx = i;
					}
				}
				 */				

				try {
					System.out.println("Selected Host: " + idx);
					//					Thread t = new Thread();			
					//					t.sleep(5000);
				} catch (Exception e){}

				if (idx!=-1){
					//Indice do host escolhido para a alocacao
					Host host = getHostList().get(idx);
					result = host.vmCreate(vm);

					if (result) { //if vm were succesfully created in the host
						Log.printLine("VmAllocationPolicyBio: VM #"+vm.getId()+ " Chosen host: #"+host.getId()+" idx:"+idx);
						Log.printLine("---Chosen host: #"+host.getId()+" idx:"+idx);
						//Guarda a informacao da alocacao
						try {
							java.io.File arquivo = new java.io.File("/home/lucio/realcloudsim.log");
							java.io.FileOutputStream fos = new java.io.FileOutputStream(arquivo,true);
							//String texto = "VmAllocationPolicyBio---Chosen host: "+host.getId()+"\n"; 
							String texto = "VmAllocationPolicyBio---Chosen host: "+host.getId()+"\n";							
							fos.write(texto.getBytes());
							fos.close();
						} catch(Exception exc){
							Log.printLine("Exception writing to the file.");
						}

						getVmTable().put(vm.getUid(), host);
						getUsedPes().put(vm.getUid(), requiredPes);
						//Reduz a carga de CPU do host
						getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
						result = true;
						break;
					} else {
						freePesTmp.set(idx, Integer.MIN_VALUE);
					}

				}//fim if

				tries++;
				//Enquanto nao conseguir criar uma VM, e
				//enquanto nao varrer todos os hosts disponiveis, na tentativa de alocae a VM 
				} while (!result && tries < getFreePes().size());				
//			} while (!result && tries < 3);				

		}

		return result;

	}//fim allocateHostForVm	

	public void atualizarPopulacao(){

		int i=0;
		int j=0;
		while (i<TAM_POPULACAO){
			j=0;
			while (j<TAM_CROMO){
				R[i][j]=R2[i][j];
				j=j+1;
			}//fim while
			i=i+1;
		}//fim while

		//exibirPopulacao(R);

	}//fim atualizarPopulacao

	public void setTamPopulacao(int tamPopulacao){
		TAM_POPULACAO=tamPopulacao;
	}

	public void inicializar(List<Integer> freePesTmp){

		/*		System.out.println("---");
		System.out.println("
");
		System.out.println("---");
		 */
		//Numero de hosts no datacenter
		int numHosts=freePesTmp.size();		

		int i=0;
		int j=0;
		int P_SERVER=1;
		//System.out.println("TAM_POP:" + TAM_POPULACAO);
		//Para cada linha da matriz
		while (i<TAM_POPULACAO){

			int host=1;
			j=0;
			while (j<TAM_CROMO-1){
				host = P_SERVER;
				R[i][j]=host + "";
				j++;

				if (P_SERVER==numHosts)
					P_SERVER=1;
				else
					P_SERVER=P_SERVER+1;
			}//fim while

			//Atualiza o fitness do cromossomo
			R[i][campoFitness]="0";
			atualizarFitness(freePesTmp,i);

			//Proximo cromossomo
			i++;

		}//fim while

		//exibirPopulacao(R);		

	}//fim inicializar	

	public void atualizarFitness(List<Integer> freePesTmp, int indiceCromo){

		int [] listaAvaliados = new int[TAM_CROMO];
		int i=0;
		while (i<TAM_CROMO){
			listaAvaliados[i]=0;
			i=i+1;
		}//fim while

		//Cada cromossomo possui uma lista de servidores
		//Cada servidor no cromossomo ira contribuir na 
		//funcao de fitness desse cromossomo        

		//Guarda a lista de servidores avaliados no cromossomo
		//para evitar repetir a pontuacao caso o mesmo servidor
		//apareca mais de uma vez no cromossomo		
		int j=0;
		int host=1;	
		boolean repetido=false;
		//indice na lista de servidores avaliados no cromossomo
		int k=0;
		while (j<TAM_CROMO-1){//-1 pq nao conta o campo de fitness
			//Recolhe o indice do servidor no cromossomo
			host = Integer.parseInt(R[indiceCromo][j]); 

			//Calcula o fitness apenas se nao tiver feito isso antes, ou seja,
			//se o servidor nao for repetido
			//valor da busca, P=populacao, indice (individuo) na populacao
			repetido = verificarRepeticao(host,listaAvaliados);
			if (!repetido){
				//System.out.println("Antes: " + R[indiceCromo][campoFitness]);
				R[indiceCromo][campoFitness]=(Float.parseFloat(R[indiceCromo][campoFitness])+calcularFitness(host,freePesTmp))+"";
				//System.out.println("Depois: " + R[indiceCromo][campoFitness]);

				//Adiciona o elemento ah lista de avaliados
				listaAvaliados[k]=host;
				k=k+1;
			}//fim if

			//Proximo host no cromossomo
			j=j+1;
		}//fim while

	}//fim atualizarFitness

	public void atualizarFitnessR2(List<Integer> freePesTmp, int indiceCromo){

		int [] listaAvaliados = new int[TAM_CROMO];
		int i=0;
		while (i<TAM_CROMO){
			listaAvaliados[i]=0;
			i=i+1;
		}//fim while

		//Cada cromossomo possui uma lista de servidores
		//Cada servidor no cromossomo ira contribuir na 
		//funcao de fitness desse cromossomo        

		//Guarda a lista de servidores avaliados no cromossomo
		//para evitar repetir a pontuacao caso o mesmo servidor
		//apareca mais de uma vez no cromossomo		
		int j=0;
		int host=1;	
		boolean repetido=false;
		//indice na lista de servidores avaliados no cromossomo
		int k=0;
		while (j<TAM_CROMO-1){//-1 pq nao conta o campo de fitness
			//Recolhe o indice do servidor no cromossomo
			host = Integer.parseInt(R2[indiceCromo][j]); 

			//Calcula o fitness apenas se nao tiver feito isso antes, ou seja,
			//se o servidor nao for repetido
			//valor da busca, P=populacao, indice (individuo) na populacao
			repetido = verificarRepeticao(host,listaAvaliados);
			if (!repetido){
				R2[indiceCromo][campoFitness]=(Float.parseFloat(R2[indiceCromo][campoFitness])+calcularFitness(host,freePesTmp))+"";

				//Adiciona o elemento ah lista de avaliados
				listaAvaliados[k]=host;
				k=k+1;
			}//fim if

			//Proximo host no cromossomo
			j=j+1;
		}//fim while

	}//fim atualizarFitnessR2

	public float calcularFitness(int host, List<Integer> freePesTmp){

		//Verifica se existe CPUs disponiveis no Selected Host
		//host-1 pq a lista de hosts comeca em 0
		float fitness = freePesTmp.get(host-1) + 1; //+1 para nao ter fitness 0 

		/*		try {
			Thread t = new Thread();			
			t.sleep(5000);
		} catch (Exception e){}
		 */
		return fitness;

	}//fim calcularFitness

	public boolean verificarRepeticao(int host, int [] listaAvaliados){

		boolean repetido=false;
		int i=1;		
		while (listaAvaliados[i]!=0 & i<=TAM_CROMO-1 & repetido==false){
			if (listaAvaliados[i]!=host) 
				i=i+1;
			else
				repetido=true;
		}//fim while

		return repetido;

	}//fim verificarRepeticao

	public void selecionar(){

		/*		System.out.println("---");
		System.out.println("selecionar");
		System.out.println("---");
		 */		

		String [] roleta = new String[TAM_POPULACAO];
		//O vetor eh passado por referencia. Entao,
		//o que for feito no metodo para o vetor
		//sera atribuido aqui tambem
		gerarRoleta(roleta);

		int i=0;
		int j=0;
		float p=0;
		int cromoselected=0;
		while (i<TAM_POPULACAO){
			//Probabilidade da cromo ser selecionada
			p = (float) Math.random();

			cromoselected = individuoRoleta(roleta,p);

			//Copia a cromo selecionada para
			//a matriz de cromos para reproducao
			j=0; 
			while (j<TAM_CROMO){
				R2[i][j] = R[cromoselected][j];
				j++;
			}//fim while

			i++;
		}//fim while

		//exibirPopulacao(R2);

	}//fim selecionar

	public void reproduzir(){

		/*		System.out.println("---");
		System.out.println("reproduzir");
		System.out.println("---");
		 */		

		//Regras antes:
		//R = | 1 | 3 | 15 | 4 | ... | fitness |
		//    | 2 | 4 | 5 | 6 | ... | fitness |
		//...
		//ex.: ponto de crossover: indice 2 (indice inicial 0)
		//
		//Regras depois:
		//R = | 1 | 3 | 5 | 6 | ... | fitness |
		//    | 2 | 4 | 15 | 4 | ... | fitness |

		//Indices possiveis para o ponto de crossover
		int minRange = 0;
		//2 bits para o tipo de acao
		int maxRange = TAM_CROMO;

		int pontoCrossover=0;

		//Faz o cruzamento das regras, 2 a 2
		//
		//Cada par possui o seu proprio ponto de crossover
		int i=0;
		int j=0;
		String aux1="0";
		String aux2="0";
		while (i<R2.length){

			pontoCrossover = (int) Math.round(minRange + Math.random() * maxRange);
			//System.out.println("Ponto de crossover: " + pontoCrossover);
			while (pontoCrossover < maxRange){

				aux1 = R2[i][pontoCrossover];
				aux2 = R2[i+1][pontoCrossover];

				R2[i][pontoCrossover] = aux2;
				R2[i+1][pontoCrossover] = aux1;

				pontoCrossover++;
			}//fim while
			i+=2;
		}//fim while

		//exibirPopulacao(R2);

	}//fim reproduzir

	/*	public void variar(){

		System.out.println("---");
		System.out.println("variar");
		System.out.println("---");	

		//Seleciona dois pontos aleatorios para mutacao na regra
		//Indices possiveis para o ponto 
		int minRange = 0;
		int maxRange = TAM_CROMO;

		int ponto1=0;
		int ponto2=0;
		String aux1="0";
		String aux2="0";

		int i=0;		
		while (i<R2.length){

			ponto1 = (int) Math.round(minRange + Math.random() * maxRange);
			ponto2 = (int) Math.round(minRange + Math.random() * maxRange);

			//System.out.println("Ponto1: " + ponto1 + " Ponto2: " + ponto2);

			aux1 = R2[i][ponto1];
			aux2 = R2[i][ponto2];

			//Troca os pontos com o inverso de seus valores

			if (aux1==0)
				aux1=1;
			else
				aux1=0;

			if (aux2==0)
				aux2=1;
			else
				aux2=0;

			//Faz a mutacao
			R2[i][ponto1]=aux2+"";
			R2[i][ponto2]=aux1+"";

			i++;
		}//fim while

		//exibirRegras(R2);

	}//fim variar
	 */

	public int avaliar(List<Integer> freePesTmp){

		//Indice do Selected Host
		int idx=-1;

		int i=0;
		while (i<R2.length){			
			//Limpa o campo de fitness
			R2[i][campoFitness]="0";
			//Atualiza o fitness do cromossomo
			atualizarFitnessR2(freePesTmp,i);
			i=i+1;
		}//fim while

		ordenarPopulacao();

		//Global: Busca o primeiro cromossomo que consiga alocar a VM, caso exista
		//Aqui, a populacao esta ordenada globalmente de acordo com o fitness e
		//localmente, tambem de acordo com o fitness
		i=0;
		int host=i;
		boolean achou=false;
		while (i<TAM_POPULACAO && achou==false){

			//Local: Busca no espaco de busca de servidores fornecidos pelo cromossomo
			//o primeiro servidor que possa alocar a VM, 
			//para que a VM nao espere muito na fila para ser alocada
			//
			//Ordena os servidores no cromossomo, com base na media local de alocacoes
			ordenarCromoLocal(i,freePesTmp);

			int j=0;
			int moreFree = Integer.MIN_VALUE;
			idx = -1;			
			while (j<TAM_CROMO-1){

				host=Integer.parseInt(R2[i][j])-1;//-1 pq a lista de hosts comeca em 0
				//Esse trecho funciona (sub-otimo)
				//achou=true;
				//idx=host;

				//we want the host with less pes in use
				if (freePesTmp.get(host) > moreFree) {
					moreFree = freePesTmp.get(host);
					idx = host;
				}//fim if

				j=j+1;
			}//fim while

			//Proximo cromossomo
			i=i+1;
		}//fim while

		//Retorna o Selected Host
		return idx;

	}//fim avaliar	

	public void ordenarCromoLocal(int indiceCromo,List<Integer> freePesTmp){

		int host1=1;
		int host2=1;
		for (int pass=1; pass<TAM_CROMO-1; pass++) //passagens (nao conta o campo de fitness)
			for (int i=0; i<TAM_CROMO-2; i++){//uma passagem (nao conta o campo de fitness)
				host1=Integer.parseInt(R2[indiceCromo][i])-1; //-1 pq a lista de hosts comeca em 0
				host2=Integer.parseInt(R2[indiceCromo][i+1])-1; //-1 pq a lista de hosts comeca em 0
				if (freePesTmp.get(host1) < freePesTmp.get(host2))
					trocaLocal(indiceCromo,i,i+1);

			}//fim for

	}//fim ordenarComossomoLocal

	public void trocaLocal(int indiceCromo, int i1, int i2){

		String aux = new String();

		aux=R2[indiceCromo][i1];

		R2[indiceCromo][i1] = R2[indiceCromo][i2];

		R2[indiceCromo][i2]=aux;		

	}//fim trocaLocal

	public void gerarRoleta(String [] roleta){

		float soma=0;
		float energia=0;

		int i=0;
		while (i<TAM_POPULACAO){

			energia = Float.parseFloat(R[i][campoFitness]);
			soma += energia;

			i++;
		}//fim while

		float anterior=0;
		float probabilidade=0;

		i=0;
		while (i<TAM_POPULACAO){

			energia = Float.parseFloat(R[i][campoFitness]);
			probabilidade = anterior + energia/soma;
			roleta[i] = probabilidade + "";
			anterior = probabilidade;
			i++;

		}//fim while

	}//fim gerarRoleta

	public int individuoRoleta(String [] roleta, float p){

		float anterior=0;
		boolean achou=false;
		int cromoselected=0;

		int i=0;
		while (i<roleta.length && !achou){

			if (p>=anterior && p <= Float.parseFloat(roleta[i])){
				cromoselected = i;
				achou=true;
			} else
				anterior = Float.parseFloat(roleta[i]);

			i++;
		}//fim while

		return cromoselected;

	}//fim individuoRoleta	

	public void exibirPopulacao(String [][] R){

		System.out.println("---");
		System.out.println("exibirPopulacao");
		System.out.println("---");

		int i=0;
		int j=0;
		while (i<R.length){

			while (j<TAM_CROMO){

				System.out.print(R[i][j] + " ");
				j++;

			}//fim while

			//Salta uma linha
			System.out.println();

			i++;
			j=0;
		}//fim while

	}//fim exibirPopulacao	

	public void ordenarPopulacao(){

		/*		System.out.println("---");
		System.out.println("ordenarPopulacao");
		System.out.println("---");	
		 */
		//Ordenacao simples (O(n^2)) pelo metodo da bolha

		for (int pass=1; pass<TAM_POPULACAO; pass++) //passagens
			for (int i=0; i<TAM_POPULACAO-1; i++) //uma passagem		
				if (Float.parseFloat(R2[i][campoFitness]) < Float.parseFloat(R2[i+1][campoFitness]))
					troca(i,i+1);

		//exibirPopulacao(R2);

	}//fim ordenarPopulacao	

	public void troca(int i1, int i2){

		String [] aux = new String[TAM_CROMO];

		int i=0;
		while(i<TAM_CROMO){
			aux[i]=R2[i1][i];
			i++;
		}//fim while

		i=0;
		while(i<TAM_CROMO){
			R2[i1][i] = R2[i2][i];
			i++;
		}//fim while

		i=0;
		while(i<TAM_CROMO){
			R2[i2][i] = aux[i];
			i++;
		}//fim while

	}//fim troca

	/*	public ArrayList inicializa(List<Integer> freePesTmp){

		//Tamanho da populacao
		int TAM_POP=20;

		//Numero de campos do cromossomo
		int TAM_CROMO=5;

		//Numero de hosts no datacenter
		int numHosts=freePesTmp.size();

		ArrayList population = new ArrayList();

		int i=1;
		int j=1;
		int host=1;
		while (i<=TAM_POP){
			ArrayList cromo = new ArrayList();
			//Gera as sequencias de cromossomos
			j=1;
			while (j<=TAM_CROMO){
				host = 1+(int)(Math.random()*numHosts);
				cromo.add(host);
				j=j+1;
			}//fim while

			//population = [cromo1,
			//              cromo2,
			//              ... ]
			population.add(cromo);

			i=i+1;
		}//fim while

		//Exibe a populacao
		Iterator itPop = population.iterator();
		i=0;
		while(itPop.hasNext()){

			Iterator itCromo = ((ArrayList)itPop.next()).iterator();

			System.out.print("[ ");
			while (itCromo.hasNext())			
				System.out.print(itCromo.next() + " ");
			System.out.print("]\n");
		}//fim while

		return population;

	}//fim inicializa
	 */

	/**
	 * Releases the host used by a VM.
	 *
	 * @param vm the vm
	 *
	 * @pre $none
	 * @post none
	 */
	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = getVmTable().remove(vm.getUid());
		int idx = getHostList().indexOf(host);
		int pes = getUsedPes().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
			getFreePes().set(idx, getFreePes().get(idx) + pes);
		}
	}

	/**
	 * Gets the host that is executing the given VM belonging to the
	 * given user.
	 *
	 * @param vm the vm
	 *
	 * @return the Host with the given vmID and userID; $null if not found
	 *
	 * @pre $none
	 * @post $none
	 */
	@Override
	public Host getHost(Vm vm) {
		return getVmTable().get(vm.getUid());
	}

	/**
	 * Gets the host that is executing the given VM belonging to the
	 * given user.
	 *
	 * @param vmId the vm id
	 * @param userId the user id
	 *
	 * @return the Host with the given vmID and userID; $null if not found
	 *
	 * @pre $none
	 * @post $none
	 */
	@Override
	public Host getHost(int vmId, int userId) {
		return getVmTable().get(Vm.getUid(userId, vmId));
	}

	/**
	 * Gets the vm table.
	 *
	 * @return the vm table
	 */
	public Map<String, Host> getVmTable() {
		return vmTable;
	}

	/**
	 * Sets the vm table.
	 *
	 * @param vmTable the vm table
	 */
	protected void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	}

	/**
	 * Gets the used pes.
	 *
	 * @return the used pes
	 */
	protected Map<String, Integer> getUsedPes() {
		return usedPes;
	}

	/**
	 * Sets the used pes.
	 *
	 * @param usedPes the used pes
	 */
	protected void setUsedPes(Map<String, Integer> usedPes) {
		this.usedPes = usedPes;
	}

	/**
	 * Gets the free pes.
	 *
	 * @return the free pes
	 */
	protected List<Integer> getFreePes() {
		return freePes;
	}

	/**
	 * Sets the free pes.
	 *
	 * @param freePes the new free pes
	 */
	protected void setFreePes(List<Integer> freePes) {
		this.freePes = freePes;
	}

	/* (non-Javadoc)
	 * @see cloudsim.VmAllocationPolicy#optimizeAllocation(double, cloudsim.VmList, double)
	 */
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm, org.cloudbus.cloudsim.Host)
	 */
	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		if (host.vmCreate(vm)) { //if vm has been succesfully created in the host
			getVmTable().put(vm.getUid(), host);
			Log.formatLine("%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(), CloudSim.clock());
			return true;
		}

		return false;
	}
}
