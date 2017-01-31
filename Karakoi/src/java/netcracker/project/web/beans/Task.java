package netcracker.project.web.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "task")
@SessionScoped
public class Task implements Serializable {

    private int taskId;
    private String taskName;
    private Date startDate;
    private Date endDate;
    private Set<Integer> afterTasks;
    private Set<Integer> beforeTasks;
    private String requiredQualification;
    private String status;
    private int taskProgress;
    private String discription;

    public Task() {
    }

    //<editor-fold defaultstate="collapsed" desc="гетери сетери">
    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setRequiredQualification(String requiredQualification) {
        this.requiredQualification = requiredQualification;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTaskProgress(int taskProgress) {
        this.taskProgress = taskProgress;
    }

    public String getTaskName() {
        return taskName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getRequiredQualification() {
        return requiredQualification;
    }

    public String getStatus() {
        return status;
    }

    public int getTaskProgress() {
        return taskProgress;
    }

    public void setDiscription(String discription) {
        this.discription = discription;
    }

    public String getDiscription() {
        return discription;
    }

    public void setAfterTasks(Set<Integer> afterTasks) {
        this.afterTasks = afterTasks;
    }

    public void setBeforeTasks(Set<Integer> beforeTasks) {
        this.beforeTasks = beforeTasks;
    }

    public Set<Integer> getAfterTasks() {
        return afterTasks;
    }

    public Set<Integer> getBeforeTasks() {
        return beforeTasks;
    }
    //</editor-fold> 

}
