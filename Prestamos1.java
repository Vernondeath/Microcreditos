
package gui_bd;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

public class Prestamos1 extends javax.swing.JFrame {

     Conexion_BD cx = new Conexion_BD();
    Connection con = cx.conexion();
    boolean flag = false;
    int filtrar = 100;
    int filtrar2= 0;
    int filtrar3=0;

    
    public Prestamos1() {
        initComponents();
        mostrarCreditos();
        this.setResizable(false);
        this.setSize(1125, 650);
        this.setLocationRelativeTo(null);
    }

   
    public String buscarReferencia(String buscarReferencia){
        String[] reg = new String[7];
        String[] tit = {"Id", "Fecha", "Poliza", "Referencia", "Concepto", "Importe"};
        DefaultTableModel a = new DefaultTableModel(null,tit);
        try{
            String buscar ="Select NumFolio,date_format(Fecha,\"%d-%m-%Y\")as Fecha,Poliza,Referencia,Concepto,Importe,IdCredito from tb_Prestamos where Referencia = ? or Concepto=?";
            PreparedStatement x = con.prepareStatement(buscar);
            x.setString(1,buscarReferencia);
            x.setString(2,buscarReferencia);
            ResultSet resu = x.executeQuery();
            while(resu.next()){
                reg[0] = resu.getString("NumFolio");
                reg[1] = resu.getString("Fecha");
                reg[2] = resu.getString("Poliza");
                reg[3] = resu.getString("Referencia");
                reg[4] = resu.getString("Concepto");
                reg[5] = resu.getString("Importe");
                a.addRow(reg);
                totalPrestamos.setText(reg[5]);
            }
            t_prestamos.setModel(a);
            buscarReferenciatxt.setText("");
        }catch(Exception e){
            
            JOptionPane.showMessageDialog(null, "Hubo un error tipo:"+e);
        }
        return reg[4];
        
    }
 
