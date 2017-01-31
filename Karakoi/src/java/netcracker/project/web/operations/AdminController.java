package netcracker.project.web.operations;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import netcracker.project.web.beans.Company;
import netcracker.project.web.beans.Enquire;
import netcracker.project.web.beans.Project;
import netcracker.project.web.controllers.QualificationController;
import netcracker.project.web.dao.Database;
import netcracker.project.web.roles.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "adminController")
@SessionScoped
public class AdminController implements Serializable {

    @Autowired
    @Qualifier("user")
    private User user;

    @Autowired
    @Qualifier("user")
    private User empl;
    
    @Autowired
    @Qualifier("project")
    private Project project;

    @Autowired
    @Qualifier("company")
    private Company company;

    private ArrayList<Enquire> allEnquires;//список усіх заявок на проекти
    private String currentSelectedEnquire;//значення поточної заявки,яка обрана для створення проекту   

    private Map<Company, Integer> allCompanies;
    private int currentCompany;//значення вибраної ккомпанії, при створенні працівника
    private Map<User, Integer> allProjectManagers;
    private int currentProjectManager;//значення вибраного проектного менеджера

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    @Qualifier("qualificationController")
    private QualificationController qualificationController;
    private String currentQualification;//значення вибраної кваліфікації, при створенні працівника

    public AdminController() {
        getAllCompaniesFromDB();
        getAllProjectManagersFromDB();
    }

