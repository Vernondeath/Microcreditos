
package gui_bd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

public class Microcreditos extends javax.swing.JFrame {

    Conexion_BD cx = new Conexion_BD();
    Connection con = cx.conexion();
    boolean repetido;
    int filtrar = 100;
    int filtrar2;
    int filtrar3;
    
    public Microcreditos() {
        initComponents();
           mostrarPagosMes();
           this.setSize(1107, 650);
           this.setResizable(false);
           this.setLocationRelativeTo(null);
    }

public int Mostrar(int filtrar, int filtrar2){
    int mes = mesMicrocreditos.getMonth()+1;
    int año = añoMicrocreditos.getYear();
    
    String[] tit = {"Id","FechaOperacion","Referencia","Concepto","Importe"};
    String[] reg = new String[5];
    DefaultTableModel z = new DefaultTableModel(null,tit);
    String query = "Select numFolio, date_format(FechaOperacion,\"%d-%m-%Y\") as FechaOperacion, ReferenciaMicrocredito, ConceptoMicrocredito, ImporteMicrocredito from tb_microcreditos where Year(FechaOperacion)= ? and Month(FechaOperacion)=? and numFolio between ? and ?";
        try {
            PreparedStatement x = con.prepareStatement(query);
            x.setInt(1,año);
            x.setInt(2,mes);
            x.setInt(3,filtrar2);
            x.setInt(4, filtrar);
            ResultSet res = x.executeQuery();
            while(res.next()){
            reg[0] = res.getString("numFolio");
            reg[1] = res.getString("FechaOperacion");
            reg[2] = res.getString("ReferenciaMicrocredito");
            reg[3] = res.getString("ConceptoMicrocredito");
            reg[4] = res.getString("ImporteMicrocredito");
            z.addRow(reg);
            }
            String query2 = "Select Cantidad from tb_totalesmes where YEAR(fechaTotalesMes)=? and MONTH(fechaTotalesMes)=?";
            PreparedStatement y = con.prepareStatement(query2);
            y.setInt(1, año);
            y.setInt(2, mes);
            ResultSet total = y.executeQuery();
            while(total.next()){
                totalPagos.setText(total.getString("Cantidad"));
            }
            tbl_e.setModel(z);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"ERROR"+e.getMessage());
        }
        return filtrar;
}

public boolean mostrarPagosMes(){
    String[] tit = {"Id","Cantidad","Fecha"};
    String[] reg = new String[3];
    DefaultTableModel z = new DefaultTableModel(null,tit);
    String query = "Select IdTotalMes,Cantidad, date_format(fechaTotalesMes,\"%d-%m-%Y\") as fechaTotalesMes from tb_totalesmes";
        try {
            Statement s = con.createStatement();
            ResultSet res = s.executeQuery(query);
            while(res.next()){
            reg[0] = res.getString("IdTotalMes");
            reg[1] = res.getString("Cantidad");
            reg[2] = res.getString("fechaTotalesMes");
            z.addRow(reg);
            }
            tb_totales.setModel(z);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"ERROR"+e.getMessage());
        }
        return true;
}

public boolean pagosRepetidos(){
        try {
            String query = "UPDATE tb_microcreditos set IdClienteMicrocredito = (SELECT IdCliente FROM tb_clientes WHERE tb_clientes.referenciaCliente = tb_microcreditos.ReferenciaMicrocredito) where IdClienteMicrocredito is null;";
            Statement x = con.createStatement();
            x.execute(query);
            JOptionPane.showMessageDialog(null, "Se han ordenado todos los microcreditos con exito!\n Vaya al apartado Clientes");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"ERROR\n"+e.getMessage());
        }
        return true;
}   



