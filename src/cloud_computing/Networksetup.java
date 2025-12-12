/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package cloud_computing;

import commoncodes.db_conn;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicyBio;

import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;



public class Networksetup {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	public static int DATACENTER_HOSTS;
        
        public static String Vmname="";
	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		//Combinacoes que funcionam
		//3000;100;3000

            int cloudletnumber = Integer.parseInt(MainCloud.jTextField1
					.getText());
			int vmnumber = Integer.parseInt(MainCloud.jTextField3.getText());
                        int dcent = Integer.parseInt(MainCloud.jTextField2.getText());
                         Vmname=MainCloud.cmbbox.getSelectedItem().toString();
            
		int numVms=vmnumber;
		int numDatacenters=dcent;
		DATACENTER_HOSTS=10;
		int numCloudlets=cloudletnumber;
        	/*		int numVms=100;
		int numDatacenters=2;
		DATACENTER_HOSTS=10;
		int numCloudlets=100;
		 */		

		/*		int numVms=30;
		int numDatacenters=1;
		DATACENTER_HOSTS=10;
		int numCloudlets=30;
		 */
         	Log.printLine("Starting TCP Network");
                try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1;   // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. 
			//We need at list one of them to run a CloudSim simulation
			Datacenter [] datacenter = new Datacenter[numDatacenters];
			int i=0;
			while (i<numDatacenters){
				datacenter[i] = createDatacenter("Datacenter_" + i);

				//Proximo datacenter 
				i=i+1;
			}//fim while

			//Third step: Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			//Fourth step: Create one virtual machine
			//
			//vmlist mantem a lista de VMs
			vmlist = new ArrayList<Vm>();

			//Cria uma lista de VMs
			Vm [] vm = new Vm[numVms]; 

			//Inicializa as variaveis para
			//VM description
			int vmid = 0;
			int mips = 250;
			long size = 10000; //image size (MB)
			int ram = 2048; //vm memory (MB)
			long bw = 1000;
			int pesNumber = 1; //number of cpus
			String vmm = Vmname; //VMM name

			i=0;
			while (i<numVms){

				vmid=i;

				//create VM
				vm[i] = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());			

				//add the VM to the vmList
				vmlist.add(vm[i]);

