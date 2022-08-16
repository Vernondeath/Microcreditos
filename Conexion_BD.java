/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui_bd;

import java.sql.DriverManager;
import java.sql.Connection;
import javax.swing.JOptionPane;

/**
 *
 * @author monse
 */
public class Conexion_BD {
//crear una variable que invoque la conexion 
    
    Connection conectar = null;
    
    public Connection conexion(){
        try {
           Class.forName("com.mysql.jdbc.Driver");
                conectar = (Connection)
                        DriverManager.getConnection("jdbc:mysql://localhost/microcreditos","root","");
                   
                
                
        } catch (Exception e) {
            
            JOptionPane.showMessageDialog(null,"Eror de Conexion"+e.getMessage());
            
        }
    
    return  conectar;
    
    }
    
    
   
    
}