    public int mostrarPrestamos(int filtrar, int filtrar2){
    int año = añoPrestamos.getYear();
    String[] tPrestamos = {"Id","Fecha","Poliza","Referencia","Concepto","Importe"};
    String[] reg = new String[6];
    DefaultTableModel z = new DefaultTableModel(null,tPrestamos);
        try {
            String query = "Select NumFolio, date_format(Fecha,\"%d-%m-%Y\") as Fecha, Poliza, Referencia, Concepto, Importe from tb_prestamos where YEAR(Fecha) = ? and NumFolio between ? and ?;";
            PreparedStatement x= con.prepareStatement(query);
            x.setInt(1, año);
            x.setInt(2, filtrar2);
            x.setInt(3, filtrar);
            ResultSet res = x.executeQuery();
            
            while(res.next()){
            reg[0] = res.getString("NumFolio");
            reg[1] = res.getString("Fecha");
            reg[2] = res.getString("Poliza");
            reg[3] = res.getString("Referencia");
            reg[4] = res.getString("Concepto");
            reg[5] = res.getString("Importe");
            z.addRow(reg);
            
            }
            t_prestamos.setModel(z);
            String query2 = "Select CreditoFinal from tb_creditos where Year(Fecha)= ?";
            PreparedStatement y = con.prepareStatement(query2);
            y.setInt(1, año);
            ResultSet total = y.executeQuery();
            while(total.next()){
                totalPrestamos.setText(total.getString("CreditoFinal"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,"ERROR"+e.getMessage());
        }
        return filtrar;
    }
    
    public float guardarSaldo(float sum, String date){
        String idCredito="";
        try{
            
            String SQL="Insert into tb_creditos(Fecha,CreditoFinal) values (?,?)";
            PreparedStatement x = con.prepareStatement(SQL);
            x.setString(1, date);
            x.setFloat(2, sum);
            x.execute();
            
            //Guarda el id de los creditos en los prestamos
            String id="Select max(IdCredito) as IdMax from tb_creditos";
            Statement s = con.createStatement();
            ResultSet res = s.executeQuery(id);
            while(res.next()){
                idCredito = res.getString("IdMax");
            }
            
            String SQL2="Update tb_prestamos set IdCredito=? where IdCredito is null";
            PreparedStatement y = con.prepareStatement(SQL2);
            y.setString(1, idCredito);
            y.execute();
            
            JOptionPane.showMessageDialog(null, "Se han insertados los datos correctamente");
            mostrarPrestamos(filtrar, filtrar2);
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Hubo un error al guardar el saldo");
            e.printStackTrace();
        }
        return sum;
    }
    
    
    public float importarArchivo(File archivo, String date){
        float sum =0.0f;
        boolean existe;
        ArrayList<String> listaReferencias = new ArrayList<>();
        ArrayList<String> referenciasRepetidas = new ArrayList<>();
        
        try{
            String sqlImportar = "insert into tb_prestamos (NumFolio,Fecha,Poliza,Referencia,Concepto,Importe) values(?,STR_TO_DATE(REPLACE(?,'/','.'), GET_FORMAT(date,'EUR')),?,?,?,?)";
            PreparedStatement sentence=con.prepareStatement(sqlImportar);
            BufferedReader lineReader=new BufferedReader(new FileReader(archivo));
            String lineText;
            
            String sqlImportarCliente = "insert into tb_clientes (referenciaCliente,conceptoCliente)values(?,?)";
            PreparedStatement sentence2=con.prepareStatement(sqlImportarCliente);
            
            while((lineText=lineReader.readLine())!=null){ 
                String [] data = lineText.split(";");
                for(int i=0; i<data.length;i++){
                    if("".equals(data[i])){
                        JOptionPane.showMessageDialog(null, "Se encontro campos vacios.\n No se pudo importar el archivo");
                        return sum;
                        
                    }
                }
                String NumFolio = data[0];
                String Fecha= data[1];
                String Poliza= data[2];
                String Referencia= data[3];                
                String Concepto= data[4];
                String Importe= data[5];
                sum = sum + Float.parseFloat(data[5]);
                String busqueda = Referencia;
                existe = listaReferencias.contains(busqueda);
                if(existe){
                    referenciasRepetidas.add(Referencia);
                }else{
                listaReferencias.add(Referencia);   // lista que guarda las referencias
                }
                
                sentence.setInt(1,Integer.parseInt(NumFolio));
                sentence.setString(2,Fecha);
                sentence.setString(3,Poliza);
                sentence.setString(4,Referencia);
                sentence.setString(5,Concepto);
                sentence.setFloat(6,Float.parseFloat(Importe));
                sentence.addBatch();
                        
                sentence2.setString(1,Referencia);
                sentence2.setString(2,Concepto);
                sentence2.addBatch();
               
                  
            }
            if(referenciasRepetidas.isEmpty()){
            sentence.executeBatch();
            sentence2.executeBatch(); 
            lineReader.close();
            guardarSaldo(sum, date);
            }else{
                JOptionPane.showMessageDialog(null, "No se pudo importar el archivo, hay referencias repetidas:\n"+referenciasRepetidas);
            }
            
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, "Hubo un error al importar el archivo\n"+e);
                e.printStackTrace();
            }
        return sum;
     
    }
    
    public void actualizarClientes(){
        try{
            String query = "update tb_clientes set idTipo = 2 WHERE idTipo is null";
            Statement s = con.createStatement();
            s.executeUpdate(query);
            String query2 = "update tb_prestamos set IdCliente = (SELECT IdCliente from tb_clientes where tb_clientes.referenciaCliente=tb_prestamos.Referencia) where IdCliente is null";
            Statement s2 = con.createStatement();
            s2.executeUpdate(query2);
        }catch( Exception e){
            JOptionPane.showMessageDialog(null,"ERROR"+e.getMessage());
            e.printStackTrace();
        }
        
    }
    
    public boolean mostrarCreditos(){
        String[] tPrestamos = {"Id","Fecha de Registro"," Total Credito"};
        String[] reg = new String[3];
    
    DefaultTableModel z = new DefaultTableModel(null,tPrestamos);
    String query = "Select IdCredito, date_format(Fecha, \"%d-%m-%Y\") as Fecha, CreditoFinal from tb_creditos";
        try {
            Statement s = con.createStatement();
            ResultSet res = s.executeQuery(query);
            
            while(res.next()){
            reg[0] = res.getString("IdCredito");
            reg[1] = res.getString("Fecha");
            reg[2] = res.getString("CreditoFinal");
            z.addRow(reg);
            
            }
            tb_creditosTotal.setModel(z);

        } catch (Exception e) {
            
            JOptionPane.showMessageDialog(null,"ERROR"+e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
    
   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        t_prestamos = new javax.swing.JTable();
        buscarReferenciatxt = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        escogerFecha = new com.toedter.calendar.JDateChooser();
        jScrollPane2 = new javax.swing.JScrollPane();
        tb_creditosTotal = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        totalPrestamos = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        botonAnterior = new javax.swing.JButton();
        paginaSiguiente = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        filtrarDatos = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        añoPrestamos = new com.toedter.calendar.JYearChooser();
        jLabel5 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        fondo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/logo-IME4-removebg-preview (1).png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 160, 100));

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/a (1).png"))); // NOI18N
        jLabel2.setText("Prestamos Otorgados a Clientes");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 10, 490, 70));

