package netcracker.project.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class Database {

    private static Connection conn;
    private static InitialContext ic;
    private static DataSource ds;

    public static Connection getConnection() {
        try {
            Locale.setDefault(Locale.ENGLISH);//обов"язково, для неангл локалі(баг Oracle XE)
            ic = new InitialContext();
            ds = (DataSource) ic.lookup("jdbc/NetCrackerProject");
            conn = ds.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }

        return conn;
    }
    
    public static void closeAll(Connection c, PreparedStatement ps) {       
        try {
          if(c != null) {c.close();}
          if(ps != null) {ps.close();}
        } catch (SQLException ex) {
          Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }  
    
    public static void closeAll(Connection c, PreparedStatement ps, ResultSet rs) {       
        try {
          if(c != null) {c.close();}
          if(ps != null) {ps.close();}
          if(rs != null) {rs.close();}
        } catch (SQLException ex) {
          Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }      
    } 
    
}
