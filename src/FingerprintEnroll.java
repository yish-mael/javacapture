/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Yishmael
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import java.sql.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import net.proteanit.sql.DbUtils;
import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.DPFPCapturePriority;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPDataListener;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.readers.DPFPReaderDescription;
import com.digitalpersona.onetouch.readers.DPFPReadersCollection;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.ImageIcon;

public class FingerprintEnroll extends javax.swing.JFrame{

    /**
     * Creates new form FingerprintEnroll
     */
    DPFPCapture capturer = DPFPGlobal.getCaptureFactory().createCapture();
    DPFPEnrollment enroller = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    DPFPTemplate template = null;
    
    byte [] right_thumb = null;
    byte [] right_index = null;
    byte [] left_thumb = null;
    byte [] left_index = null;

    
    ResultSet rs = null;
    PreparedStatement pst = null;
    Connection conn = null;
    int startcap = 0;
    String user_id ="";
    String search_addition = "";
    
    public FingerprintEnroll() {
        initComponents();

        capturer.addDataListener(new DPFPDataAdapter() {
            public void dataAcquired(final DPFPDataEvent e) {
                //log.append("The fingerprint sample was captured.\n");
                prompt.setText("Scan the same fingerprint again.");
                process(e.getSample());
            }
        });
                    
        //Check if the fingerprint scanner is connected/disconnected.
        capturer.addReaderStatusListener(new DPFPReaderStatusAdapter() {
            public void readerConnected (final DPFPReaderStatusEvent e) {
                log.append("Fingerprint connected.\n");
            }
            public void readerDisconnected (final DPFPReaderStatusEvent e) {
                prompt.setText("Fingerprint disconnected.\n");
            }
        });
        
        cn();  
    }
    
    protected Connection cn() 
    {
	try 
        { 
            Class.forName("com.mysql.jdbc.Driver");
            //conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "");
            conn = DriverManager.getConnection("jdbc:mysql://kmdmarket.mysql.database.azure.com:3306/kmdmc", "adminkmdmc@kmdmarket", "KMDMC@1980");
            FillTable();
	} 
        catch(Exception e) 
        { 
            System.out.println(e); 
        }
	return conn;
    }
    