protected float importarArchivo(File archivo, String date) {
    float sum = 0.0f;
        try{     
            String sqlImportar = "insert into tb_microcreditos (numFolio,FechaOperacion,ReferenciaMicrocredito,ConceptoMicrocredito,ImporteMicrocredito) values(?,STR_TO_DATE(REPLACE(?,'/','.'), GET_FORMAT(date,'EUR')),?,?,?)";
            PreparedStatement sentence=con.prepareStatement(sqlImportar);
            BufferedReader lineReader=new BufferedReader(new FileReader(archivo));
            String lineText;
            while((lineText=lineReader.readLine())!=null){
                String [] data = lineText.split(";");
                for(int i=0; i<data.length;i++){
                    if("".equals(data[i])){
                        JOptionPane.showMessageDialog(null, "Se encontro campos vacios.\n No se pudo importar el archivo");
                        return 0;  
                    }
                }
                String numFolio = data[0];
                String FechaOperacion= data[1];
                String ReferenciaMicrocredito= data[2];
                String ConceptoMicrocredito= data[3];
                String ImporteMicrocredito= data[4];
                sum = sum + Float.parseFloat(data[4]);
                sentence.setInt(1,Integer.parseInt(numFolio));
                sentence.setString(2,FechaOperacion);
                sentence.setString(3,ReferenciaMicrocredito);
                sentence.setString(4,ConceptoMicrocredito);
                sentence.setFloat(5,Float.parseFloat(ImporteMicrocredito));
                sentence.addBatch();
                sentence.executeBatch();
                
            }
            lineReader.close();
            insertarPagosMes(sum, date);
            actualizarEstadoClientes();
            JOptionPane.showMessageDialog(null,"Se han insertado los pagos a los estados de cuenta de los clientes");
            Fecha3.setCalendar(null);
            
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Se ha producido un error \n"+ e.getMessage());
            e.printStackTrace();
        }
        return sum;
    }

public boolean insertarPagosMes(float totalmicrocredito, String date){
    String id = null;

    try{
        String Sql= "Insert into tb_totalesmes (Cantidad, fechaTotalesMes) values(?,?)";
        PreparedStatement x = con.prepareStatement(Sql);
        x.setString(1, Float.toString(totalmicrocredito));
        x.setString(2,date);
        x.execute();
        String Sql2= "Select MAX(IdTotalMes) from tb_totalesmes";
        Statement s= con.createStatement();
        ResultSet res = s.executeQuery(Sql2);
        while(res.next()){
            id=  res.getString(1);
        }
        String Sql3= "Update tb_microcreditos SET IdtotalMesF= ? where IdtotalMesF is null";
        PreparedStatement y = con.prepareStatement(Sql3);
        y.setString(1, id);
        y.execute();     
        mostrarPagosMes();
    }catch(Exception e){
        JOptionPane.showMessageDialog(null, "Hubo un error al ingresar el total ");
        e.printStackTrace();
    }
    return true;
}    