				//Proxima VM
				i=i+1;

			}//fim while

			//submit vm list to the broker
			broker.submitVmList(vmlist);

			//Fifth step: Create one Cloudlet
			//
			//cloudletList eh uma lista que mantem os cloudlets
			cloudletList = new ArrayList<Cloudlet>();

			Cloudlet [] cloudlet = new Cloudlet[numCloudlets];
			//Cloudlet properties
			int id = 0;
			long length = 40000;
			long fileSize = 300;
			long outputSize = 300;
			/*
			 * The UtilizationModelFull class is a simple model, according to which
			 * a Cloudlet always utilize all the available CPU capacity.
			 */
			UtilizationModel utilizationModel = new UtilizationModelFull();

			i=0;
			while (i<numCloudlets){

				id=i;
				cloudlet[i] = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
				cloudlet[i].setUserId(brokerId);

				//add the cloudlet to the list
				cloudletList.add(cloudlet[i]);

				//proximo cloudlet
				i=i+1;
			}
                        //fim while

			//submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);

			//Sixth step: configure network
			//load the network topology file
			//NetworkTopology.buildNetworkTopology("/usr/local/src/workspace/REALCloudSim-1.0/examples/org/cloudbus/cloudsim/examples/network/topology.brite");

			// Seventh step: Starts the simulation
			CloudSim.startSimulation();


			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

			printCloudletList(newList);

			//Print the debt of each user to each datacenter
			i=0;
			while (i<numDatacenters){
				datacenter[i].printDebts();
				i=i+1;
			}//fim while

			Log.printLine("TCP Network Completed!");
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	private static Datacenter createDatacenter(String name){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		//    our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 1000;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

		//4. Create Host with its id and list of PEs and add them to the list of machines
		int hostId=0;
		int ram = 2048; //host memory (MB)
		long storage = 1000000; //host storage
		int bw = 10000;

		//NetworkTopology.buildNetworkTopology("/usr/local/src/workspace/REALCloudSim-1.0/examples/org/cloudbus/cloudsim/examples/network/topology.brite");
		/*		//maps CloudSim entities to BRITE entities
		//PowerDatacenter will correspond to BRITE node 0
		int i=0;
		//Nota: Nao ocorrem erros quando atribuo um datacenter a
		//um node que nao existe na topologia
		int briteNode=0;
		while (i<numDatacenters){			   
			briteNode=i;	
			NetworkTopology.mapNode(datacenter[i].getId(),briteNode);   
			i=i+1;
		}//fim while

		//Broker ira corresponder ao proximo node
		briteNode=i;
		NetworkTopology.mapNode(broker.getId(),briteNode);
		 */		

		//Importante: para a politica de alocacao,
		//comeco com o indice 1        
		int i=1;

		int briteNode=0;
		while (i<=DATACENTER_HOSTS){

			hostId=i;
			//Mapeia o host ao no da topologia Brite
			//Faco isso na politica de alocacao
			hostList.add(
					new Host(
							hostId,
							new RamProvisionerSimple(ram),
							new BwProvisionerSimple(bw),
							storage,
							peList,
							new VmSchedulerTimeShared(peList)
					)
			); // This is our machine

			i=i+1;
			briteNode++;

		}//fim while			

		//Verificar se consigo recuperar o menor custo ao no 0 
		//(0 eh um possivel recurso alvo. Intencao de alocar proximo ao recurso)
		//
		//Tenho a topologia mapeada para os hosts (OK)
		//Tenho o calculo do delay entre quaisquer dois nós do grafo (OK)
		//- Nao funciona o padrao FloyWarshall do CloudSim
		

		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = Vmname;
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.001;	// the cost of using storage in this resource
		double costPerBw = 0.0;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now		

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicyBio(hostList), storageList, 0);
			//			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static DatacenterBroker createBroker(){

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet = null;

		db_conn db = new db_conn();

		
		
		  double totalcost=0;
	        double totCputime=0;
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		MainCloud.jTextArea1.setText("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + indent
				+ "Time" + indent + "Start Time" + indent + "Finish Time");

		MainCloud.jTextArea1.append("\n" + "Cloudlet ID" + indent + "STATUS"
				+ indent + "Data center ID" + indent + "VM ID" + indent
				+ indent + "Time" + indent + "Start Time" + indent
				+ "Finish Time");
		DecimalFormat dft = new DecimalFormat("###.##");

		  int num1[] = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000,50,150,250,350,450,550,650,750,850,950};
  Random rand = new Random();
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			// cloudlet.getcloudlet
			Log.print(indent + (cloudlet.getCloudletId()+1) + indent + indent);
			MainCloud.jTextArea1.append("\n" + indent
					+ (cloudlet.getCloudletId()+1) + indent + indent);
			// cloudlet.
			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");
				MainCloud.jTextArea1.append("SUCCESS");
				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
				MainCloud.jTextArea1.append(indent + indent
						+ cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getVmId() + indent + indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
                                db_conn db11=new db_conn();
                                
                                   int r = rand.nextInt(19);
        System.out.println(r);
        
          int r1 = rand.nextInt(19);
        System.out.println(r1);
                                int cloudletid=cloudlet.getCloudletId()+1;
String InsertQry="insert into tbl_cloud values('"+cloudletid+"','"+cloudlet.getResourceId()+"','"+cloudlet.getVmId()+"','"+dft.format(cloudlet.getActualCPUTime())+"','"+dft.format(cloudlet.getExecStartTime())+"','"+dft.format(cloudlet.getFinishTime())+"','"+num1[r]+"','"+num1[r1]+"')";
                                  System.out.println(InsertQry);
        
         try {
            
                db11.stmt.executeUpdate(InsertQry);
               //jProgressBar1.setVisible(false);
       
            } catch (SQLException ex) {
                ex.printStackTrace();
        }
                                
                        }
	}
                
                 JOptionPane.showMessageDialog(null, "Cloud Architecture Constructed Successfully");
 System.out.println("Print Process  *************");
	        System.out.println("Cloudlet History"+cloudlet.getCloudletHistory());
	        System.out.println("Cloudlet Vm id---"+cloudlet.getVmId());
	        
	        System.out.println("Cloudlet getWaitingTime---"+cloudlet.getWaitingTime());
	        System.out.println("Cloudlet TotalLength---"+cloudlet.getCloudletTotalLength());
	        System.out.println("Cloudlet UtilizationModelRam()---"+cloudlet.getUtilizationModelRam());
	        System.out.println("Cloudlet Costpercsecond---"+cloudlet.getCostPerSec());
	        System.out.println("Cloudlet UtilizationModelCpu()---"+cloudlet.getUtilizationModelCpu());
	        System.out.println("Cloudlet Wallclock time---"+cloudlet.getWallClockTime());
               // MainCloud.jTextArea1.append("\n" + "Created Of Agent and Service provider Completed");
                

	}
	
}