    private void FillTable()
    {
        try
        {
            String sql = "select id,concat(firstname,' ',lastname) as name,email,'' as department,'' as branch,'' as pic_path from application WHERE finger IS NULL "+search_addition;
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            users.setModel(DbUtils.resultSetToTableModel(rs));
            
            users.getColumnModel().getColumn(5).setMinWidth(0);
            users.getColumnModel().getColumn(5).setMaxWidth(0);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    
    protected void insert(String id, byte[] r_thumb, byte[] l_thumb, byte[] r_index, byte[] l_index)
    {
        PreparedStatement st;
        try
        {
            st = cn().prepareStatement("UPDATE application SET finger=?, thumb=?, right_index=?, left_index=? WHERE id=?");
            st.setBytes(1, r_thumb);
            st.setBytes(2, l_thumb);
            st.setBytes(3, r_index);
            st.setBytes(4, l_index);
            st.setString(5, id);
            st.executeUpdate();
            log.append("Succesfully inserted to database!");
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }
        
    
    protected void process (DPFPSample sample)
    {
        //Draw fingerprint sample image.
        drawPicture(convertSampleToBitmap(sample));
        
        // Process the sample and create a feature set for the enrollment purpose.
	DPFPFeatureSet features = extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
        
        if (features != null)
            try
            {
                if(right_thumb == null){
                    log.append("Right Thumb fingerprint was created.\n");
                    enroller.addFeatures(features);  
                    template = enroller.getTemplate();
                    right_thumb = template.serialize();
                    log.append(" "+right_thumb+" \n");
                    enroller.clear();
                    stopCapturing();
                    startCapturing();
                    //log.append("Using the fingerprint reader, scan your Right Index.\n");
                }else if(right_index == null){
                    log.append("Right Index fingerprint was created.\n");
                    enroller.addFeatures(features);  
                    template = enroller.getTemplate();
                    right_index = template.serialize(); //Serialize fingerprint to save it to database
                    log.append(" "+right_index+" \n");
                    enroller.clear();
                    stopCapturing();
                    startCapturing();
                }else if(left_thumb == null){
                    log.append("Left Thumb fingerprint was created.\n");
                    enroller.addFeatures(features);  
                    template = enroller.getTemplate();
                    left_thumb = template.serialize(); //Serialize fingerprint to save it to database
                    log.append(" "+left_thumb+" \n");
                    enroller.clear();
                    stopCapturing();
                    startCapturing();
                }else if(left_index == null){
                    log.append("Left Index fingerprint was created.\n");
                    enroller.addFeatures(features);  
                    template = enroller.getTemplate();
                    left_index = template.serialize(); //Serialize fingerprint to save it to database
                    log.append(" "+left_index+" \n");
                    enroller.clear();
                    stopCapturing();
                    startCapturing();
                }else{
                    insert(user_id, right_thumb, left_thumb, right_index, left_index);
                    //log.setText("RThumb"+right_thumb+" RIndex"+right_index+" LThunb"+left_thumb+" LIndex"+left_index);
                }                
            }
            catch (DPFPImageQualityException ex) { }

    }
    
    public void drawPicture(Image image) 
    {
	picture.setIcon(new ImageIcon(
		image.getScaledInstance(picture.getWidth(), picture.getHeight(), Image.SCALE_DEFAULT)));
    }    
    
    
    protected Image convertSampleToBitmap(DPFPSample sample) {
	return DPFPGlobal.getSampleConversionFactory().createImage(sample);
    }
    
    protected DPFPFeatureSet extractFeatures(DPFPSample sample, DPFPDataPurpose purpose)
    {
	DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
	try {
		return extractor.createFeatureSet(sample, purpose);
	} catch (DPFPImageQualityException e) {
		return null;
	}
    }
    
    protected void startCapturing()
    {
            capturer.startCapture();
            
            if(right_thumb == null){
                log.append("Using the fingerprint reader, scan your Right Thumb.\n");
            }else if (right_index == null){
                log.append("Using the fingerprint reader, scan your Right Index.\n");
            }else if (left_thumb == null){
                log.append("Using the fingerprint reader, scan your Left Thumb.\n");
            }else if (left_index == null){
                log.append("Using the fingerprint reader, scan your Left Index.\n");
            }else{
                stopCapturing();
                insert(user_id, right_thumb, left_thumb, right_index, left_index);
                log.setText("RThumb"+right_thumb+" RIndex"+right_index+" LThumb"+left_thumb+" LIndex"+left_index);
                
            }          
    }
    
    
    protected void stopCapturing()
    {
        capturer.stopCapture();
        startcap=0;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        picture = new javax.swing.JLabel();
        prompt = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        log = new javax.swing.JTextArea();
        register = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        users = new javax.swing.JTable();
        emp_name = new javax.swing.JLabel();
        bio_stat = new javax.swing.JLabel();
        capture_btn = new javax.swing.JButton();
        search_input = new javax.swing.JTextField();
        refreshButton = new javax.swing.JButton();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Scan your fingerprint to continue.");

        prompt.setEditable(false);
        prompt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                promptActionPerformed(evt);
            }
        });

        log.setEditable(false);
        log.setColumns(20);
        log.setRows(5);
        jScrollPane1.setViewportView(log);

        register.setText("Register");
        register.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registerActionPerformed(evt);
            }
        });

        users.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                usersMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(users);

        emp_name.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        capture_btn.setText("START CAPTURING");
        capture_btn.setEnabled(false);
        capture_btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                capture_btnMouseClicked(evt);
            }
        });
        capture_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                capture_btnActionPerformed(evt);
            }
        });

        search_input.setToolTipText("Search");
        search_input.setName("searchInput"); // NOI18N
        search_input.setOpaque(false);
        search_input.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                search_inputActionPerformed(evt);
            }
        });

        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(prompt))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(picture, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(20, 20, 20)
                                    .addComponent(emp_name, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(jLabel1))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(search_input, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(capture_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addComponent(bio_stat, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(42, 42, 42)
                                .addComponent(register, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 478, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 498, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(emp_name, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(search_input, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(refreshButton, javax.swing.GroupLayout.Alignment.TRAILING)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addComponent(jLabel1)
                        .addGap(12, 12, 12)
                        .addComponent(picture, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(prompt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addComponent(bio_stat, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(register, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(capture_btn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(11, 11, 11))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void promptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_promptActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_promptActionPerformed

    private void registerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerActionPerformed
        
        cn();
       
    }//GEN-LAST:event_registerActionPerformed

    private void usersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usersMouseClicked
        // TODO add your handling code here:
        
        capture_btn.setEnabled(true);
        try 
        {
            int row = users.getSelectedRow();
            
            user_id = (users.getModel().getValueAt(row, 0).toString());
            String name = (users.getModel().getValueAt(row, 1).toString());
            String email = (users.getModel().getValueAt(row, 2).toString());
            String department = (users.getModel().getValueAt(row, 3).toString());
            String branch = (users.getModel().getValueAt(row, 4).toString());
            String pic_path = (users.getModel().getValueAt(row, 5).toString());
            
            emp_name.setText(""+name+", ");
            
        } 
        catch (Exception e) 
        {
            System.out.println(e.getMessage());
        }
    }//GEN-LAST:event_usersMouseClicked

    private void capture_btnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_capture_btnMouseClicked
        // TODO add your handling code here:
        startCapturing();
    }//GEN-LAST:event_capture_btnMouseClicked

    private void search_inputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_search_inputActionPerformed
        // TODO add your handling code here:
        String search_value = search_input.getText();
        search_addition = "AND firstname LIKE '%"+search_value+"%' OR lastname LIKE '%"+search_value+"%' LIMIT 5";
        cn();
    }//GEN-LAST:event_search_inputActionPerformed

    private void capture_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_capture_btnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_capture_btnActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        // TODO add your handling code here:
        try{
            cn();
        }
        catch (Exception e) 
        {
            System.out.println(e.getMessage());
        }
    }//GEN-LAST:event_refreshButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FingerprintEnroll.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FingerprintEnroll.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FingerprintEnroll.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FingerprintEnroll.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
           
            public void run() {
                new FingerprintEnroll().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bio_stat;
    private javax.swing.JButton capture_btn;
    private javax.swing.JLabel emp_name;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea log;
    private javax.swing.JLabel picture;
    private javax.swing.JTextField prompt;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton register;
    private javax.swing.JTextField search_input;
    private javax.swing.JTable users;
    // End of variables declaration//GEN-END:variables
}
