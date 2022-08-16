
package gui_bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;


public class Clientes extends javax.swing.JFrame {
     Conexion_BD cx = new Conexion_BD();
     Connection con = cx.conexion();
     
     String idRepo = "";
     float totalfinal;
     String idClienteReporte;
     int filtrar = 100;
     int filtrar2;
     int filtrar3;
    
    public Clientes() {
        initComponents();
        mostrarClientes(filtrar,filtrar2);
        this.setResizable(false);
        this.setSize(948, 550);
        this.setLocationRelativeTo(null);
    }
    
  
     public int mostrarClientes(int filtrar, int filtrar2){
    
       
    String[] tClientes = {"Id","Referencia","Concepto","Estado"};
    String[] reg = new String[4];
    
    DefaultTableModel z = new DefaultTableModel(null,tClientes);
    String query = "SELECT IdCliente, referenciaCliente,conceptoCliente, TipoCliente from tb_clientes INNER JOIN tb_tiposcliente ON tb_clientes.idTipo = tb_tiposcliente.idTipos  where IdCliente between ? and ?;";
        try {
            PreparedStatement x = con.prepareStatement(query);
            x.setInt(1, filtrar2);
            x.setInt(2, filtrar);
            ResultSet res = x.executeQuery();
            
            while(res.next()){
            reg[0] = res.getString("IdCliente");
            reg[1] = res.getString("referenciaCliente");
            reg[2] = res.getString("conceptoCliente");
            reg[3] = res.getString("TipoCliente");
            z.addRow(reg);
            
            }
            tb_clientes.setModel(z);

        } catch (Exception e) {
            
            JOptionPane.showMessageDialog(null,"ERROR"+e.getMessage());
        }
        return filtrar;
        
    }
     
     public String mostrarPrestamos(String valor2){
         
         String[] tPrestamos = {"Fecha","Importe"};
         String[] reg = new String[3];
        
         DefaultTableModel z = new DefaultTableModel(null,tPrestamos);
        String query = "select date_format(Fecha,\"%d-%m-%Y\") as Fecha, Importe from tb_prestamos where IdCliente = ?";
        try {
            PreparedStatement x = con.prepareStatement(query);
            x.setString(1, valor2);
            ResultSet res = x.executeQuery();
            
            while(res.next()){
            reg[0] = res.getString("Fecha");
            reg[1] = res.getString("Importe");
            z.addRow(reg);
            
            }
            tb_prestamos.setModel(z);
            
        } catch (Exception e) {
            
            JOptionPane.showMessageDialog(null,"ERROR"+e.getMessage());
            
        }
        return reg[1];
     }
     
     public String mostrarPagos(String valor2){
         String[] tPagos = {"Fecha","Importe"};
         String[] reg = new String[3];
         DefaultTableModel z = new DefaultTableModel(null,tPagos);
        String query = "select date_format(FechaOperacion,\"%d-%m-%Y\") as FechaOperacion, ImporteMicrocredito from tb_microcreditos where IdClienteMicrocredito = ?";
        try {
            PreparedStatement x = con.prepareStatement(query);
            x.setString(1, valor2);
            ResultSet res = x.executeQuery();
            
            while(res.next()){
            reg[0] = res.getString("FechaOperacion");
            reg[1] = res.getString("ImporteMicrocredito");
            z.addRow(reg);
            
            }
            tb_pagos.setModel(z);

        } catch (Exception e) {
            
            JOptionPane.showMessageDialog(null,"ERROR"+e.getMessage());
            e.printStackTrace();
        }
        return reg[1];
     }
    
