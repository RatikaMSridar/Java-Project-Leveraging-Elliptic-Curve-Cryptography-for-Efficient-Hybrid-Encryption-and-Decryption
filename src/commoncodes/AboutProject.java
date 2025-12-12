


package commoncodes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class AboutProject extends JInternalFrame implements ActionListener
{

    static JLabel label;
    static JTextArea jText;
    static JScrollPane jScroll;
    static JButton btnExit;

    static BufferedReader in = null;

    String ss = ""+System.getProperty("user.dir");
    String path = ss + "\\Project_Documents\\Abstract.txt";
    static StringBuilder  sb  = new StringBuilder();
    static String line;

    public AboutProject()
    {

        super("PROJECT  INFORMATION", true, true, true, true);
        setTitle("PROJECT  INFORMATION");
        setLayout(null);
        getContentPane().setBackground(Color.gray);

        label =new JLabel("PROJECT  INFORMATION");
        label.setFont(new Font("Monotype Corsiva",Font.ITALIC,30));
        label.setForeground(Color.ORANGE);

        jText = new JTextArea();
        jText.setBackground(Color.CYAN);
        jText.setEditable(false);

        jScroll = new JScrollPane(jText);

        btnExit = new JButton("E X I T");
        btnExit.setFont(new Font("",Font.BOLD,14));
        btnExit.setToolTipText("Exit From Here");
        btnExit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExit.setBackground(Color.green);

          label.setBounds( 180,  10, 450,  50);
        jScroll.setBounds(  40,  70, 600, 500);
        btnExit.setBounds( 275, 580, 100,  30);

        add(label);
        add(jScroll);
        add(btnExit);

        setSize(700,650);
        setVisible(true);

        btnExit.addActionListener(this);

        try
        {
            in  = new BufferedReader(new FileReader(path));
            while ( (line = in.readLine()) != null )
            {
                jText.append(line+"\n");
            }
        }

        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    public void actionPerformed(ActionEvent ae)
    {
        if (ae.getActionCommand().equals("E X I T"))
        {
             int option = JOptionPane.showConfirmDialog(null, "Would  you  like  to  Exit  from  this  Window?  Click (Yes/No)", "Confirmation  Message",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION)
            {
                setVisible(false);
            }
            else
            {
                setVisible(true);
            }

        }

    }
    
}
