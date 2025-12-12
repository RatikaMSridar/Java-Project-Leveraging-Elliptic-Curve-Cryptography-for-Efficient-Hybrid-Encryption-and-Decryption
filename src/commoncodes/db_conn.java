



package commoncodes;
import java.sql.*;

public class db_conn 
{   public Connection con = null;
    
    public Statement st = null;
    public Statement stmt = null;
    public Statement stmt1 = null;
    public Statement stmt2 = null;
    public Statement stmt3 = null;
    public Statement stmt4 = null;
    public Statement stmt5 = null;
    public Statement stmt6 = null;
    public Statement stmt7 = null;
    public Statement stmt8 = null;
    public Statement stmt9 = null;
    public Statement stmt10 = null;
    
    public Statement stmt11 = null;
    public Statement stmt12 = null;
    public Statement stmt13 = null;
    public Statement stmt14 = null;
    public Statement stmt15 = null;
    public Statement stmt16 = null;
    public Statement stmt17 = null;
    public Statement stmt18 = null;
    public Statement stmt19 = null;
    public Statement stmt20 = null;
    public Statement stmt21 = null;
    public db_conn() 
    {
        try
        {
          
            Class.forName("com.mysql.jdbc.Driver");
           con =DriverManager.getConnection("jdbc:mysql://localhost:3308/tracon_second","root","root");
              st  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
              stmt  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             stmt1  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             stmt2  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             stmt3  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             stmt4  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             stmt5  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             stmt6  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             stmt7  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             stmt8  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             stmt9  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt10  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            
            stmt11  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt12  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt13  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt14  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt15  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt16  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt17  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt18  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt19  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt20  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             stmt21  =  con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            
        } 
        
        catch (Exception e) 
        {
            System.out.println("Error in database_conn.java " + e);
        }
        
        
    }
    public static void main(String []args){
        db_conn db=new db_conn();
        System.out.println("connected " );
        }
}
