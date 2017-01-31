package netcracker.project.web.beans;

import java.io.Serializable;
import netcracker.project.web.roles.User;
//import netcracker.project.web.roles.Employee;
import java.util.Date;
import java.util.Queue;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "project")
@SessionScoped
public class Project implements Serializable{

  private int projectID;
  private String projectName;
  @Autowired
  @Qualifier("company")
  private Company company;
  @Autowired
  @Qualifier("user")
  private User consumer;
  @Autowired
  @Qualifier("user")
  private User projectManager;
  private int enquireID;
  private Date startDate;
  private Date endDate;
  private Queue phases;
  private String description;

    public void createProject() {
    }
  //<editor-fold defaultstate="collapsed" desc="гетери сетери">
  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public void setConsumer(User consumer) {
    this.consumer = consumer;
  }

  public void setProjectManager(User projectManager) {
    this.projectManager = projectManager;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public void setPhases(Queue phases) {
    this.phases = phases;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Company getCompany() {
    return company;
  }

  public User getConsumer() {
    return consumer;
  }

  public User getProjectManager() {
    return projectManager;
  }

  public Date getStartDate() {
    return startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public Queue getPhases() {
    return phases;
  }

  public String getDescription() {
    return description;
  }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }
  
    public int getProjectID() {
        return projectID;
    }

    public void setEnquireID(int enquireID) {
        this.enquireID = enquireID;
    }
    
    public int getEnquireID() {
        return enquireID;
    }
  
  //</editor-fold>    

    

}
