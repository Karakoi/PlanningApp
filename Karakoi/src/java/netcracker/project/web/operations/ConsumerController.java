package netcracker.project.web.operations;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import netcracker.project.web.beans.Enquire;
import netcracker.project.web.controllers.EnquireStatusController;
import netcracker.project.web.dao.Database;
import netcracker.project.web.enums.EnquireStatus;
import netcracker.project.web.roles.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "consumerController")
@SessionScoped
public class ConsumerController implements Serializable {

    @Autowired
    @Qualifier("user")
    private User user;

    @Autowired
    @Qualifier("enquireStatusController")
    private EnquireStatusController enquireStatusController;

    private String currentEnquireStatus; // поточне значення критерію пошуку заявок проектів(h:selectOneMenu)
    private ArrayList<Enquire> currentProjectList; // поточне значення списку проектів замовника,знайдених по критерію

    public ConsumerController() { 
    }

    public String registration() {
        int i = 0;
        if (user.getFirstName() != null) {
            PreparedStatement ps = null;
            Connection con = null;
            try {
                con = Database.getConnection();
                if (con != null) {
                    String sql = "INSERT INTO users (group_id, firstname, lastname, age, email, address, phone, login, password) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    ps = con.prepareStatement(sql);
                    ps.setInt(1, user.getRole().get("Consumer"));
                    ps.setString(2, user.getFirstName());
                    ps.setString(3, user.getLastName());
                    ps.setInt(4, user.getAge());
                    ps.setString(5, user.getEmail());
                    ps.setString(6, user.getAddress());
                    ps.setString(7, user.getPhone());
                    ps.setString(8, user.getLogin());
                    ps.setString(9, DigestUtils.sha256Hex(user.getPassword()));
                    i = ps.executeUpdate();
                }

            } catch (SQLException e) {
                System.out.println(e);
            } finally {
                Database.closeAll(con, ps);
            }
        }
        if (i > 0) {
            return "successRegistration";
        } else {
            return "unsuccessRegistration";
        }
    }

    public String createEnquire() {
        int i = 0;
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "INSERT INTO enquire (discription, status, consumer_id) VALUES(?, ?, ?)";
                ps = con.prepareStatement(sql);
                ps.setString(1, user.getEnquire().getDiscription());
                ps.setString(2, enquireStatusController.getStatusList().get(EnquireStatus.Processing));
                ps.setInt(3, user.getIdByLogin(user.getLogin()));
                i = ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }

        if (i > 0) {
            return "successEnquire";
        } else {
            return "unsuccessEnquire";
        }
    }

    public void getEnquiresByStatus() {
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {String sql = "SELECT enquire_id, discription, status FROM enquire WHERE consumer_id = " + user.getIdByLogin(user.getLogin()) + " AND status = '" + currentEnquireStatus + "'";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
               currentProjectList = new ArrayList<>();
                while (rs.next()) {
                    Enquire en = new Enquire();
                    en.setEnquireID(Integer.valueOf(rs.getString("enquire_id")));
                    en.setDiscription(rs.getString("discription"));
                    en.setStatus(rs.getString("status"));                               
                    currentProjectList.add(en);
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }        
    }

    public void getProjectReportByEnquireID(){
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT enquire_id, discription, status FROM project WHERE consumer_id = " + user.getIdByLogin(user.getLogin()) + " AND status = '" + currentEnquireStatus + "'";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
               currentProjectList = new ArrayList<>();
                while (rs.next()) {
                    Enquire en = new Enquire();
                    en.setEnquireID(Integer.valueOf(rs.getString("enquire_id")));
                    en.setDiscription(rs.getString("discription"));
                    en.setStatus(rs.getString("status"));                               
                    currentProjectList.add(en);
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }   
    }
    
    public void currentEnquireStatusChanged(ValueChangeEvent e) {
        currentEnquireStatus = (String) e.getNewValue();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public EnquireStatusController getEnquireStatus() {
        return enquireStatusController;
    }

    public void setEnquireStatus(EnquireStatusController enquireStatus) {
        this.enquireStatusController = enquireStatus;
    }

    public ArrayList<Enquire> getCurrentProjectList() {
        return currentProjectList;
    }

    public void setCurrentProjectList(ArrayList<Enquire> currentProjectList) {
        this.currentProjectList = currentProjectList;
    }

    public EnquireStatusController getEnquireStatusController() {
        return enquireStatusController;
    }

    public String getCurrentEnquireStatus() {
        return currentEnquireStatus;
    }

    public void setEnquireStatusController(EnquireStatusController enquireStatusController) {
        this.enquireStatusController = enquireStatusController;
    }

    public void setCurrentEnquireStatus(String currentEnquireStatus) {
        this.currentEnquireStatus = currentEnquireStatus;
    }

}
