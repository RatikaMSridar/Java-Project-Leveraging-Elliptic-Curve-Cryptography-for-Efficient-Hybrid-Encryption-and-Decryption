/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cloud_computing;



//import commoncodes.db_conn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.sql.*;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable.*;
import javax.swing.JTextField;
import javax.swing.UIManager;
import org.jfree.chart.plot.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.jdbc.JDBCCategoryDataset;
import  org.jfree.util.PublicCloneable;

public class PerformenceCompare_Executiontime extends JFrame {

  //  db_conn db;
    CategoryPlot p;
    JFreeChart chart;
    String[][] dbo;
    JOptionPane jop;
    JDialog dialog, dlg;
    JProgressBar progressBar;
    Vector dis = new Vector();
    Vector dis1 = new Vector();
    int a[] = new int[5];

   
    public PerformenceCompare_Executiontime() {
        int cnt = 0;
     

        try {
           
    
                Class.forName("com.mysql.jdbc.Driver");
            Connection cn =DriverManager.getConnection("jdbc:mysql://localhost:3308/tracon_second","root","root");
                Statement stat = cn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                JDBCCategoryDataset testSet = new JDBCCategoryDataset(cn);
                String qry = "SELECT Algorithmname,Executionpertask FROM tracon_second.tbl_performenceexecutiontime t;;";
               
                testSet.executeQuery(qry);

               
                chart = ChartFactory.createBarChart("AlgorithmName (ECC and RSA)", "Metrics", "ExecutionTime(ms)  ",  testSet, PlotOrientation.VERTICAL, false, true, true);
                
                ChartFrame chartFrame = new ChartFrame("AlgorithmName (ECC and RSA)", chart);

                

                CategoryPlot plot = chart.getCategoryPlot();

                
                chartFrame.pack();
                
                chartFrame.setSize(400, 500);
                {

                    plot.setBackgroundPaint(Color.white);
                    plot.setRangeGridlinePaint(Color.red);
                    CategoryItemRenderer renderer1 = plot.getRenderer();
                    renderer1.setSeriesPaint(0, Color.MAGENTA);
                    chartFrame.setLocation(20, 100);

                }
                chartFrame.setVisible(true);

                try {
                } catch (Exception e) {
                    System.out.println("Problem in creating chart.");
                    e.printStackTrace();
                }

                cn.close();
           
        } catch (Exception ee) {
            System.out.println("Problem in Connection" + ee.getStackTrace() + ee.getMessage());
            ee.printStackTrace();

        }
    }


    public static void main(String[] args) {
        System.out.println("inside");
       PerformenceCompare_Executiontime pf = new PerformenceCompare_Executiontime();
    }
}

