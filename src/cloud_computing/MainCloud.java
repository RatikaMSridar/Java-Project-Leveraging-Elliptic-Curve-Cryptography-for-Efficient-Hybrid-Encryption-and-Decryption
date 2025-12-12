package cloud_computing;

/****************************************************************/
/*                      MainCloud	                            */
/*                                                              */
/****************************************************************/
import commoncodes.db_conn;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.imageio.ImageIO;
import javax.swing.*;

//import ProgressBarStep.BarThread;
/**
 * Summary description for MainCloud
 *
 */
public class MainCloud extends JFrame
{
	// Variables declaration
	private JLabel jLabel1;
	private JLabel jLabel2;
	private JLabel jLabel3;
	private JLabel jLabel4;
	private JLabel jLabel5;
	public static JTextField jTextField1;
	public static JTextField jTextField2;
	public static JTextField jTextField3;
        public static JComboBox cmbbox;
        
	static JTextArea jTextArea1;
	private JScrollPane jScrollPane1;
	private JProgressBar jProgressBar1;
	private JButton jButton1,jButton2;
	private JPanel contentPane;
	static String value="";

	final static int interval = 1000;int i;

	Timer timer;
	// End of variables declaration


	public MainCloud()
	{
		super();
		initializeComponent();
		//
		// TODO: Add any constructor code after initializeComponent call
		//

		this.setVisible(true);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always regenerated
	 * by the Windows Form Designer. Otherwise, retrieving design might not work properly.
	 * Tip: If you must revise this method, please backup this GUI file for JFrameBuilder
	 * to retrieve your design properly in future, before revising this method.
	 */
	private void initializeComponent()
	{
		
		jLabel1 = new JLabel();
		jLabel2 = new JLabel();
		jLabel3 = new JLabel();
		jLabel4 = new JLabel();
		jLabel5 =new JLabel("Select Virtualization  Setup");
		jTextField1 = new JTextField();
		jTextField2 = new JTextField();
		jTextField3 = new JTextField();
                
                cmbbox =new JComboBox();
                cmbbox.addItem("Xen 4.0");
                cmbbox.addItem("Open VZ ");
                cmbbox.addItem("KVM 0.12.5");
                        
		jTextArea1 = new JTextArea();
		jScrollPane1 = new JScrollPane();
		jProgressBar1 = new JProgressBar();
		jButton1 = new JButton();
                
                jButton2 =new JButton();
		contentPane = (JPanel)this.getContentPane();

		//
		// jLabel1
		//
		jLabel1.setText(" No.Of.Cloudlet");
		//
		try{
		
//		BufferedImage myPicture = ImageIO.read(new File("love YOU IMAGE 6.gif"));
	// jLabel5 = new JLabel(new ImageIcon( myPicture ));
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		// jLabel2
		//
		jLabel2.setText(" No.Of.Datacenter");
		//
		// jLabel3
		//
		jLabel3.setText(" No.Of.Virtual Machine");
		//
		// jLabel4
		//
		jLabel4.setText("Optimized Cloud Security: Leveraging Elliptic Curve Cryptography for  Efficient Hybrid Encryption  ");
		//
		// jTextField1
		//
		 ActionListener actionListener = new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		    	  jButton1.setEnabled(false);
		        Thread stepper = new BarThread(jProgressBar1);
		        stepper.start();
		      }
		    };

		    jButton1.addActionListener(actionListener);
		jTextField1.setText("");
		
		//
		// jTextField2
		//
		jTextField2.setText("");
		
		//
		// jTextField3
		//
		jTextField3.setText("");
		
		jTextArea1.setText("");
		//
		// jScrollPane1
		//
		jScrollPane1.setViewportView(jTextArea1);
		
		jProgressBar1.setForeground(new Color(72, 128, 0));
		 jProgressBar1 = new JProgressBar(0, 20);
	        jProgressBar1.setValue(0);
	        jProgressBar1.setStringPainted(true);
	        
	              //jTextField2.setEditable(false);              
	        
	    
		jButton1.setText("Submit");
		jButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				jButton1_actionPerformed(e);
			}

		});
                
                
                jButton2.setText("Exit");
		jButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				jButton2_actionPerformed(e);
			}

		});
                
		jLabel4.setFont(new Font("Dialog", 1, 25));
		
		contentPane.setLayout(null);
		contentPane.setBackground(Color.lightGray);//ew Color(148, 148, 204));
		addComponent(contentPane, jLabel1, 102,102,106,32);//add 20 for y
		addComponent(contentPane, jLabel2, 306,102,115,23);
		addComponent(contentPane, jLabel3, 538,102,134,18);
		addComponent(contentPane, jLabel4, 10,0,950,30);
		addComponent(contentPane, jTextField1, 205,101,100,26);
		addComponent(contentPane, jTextField2, 435,101,100,25);
		addComponent(contentPane, jTextField3, 689,101,100,31);
		addComponent(contentPane, jScrollPane1, 143,264,620,287);
		addComponent(contentPane, jLabel5, 100,166,150,32);
                
                addComponent(contentPane, cmbbox, 250,166,83,32);
		addComponent(contentPane, jProgressBar1, 384,211,100,12);
		addComponent(contentPane, jButton1, 392,166,83,32);
                
                addComponent(contentPane, jButton2, 520,166,83,32);
		//
		// MainCloud
		//
		this.setTitle("Security Enhance  Homomorphic Encryption in Cloud Computing ");
		this.setLocation(new Point(0, 0));
		this.setSize(new Dimension(1000, 600));
	}

	/** Add Component Without a Layout Manager (Absolute Positioning) */
	private void addComponent(Container container,Component c,int x,int y,int width,int height)
	{
		c.setBounds(x,y,width,height);
		container.add(c);
	}
	 class BarThread extends Thread {
	    private  int DELAY = 500;

	    JProgressBar progressBar;

	    public BarThread(JProgressBar bar) {
	      progressBar = bar;
	    }

	    public void run() {
	      int minimum = progressBar.getMinimum();
	      int maximum = progressBar.getMaximum();
	      Runnable runner = new Runnable() {
	        public void run() {
	          int value = progressBar.getValue();
	          progressBar.setValue(value + 1);
	        }
	      };
	      for (int i = minimum; i < maximum; i++) {
	        try {
	          SwingUtilities.invokeAndWait(runner);
	          // our job for each step is to just sleep
	          Thread.sleep(DELAY);
	        } catch (InterruptedException ignoredException) {
	        } catch (InvocationTargetException ignoredException) {
	        }
	      }
	      jButton1.setEnabled(true);
	      
	    }
	  }
	//
	// TODO: Add any appropriate code in the following Event Handling Methods
	//
	

	

	

	private void jButton1_actionPerformed(ActionEvent e)
	{
		System.out.println("\njButton1_actionPerformed(ActionEvent e) called.");
		
                
          db_conn db11=new db_conn();
//                                
//                                
//                                
              String InsertQry="truncate table tbl_cloud ";
              System.out.println(InsertQry);
           try {
            db11.stmt.executeUpdate(InsertQry);
            
          
//            
//            //jProgressBar1.setVisible(false);
              } catch (Exception ex) {
//            //Logger.getLogger(SLAFrame.class.getName()).log(Level.SEVERE, null, ex);
//            
              ex.printStackTrace();
             }
                                
                
                
                
		    	  jButton1.setEnabled(false);
		        Thread stepper = new BarThread(jProgressBar1);
		        stepper.start();
		        String ss[]=null;
		       // Cloudmainpack.main(ss);
                        Networksetup.main(ss);
                        
                        
                        int limit=Integer.parseInt(jTextField1.getText().trim());
                        for(int ii=0;ii<limit;ii++){
                         File file1 = new File("CloudStorage\\"+LiveLogin.loginusername+"\\VirtualMachine"+ii+"\\");
	if (!file1.exists()) {
		if (file1.mkdir()) {
			System.out.println("Directory is created!");
		} else {
			System.out.println("Failed to create directory!");
		}
	        }
                        }     
                        
                        
                        
		    
	}

        
        private void jButton2_actionPerformed(ActionEvent e)
	{
            this.dispose();
        }
	//
	// TODO: Add any method code to meet your needs in the following area
	//






























 

//============================= Testing ================================//
//=                                                                    =//
//= The following main method is just for testing this class you built.=//
//= After testing,you may simply delete it.                            =//
//======================================================================//
	public static void main(String[] args)
	{
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		catch (Exception ex)
		{
			System.out.println("Failed loading L&F: ");
			System.out.println(ex);
		}
		new MainCloud();
	}
//= End of Testing =


}
