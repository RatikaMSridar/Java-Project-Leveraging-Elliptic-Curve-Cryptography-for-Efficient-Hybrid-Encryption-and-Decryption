/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package EXISTING;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import javax.swing.JFileChooser;
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import commoncodes.db_conn;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class TASKCREATION extends javax.swing.JFrame {

    String str = "";
    public static String sd = "";
    public static String finame = "";
    public static String filepath = "";
    public static String filetype = "";
    public static String Signature="";
    private String algo;
    private File file;
    String username = "";
    db_conn db = new db_conn();
    
    public static String encfilename="";
    String RBFHA = cloud_computing.LiveLogin.RBFHA;//"DES/ECB/PKCS5Padding";
     public static Vector vencrypt=new Vector();
    
    public static Calendar cal = Calendar.getInstance();

    public TASKCREATION() {
        initComponents();

        username = cloud_computing.LiveLogin.txtUserName.getText().toString().trim();
    }

    public TASKCREATION(String algo, String path) {
        this.algo = algo; //setting algo
        this.file = new File(path); //settong file
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        cmbtasktype = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(255, 255, 0)));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("****   APPLICATION CREATION  ****");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(201, 201, 201)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(103, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(24, 24, 24))
        );

        jPanel2.setBackground(new java.awt.Color(204, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 4, 4, 4, new java.awt.Color(255, 255, 255)));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel2.setText("Select Application Type");

        cmbtasktype.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TEXTFILE", "PDF" }));

        jButton1.setText("AES and RSA  Encryption ");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("PROCESS");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("EXIT");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jProgressBar1.setIndeterminate(true);

        jButton4.setText("Decryption");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(242, 242, 242)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(102, 102, 102)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(67, 67, 67))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(81, 81, 81)
                                .addComponent(jButton2)
                                .addGap(96, 96, 96)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jButton1)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(cmbtasktype, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(116, 116, 116))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addGap(146, 146, 146))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cmbtasktype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(46, 46, 46)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGap(30, 30, 30)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton3)
                .addGap(34, 34, 34))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(74, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void encrypt() throws Exception {
        //opening streams

        String Encryption = "CloudStorage\\" + username + "\\" + "UploadFile\\";
        String filepath = file.getAbsolutePath();
         System.out.println(filepath);
        FileInputStream fis = new FileInputStream(file);
        
       

        //D:\2014\cloudsimFirstModule1\cloudsimFirstModule\CloudStorage\senthilkumar\FILES\RecommendationFramework.pdf.pdf

        filepath = filepath.replace("FILES", "VirtualMachine0");
        file = new File(filepath + ".enc");
        
        
        encfilename=filepath + ".enc";
        FileOutputStream fos = new FileOutputStream(file);
        //generating key
        byte k[] = "HignDlPs".getBytes();
        SecretKeySpec key = new SecretKeySpec(k, algo.split("/")[0]);
        //creating and initialising cipher and cipher streams
        Cipher encrypt = Cipher.getInstance(algo);
        
           System.out.println("Secret Key  "+k); 
        encrypt.init(Cipher.ENCRYPT_MODE, key);
        CipherOutputStream cout = new CipherOutputStream(fos, encrypt);

        byte[] buf = new byte[1024];
        int read;
        while ((read = fis.read(buf)) != -1) //reading data
        {
            cout.write(buf, 0, read);  //writing encrypted data
        }         //closing streams
        fis.close();
        cout.flush();
        cout.close();
//        
//         String fileName1 = file.getName();
//
//				        String ip ="192.168.1.4";  //f$\\Murugesan\\IRM\\IRMsystem
//				        //database_conn con=new database_conn();
//				        try {
//				           // ResultSet rs = db.stat5.executeQuery("select count(*) as ip from tblclients where ipaddress='" + ip + "'");
//				           // rs.next();
//				           // int aaa = rs.getInt("ip");
//				           
//				            
//				                String  targetFileName1 ="enc.txt";
//				                
//				            
//				                 String   targetFileName = "\\\\" + ip + "\\cloud\\" + targetFileName1;
//				            
//				                FileInputStream in = null;
//				                FileOutputStream outt = null;
//				                
//				                    in = new FileInputStream(fileName1);
//				                    outt = new FileOutputStream(targetFileName);
//				                    int c;
//				                    while ((c = in.read()) != -1) {
//				                        outt.write(c);
//				                    }
//                                                    
//                                        }catch(Exception exp)
//                                        {
//                                            exp.printStackTrace();
//                                        }    
                                                


    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed




        // print current time
        System.out.println(" Start time is : " + cal.getTime());
        filetype = cmbtasktype.getSelectedItem().toString();

        double starttime = System.nanoTime();
        if (cmbtasktype.getSelectedItem().toString().equalsIgnoreCase("PDF")) {

            try {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("."));
                //fc.addChoosableFileFilter(new OnlyExt());
                fc.setAcceptAllFileFilterUsed(false);

                int returnval = fc.showOpenDialog(null);
                if (returnval == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    sd = file.getPath();
                    finame = file.getName();

                    System.out.println(sd);
                    System.out.println();
                }



                PdfReader reader = new PdfReader(sd);//"D:\\M.E\\Saranya\\entity.pdf");
                int n = reader.getNumberOfPages();
                Rectangle psize = reader.getPageSize(1);
                Document document = new Document(psize);

                PdfReader reader1 = new PdfReader(sd);
                int n1 = reader1.getNumberOfPages();



                for (int i = 1; i <= n1; i++) {

                    System.out.println("Page  Number==>" + i);
                    String str = PdfTextExtractor.getTextFromPage(reader1, i);
                    System.out.println(str);
                }
//Pdf writeing


                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("CloudStorage\\" + username + "\\" + "FILES\\" + finame + ".pdf"));
                document.open();
                for (int i = 1; i <= n1; i++) {
                    PdfContentByte pdf = writer.getDirectContent();
                    document.newPage();

                    PdfImportedPage page = writer.getImportedPage(reader, i);

                    pdf.addTemplate(page, .5f, 0, 0, .5f, 60, 120);
                }
                document.close();

                ////
                double kilobytes = 0;
                File file1 = new File(sd);
                if (file1.exists()) {

                    double bytes = file1.length();
                    kilobytes = (bytes / 1024);
                    double megabytes = (kilobytes / 1024);
                    double gigabytes = (megabytes / 1024);
                    double terabytes = (gigabytes / 1024);
                    double petabytes = (terabytes / 1024);
                    double exabytes = (petabytes / 1024);
                    double zettabytes = (exabytes / 1024);
                    double yottabytes = (zettabytes / 1024);

                    System.out.println("bytes : " + bytes);
                    System.out.println("kilobytes : " + kilobytes);
                    System.out.println("megabytes : " + megabytes);
                    System.out.println("gigabytes : " + gigabytes);
                    System.out.println("terabytes : " + terabytes);
                    System.out.println("petabytes : " + petabytes);
                    System.out.println("exabytes : " + exabytes);
                    System.out.println("zettabytes : " + zettabytes);
                    System.out.println("yottabytes : " + yottabytes);
                }

                ////

                String[] clus = new String[20];

                int j = 1;
                int k = 0;
                String st = "";

                String extension = ".pdf";


                double Endtime = System.nanoTime();
                double ExeTime = Endtime - starttime;
                db.stmt11.executeUpdate("update jobs set FileName='" + finame + "',StartTime='" + starttime + "',EndTime='" + Endtime + "',JobMemory='" + kilobytes + "',ExecutionTime='" + ExeTime + "',Extension='" + extension + "'  where Type='" + filetype + "'");

                String insertqry = "insert into jobsnew(Type,FileName,StartTime,EndTime,Extension,JobMemory,ExecutionTime )  values('" + filetype + "','" + finame + "','" + starttime + "','" + Endtime + "','" + extension + "','" + kilobytes + "','" + ExeTime + "' )  ";
                db.stmt12.executeUpdate(insertqry);


               


            } catch (Exception de) {

                de.printStackTrace();
            }


        } else if (cmbtasktype.getSelectedItem().toString().equalsIgnoreCase("IMAGE")) {
            BufferedImage image = null;
            try {


                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("."));
                //fc.addChoosableFileFilter(new OnlyExt());
                fc.setAcceptAllFileFilterUsed(false);

                int returnval = fc.showOpenDialog(null);
                if (returnval == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    sd = file.getPath();
                    finame = file.getName();

                    System.out.println(sd);
                    System.out.println();
                }


                //you can either use URL or File for reading image using ImageIO
                File imagefile = new File(sd);//"C:\\Cloudspace\\C1.jpg");
                image = ImageIO.read(imagefile);

                //ImageIO Image write Example in Java
                ImageIO.write(image, "jpg", new File("CloudStorage\\" + username + "\\" + "FILES\\" + finame + ".jpg"));
                // ImageIO.write(image, "bmp",new File("C:\\credit_card_loan.bmp"));
                // ImageIO.write(image, "gif",new File("C:\\personal_loan.gif"));
                // ImageIO.write(image, "png",new File("C:\\auto_loan.png"));


                ////////////
                double kilobytes = 0;
                File file1 = new File(sd);
                if (file1.exists()) {

                    double bytes = file1.length();
                    kilobytes = (bytes / 1024);
                    double megabytes = (kilobytes / 1024);
                    double gigabytes = (megabytes / 1024);
                    double terabytes = (gigabytes / 1024);
                    double petabytes = (terabytes / 1024);
                    double exabytes = (petabytes / 1024);
                    double zettabytes = (exabytes / 1024);
                    double yottabytes = (zettabytes / 1024);

                    System.out.println("bytes : " + bytes);
                    System.out.println("kilobytes : " + kilobytes);
                    System.out.println("megabytes : " + megabytes);
                    System.out.println("gigabytes : " + gigabytes);
                    System.out.println("terabytes : " + terabytes);
                    System.out.println("petabytes : " + petabytes);
                    System.out.println("exabytes : " + exabytes);
                    System.out.println("zettabytes : " + zettabytes);
                    System.out.println("yottabytes : " + yottabytes);
                }

                ////

                String[] clus = new String[20];

                int j = 1;
                int k = 0;
                String st = "";

                String extension = ".jpg";


                double Endtime = System.nanoTime();
                double ExeTime = Endtime - starttime;
                try {
                    db.stmt11.executeUpdate("update jobs set FileName='" + finame + "',StartTime='" + starttime + "',EndTime='" + Endtime + "',JobMemory='" + kilobytes + "',ExecutionTime='" + ExeTime + "',Extension='" + extension + "'  where Type='" + filetype + "'");
                    String insertqry = "insert into jobsnew(Type,FileName,StartTime,EndTime,Extension,JobMemory,ExecutionTime )  values('" + filetype + "','" + finame + "','" + starttime + "','" + Endtime + "','" + extension + "','" + kilobytes + "','" + ExeTime + "' )  ";
                    db.stmt12.executeUpdate(insertqry);
                    ///////////
                } catch (SQLException ex) {
                    Logger.getLogger(TASKCREATION.class.getName()).log(Level.SEVERE, null, ex);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Success");



        } else {
            // String sd="";
            // String finame="";

            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("."));
            //fc.addChoosableFileFilter(new OnlyExt());
            fc.setAcceptAllFileFilterUsed(false);

            int returnval = fc.showOpenDialog(null);
            if (returnval == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                sd = file.getPath();
                finame = file.getName();

                System.out.println(sd);
                System.out.println();
            }
            try {

                String str1 = "";

                BufferedReader br = new BufferedReader(new FileReader(sd));
                String attributeword = "";
                while ((str = br.readLine()) != null) {
                    System.out.println(str);

                    str1 = str1 + str + "\n";               //j++;

                }
                System.out.println("===>" + str1);

                FileWriter fileWriter = new FileWriter("CloudStorage\\" + username + "\\" + "FILES\\" + finame+".txt");

                BufferedWriter bufferedWriter =
                        new BufferedWriter(fileWriter);

                bufferedWriter.write(str1);

                bufferedWriter.close();


                //////////////////
                double kilobytes = 0;
                File file1 = new File(sd);
                if (file1.exists()) {

                    double bytes = file1.length();
                    kilobytes = (bytes / 1024);
                    double megabytes = (kilobytes / 1024);
                    double gigabytes = (megabytes / 1024);
                    double terabytes = (gigabytes / 1024);
                    double petabytes = (terabytes / 1024);
                    double exabytes = (petabytes / 1024);
                    double zettabytes = (exabytes / 1024);
                    double yottabytes = (zettabytes / 1024);

                    System.out.println("bytes : " + bytes);
                    System.out.println("kilobytes : " + kilobytes);
                    System.out.println("megabytes : " + megabytes);
                    System.out.println("gigabytes : " + gigabytes);
                    System.out.println("terabytes : " + terabytes);
                    System.out.println("petabytes : " + petabytes);
                    System.out.println("exabytes : " + exabytes);
                    System.out.println("zettabytes : " + zettabytes);
                    System.out.println("yottabytes : " + yottabytes);
                }

                ////

                String[] clus = new String[20];

                int j = 1;
                int k = 0;
                String st = "";

                String extension = ".txt";


                double Endtime = System.nanoTime();
                double ExeTime = Endtime - starttime;
                db.stmt11.executeUpdate("update jobs set FileName='" + finame + "',StartTime='" + starttime + "',EndTime='" + Endtime + "',JobMemory='" + kilobytes + "',ExecutionTime='" + ExeTime + "',Extension='" + extension + "'  where Type='" + filetype + "'");
                String insertqry = "insert into jobsnew(Type,FileName,StartTime,EndTime,Extension,JobMemory,ExecutionTime )  values('" + filetype + "','" + finame + "','" + starttime + "','" + Endtime + "','" + extension + "','" + kilobytes + "','" + ExeTime + "' )  ";
                db.stmt12.executeUpdate(insertqry);
                /////////////////
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }


        try {
            Calendar cal2 = Calendar.getInstance();

            long milliSec1 = cal2.getTimeInMillis();

            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();

            }

            Calendar cal = Calendar.getInstance();
            long milliSec2 = cal.getTimeInMillis();

            long timeDifInMilliSec;
            if (milliSec1 >= milliSec2) {
                timeDifInMilliSec = milliSec1 - milliSec2;
            } else {
                timeDifInMilliSec = milliSec2 - milliSec1;
            }

            long timeDifSeconds = timeDifInMilliSec / 1000;
            long timeDifMinutes = timeDifInMilliSec / (60 * 1000);
            long timeDifHours = timeDifInMilliSec / (60 * 60 * 1000);
            long timeDifDays = timeDifInMilliSec / (24 * 60 * 60 * 1000);





            System.out.println(timeDifInMilliSec);
        } catch (Exception expp) {
            expp.printStackTrace();
        }


        JOptionPane.showMessageDialog(null, "Task Created Successfully");
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:

        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed


        try {
            
           if (cmbtasktype.getSelectedItem().toString().equalsIgnoreCase("PDF")) {
               new TASKCREATION(RBFHA, "CloudStorage\\" + username + "\\FILES\\" + finame+".pdf" ).encrypt();

            filepath = "CloudStorage\\" + username + "\\FILES\\" + finame + ".txt";

            try {
                Calendar cal2 = Calendar.getInstance();

                long milliSec1 = cal2.getTimeInMillis();

                try {
                    Thread.sleep(7000);
                } catch (Exception e) {
                    e.printStackTrace();

                }
                long milliSec2 = cal.getTimeInMillis();

                long timeDifInMilliSec;
                if (milliSec1 >= milliSec2) {
                    timeDifInMilliSec = milliSec1 - milliSec2;
                } else {
                    timeDifInMilliSec = milliSec2 - milliSec1;
                }

                long timeDifSeconds = timeDifInMilliSec / 1000;
                long timeDifMinutes = timeDifInMilliSec / (60 * 1000);
                long timeDifHours = timeDifInMilliSec / (60 * 60 * 1000);
                long timeDifDays = timeDifInMilliSec / (24 * 60 * 60 * 1000);

                File file1 = new File("CloudStorage\\" + username + "\\FILES\\" + finame + ".txt");
                double megabytes = 0;
                if (file1.exists()) {

                    double bytes = file1.length();
                    double kilobytes = (bytes / 1024);
                    megabytes = (kilobytes / 1024);

                }







                System.out.println("megabytes+++" + megabytes);
                System.out.println("timeDifSeconds" + timeDifSeconds);
                
                    String insert_qry1="insert into tbl_performenceexecutiontime(Algorithmname,Executionpertask) values('AES and RSA','" + timeDifSeconds + "')";
                   // double throughput = megabytes / timeDifSeconds;
                    //double throughput=megabytes/timeDifInMilliSec;
                    db.stmt13.executeUpdate(insert_qry1);
                
                double throughput = megabytes / timeDifSeconds;
                //double throughput=megabytes/timeDifInMilliSec;

                String insert_Qry = "insert into tbl_performencecomparision(Algorithmname,Throughput) values( 'AES and RSA','" + throughput + "')";
                System.out.println("Time differences expressed in various units are given below");

                //  db.stmt12.executeUpdate(insert_Qry);
                System.out.println(timeDifInMilliSec + " Milliseconds");
                System.out.println(timeDifSeconds + " Seconds");
                System.out.println(timeDifMinutes + " Minutes");
                System.out.println(timeDifHours + " Hours");
                System.out.println(timeDifDays + " Days");

            } catch (Exception ex) {
                //Logger.getLogger(ENCRYPTION.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }

homomorphic_AES();
            JOptionPane.showMessageDialog(null, "TASK Encryption Completed");
           }else{
            new TASKCREATION(RBFHA, "CloudStorage\\" + username + "\\FILES\\" + finame + ".txt").encrypt();

            filepath = "CloudStorage\\" + username + "\\FILES\\" + finame + ".txt";

            try {
                Calendar cal2 = Calendar.getInstance();

                long milliSec1 = cal2.getTimeInMillis();

                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();

                }
                long milliSec2 = cal.getTimeInMillis();

                long timeDifInMilliSec;
                if (milliSec1 >= milliSec2) {
                    timeDifInMilliSec = milliSec1 - milliSec2;
                } else {
                    timeDifInMilliSec = milliSec2 - milliSec1;
                }

                long timeDifSeconds = timeDifInMilliSec / 1000;
                long timeDifMinutes = timeDifInMilliSec / (60 * 1000);
                long timeDifHours = timeDifInMilliSec / (60 * 60 * 1000);
                long timeDifDays = timeDifInMilliSec / (24 * 60 * 60 * 1000);

                File file1 = new File("CloudStorage\\" + username + "\\FILES\\" + finame + ".txt");
                double megabytes = 0;
                if (file1.exists()) {

                    double bytes = file1.length();
                    double kilobytes = (bytes / 1024);
                    megabytes = (kilobytes / 1024);

                }







                System.out.println("megabytes+++" + megabytes);
                System.out.println("timeDifSeconds" + timeDifSeconds);
                
                String insert_qry1="insert into tbl_performenceexecutiontime(Algorithmname,Executionpertask) values('ECC and AES','" + timeDifSeconds + "')";
                   // double throughput = megabytes / timeDifSeconds;
                    //double throughput=megabytes/timeDifInMilliSec;
                    db.stmt13.executeUpdate(insert_qry1);
                
                double throughput = megabytes / timeDifSeconds;
                //double throughput=megabytes/timeDifInMilliSec;

                String insert_Qry = "insert into tbl_performencecomparision(Algorithmname,Throughput) values( 'ECC and AES','" + throughput + "')";
                System.out.println("Time differences expressed in various units are given below");

                 db.stmt12.executeUpdate(insert_Qry);
                System.out.println(timeDifInMilliSec + " Milliseconds");
                System.out.println(timeDifSeconds + " Seconds");
                System.out.println(timeDifMinutes + " Minutes");
                System.out.println(timeDifHours + " Hours");
                System.out.println(timeDifDays + " Days");

            } catch (Exception ex) {
                //Logger.getLogger(ENCRYPTION.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }

homomorphic_AES();
            JOptionPane.showMessageDialog(null, "TASK Encryption Completed");


           }
           
           System.out.println("----path----"+filepath);
           
           File file11 = new File(filepath);
         
        if(file11.delete())
        {
            System.out.println("File deleted successfully");
        }
        else
        {
           System.out.println("Failed to delete the file");
        }

        } catch (Exception ex) {
            Logger.getLogger(TASKCREATION.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
         
        String args[]=null;
       Decryption_Homomorphic.main(args);
        
        
         
    }//GEN-LAST:event_jButton4ActionPerformed
 public void homomorphic_AES(){
        
           try {
             //  new TASKCREATION(RBFHA, "CloudStorage\\" + username + "\\FILES\\" + finame + ".txt");
 if (cmbtasktype.getSelectedItem().toString().equalsIgnoreCase("PDF")) {
          filepath = "CloudStorage\\" + username + "\\FILES\\" + finame + ".txt";

                try {
                    Calendar cal2 = Calendar.getInstance();

                    long milliSec1 = cal2.getTimeInMillis();

                    try {
                        Thread.sleep(7000);
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                    long milliSec2 = cal.getTimeInMillis();

                    long timeDifInMilliSec;
                    if (milliSec1 >= milliSec2) {
                        timeDifInMilliSec = milliSec1 - milliSec2;
                    } else {
                        timeDifInMilliSec = milliSec2 - milliSec1;
                    }

                    long timeDifSeconds = timeDifInMilliSec / 1000;
                    long timeDifMinutes = timeDifInMilliSec / (60 * 1000);
                    long timeDifHours = timeDifInMilliSec / (60 * 60 * 1000);
                    long timeDifDays = timeDifInMilliSec / (24 * 60 * 60 * 1000);

                    File file1 = new File("CloudStorage\\" + username + "\\FILES\\" + finame + ".txt");
                    double megabytes = 0;
                    if (file1.exists()) {

                        double bytes = file1.length();
                        double kilobytes = (bytes / 1024);
                        megabytes = (kilobytes / 1024);

                    }


                     
        
        
        
         String fileName = "CloudStorage\\" + username + "\\FILES\\" + finame ;

         String data="";
         // This will reference one line at a time
        String line = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                data=data+line;
            }   

            // Always close files.
            bufferedReader.close();  
        
        
        }catch(Exception expp)
        {
            expp.printStackTrace();
        }    
        String str=data ;
        
        str=str.toLowerCase();
        
        char c[]=str.toCharArray();
        for(int xx=0;xx<c.length;xx++)
        {
        int a = (int) c[xx];
        
        System.out.println(a);
        int z=encryption(a);
        
        vencrypt.add(z);
        
        }
        
        String encryptedstring="";        
        for(int i=0;i<vencrypt.size();i++)
        {
            encryptedstring=encryptedstring+vencrypt.get(i).toString();
        }    
        
        
        System.out.println("Encrypted Value"+encryptedstring+"\n\n\n");
                    
            
        
         String EncfileName = "CloudStorage\\" + username + "\\FILES\\" + finame + "encrypted.txt.enc";

        try {
            // Assume default encoding.
            FileWriter fileWriter =
                new FileWriter(EncfileName);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter =
                new BufferedWriter(fileWriter);

            // Note that write() does not automatically
            // append a newline character.
            bufferedWriter.write(encryptedstring);
         
            // Always close files.
            bufferedWriter.close();
        }
        catch(IOException ex) {
            System.out.println(
                "Error writing to file '"
                + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }





                    System.out.println("megabytes+++" + megabytes);
                    System.out.println("timeDifSeconds" + timeDifSeconds);
                    double throughput = megabytes / timeDifSeconds;
                    //double throughput=megabytes/timeDifInMilliSec;

                    String insert_Qry = "insert into tbl_performencecomparision(Algorithmname,Throughput) values( 'Homomorphic Encryption','" + throughput + "')";
                    System.out.println("Time differences expressed in various units are given below");

                    //  db.stmt12.executeUpdate(insert_Qry);
                    System.out.println(timeDifInMilliSec + " Milliseconds");
                    System.out.println(timeDifSeconds + " Seconds");
                    System.out.println(timeDifMinutes + " Minutes");
                    System.out.println(timeDifHours + " Hours");
                    System.out.println(timeDifDays + " Days");

                } catch (Exception ex) {
                    //Logger.getLogger(ENCRYPTION.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }


             //   JOptionPane.showMessageDialog(null, "TASK Encryption Completed");
 }else{
                filepath = "CloudStorage\\" + username + "\\FILES\\" + finame + ".txt";

                try {
                    Calendar cal2 = Calendar.getInstance();

                    long milliSec1 = cal2.getTimeInMillis();

                    try {
                        Thread.sleep(7000);
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                    long milliSec2 = cal.getTimeInMillis();

                    long timeDifInMilliSec;
                    if (milliSec1 >= milliSec2) {
                        timeDifInMilliSec = milliSec1 - milliSec2;
                    } else {
                        timeDifInMilliSec = milliSec2 - milliSec1;
                    }

                    long timeDifSeconds = timeDifInMilliSec / 1000;
                    long timeDifMinutes = timeDifInMilliSec / (60 * 1000);
                    long timeDifHours = timeDifInMilliSec / (60 * 60 * 1000);
                    long timeDifDays = timeDifInMilliSec / (24 * 60 * 60 * 1000);

                    File file1 = new File("CloudStorage\\" + username + "\\FILES\\" + finame + ".txt");
                    double megabytes = 0;
                    if (file1.exists()) {

                        double bytes = file1.length();
                        double kilobytes = (bytes / 1024);
                        megabytes = (kilobytes / 1024);

                    }


                     
        
        
        
         String fileName = "CloudStorage\\" + username + "\\FILES\\" + finame + ".txt";

         String data="";
         // This will reference one line at a time
        String line = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                data=data+line;
            }   

            // Always close files.
            bufferedReader.close();  
        
        
        }catch(Exception expp)
        {
            expp.printStackTrace();
        }    
        String str=data ;
        
        str=str.toLowerCase();
        
        char c[]=str.toCharArray();
        for(int xx=0;xx<c.length;xx++)
        {
        int a = (int) c[xx];
        
        System.out.println(a);
        int z=encryption(a);
        
        vencrypt.add(z);
        
        }
        
        String encryptedstring="";        
        for(int i=0;i<vencrypt.size();i++)
        {
            encryptedstring=encryptedstring+vencrypt.get(i).toString();
        }    
        
        
        System.out.println("Encrypted Value"+encryptedstring+"\n\n\n");
                    
            
        
         String EncfileName = "CloudStorage\\" + username + "\\FILES\\" + finame + "encrypted.txt.enc";

        try {
            // Assume default encoding.
            FileWriter fileWriter =
                new FileWriter(EncfileName);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter =
                new BufferedWriter(fileWriter);

            // Note that write() does not automatically
            // append a newline character.
            bufferedWriter.write(encryptedstring);
         
            // Always close files.
            bufferedWriter.close();
        }
        catch(IOException ex) {
            System.out.println(
                "Error writing to file '"
                + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }





                    System.out.println("megabytes+++" + megabytes);
                    System.out.println("timeDifSeconds" + timeDifSeconds);
                    double throughput = megabytes / timeDifSeconds;
                    //double throughput=megabytes/timeDifInMilliSec;

                    String insert_Qry = "insert into tbl_performencecomparision(Algorithmname,Throughput) values( 'Homomorphic Encryption','" + throughput + "')";
                    System.out.println("Time differences expressed in various units are given below");

                    //  db.stmt12.executeUpdate(insert_Qry);
                    System.out.println(timeDifInMilliSec + " Milliseconds");
                    System.out.println(timeDifSeconds + " Seconds");
                    System.out.println(timeDifMinutes + " Minutes");
                    System.out.println(timeDifHours + " Hours");
                    System.out.println(timeDifDays + " Days");

                } catch (Exception ex) {
                    //Logger.getLogger(ENCRYPTION.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }


               // JOptionPane.showMessageDialog(null, "TASK Encryption Completed");
            

 }

        } catch (Exception ex) {
            Logger.getLogger(TASKCREATION.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
     public static int encryption(int a){
        int val=0;
        
        
        if(a<78)
        {    
        
        val=a+13;
        
        }
        else
        {
            val=a-13;
        }    
        return val;
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TASKCREATION.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TASKCREATION.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TASKCREATION.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TASKCREATION.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new TASKCREATION().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public static javax.swing.JComboBox cmbtasktype;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables
}
