package netcracker.project.web.beans;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "taskEmployee")
@SessionScoped
public class TaskEmployee implements Serializable{

  private int id;
  private int taskId;
  private int employeeId;

  public TaskEmployee(){
  }  

    //<editor-fold defaultstate="collapsed" desc="гетери сетери">
    public int getId() {
        return id;
    }

    public int getTaskId() {
        return taskId;
    }

    public int getEmployeeId() {
        return employeeId;
    }
    
     public void setId(int id) {
        this.id = id;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }
    //</editor-fold>

}
