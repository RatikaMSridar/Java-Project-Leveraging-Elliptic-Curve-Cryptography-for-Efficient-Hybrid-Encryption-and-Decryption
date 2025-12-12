/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cloud_computing;


import commoncodes.db_conn;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;


public class userregister extends javax.swing.JFrame {

    
    db_conn db = new db_conn();
    public userregister() {
        initComponents();
        
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextField_username = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPasswordField = new javax.swing.JPasswordField();
        jLabel5 = new javax.swing.JLabel();
        jTextField_fname = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextField_lname = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jComboBox_gender = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jTextField_cno = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtlocation = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 1, true), "Enter your Details", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("High Tower Text", 1, 18), new java.awt.Color(0, 102, 102))); // NOI18N
        jPanel3.setFont(new java.awt.Font("High Tower Text", 1, 18)); // NOI18N

        jLabel3.setFont(new java.awt.Font("High Tower Text", 1, 18)); // NOI18N
        jLabel3.setText("User Name");

        jTextField_username.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_usernameActionPerformed(evt);
            }
        });
        jTextField_username.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField_usernameKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField_usernameKeyReleased(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("High Tower Text", 1, 18)); // NOI18N
        jLabel4.setText("Password");

        jPasswordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPasswordFieldActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("High Tower Text", 1, 18)); // NOI18N
        jLabel5.setText("First Name");

        jTextField_fname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_fnameActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("High Tower Text", 1, 18)); // NOI18N
        jLabel6.setText("Last Name");

        jTextField_lname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_lnameActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("High Tower Text", 1, 18)); // NOI18N
        jLabel7.setText("Gender");

        jComboBox_gender.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select Gender", "Male", "Female" }));
        jComboBox_gender.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_genderActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("High Tower Text", 1, 18)); // NOI18N
        jLabel8.setText("Contact No");

        jTextField_cno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_cnoActionPerformed(evt);
            }
        });
        jTextField_cno.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField_cnoKeyPressed(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(255, 255, 255));
        jButton1.setFont(new java.awt.Font("High Tower Text", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(153, 0, 153));
        jButton1.setText("Register");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(255, 255, 255));
        jButton2.setFont(new java.awt.Font("High Tower Text", 1, 18)); // NOI18N
        jButton2.setForeground(new java.awt.Color(153, 0, 153));
        jButton2.setText("Clear all");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(255, 255, 255));
        jButton3.setFont(new java.awt.Font("High Tower Text", 1, 18)); // NOI18N
        jButton3.setForeground(new java.awt.Color(153, 0, 153));
        jButton3.setText("Exit");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("High Tower Text", 1, 18)); // NOI18N
        jLabel1.setText("Location");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap(55, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jTextField_cno)
                            .addComponent(jTextField_lname, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField_username, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField_fname, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPasswordField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox_gender, 0, 234, Short.MAX_VALUE)
                            .addComponent(txtlocation, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(38, 38, 38))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField_username, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField_fname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextField_lname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7)
                    .addComponent(jComboBox_gender, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_cno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 4, Short.MAX_VALUE)
                        .addComponent(jLabel1))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtlocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addGap(19, 19, 19))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 2, true));

        jLabel2.setFont(new java.awt.Font("High Tower Text", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(102, 0, 204));
        jLabel2.setText("User Registeration");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(139, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(108, 108, 108))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 510, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 449, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField_usernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_usernameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_usernameActionPerformed

    private void jTextField_usernameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField_usernameKeyPressed
        // TODO add your handling code here:

   }//GEN-LAST:event_jTextField_usernameKeyPressed

    private void jTextField_usernameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField_usernameKeyReleased
        // TODO add your handling code here:

   }//GEN-LAST:event_jTextField_usernameKeyReleased

    private void jPasswordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jPasswordFieldActionPerformed

    private void jTextField_fnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_fnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_fnameActionPerformed

    private void jTextField_lnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_lnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_lnameActionPerformed

    private void jComboBox_genderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_genderActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox_genderActionPerformed

    private void jTextField_cnoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_cnoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_cnoActionPerformed

    private void jTextField_cnoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField_cnoKeyPressed
        // TODO add your handling code here:
        String allSplChars = "a!@b#c$d%e^&f*g()h_+ijk=-l~m`,.no/p?>q|r<;'s:t[]u^{v\"w}x\\yz";

        for (int s = 0; s < allSplChars.length(); s++) {
            if (evt.getKeyChar() == allSplChars.charAt(s)) {
                JOptionPane pane = new JOptionPane("     Please Enter Numeric values only    ", JOptionPane.ERROR_MESSAGE);
                JDialog dialog = pane.createDialog(null, "Error  Message!!!");
                dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                dialog.show();
                jTextField_cno.setText("");
                jTextField_cno.requestFocusInWindow();
            } else {
                jTextField_cno.requestFocusInWindow();
                jTextField_cno.setEditable(true);
            }

        }
    }//GEN-LAST:event_jTextField_cnoKeyPressed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        try {
            if (jTextField_username.getText().isEmpty() || jPasswordField.getText().isEmpty() || jTextField_fname.getText().isEmpty() || jTextField_lname.getText().isEmpty() || jComboBox_gender.getSelectedIndex() == 0 || jTextField_cno.getText().isEmpty()) {
                JOptionPane.showMessageDialog(rootPane, "Please Fill all Fields!", "WARNING_MESSAGE", JOptionPane.WARNING_MESSAGE);
            } else {
                String uname = jTextField_username.getText().toString();
                String SQL_QUERY = "select username from tbl_userregister where username='" + uname + "'";
                ResultSet rst = db.stmt10.executeQuery(SQL_QUERY);
                if (rst.next()) {
                    JOptionPane.showMessageDialog(rootPane, "Note " + uname + " Already exists!", "WARNING_MESSAGE", JOptionPane.WARNING_MESSAGE);
                }
                else {
                    
                    
                    register();
                    
                    
                }
//                for(Iterator it=query.iterate();it.hasNext();){
//	 		Object[] row = (Object[]) it.next();
//	 		System.out.println("ID: " + row[0]);
//	 		System.out.println("Name: " + row[1]);
//	 		System.out.println("Amount: " + row[2]);
//	 	}
//	 	


            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        jTextField_username.setText(null);
        jPasswordField.setText(null);
        jTextField_fname.setText(null);
        jTextField_lname.setText(null);
        jTextField_cno.setText(null);
        jComboBox_gender.setSelectedIndex(0);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

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
            java.util.logging.Logger.getLogger(userregister.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(userregister.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(userregister.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(userregister.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new userregister().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox jComboBox_gender;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPasswordField jPasswordField;
    private javax.swing.JTextField jTextField_cno;
    private javax.swing.JTextField jTextField_fname;
    private javax.swing.JTextField jTextField_lname;
    public static javax.swing.JTextField jTextField_username;
    private javax.swing.JTextField txtlocation;
    // End of variables declaration//GEN-END:variables

public void register()
 {
     
     try{
         
                db.stmt11.executeUpdate("insert into tbl_userregister(username, pword, firstname, lastname, gender, contactno,location) values( '"+jTextField_username.getText().trim()+"','"+jPasswordField.getText().trim()+"','"+jTextField_fname.getText().trim()+"','"+jTextField_lname.getText().trim()+"', '"+jComboBox_gender.getSelectedItem().toString()+"','"+jTextField_cno.getText().trim()+"','"+txtlocation.getText().toString()+"') ");
		System.out.println("Done");
              
                File file = new File("CloudStorage\\"+jTextField_username.getText().trim()+"\\");
	if (!file.exists()) {
		if (file.mkdir()) {
			System.out.println("Directory is created!");
		} else {
			System.out.println("Failed to create directory!");
		}
	}
        
        
          File file1 = new File("CloudStorage\\"+jTextField_username.getText().trim()+"\\UploadFile\\");
	if (!file1.exists()) {
		if (file1.mkdir()) {
			System.out.println("Directory is created!");
		} else {
			System.out.println("Failed to create directory!");
		}
	}
        
        
         File file2 = new File("CloudStorage\\"+jTextField_username.getText().trim()+"\\DownloadFile\\");
	if (!file2.exists()) {
		if (file2.mkdir()) {
			System.out.println("Directory is created!");
		} else {
			System.out.println("Failed to create directory!");
		}
	}
        
        
         File file3 = new File("CloudStorage\\"+jTextField_username.getText().trim()+"\\FILES\\");
	if (!file3.exists()) {
		if (file3.mkdir()) {
			System.out.println("Directory is created!");
		} else {
			System.out.println("Failed to create directory!");
		}
	}
                JOptionPane.showMessageDialog(rootPane,"    Registeration Successfull!    ","INFORMATION_MESSAGE",JOptionPane.INFORMATION_MESSAGE);
     }
     catch(SQLException ex)
     {
         ex.printStackTrace();}
            
        }

}
