/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package commoncodes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.ResultSet;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author User
 */
public class tableview1 {
    
    db_conn db = new db_conn();
    public void table(final String query, final String title)
    {
         Thread tt = new Thread(new Runnable() {
            public void run() {
        try
        {
             Vector columnNames = new Vector();
            Vector data = new Vector();
           JFrame jf = new JFrame(title);
            ResultSet rs1 =db.stmt21.executeQuery(query);
            java.sql.ResultSetMetaData rsm = rs1.getMetaData();
            int cols = rsm.getColumnCount();
            for (int h = 1; h <= cols; h++)
            {
                columnNames.addElement(rsm.getColumnName(h));
            }
            while (rs1.next()) {
                Vector rows = new Vector(cols);
                for (int h = 1; h <= cols; h++)
                {
                    rows.addElement(rs1.getString(h));
                }
                data.addElement(rows);
            }
            if (data.size() > 0) {
                DefaultTableModel dtm = new DefaultTableModel(data, columnNames);
              
                JTable jt = new JTable(dtm) {
                    public boolean isCellEditable(int rowIndex, int colIndex) {
                        return false;
                    }
                };
                jt.setRowSelectionAllowed(false);
                jt.setMaximumSize(new Dimension(150, 150));
                
                jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                jt.getColumnModel().getColumn(0).getResizable();

                int vColIndex = 1;
                TableColumn colu = jt.getColumnModel().getColumn(vColIndex);
                int width = 50;
               // colu.setPreferredWidth(width);
                colu.sizeWidthToFit();


                SetRowHight(jt);
                jt.setColumnSelectionAllowed(false);
                
                Dimension dim = new Dimension(550, 550);
                jt.setPreferredScrollableViewportSize(dim);
                JScrollPane jsp = new JScrollPane(jt, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

                Font font1 = new Font("TimesRoman", Font.BOLD, 16);
                jt.setFont(font1);
                jt.setForeground(Color.darkGray);
                 jt.setBackground(Color.white);
                //jp.add(jsp);
                jsp.setBounds(150, 130, 700, 500);
                jf.add(jsp);
                jf.setSize(700, 600);
                jf.setVisible(true);
                jf.setLocation(200, 80);

            } else
            {
                JOptionPane.showMessageDialog(null, " No Records Found...... ");
            }
        }
        catch(Exception ee)
        {
            ee.printStackTrace();
        }
          }
        });
        tt.start();
    }
     public void SetRowHight(JTable jt)
     {
        int height = jt.getRowHeight();
        jt.setRowHeight(height + 15);
     }
}