public int actualizarEstadoClientes(){
    int id=0;
    float sum, presta1=0f;
    String cantidad; 
    try{
        String query= "Select MAX(IdCliente) from tb_prestamos";
        Statement y = con.createStatement();
        ResultSet res = y.executeQuery(query);
        while(res.next()){
             id = res.getInt(1);
        }
        
        for(int i=1;i<=id;i++){
            sum=0f;
            String sql="Select Importe from tb_prestamos where IdPrestamo = ?";
            PreparedStatement x = con.prepareStatement(sql);
            x.setString(1, Integer.toString(i));
            ResultSet resu = x.executeQuery();
            while(resu.next()){
                presta1= resu.getInt(1);
            }
            String sql2="Select ImporteMicrocredito from tb_microcreditos where IdClienteMicrocredito=?";
            PreparedStatement s = con.prepareStatement(sql2);
            s.setString(1,Integer.toString(i));
            ResultSet res2 = s.executeQuery();
            while(res2.next()){
                cantidad = res2.getString("ImporteMicrocredito");
                sum = sum + Float.parseFloat(cantidad);
            }
           
            if (sum<presta1){
                String sql3="Update tb_clientes set idTipo=2 where IdCliente = ?";
                PreparedStatement f= con.prepareStatement(sql3);
                f.setString(1, Integer.toString(i));
                f.execute();
            }else{
                String sql3="Update tb_clientes set idTipo=1 where IdCliente = ?";
                PreparedStatement f= con.prepareStatement(sql3);
                f.setString(1, Integer.toString(i));
                f.execute();
            }
        }
       
    }catch(Exception e){
        e.printStackTrace();
    }
    return id;
}
public String buscar(String buscarReferencia){
    float sum=0f;
    String[] reg = new String[6];
    String[] tit = {"Id", "Fecha de Operacion", "Referencia", "Concepto", "Importe"};
    String sql = "Select numFolio,date_format(FechaOperacion,\"%d-%m-%Y\") as FechaOperacion, ReferenciaMicrocredito, ConceptoMicrocredito, ImporteMicrocredito from tb_microcreditos where ReferenciaMicrocredito= ? or ConceptoMicrocredito=?";
    DefaultTableModel a = new DefaultTableModel(null,tit);
    try{
        PreparedStatement x=con.prepareStatement(sql);
        x.setString(1, buscarReferencia);
        x.setString(2, buscarReferencia);
        ResultSet res = x.executeQuery();
        while(res.next()){
            reg[0]= res.getString("numFolio");
            reg[1] = res.getString("FechaOperacion");
            reg[2]= res.getString("ReferenciaMicrocredito");
            reg[3]= res.getString("ConceptoMicrocredito");
            reg[4]=res.getString("ImporteMicrocredito");
            a.addRow(reg);
            sum = sum + Float.parseFloat(reg[4]);
        }
        totalPagos.setText(Float.toString(sum));
        tbl_e.setModel(a);
        buscartxt.setText("");
    }catch(Exception e){
        e.printStackTrace();
    }
    return reg[3];
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        prestamo = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_e = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        tb_totales = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        buscartxt = new javax.swing.JTextField();
        jButton6 = new javax.swing.JButton();
        Fecha3 = new com.toedter.calendar.JDateChooser();
        jLabel6 = new javax.swing.JLabel();
        totalPagos = new javax.swing.JLabel();
        filtrarDatos = new javax.swing.JComboBox<>();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        mesMicrocreditos = new com.toedter.calendar.JMonthChooser();
        añoMicrocreditos = new com.toedter.calendar.JYearChooser();
        jLabel14 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        fondo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/microcreditos__1_-removebg-preview (1).png"))); // NOI18N
        jLabel1.setText("Recuperacion de Microcreditos");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 0, 940, 90));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/logo-IME4-removebg-preview (1).png"))); // NOI18N
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 160, 100));

        prestamo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/a (1).png"))); // NOI18N
        prestamo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prestamo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prestamoActionPerformed(evt);
            }
        });
        getContentPane().add(prestamo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/4 (1) (1).png"))); // NOI18N
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, -1));

        jLabel3.setText("Prestamos");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, -1, -1));

        jLabel4.setText("Clientes");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 270, -1, -1));

        tbl_e.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tbl_e);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 180, 680, 260));

        tb_totales.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tb_totales);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 180, 230, 260));

        jLabel5.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel5.setText("Recuperacion de Microcreditos por Mes");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 150, -1, -1));

        jButton4.setBackground(new java.awt.Color(255, 0, 0));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/pdf.png"))); // NOI18N
        jButton4.setText("Generar Reporte");
        jButton4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 520, 140, -1));

        jButton5.setBackground(new java.awt.Color(0, 204, 0));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/excel.png"))); // NOI18N
        jButton5.setText("Importar");
        jButton5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 520, 120, -1));
        getContentPane().add(buscartxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 150, 190, -1));

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/buscar (1).png"))); // NOI18N
        jButton6.setText("Buscar");
        jButton6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 150, -1, -1));

        Fecha3.setDateFormatString("dd-MM-yyyy");
        getContentPane().add(Fecha3, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 520, 140, -1));

        jLabel6.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel6.setText("Total");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 440, -1, -1));

        totalPagos.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        totalPagos.setText("0.00");
        getContentPane().add(totalPagos, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 440, 150, -1));

        filtrarDatos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "100", "200", "500", "1000" }));
        filtrarDatos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        filtrarDatos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filtrarDatosActionPerformed(evt);
            }
        });
        getContentPane().add(filtrarDatos, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 460, 70, -1));

        jButton8.setText("<<");
        jButton8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 460, 50, -1));

        jButton9.setText("<");
        jButton9.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 460, 40, -1));

        jButton10.setText(">");
        jButton10.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton10, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 460, 40, -1));

        jButton11.setText(">>");
        jButton11.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton11, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 460, -1, -1));

        jLabel11.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("Paginas");
        getContentPane().add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 440, -1, -1));

        jLabel10.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setText("Registro por Paginas");
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 440, -1, -1));

        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("Buscar por Referencia o Concepto");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 150, -1, -1));

        jLabel9.setText("Ingresar Archivo de Microcreditos");
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 500, -1, -1));
        getContentPane().add(mesMicrocreditos, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 100, -1, -1));
        getContentPane().add(añoMicrocreditos, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 100, -1, -1));

        jLabel14.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(0, 0, 0));
        jLabel14.setText("Mostrar los microcreditos de:");
        getContentPane().add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 100, -1, -1));

        jButton3.setText("Mostrar");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 100, -1, -1));

        fondo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/fondoprograma (1).jpg"))); // NOI18N
        fondo.setText("jLabel7");
        getContentPane().add(fondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1110, 680));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void prestamoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prestamoActionPerformed

        Prestamos1 pres = new Prestamos1();
        pres.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_prestamoActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Clientes cliente = new Clientes();
        cliente.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        JFileChooser seleccionarArchivo = new JFileChooser();
        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Archivo csv","csv");
       seleccionarArchivo.setFileFilter(filtro);
       int seleccionar = seleccionarArchivo.showOpenDialog(this);
      
       if(seleccionar == JFileChooser.APPROVE_OPTION){
           SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd");
           String date=dFormat.format(Fecha3.getDate());
           File archivo = seleccionarArchivo.getSelectedFile();
           importarArchivo(archivo, date);
           
       }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        String buscarReferencia= buscartxt.getText();
        
        buscar(buscarReferencia);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        JasperReport reporte = null;
        String a= System.getProperty("user.dir");
        String path = a + "\\reportes\\microcreditos.jasper";
        String total;
        int mes = mesMicrocreditos.getMonth()+1;
        int año = añoMicrocreditos.getYear();
        total = totalPagos.getText();
        try {
            Map parametros = new HashMap();

            parametros.put("fecha_1", año);
            parametros.put("fecha_2", mes);
            parametros.put("total", total);
            parametros.put("ruta", a);
            reporte = (JasperReport) JRLoader.loadObjectFromFile(path);
            JasperPrint jprint = JasperFillManager.fillReport(path,parametros,con);
            JasperViewer view = new JasperViewer(jprint,false);
            view.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            view.setVisible(true);

            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        if(filtrar==0){
            JOptionPane.showMessageDialog(null, "No hay mas registros");
        }else{
            filtrar3=filtrar3-filtrar;
            filtrar2=filtrar2-filtrar;
            Mostrar(filtrar3,filtrar2);
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        int mes = mesMicrocreditos.getMonth()+1;
        int año = añoMicrocreditos.getYear();
        try{
            String query="Select Max(numFolio) as maxMicrocredito from tb_microcreditos where YEAR(FechaOperacion)= ? and MONTH(FechaOperacion)= ?";
            PreparedStatement x = con.prepareStatement(query);
            x.setInt(1, año);
            x.setInt(2, mes);
            ResultSet res = x.executeQuery();
            while(res.next()){
                filtrar3 = res.getInt("maxMicrocredito");
            }
            filtrar2 = filtrar3 - filtrar;
            Mostrar(filtrar3,filtrar2);
        }catch(Exception e){
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButton11ActionPerformed

    private void filtrarDatosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filtrarDatosActionPerformed
        String f = filtrarDatos.getSelectedItem().toString();
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
          filtrar2=0;
          filtrar3=0;
          filtrar = Integer.parseInt(f);
        Mostrar(filtrar, filtrar2);
    }//GEN-LAST:event_filtrarDatosActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        filtrar2=0;
        Mostrar(filtrar,filtrar2);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        
        if(filtrar2==0){
            filtrar2=filtrar;
            filtrar3=filtrar2+filtrar;
            Mostrar(filtrar3, filtrar2);
        }else{
            filtrar2 = filtrar2+filtrar;
            filtrar3 = filtrar2 + filtrar;
            Mostrar(filtrar3, filtrar2);
        }
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        filtrar2=0;
        Mostrar(filtrar, filtrar2);
    }//GEN-LAST:event_jButton3ActionPerformed

   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.toedter.calendar.JDateChooser Fecha3;
    private com.toedter.calendar.JYearChooser añoMicrocreditos;
    private javax.swing.JTextField buscartxt;
    private javax.swing.JComboBox<String> filtrarDatos;
    private javax.swing.JLabel fondo;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private com.toedter.calendar.JMonthChooser mesMicrocreditos;
    private javax.swing.JButton prestamo;
    private javax.swing.JTable tb_totales;
    private javax.swing.JTable tbl_e;
    private javax.swing.JLabel totalPagos;
    // End of variables declaration//GEN-END:variables
}
