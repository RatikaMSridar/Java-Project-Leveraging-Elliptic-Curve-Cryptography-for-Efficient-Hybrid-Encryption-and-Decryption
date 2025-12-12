/**
 * Projeto RealCloud
 * 
 * Ultima modificacao: 25/10/2010
 * 
 * @author Lucio Agostinho Rocha
 */

package org.cloudbus.cloudsim; 

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class A_PO_BD {


	//Variaveis de instancia das consultas SQL
	//conexao com o BD
	private Connection c = null;

	//declaracao - recebe a atribuicao da conexao c
	//com o BD para realizar as declaracoes SQL
	private Statement s = null;

	//recolhe as consultas SQL
	private ResultSet rs = null;

	//Instrucoes SQL
	private String sql;

	//Objeto que faz a conexao com o BD
	private BDConexao conexaoBD;		
      
	private int lastId = 0;

	// beans attributes
	private String nodeid="";

	private String xpos="";

	private String ypos="";

	private String indegree="";

	private String outdegree="";

	private String asid="";
	
	private String type="";
	
	//Para as arestas
	private String edgeid="";
	
	private String sourcenode="";
	
	private String destinationnode="";
	
	private String length="";
	
	private String delay="";
	
	private String bandwidth="";
	
	private String asfrom="";
	
	private String asto="";
	
	private String edgetype="";
	
	//Para os servers
	private String serverId="";
	private String serverDatacenterId="";
	private String serverCapCPU="";
	private String serverCapRAM="";
	private String serverCapDisk="";
	private String serverCapBw="";
	private String serverVirtualizer="";
	private String serverCostCPU="";
	private String serverCostRAM="";
	private String serverCostDisk="";
	private String serverCostBw="";
	private String serverXPos="";
	private String serverYPos="";
	
	//Para as VMs
	private String vmId="";
	private String vmInitialServer="";
	private String vmCPUReq="";
	private String vmRAMReq="";
	private String vmDiskReq="";
	private String vmBwReq="";
	private String vmFluxoGerado="";
	private String vmVirtualizer="";
	private String vmFluxo="";
	private String vmXPos="";
	private String vmYPos="";
	
	public A_PO_BD(){

		super();
		
	}//fim construtor

	// get last Id of an auto-incremented update
	public int getLastId() {
		return lastId;
	}

	public String getNodeId() {
		return nodeid;
	}

	public String getXPos() {
		return xpos;
	}

	public String getYPos() {
		return ypos;
	}

	public String getIndegree() {
		return indegree;
	}

	public String getOutdegree() {
		return outdegree;
	}
	
	public String getASId() {
		return asid;
	}
	
	public String getType() {
		return type;
	}

	public void setNodeId(String nodeid) {
		this.nodeid = nodeid;
	}

	public void setXPos(String xpos) {
		this.xpos = xpos;
	}

	public void setYPos(String ypos) {
		this.ypos = ypos;
	}

	public void setIndegree(String indegree) {		
		this.indegree = indegree;
	}
	
	public void setOutdegree(String outdegree) {
		this.outdegree = outdegree;
	}
	
	public void setASId(String asid) {
		this.asid = asid;
	}

	public void setType(String type) {
		this.type = type;
	}	

	//Para as arestas
	public String getEdgeId() {
		return edgeid;
	}
	
	public String getSourceNode() {
		return sourcenode;
	}
	
	public String getDestinationNode() {
		return destinationnode;
	}
	
	public String getLength() {
		return length;
	}
	
	public String getDelay() {
		return delay;
	}
	
	public String getBandwidth() {
		return bandwidth;
	}
	
	public String getASFrom() {
		return asfrom;
	}
	
	public String getASTo() {
		return asto;
	}
	
	public String getEdgeType() {
		return edgetype;
	}

	public void setEdgeId(String edgeid) {
		this.edgeid=edgeid;
	}
	
	public void setSourceNode(String sourcenode) {
		this.sourcenode = sourcenode;
	}
	
	public void setDestinationNode(String destinationnode) {
		this.destinationnode = destinationnode;
	}
	
	public void setLength(String length) {
		this.length = length;
	}
	
	public void setDelay(String delay) {
		this.delay = delay;
	}
	
	public void setBandwidth(String bandwidth) {
		this.bandwidth = bandwidth;
	}
	
	public void setASFrom(String asfrom) {
		this.asfrom = asfrom;
	}
	
	public void setASTo(String asto) {
		this.asto = asto;
	}
	
	public void setEdgeType(String edgetype) {
		this.edgetype = edgetype;
	}	
	
	//Para os servers
	public String getServerId(){
		return serverId;
	}
	
	public String getServerDatacenterId(){
		return serverDatacenterId;
	}
	
	public String getServerCapCPU(){
		return serverCapCPU;
	}
	
	public String getServerCapRAM(){
		return serverCapRAM;
	}
	
	public String getServerCapDisk(){
		return serverCapDisk;
	}
	
	public String getServerCapBw(){
		return serverCapBw;
	}
	
	public String getServerVirtualizer(){
		return serverVirtualizer;
	}
	
	public String getServerCostCPU(){
		return serverCostCPU;
	}
	
	public String getServerCostRAM(){
		return serverCostRAM;
	}
	
	public String getServerCostDisk(){
		return serverCostDisk;
	}
	
	public String getServerCostBw(){
		return serverCostBw;
	}
	
	public String getServerXPos(){
		return serverXPos;
	}
	
	public String getServerYPos(){
		return serverYPos;
	}
	
	public void setServerId(String serverId){
		this.serverId = serverId;
	}
	
	public void setServerDatacenterId(String serverDatacenterId){
		this.serverDatacenterId = serverDatacenterId;
	}
	
	public void setServerCapCPU(String serverCapCPU){
		this.serverCapCPU = serverCapCPU;
	}
	
	public void setServerCapRAM(String serverCapRAM){
		this.serverCapRAM = serverCapRAM;
	}
	
	public void setServerCapDisk(String serverCapDisk){
		this.serverCapDisk = serverCapDisk;
	}
	
	public void setServerCapBw(String serverCapBw){
		this.serverCapBw = serverCapBw;
	}
	
	public void setServerVirtualizer(String serverVirtualizer){
		this.serverVirtualizer = serverVirtualizer;
	}
	
	public void setServerCostCPU(String serverCostCPU){
		this.serverCostCPU = serverCostCPU;
	}
	
	public void setServerCostRAM(String serverCostRAM){
		this.serverCostRAM = serverCostRAM;
	}
	
	public void setServerCostDisk(String serverCostDisk){
		this.serverCostDisk = serverCostDisk;
	}
	
	public void setServerCostBw(String serverCostBw){
		this.serverCostBw = serverCostBw;
	}
	
	public void setServerXPos(String serverXPos){
		this.serverXPos = serverXPos;
	}
	
	public void setServerYPos(String serverYPos){
		this.serverYPos = serverYPos;
	}
	
	//Para as VMs
	public String getVMId(){
		return vmId;
	}
	
	public String getVMInitialServer(){
		return vmInitialServer;
	}
	
	public String getVMCPUReq(){
		return vmCPUReq;
	}
	
	public String getVMRAMReq(){
		return vmRAMReq;
	}
	
	public String getVMDiskReq(){
		return vmDiskReq;
	}
	
	public String getVMBwReq(){
		return vmBwReq;
	}
	
	public String getVMFluxoGerado(){
		return vmFluxoGerado;
	}
	
	public String getVMVirtualizer(){
		return vmVirtualizer;
	}
	
	public String getVMFluxo(){
		return vmFluxo;
	}
	
	public String getVMXPos(){
		return vmXPos;
	}
	
	public String getVMYPos(){
		return vmYPos;
	}
	
	public void setVMId(String vmId){
		this.vmId = vmId;
	}
	
	public void setVMInitialServer(String vmInitialServer){
		this.vmInitialServer = vmInitialServer;
	}
	
	public void setVMCPUReq(String vmCPUReq){
		this.vmCPUReq = vmCPUReq;
	}
	
	public void setVMRAMReq(String vmRAMReq){
		this.vmRAMReq = vmRAMReq;
	}
	
	public void setVMDiskReq(String vmDiskReq){
		this.vmDiskReq = vmDiskReq;
	}
	
	public void setVMBwReq(String vmBwReq){
		this.vmBwReq = vmBwReq;
	}
	
	public void setVMFluxo(String vmFluxo){
		this.vmFluxo = vmFluxo;
	}
	
	public void setVMVirtualizer(String vmVirtualizer){
		this.vmVirtualizer = vmVirtualizer;
	}
	
	public void setVMXPos(String vmXPos){
		this.vmXPos = vmXPos;
	}
	
	public void setVMYPos(String vmYPos){
		this.vmYPos = vmYPos;
	}
	
	// store data into DB	
	public boolean insertNodesIntoDB() {

		boolean resultado = true;		

		int result = 0;		

		if ( conectarBD() ){
			
			// store data
			sql = "INSERT INTO nodes (nodeid, xpos, ypos, indegree, outdegree, asid, type) VALUES " +
					"('" + nodeid + "','" + xpos + "','" + ypos + "','" + indegree + "','" + outdegree + "','" + asid + "','" + type + "');";
			System.out.println("Query: " + sql);
			try {
				s = c.createStatement();
				result = s.executeUpdate(sql);

			} catch ( Exception ex ) {				
				ex.printStackTrace();
				resultado = false;
			}//fim catch

			//Encerra os recursos da conexao com a base de dados
			fecharRecursosBD();

		} else 			
			System.out.println("Erro ao realizar a conexao com a BD.");

		return resultado;

	}//fim insertNodesIntoDB
	
	public boolean insertEdgesIntoDB() {

		boolean resultado = true;		

		int result = 0;		

		if ( conectarBD() ){
			
			// store data
			sql = "INSERT INTO edges (edgeid, sourcenode, destinationnode, length, delay, bandwidth, asfrom, asto, edgetype) VALUES " +
					"('" + edgeid + "','" + 
					sourcenode + "','" + 
					destinationnode + "','" + 
					length + "','" + 
					delay + "','" + 
					bandwidth + "','" + 
					asfrom + "','" +
					asto + "','" +
					edgetype + "');";
			System.out.println("Query: " + sql);
			try {
				s = c.createStatement();
				result = s.executeUpdate(sql);

			} catch ( Exception ex ) {				
				ex.printStackTrace();
				resultado = false;
			}//fim catch

			//Encerra os recursos da conexao com a base de dados
			fecharRecursosBD();

		} else 			
			System.out.println("Erro ao realizar a conexao com a BD.");

		return resultado;

	}//fim insertEdgesIntoDB
	
	public boolean insertServersIntoDB() {

		boolean resultado = true;		

		int result = 0;		

		if ( conectarBD() ){
			
			// store data
			sql = "INSERT INTO servers (datacenterid, serverid, cap_cpu, cap_ram, cap_disk, cap_bw, virtualizer, cost_cpu, cost_ram, cost_disk, cost_bw, xpos, ypos) VALUES " +
					"('" + serverDatacenterId + "','" + 					
					serverId + "','" + 
					serverCapCPU + "','" + 
					serverCapRAM + "','" + 
					serverCapDisk + "','" + 
					serverCapBw + "','" + 
					serverVirtualizer + "','" +
					serverCostCPU + "','" +
					serverCostRAM + "','" +
					serverCostDisk + "','" +
					serverCostBw + "','" +
					serverXPos + "','" +
					serverYPos + "');";
			System.out.println("Query: " + sql);
			try {
				s = c.createStatement();
				result = s.executeUpdate(sql);

			} catch ( Exception ex ) {				
				ex.printStackTrace();
				resultado = false;
			}//fim catch

			//Encerra os recursos da conexao com a base de dados
			fecharRecursosBD();

		} else 			
			System.out.println("Erro ao realizar a conexao com a BD.");

		return resultado;

	}//fim insertServersIntoDB
	
	public boolean insertVMsIntoDB() {

		boolean resultado = true;		

		int result = 0;		

		if ( conectarBD() ){
			
			// store data
			sql = "INSERT INTO vms (vmid, initialserver, cpu_req, ram_req, disk_req, bw_req, fluxo_gerado, virtualizer, xpos, ypos) VALUES " +
					"('" + vmId + "','" + 
					vmInitialServer + "','" + 
					vmCPUReq + "','" + 
					vmRAMReq + "','" + 
					vmDiskReq + "','" + 
					vmBwReq + "','" + 
					vmFluxoGerado + "','" +
					vmVirtualizer + "','" +					
					vmXPos + "','" +
					vmYPos + "');";
			System.out.println("Query: " + sql);
			try {
				s = c.createStatement();
				result = s.executeUpdate(sql);

			} catch ( Exception ex ) {				
				ex.printStackTrace();
				resultado = false;
			}//fim catch

			//Encerra os recursos da conexao com a base de dados
			fecharRecursosBD();

		} else 			
			System.out.println("Erro ao realizar a conexao com a BD.");

		return resultado;

	}//fim insertVMsIntoDB

	public boolean removeAllNodesFromDB() {

		boolean resultado = true;

		int result = 0;

		if ( conectarBD() ){
			//remove data
		    sql = "DELETE FROM nodes WHERE " +
			"nodeid!='null';";			
			System.out.println("Query: " + sql);
			try {
				s = c.createStatement();
				result = s.executeUpdate(sql);

			} catch ( Exception ex ) {
				// error
				ex.printStackTrace();
				resultado = false;

			}//fim catch

			//Encerra os recursos da conexao com a base de dados
			fecharRecursosBD();

		} else 			
			System.out.println("Erro ao realizar a conexao com a BD.");

		return resultado;
	}//fim removeAllNodesFromDB	
	
	public boolean removeAllEdgesFromDB() {

		boolean resultado = true;

		int result = 0;

		if ( conectarBD() ){
			//remove data
		    sql = "DELETE FROM edges WHERE " +
			"edgeid!='null';";			
			System.out.println("Query: " + sql);
			try {
				s = c.createStatement();
				result = s.executeUpdate(sql);

			} catch ( Exception ex ) {
				// error
				ex.printStackTrace();
				resultado = false;

			}//fim catch

			//Encerra os recursos da conexao com a base de dados
			fecharRecursosBD();

		} else 			
			System.out.println("Erro ao realizar a conexao com a BD.");

		return resultado;
	}//fim removeAllEdgesFromDB
	
	public boolean removeAllServersFromDB() {

		boolean resultado = true;

		int result = 0;

		if ( conectarBD() ){
			//remove data
		    sql = "DELETE FROM servers WHERE " +
			"serverid!='null';";			
			System.out.println("Query: " + sql);
			try {
				s = c.createStatement();
				result = s.executeUpdate(sql);

			} catch ( Exception ex ) {
				// error
				ex.printStackTrace();
				resultado = false;

			}//fim catch

			//Encerra os recursos da conexao com a base de dados
			fecharRecursosBD();

		} else 			
			System.out.println("Erro ao realizar a conexao com a BD.");

		return resultado;
	}//fim removeAllServersFromDB
	
	public boolean removeAllVMsFromDB() {

		boolean resultado = true;

		int result = 0;

		if ( conectarBD() ){
			//remove data
		    sql = "DELETE FROM vms WHERE " +
			"vmid!='null';";			
			System.out.println("Query: " + sql);
			try {
				s = c.createStatement();
				result = s.executeUpdate(sql);

			} catch ( Exception ex ) {
				// error
				ex.printStackTrace();
				resultado = false;

			}//fim catch

			//Encerra os recursos da conexao com a base de dados
			fecharRecursosBD();

		} else 			
			System.out.println("Erro ao realizar a conexao com a BD.");

		return resultado;
	}//fim removeAllVMsFromDB	
	
	// update data into DB
/*	public boolean updateIntoDB() {

		boolean resultado = true;

		int result = 0;		

		if ( conectarBD() ){
			
			// store data
			sql = "UPDATE resources SET " + 
			    "name='" + name + "', " + 
			    "fqdn='" + fqdn + "', " + 
			    "description='" + description + "', " + 
			    "active='" + active + "', " + 
			    "shareable='" + shareable + "', " + 
			    "parentUrl='" + parentUrl + "', " + 
			    "publicUrl='" + publicUrl + "', " +
			    "privateUrl='" + privateUrl + "', " +
			    "privatePort='" + privatePort + "', " +
			    "owner='" + owner + "', " +
			    "labUrl='" + labUrl + "', " + 
			    "others='" + others + "' WHERE " +
			    "name='" + name + "'";
			
			System.out.println("Query: " + sql);
			try {
				s = c.createStatement();
				result = s.executeUpdate(sql);

				if(result != 1) resultado = false;

			} catch ( Exception ex ) {
				// error
				ex.printStackTrace();
				resultado = false;
			}//fim catch

			//Encerra os recursos da conexao com a base de dados
			fecharRecursosBD();

		} else 			
			System.out.println("Erro ao realizar a conexao com a BD.");

		return resultado;
		
	}//fim updateIntoDB

	// retrive data from DB
	public boolean retrieveFromDB() {

		boolean resultado = true;

		if ( conectarBD() ){

			// retrieve data
			sql = "SELECT * FROM resources WHERE ";
			
			sql = sql + "name='" + name + "'";

			System.out.println("Query: " + sql);
			try {
				int k = 1;
				s = c.createStatement();
				rs = s.executeQuery(sql);
				if(rs.next() == false)					
					resultado = false; 
				else {
										
					name = rs.getString(k++);
					fqdn = rs.getString(k++);
					description = rs.getString(k++);
					active = rs.getString(k++);
					shareable = rs.getString(k++);
					parentUrl = rs.getString(k++);
					publicUrl = rs.getString(k++);
					privateUrl = rs.getString(k++);
					privatePort = rs.getString(k++);
					owner = rs.getString(k++);
					labUrl = rs.getString(k++);
					others = rs.getString(k++);
									
				}//fim else
			} catch ( Exception ex ) {
				// error
				ex.printStackTrace();
				resultado = false;
			}//fim catch

			//Encerra os recursos da conexao com a base de dados
			fecharRecursosBD();

		} else 			
			System.out.println("Erro ao realizar a conexao com a BD.");

		return resultado;
		
	}//fim retrieveFromDB

	// remove from DB
	public boolean removeFromDB() {

		boolean resultado = true;

		int result = 0;

		if ( conectarBD() ){
			//remove data
		    sql = "DELETE FROM resources WHERE " +
			"name='" + name + "'";			
			System.out.println("Query: " + sql);
			try {
				s = c.createStatement();
				result = s.executeUpdate(sql);

				if(result != 1) resultado = false;

			} catch ( Exception ex ) {
				// error
				ex.printStackTrace();
				resultado = false;

			}//fim catch

			//Encerra os recursos da conexao com a base de dados
			fecharRecursosBD();

		} else 			
			System.out.println("Erro ao realizar a conexao com a BD.");

		return resultado;
	}//fim removeFromDB

	// retrieve according to the keys stated in the selector
	public ArrayList retrieveAll() {

		ArrayList lista = null;
		
		A_PO_BD recurso = null;

		if ( conectarBD() ){

			lista = new ArrayList();
			
			sql = "SELECT * FROM resources ORDER BY name";

			System.out.println("Query: " + sql);
			try {
				
				rs = s.executeQuery(sql);
				
				int coluna=1;
								
				while (rs.next()) {

					//Importante: COLUNAS no MySQL comecam com indice 1
					coluna=1;
					recurso = new A_PO_BD();
					
					recurso.setName((String) rs.getObject(coluna++));
					recurso.setFQDN((String) rs.getObject(coluna++));
					recurso.setDescription((String) rs.getObject(coluna++));
					recurso.setActive((String) rs.getObject(coluna++));
					recurso.setShareable((String) rs.getObject(coluna++));
					recurso.setParentUrl((String) rs.getObject(coluna++));
					recurso.setPublicUrl((String) rs.getObject(coluna++));
					recurso.setPrivateUrl((String) rs.getObject(coluna++));
					recurso.setPrivatePort((String) rs.getObject(coluna++));
					recurso.setOwner((String) rs.getObject(coluna++));
					recurso.setLabUrl((String) rs.getObject(coluna++));
					recurso.setOthers((String) rs.getObject(coluna++));
					
					lista.add(recurso);
					
				}//fim while

			} catch ( Exception ex ) {
				// error
				ex.printStackTrace();
			}//fim catch
			
			//Encerra os recursos da conexao com a base de dados
			fecharRecursosBD();

		} else 			
			System.out.println("Erro ao realizar a conexao com a BD.");
		
		return lista;
		
	}//fim retrieveAll

    public String getStatus(String currentUser){

	String resultado="";

	String REGEX="";	
	Pattern pattern;
	Matcher matcher;

                String comando = "/usr/sbin/qm list";		

		System.out.println(comando);

		try {
			Process proc = Runtime.getRuntime().exec(comando);
			proc.waitFor();
			if (proc.exitValue() != 0) {
				resultado = "Erro Comando: " + comando;
                        } else {
			    BufferedReader saida = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                            String linha;
			    StringTokenizer st;
                            resultado="";
			    String id;
			    //Adquire a primeira linha
			    if ((linha = saida.readLine()) != null ){
				resultado += linha + "\n";
				//Adquire as proximas linhas
				while ((linha = saida.readLine()) != null ){
				    
				    //Faz o parser da linha, em busca do id do usuario
				    st = new StringTokenizer(linha);
				    if (st.hasMoreTokens()) {
				    
					id = st.nextToken();
					if(existeRecursoUsuario(id,currentUser))
					    resultado += linha + "\n";
				    }//fim if
				}//fim while
			    }//fim if			
			    saida.close();
			}//fim else
		
		} catch (Exception e) {	    	 
		    resultado = "Excecao Comando: " + comando;			
		}//fim catch

		return resultado;

    }//fim getStatus

    public boolean existeRecursoUsuario(String id, String currentUser){
		
		boolean result = false;

		if ( conectarBD() ){
			
			sql = "SELECT * FROM resources WHERE " +
			    "name='" + id + "' AND " + 
			    "owner='" + currentUser + "'";

			System.out.println("Query: " + sql);
			try {
				
				rs = s.executeQuery(sql);
								
				if (rs.next()) 
				    result=true;			       

			} catch ( Exception ex ) {
				// error
				ex.printStackTrace();
			}//fim catch
			
			//Encerra os recursos da conexao com a base de dados
			fecharRecursosBD();

		} else 			
			System.out.println("Erro ao realizar a conexao com a BD.");

		return result;

    }//fim exiteRecursoUsuario
    

    public String showGridProxyInfo(){

	String resultado="";

                String comando = "/usr/local/src/workspace/RealCloud/src/Scripts/showGridProxyInfo.sh";	   

		System.out.println(comando);

		try {
			Process proc = Runtime.getRuntime().exec(comando);
			proc.waitFor();
			
			    BufferedReader saida = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                            String linha;
                            resultado="";
                            while ((linha = saida.readLine()) != null )
				resultado += linha + "\n";
                            saida.close();			

		} catch (Exception e) {	    	 
			resultado = "Excecao Comando: " + comando;			
		}//fim catch

		return resultado;

    }//fim showGridProxyInfo

    public String showProxy(){

	String resultado="";

                String comando = "/bin/cat /tmp/x509up_u1002";		

		System.out.println(comando);

		try {
			Process proc = Runtime.getRuntime().exec(comando);
			proc.waitFor();
			if (proc.exitValue() != 0) {
				resultado = "Erro: Proxy nao iniciado.";
                        } else {
			    BufferedReader saida = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                            String linha;
                            resultado="";
                            while ((linha = saida.readLine()) != null )
				resultado += linha + "\n";
                            saida.close();
			}//fim else

		} catch (Exception e) {	    	 
			resultado = "Excecao Comando: " + comando;			
		}//fim catch

		return resultado;

    }//fim showProxy

    public String iniciarProxyGrade(){

	String resultado="0";

	//Inicia o proxy para o usuario
	String comando = "/usr/local/src/workspace/RealCloud/src/Scripts/iniciarProxy.sh";

	//System.out.println(comando);

	try {
	    Process proc = Runtime.getRuntime().exec(comando);
	    proc.waitFor();
	    if (proc.exitValue() != 0) 
		resultado = "Erro Comando: " + comando;

	} catch (Exception e) {     
	    resultado = "Excecao Comando: " + comando;
	}//fim catch

	return resultado;

    }//fim iniciarProxyGrade
*/
    public boolean conectarBD(){

		conexaoBD = new BDConexao();

		boolean conectou = conexaoBD.conectarBD();
		//Se conectou, adquire as referencias
		if (conectou){
			c = conexaoBD.getC();
			s = conexaoBD.getS();
			rs = conexaoBD.getRS();
		}//fim if

		return conectou;

	}//fim conectarBD

	public void fecharRecursosBD(){

		conexaoBD.fecharConexaoBD(c,s,rs);

	}//fecharRecursosBD

	public String exibirErrosSQL(SQLException e) {

		return "\nSQLException: " + e.getMessage() + 
		"\nSQLState:     " + e.getSQLState() +
		"\nVendorError:  " + e.getErrorCode();

	}//fim exibirErrosSQL

	public String tratamentoExcecao( int numeroExcecao, SQLException e ){

		String descricaoExcecao = "";

		switch ( numeroExcecao ){

		case 1:
			descricaoExcecao = "Nao foi possivel inserir a configuracao na BD.";
			break;

		case 2:
			descricaoExcecao = "Nao foi possivel remover a configuracao da BD.";
			break;

		case 3:
			descricaoExcecao = "Nao foi possivel recolher a configuracao da BD.";
			break;

		case 4:
			descricaoExcecao = "Nao foi possivel consultar o registro da BD.";
			break;		      

		case 5:
			descricaoExcecao = "Nao foi possivelatualizar o registro na BD.";
			break;		  		  

		case 6:
			descricaoExcecao = "Erro no thread de atualizacao.";
			break;

		case 7:
			descricaoExcecao = "Nao foi possivel ler todos os registros recolhidos";
			break;

		}//fim switch

		String mensagemExcecao = "Excecao " + numeroExcecao + ": " + descricaoExcecao + "\n" + exibirErrosSQL(e); 

		return mensagemExcecao;

	}//fim tratamentoExcecao

}//fim classe
