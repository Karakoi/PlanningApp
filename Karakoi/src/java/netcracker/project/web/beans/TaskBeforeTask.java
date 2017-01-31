package netcracker.project.web.beans;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "taskBeforeTask")
@SessionScoped
public class TaskBeforeTask implements Serializable{

  private int id;
  private int taskId;
  private int beforeTaskId;

  public TaskBeforeTask(){
  }  

    //<editor-fold defaultstate="collapsed" desc="гетери сетери">
    public int getId() {
        return id;
    }

    public int getTaskId() {
        return taskId;
    }

    public int getBeforeTaskId() {
        return beforeTaskId;
    }
    
     public void setId(int id) {
        this.id = id;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setBeforeTaskId(int beforeTaskId) {
        this.beforeTaskId = beforeTaskId;
    }
    //</editor-fold>

}
