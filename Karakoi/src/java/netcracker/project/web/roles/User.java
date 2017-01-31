package netcracker.project.web.roles;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import netcracker.project.web.beans.Enquire;
import netcracker.project.web.dao.Database;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "user")
@SessionScoped
public class User implements Serializable {

    private int userID;
    private String login;
    private String password;
    private String firstName;
    private String lastName;
    private int age;
    private String address;
    private String phone;
    private String email;
    private String companyID;
    private String qualification;
    private Map<String, Integer> role;

    @Autowired
    @Qualifier("enquire")
    private Enquire enquire;

    private String checkLogin;
    private String checkPassword;

    public User() {
        initRoles();
    }

    public final void initRoles() {
        role = new HashMap<String, Integer>();
        role.put("Administrator", 1);
        role.put("Consumer", 2);
        role.put("Employee", 3);
    }

    public void checkLoginAndPassword(String l) {
        if (l != null) {
            PreparedStatement ps = null;
            Connection con = null;
            ResultSet rs = null;
            try {
                con = Database.getConnection();
                if (con != null) {
                    String sql = "select login,password from users where login = '"
                            + l + "'";
                    ps = con.prepareStatement(sql);
                    rs = ps.executeQuery();
                    rs.next();
                    checkLogin = rs.getString("login");
                    checkPassword = rs.getString("password");
                    if (checkLogin == null) {
                        FacesContext.getCurrentInstance().addMessage(":login_form:login_form_error", new FacesMessage("aaaaaaaaaaa"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "_Catch message_", null));
                System.out.println(FacesContext.getCurrentInstance().getMessageList().get(0).getSummary());
                Database.closeAll(con, ps, rs);
            }
        }
    }

    public String login() {
        checkLoginAndPassword(login);
        String encryptedPass = DigestUtils.sha256Hex(password);
        if (login.equals(checkLogin) && encryptedPass.equals(checkPassword)) {
            return "successLogin";
        } else {
            return "unsuccessLogin";
        }
    }

    public void logout() {
        FacesContext.getCurrentInstance().getExternalContext()
                .invalidateSession();
        FacesContext.getCurrentInstance()
                .getApplication().getNavigationHandler()
                .handleNavigation(FacesContext.getCurrentInstance(), null, "/login.xhtml?faces-redirect=true");
    }

    public int getIdByLogin(String login) {
        int id = 0;
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT user_id FROM users WHERE login = '" + getLogin() + "'";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                id = rs.getInt("user_id");
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
        return id;
    }

    public String getRoleByLogin() {
        String role = "";
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT g.role FROM users u INNER JOIN groups g ON u.group_id = g.group_id WHERE u.login = '" + getLogin() + "'";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    role = rs.getString("role");
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
        return role;
    }

    public String getQulificationByLogin() {
        String qualification = "";
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT qualification FROM users WHERE login = '" + getLogin() + "'";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                qualification = rs.getString("qualification");
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
        if (qualification == null) {
            return "";
        } else {
            return qualification;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="гетери сетери">
    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getQualification() {
        return qualification;
    }

    public Map<String, Integer> getRole() {
        return role;
    }

    public String getCheckLogin() {
        return checkLogin;
    }

    public String getCheckPassword() {
        return checkPassword;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public void setRole(Map<String, Integer> role) {
        this.role = role;
    }

    public void setCheckLogin(String checkLogin) {
        this.checkLogin = checkLogin;
    }

    public void setCheckPassword(String checkPassword) {
        this.checkPassword = checkPassword;
    }

    public Enquire getEnquire() {
        return enquire;
    }

    public void setEnquire(Enquire enquire) {
        this.enquire = enquire;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getUserID() {
        return userID;
    }

    //</editor-fold>
}