    public String createProject() {
        int i = 0;
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "INSERT INTO project (company_id, project_name, project_manager_id, start_date, end_date, discription, consumer_id, enquire_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
                ps = con.prepareStatement(sql);
                ps.setInt(1, currentCompany);
                ps.setString(2, project.getProjectName());
                ps.setInt(3, currentProjectManager);
                ps.setString(4, simpleDateFormat.format(project.getStartDate()));
                ps.setString(5, simpleDateFormat.format(project.getEndDate()));
                ps.setString(6, project.getDescription());
                ps.setInt(7, getConsumerForEnquireIDFromDB());
                ps.setInt(8, Integer.valueOf(currentSelectedEnquire));
                i = ps.executeUpdate();
                updateEnquireStatus(Integer.valueOf(currentSelectedEnquire));
            }

        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
        if (i > 0) {
            return "successProject";
        } else {
            return "unsuccessProject";
        }
    }

    public void updateEnquireStatus(int enquireID) {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "UPDATE enquire SET status = 'Accepted' WHERE enquire_id =" + enquireID;
                ps = con.prepareStatement(sql);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
    }

    public User newEmpl() {
        if (empl == null) {
            empl = new User();
        }
        return empl;
    }

    public String createEmloyee() {
        int i = 0;
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "INSERT INTO users (group_id, firstname, lastname, age, email, address, phone, company_id, qualification, login, password) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                ps = con.prepareStatement(sql);
                ps.setInt(1, empl.getRole().get("Employee"));
                ps.setString(2, empl.getFirstName());
                ps.setString(3, empl.getLastName());
                ps.setInt(4, empl.getAge());
                ps.setString(5, empl.getEmail());
                ps.setString(6, empl.getAddress());
                ps.setString(7, empl.getPhone());
                ps.setInt(8, currentCompany);
                ps.setString(9, currentQualification);
                ps.setString(10, empl.getLogin());
                ps.setString(11, DigestUtils.sha256Hex(empl.getPassword()));
                i = ps.executeUpdate();
                System.out.println("Data Added Successfully");
            }

        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
        if (i > 0) {
            return "successEmployee";
        } else {
            return "unsuccessEmployee";
        }
    }

    public String createCompany() {
        int i = 0;
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "INSERT INTO company (company_name, address, phone) VALUES(?, ?, ?)";
                ps = con.prepareStatement(sql);
                ps.setString(1, company.getName());
                ps.setString(2, company.getAddress());
                ps.setString(3, company.getPhone());
                i = ps.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
        if (i > 0) {
            return "successCompany";
        } else {
            return "unsuccessCompany";
        }
    }

    //<editor-fold defaultstate="collapsed" desc="допоміжні методи">
    
    public String getCurrEnquireDiscription(){
        String disc = "";
        for(Enquire e: allEnquires){
            if(e.getEnquireID() == Integer.valueOf(currentSelectedEnquire))
            disc = e.getDiscription();            
        }     
        System.out.println("currentSelectedEnquire=" + Integer.valueOf(currentSelectedEnquire));
        
        return disc;
    }
    
    public void qualificationControllerChanged(ValueChangeEvent e) {
        currentQualification = (String) e.getNewValue();
    }

    public void companiesControllerChanged(ValueChangeEvent e) {
        currentCompany = (int) e.getNewValue();
    }

    public void projectManagersControllerChanged(ValueChangeEvent e) {
        currentProjectManager = (int) e.getNewValue();
    }
    //</editor-fold >

    public void getAllEnquiresFromDB() {
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT enquire_id, discription, status, consumer_id FROM enquire WHERE status = 'Processing'";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                allEnquires = new ArrayList<>();
                while (rs.next()) {
                    Enquire en = new Enquire();
                    en.setEnquireID(Integer.valueOf(rs.getString("enquire_id")));
                    en.setDiscription(rs.getString("discription"));
                    en.setStatus(rs.getString("status"));
                    en.setConsumerId(rs.getInt("consumer_id"));
                    allEnquires.add(en);
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
    }

    public int countNewEnquires() {
        int countNewEnquire = 0;
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT COUNT(status) AS c FROM enquire WHERE status = 'Processing'";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                countNewEnquire = rs.getInt("c");
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
        return countNewEnquire;
    }

    public int getConsumerForEnquireIDFromDB() {
        int consumerId = 0;
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT consumer_id FROM enquire WHERE enquire_id = '" + currentSelectedEnquire + "'";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                consumerId = rs.getInt("consumer_id");
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
        return consumerId;
    }

    public void getAllCompaniesFromDB() {
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT company_id, company_name FROM company";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                allCompanies = new HashMap<>();
                while (rs.next()) {
                    Company com = new Company();
                    com.setId(Integer.valueOf(rs.getString("company_id")));
                    com.setName(rs.getString("company_name"));
                    allCompanies.put(com, com.getId());
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
    }

    public void getAllProjectManagersFromDB() {
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT login, lastname, company_id FROM users WHERE qualification = 'Project Manager'";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                allProjectManagers = new HashMap<>();
                while (rs.next()) {
                    User pm = new User();
                    pm.setLogin(rs.getString("login"));
                    pm.setLastName(rs.getString("lastname"));
                    pm.setCompanyID(rs.getString("company_id"));
                    allProjectManagers.put(pm, pm.getIdByLogin(pm.getLogin()));
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="гетери сетери">
    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public ArrayList<Enquire> getAllEnquires() {
        return allEnquires;
    }

    public void setAllEnquires(ArrayList<Enquire> allEnquires) {
        this.allEnquires = allEnquires;
    }

    public User getEmpl() {
        return empl;
    }

    public void setEmpl(User empl) {
        this.empl = empl;
    }

    public QualificationController getQualificationController() {
        return qualificationController;
    }

    public String getCurrentQualification() {
        return currentQualification;
    }

    public void setQualificationController(QualificationController qualificationController) {
        this.qualificationController = qualificationController;
    }

    public void setCurrentQualification(String currentQualification) {
        this.currentQualification = currentQualification;
    }

    public Map<Company, Integer> getAllCompanies() {
        return allCompanies;
    }

    public int getCurrentCompany() {
        return currentCompany;
    }

    public void setAllCompanies(Map<Company, Integer> allCompanies) {
        this.allCompanies = allCompanies;
    }

    public void setCurrentCompany(int currentCompany) {
        this.currentCompany = currentCompany;
    }

    public void setCurrentSelectedEnquire(String currentSelectedEnquire) {
        this.currentSelectedEnquire = currentSelectedEnquire;
    }

    public String getCurrentSelectedEnquire() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
        currentSelectedEnquire = params.get("enquireID");
        
        return currentSelectedEnquire;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public Map<User, Integer> getAllProjectManagers() {
        return allProjectManagers;
    }

    public int getCurrentProjectManager() {
        return currentProjectManager;
    }

    public void setAllProjectManagers(Map<User, Integer> allProjectManagers) {
        this.allProjectManagers = allProjectManagers;
    }

    public void setCurrentProjectManager(int currentProjectManager) {
        this.currentProjectManager = currentProjectManager;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    public Company getCompany() {
        return company;
    }

    public SimpleDateFormat getSimpleDateFormat() {
        return simpleDateFormat;
    }

    //</editor-fold>
}