     public double mostrarDiferencia(String valor2){
         float total=0f, total2=0f;
         String sql ="Select SUM(Importe) as totalImporte from tb_prestamos where IdCliente = ?";
         String sql2 ="Select SUM(ImporteMicrocredito) as totalImporte2 from tb_microcreditos where IdClienteMicrocredito = ?";
         try{
             PreparedStatement x = con.prepareStatement(sql);
             x.setString(1,valor2);
             ResultSet res = x.executeQuery();
             while(res.next()){
                 total = res.getFloat("totalImporte");
             }
            
             PreparedStatement y = con.prepareStatement(sql2);
             y.setString(1,valor2);
             ResultSet res2 = y.executeQuery();
             while(res2.next()){
                 total2 = res2.getFloat("totalImporte2");
             }
             totalfinal = total-total2;
             resutxt.setText(Float.toString(totalfinal));
         }catch(Exception e){
             e.printStackTrace();
         }
         return totalfinal;
     }
     
     public String filtrarClientes(String tipocliente){
          String[] tClientes = {"Id","Referencia","Concepto","Estado"};
          String[] reg = new String[4];
          DefaultTableModel z = new DefaultTableModel(null,tClientes);
         String sql ="SELECT IdCliente, referenciaCliente,conceptoCliente,TipoCliente from tb_clientes INNER JOIN tb_tiposcliente ON tb_clientes.idTipo = tb_tiposcliente.idTipos Where idTipo = ?";
        
        try{
            PreparedStatement x = con.prepareStatement(sql);
            x.setString(1, tipocliente);
            ResultSet res = x.executeQuery();
            
            while(res.next()){
            reg[0] = res.getString("IdCliente");
            reg[1] = res.getString("referenciaCliente");
            reg[2] = res.getString("conceptoCliente");
            reg[3] = res.getString("TipoCliente");
            z.addRow(reg);
            
            }
            tb_clientes.setModel(z);
        }catch(Exception e){
            e.printStackTrace();
        }
        return tipocliente;
     }
    
     public float reporte(String idRepo, float total){
        JasperReport reporte =null;
        String a= System.getProperty("user.dir");
        String path = a + "\\reportes\\clientes.jasper";
        
         int idRepo2 = Integer.parseInt(idRepo);
 
        try {
            Map parametros = new HashMap();

            parametros.put("idRepo", idRepo2);
            parametros.put("total", total);
            parametros.put("ruta", a);
           
            reporte = (JasperReport) JRLoader.loadObjectFromFile(path);
            JasperPrint jprint = JasperFillManager.fillReport(reporte,parametros,con);
            JasperViewer view = new JasperViewer(jprint,false);
            view.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            view.setVisible(true);
            
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error\n"+ex);
        }
        return total;
     }
     
     public String buscarCliente(String buscarClientes){
         String[] reg = new String[5];
         String[] tit = {"Id", "Referencia", "Concepto", "Estado"};
         try{
             DefaultTableModel a = new DefaultTableModel(null,tit);
             String buscar ="SELECT IdCliente, referenciaCliente,conceptoCliente,TipoCliente from tb_clientes INNER JOIN tb_tiposcliente ON tb_clientes.idTipo = tb_tiposcliente.idTipos Where referenciaCliente =? or conceptoCliente = ?";
            PreparedStatement x = con.prepareStatement(buscar);
            x.setString(1,buscarClientes);
            x.setString(2,buscarClientes);
            ResultSet resu = x.executeQuery();
            while(resu.next()){
                reg[0] = resu.getString("IdCliente");
                reg[1] = resu.getString("referenciaCliente");
                reg[2] = resu.getString("conceptoCliente");
                reg[3] = resu.getString("TipoCliente");
                a.addRow(reg);
            }
            tb_clientes.setModel(a);
            buscarCliente.setText("");
            
         }catch(Exception e){
             e.printStackTrace();
         }
        return reg[1];
         
     }
     
     public int ultimosRegistros(){
         try{
            String query="Select Max(IdCliente) as maxCliente from tb_clientes";
            Statement x = con.createStatement();
            ResultSet res = x.executeQuery(query);
            while(res.next()){
                filtrar3 = res.getInt("maxCliente");
            }
            filtrar2 = filtrar3 - filtrar;
            mostrarClientes(filtrar3,filtrar2);
        }catch(Exception e){
            e.printStackTrace();
        }
         return filtrar3;
     }
     
