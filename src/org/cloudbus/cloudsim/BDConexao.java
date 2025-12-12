package org.cloudbus.cloudsim;

import java.sql.*;

/**
 * Projeto RealCloud
 *
 * Autor: Lucio Agostinho Rocha 
 * Ultima atualizacao: 10/02/2010
 * 
 * Classe: ConexaoBD.java
 * Descricao: Classe que permite a conexao com a base de dados do projeto.
 * Nota: Podem ser abertas duas ou mais conexoes ao banco de dados desde que
 * eles sejam corretamente finalizadas.
 */

public class BDConexao {

	//conexao com o BD
	private Connection c;  

	//declaracao - recebe a atribuicao da conexao c
	//com o BD para realizar as declaracoes SQL
	private Statement s = null;

	//recolhe as consultas SQL
	private ResultSet rs = null;

	//URL (Uniform Resource Locator) da conta
	private String host = "localhost";
	private String db = "modeloRealcloud";
	private String conta = "jdbc:mysql://" + host + "/" + db + "?user=root&password=root"; 

	public BDConexao() {
		try {
			//Class.forName dinamicamente carrega uma classe Java
			//em tempo de execucao
			//
			//Aqui eh feito um teste para saber se o driver JDBC
			//foi corretamente instanciado
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception  e) {
			//System.err.println("\nNao foi possivel encontrar e carregar o driver.");
		}
	}

	public boolean conectarBD() {

		boolean conectou = true;

		try {
			//DriverManager eh o responsavel por gerenciar
			//os drivers JDBC
			//
			//Aqui ocorre uma tentativa de conexao com a base de dados
			c = DriverManager.getConnection(conta);

			//Atribui a declaracao (necessaria para consultas SQL) ah partir da conexao
			s = c.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			//Inicio o objeto que contem o resultado das consultas
			//Mesmo iniciando o objeto rs junto com as variaveis de instancia,
			//uma vez que 's' eh estabelecida, o objeto 'rs' tambem precisa ser novamente
			//inicializado (Faz sentido pq 'rs' e 's', antes, nao tinham nenhum vinculo;
			//agora eles tem
			rs = null;

		}
		catch(SQLException e) {
			conectou = false;
		}

		return conectou;
	}

	public Connection getC(){ return c; }
	public Statement getS(){ return s; }
	public ResultSet getRS(){ return rs; }

	public void fecharConexaoBD(Connection cRecebido,
			Statement sRecebido,
			ResultSet rsRecebido ) {

		c = cRecebido;
		s = sRecebido;
		rs = rsRecebido;

		//Fecha a conexao com o banco de dados
		try { if (c!=null) c.close (); }
		catch (SQLException e) { System.out.println(tratamentoExcecao(1,e)); }
		//Terminadas as alteracoes, vamos liberar os recursos da base de dados
		try { if (s!=null) s.close (); }
		catch (SQLException e) { System.out.println(tratamentoExcecao(2,e)); }
		try { if (rs!=null) rs.close (); }
		catch (SQLException e) { System.out.println(tratamentoExcecao(3,e)); }

	}//fim fecharConexaoBD

	public String exibirErrosSQL(SQLException e) {

		return "\nSQLException: " + e.getMessage() + 
		"\nSQLState:     " + e.getSQLState() +
		"\nVendorError:  " + e.getErrorCode();

	}//fim exibirErrosSQL	

	public String tratamentoExcecao( int numeroExcecao, SQLException e ){

		String descricaoExcecao="";

		switch ( numeroExcecao ){

		case 1:
			descricaoExcecao = "Erro ao fechar a conexao com o BD.";
			break;

		case 2:
			descricaoExcecao = "Erro ao fechar a declaracao do tipo de conexao com o BD.";
			break;

		case 3:
			descricaoExcecao = "Erro ao fechar o resultado da consulta com o BD.";
			break;

		}//fim switch

		String mensagemExcecao = "Excecao " + numeroExcecao + ": " + descricaoExcecao + "\n" + exibirErrosSQL(e); 

		return mensagemExcecao;

	}//fim tratamentoExcecao

}//fim classe