        jLabel3.setText("Clientes");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 250, -1, -1));

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/aguinaldo (1).png"))); // NOI18N
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 90, 60));

        jLabel4.setText("Microcreditos");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, -1, -1));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/4 (1) (1).png"))); // NOI18N
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 90, -1));

        t_prestamos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(t_prestamos);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 220, 660, 250));
        getContentPane().add(buscarReferenciatxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 180, 230, 30));

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/buscar (1).png"))); // NOI18N
        jButton4.setText("Buscar");
        jButton4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 190, -1, 20));

        jButton5.setBackground(new java.awt.Color(0, 204, 0));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/excel.png"))); // NOI18N
        jButton5.setText("Importar");
        jButton5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 560, 100, 20));

        escogerFecha.setDateFormatString("dd-MM-yyyy");
        getContentPane().add(escogerFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 560, 120, -1));

        tb_creditosTotal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tb_creditosTotal);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 220, 250, 250));

        jLabel7.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel7.setText("Cantidad Total Prestada por Año");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 190, -1, 20));

        jLabel8.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel8.setText("Total:");
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 470, 60, -1));

        totalPrestamos.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        totalPrestamos.setText("0.00");
        getContentPane().add(totalPrestamos, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 470, 190, 30));

        jButton7.setBackground(new java.awt.Color(247, 12, 45));
        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/pdf.png"))); // NOI18N
        jButton7.setText("Generar Reporte");
        jButton7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 560, 140, 20));

        jButton8.setText("<<");
        jButton8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 490, 50, -1));

        botonAnterior.setText("<");
        botonAnterior.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonAnterior.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAnteriorActionPerformed(evt);
            }
        });
        getContentPane().add(botonAnterior, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 490, 40, -1));

        paginaSiguiente.setText(">");
        paginaSiguiente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        paginaSiguiente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paginaSiguienteActionPerformed(evt);
            }
        });
        getContentPane().add(paginaSiguiente, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 490, 40, -1));

        jButton11.setText(">>");
        jButton11.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton11, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 490, -1, -1));

        jLabel11.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("Paginas");
        getContentPane().add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 470, -1, -1));

        jLabel10.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setText("Registro por Paginas");
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 470, -1, -1));

        filtrarDatos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "100", "200", "500", "1000" }));
        filtrarDatos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        filtrarDatos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filtrarDatosActionPerformed(evt);
            }
        });
        getContentPane().add(filtrarDatos, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 490, 70, -1));

        jLabel12.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 0));
        jLabel12.setText("Importar Archivo de Prestamos");
        getContentPane().add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 530, 260, -1));

        jLabel13.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(0, 0, 0));
        jLabel13.setText("Buscar por Concepto o Referencia");
        getContentPane().add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 160, 200, -1));
        getContentPane().add(añoPrestamos, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 110, 60, 20));

        jLabel5.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Mostrar los prestamos del año de:");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 110, -1, -1));

        jButton6.setText("Mostrar");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 110, -1, 20));

        fondo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui_bd/img/fondoprograma (1).jpg"))); // NOI18N
        getContentPane().add(fondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, 1130, 720));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        Microcreditos micro = new Microcreditos ();
        micro.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Clientes cliente = new Clientes();
        cliente.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        String buscar = buscarReferenciatxt.getText();
        buscarReferencia(buscar);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
         SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd");
         String date=dFormat.format(escogerFecha.getDate());
         JFileChooser seleccionarArchivo = new JFileChooser();
         FileNameExtensionFilter filtro = new FileNameExtensionFilter("Archivo csv","csv");
         seleccionarArchivo.setFileFilter(filtro);
       
         int seleccionar = seleccionarArchivo.showOpenDialog(this);
       if(seleccionar == JFileChooser.APPROVE_OPTION){
           File archivo = seleccionarArchivo.getSelectedFile();
           
           importarArchivo(archivo,date);
           actualizarClientes();
           mostrarCreditos();
           escogerFecha.setCalendar(null);
       }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        JasperReport reporte = null;
        String a= System.getProperty("user.dir");
        String path = a + "\\reportes\\prestamos.jasper";
        String total;
        total = totalPrestamos.getText();
        int fecha = añoPrestamos.getYear();
        
        try{
            Map parametros = new HashMap();
            parametros.put("fecha_1", fecha);
            parametros.put("total", total);
            parametros.put("ruta",a);
            reporte = (JasperReport) JRLoader.loadObjectFromFile(path);
            JasperPrint jprint = JasperFillManager.fillReport(path,parametros,con);
            JasperViewer view = new JasperViewer(jprint,false);
            view.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            view.setVisible(true);
        }catch( Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e);
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void botonAnteriorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAnteriorActionPerformed
        if(filtrar==0){
            JOptionPane.showMessageDialog(null, "No hay mas registros");
        }else{
            filtrar3=filtrar3-filtrar;
            filtrar2=filtrar2-filtrar;
            mostrarPrestamos(filtrar3,filtrar2);
        }
    }//GEN-LAST:event_botonAnteriorActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        int fecha = añoPrestamos.getYear();
        try{
            String query="Select Max(NumFolio) as maxPrestamo from tb_prestamos where YEAR(Fecha)= ?";
            PreparedStatement x = con.prepareStatement(query);
            x.setInt(1, fecha);
            ResultSet res = x.executeQuery();
            while(res.next()){
                filtrar3 = res.getInt("maxPrestamo");
            }
            filtrar2 = filtrar3 - filtrar;
            mostrarPrestamos(filtrar3,filtrar2);
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
        filtrar2 =0;
        filtrar3 =0;
        filtrar = Integer.parseInt(f);
        mostrarPrestamos(filtrar, filtrar2);
    }//GEN-LAST:event_filtrarDatosActionPerformed

    private void paginaSiguienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paginaSiguienteActionPerformed
        
        if(filtrar2==0){
            filtrar2=filtrar;
            filtrar3=filtrar2+filtrar;
            mostrarPrestamos(filtrar3, filtrar2);
        }else{
            filtrar2 = filtrar2+filtrar;
            filtrar3 = filtrar2 + filtrar;
            mostrarPrestamos(filtrar3, filtrar2);
        }
    }//GEN-LAST:event_paginaSiguienteActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        filtrar2=0;
        mostrarPrestamos(filtrar,filtrar2);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        
        mostrarPrestamos(filtrar,filtrar2);
    }//GEN-LAST:event_jButton6ActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.toedter.calendar.JYearChooser añoPrestamos;
    private javax.swing.JButton botonAnterior;
    private javax.swing.JTextField buscarReferenciatxt;
    private com.toedter.calendar.JDateChooser escogerFecha;
    private javax.swing.JComboBox<String> filtrarDatos;
    private javax.swing.JLabel fondo;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton paginaSiguiente;
    private javax.swing.JTable t_prestamos;
    private javax.swing.JTable tb_creditosTotal;
    private javax.swing.JLabel totalPrestamos;
    // End of variables declaration//GEN-END:variables

}