     public int filtrarDatos(String f){
         
          switch (f) {
             case "100":
                 f = "100";
                 break;
             case "200":
                 f = "200";
                 break;
             case "500":
                  f = "500";
                 break;
             case "1000":
                 f = "1000";
                 break;
         }
        filtrar2 =0;
        filtrar3 =0;
        filtrar = Integer.parseInt(f);
        mostrarClientes(filtrar, filtrar2);
        return filtrar;
     }     
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        prestamo = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tb_clientes = new javax.swing.JTable();
        listaPersonas = new javax.swing.JComboBox<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        tb_prestamos = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tb_pagos = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        resutxt = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        nombreCliente = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        paginaAnterior = new javax.swing.JButton();
        paginaSiguiente = new javax.swing.JButton();
        filtrarDatos = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jButton9 = new javax.swing.JButton();
        buscarCliente = new javax.swing.JTextField();
        Fondo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/4 (1) (1).png"))); // NOI18N
        jLabel2.setText("Clientes y Deudores");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 20, 320, 60));

        prestamo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/a (1).png"))); // NOI18N
        prestamo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prestamo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prestamoActionPerformed(evt);
            }
        });
        getContentPane().add(prestamo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 80, -1));

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/aguinaldo (1).png"))); // NOI18N
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, 80, 60));

        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("Prestamos");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, -1, -1));

        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Microcreditos");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 270, -1, -1));

        jButton3.setText("Atras");
        jButton3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 290, -1, -1));

        tb_clientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tb_clientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tb_clientesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tb_clientes);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 190, 420, 250));

        listaPersonas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "Pagados", "Deudores" }));
        listaPersonas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        listaPersonas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listaPersonasActionPerformed(evt);
            }
        });
        getContentPane().add(listaPersonas, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 160, 90, 20));

        tb_prestamos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tb_prestamos);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 190, 320, 110));

        jLabel4.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Estado de Cuenta del Cliente:");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 110, 240, -1));

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Prestamo");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 170, -1, -1));

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("Pagos");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 300, -1, -1));

        tb_pagos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(tb_pagos);

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 320, 320, 140));

        jLabel7.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("Clientes Dados de Alta  ");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 120, -1, -1));

        jLabel8.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("Saldo:");
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 470, -1, -1));

        resutxt.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        resutxt.setForeground(new java.awt.Color(0, 0, 0));
        resutxt.setText("0.00");
        getContentPane().add(resutxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 470, 110, -1));

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/logo-IME4-removebg-preview (1).png"))); // NOI18N
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jButton4.setBackground(new java.awt.Color(255, 0, 0));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/pdf.png"))); // NOI18N
        jButton4.setText("Reporte Clientes");
        jButton4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 450, -1, 20));

        jButton5.setBackground(new java.awt.Color(255, 0, 0));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/pdf.png"))); // NOI18N
        jButton5.setText("Reporte Historial");
        jButton5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 470, 150, 30));

        nombreCliente.setForeground(new java.awt.Color(0, 0, 0));
        getContentPane().add(nombreCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 130, 320, 20));

        jButton1.setText("<<");
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 480, 50, -1));

        jButton6.setText(">>");
        jButton6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 480, -1, -1));

        paginaAnterior.setText("<");
        paginaAnterior.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        paginaAnterior.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paginaAnteriorActionPerformed(evt);
            }
        });
        getContentPane().add(paginaAnterior, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 480, 40, -1));

        paginaSiguiente.setText(">");
        paginaSiguiente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        paginaSiguiente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paginaSiguienteActionPerformed(evt);
            }
        });
        getContentPane().add(paginaSiguiente, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 480, 40, -1));

        filtrarDatos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "100", "200", "500", "1000" }));
        filtrarDatos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        filtrarDatos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filtrarDatosActionPerformed(evt);
            }
        });
        getContentPane().add(filtrarDatos, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 480, 70, -1));

        jLabel11.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("Paginas");
        getContentPane().add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 450, -1, -1));

        jLabel10.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setText("Registro por Paginas");
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 450, -1, -1));

        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/buscar (1).png"))); // NOI18N
        jButton9.setText("Buscar");
        jButton9.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 150, -1, -1));
        getContentPane().add(buscarCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 150, 220, 30));

        Fondo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/fondoprograma (1).jpg"))); // NOI18N
        getContentPane().add(Fondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 950, 530));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void prestamoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prestamoActionPerformed

        Prestamos1 pres = new Prestamos1();
        pres.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_prestamoActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        Microcreditos micro = new Microcreditos ();
        micro.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        Menu menu = new Menu();
        menu.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void tb_clientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tb_clientesMouseClicked
       int valor =tb_clientes.getSelectedRow();
       idRepo = tb_clientes.getValueAt(valor, 0).toString();
       nombreCliente.setText(tb_clientes.getValueAt(valor, 2).toString());
       
       mostrarPrestamos(idRepo);
       mostrarPagos(idRepo);
       mostrarDiferencia(idRepo);
       
    }//GEN-LAST:event_tb_clientesMouseClicked

    private void listaPersonasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listaPersonasActionPerformed
        String tipocliente;
         tipocliente=listaPersonas.getSelectedItem().toString();
          switch (tipocliente) {
             case "Pagados":
                 tipocliente = "1";
                 break;
             case "Deudores":
                 tipocliente = "2";
                 break;
             case "Todos":
                 filtrar2=0;
                 mostrarClientes(filtrar,filtrar2);
                 return ;
         }
        filtrarClientes(tipocliente);
        idClienteReporte = tipocliente;
    }//GEN-LAST:event_listaPersonasActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
      reporte(idRepo,totalfinal);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        JasperReport reporte =null;
        String a= System.getProperty("user.dir");
        String path = a + "\\reportes\\totalClientes.jasper";
       
 
        try {
            Map parametros = new HashMap();

            parametros.put("id", idClienteReporte);
            parametros.put("ruta", a);
           
           
            reporte = (JasperReport) JRLoader.loadObjectFromFile(path);
            JasperPrint jprint = JasperFillManager.fillReport(reporte,parametros,con);
            JasperViewer view = new JasperViewer(jprint,false);
            view.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            view.setVisible(true);
            
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void paginaAnteriorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paginaAnteriorActionPerformed
       if(filtrar==0){
            JOptionPane.showMessageDialog(null, "No hay mas registros");
        }else{
            filtrar3=filtrar3-filtrar;
            filtrar2=filtrar2-filtrar;
            mostrarClientes(filtrar3,filtrar2);
        }
    }//GEN-LAST:event_paginaAnteriorActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        ultimosRegistros();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        String buscar = buscarCliente.getText();
        buscarCliente(buscar);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void filtrarDatosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filtrarDatosActionPerformed
        String f = filtrarDatos.getSelectedItem().toString();
        filtrarDatos(f);
         
    }//GEN-LAST:event_filtrarDatosActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        filtrar2=0;
        mostrarClientes(filtrar,filtrar2);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void paginaSiguienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paginaSiguienteActionPerformed
         if(filtrar2==0){
            filtrar2=filtrar;
            filtrar3=filtrar2+filtrar;
            mostrarClientes(filtrar3, filtrar2);
        }else{
            filtrar2 = filtrar2+filtrar;
            filtrar3 = filtrar2 + filtrar;
            mostrarClientes(filtrar3, filtrar2);
        }
        
      
    }//GEN-LAST:event_paginaSiguienteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Fondo;
    private javax.swing.JTextField buscarCliente;
    private javax.swing.JComboBox<String> filtrarDatos;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JComboBox<String> listaPersonas;
    private javax.swing.JLabel nombreCliente;
    private javax.swing.JButton paginaAnterior;
    private javax.swing.JButton paginaSiguiente;
    private javax.swing.JButton prestamo;
    private javax.swing.JLabel resutxt;
    private javax.swing.JTable tb_clientes;
    private javax.swing.JTable tb_pagos;
    private javax.swing.JTable tb_prestamos;
    // End of variables declaration//GEN-END:variables

   
}
