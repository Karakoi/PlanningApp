package netcracker.project.web.beans;

import java.io.Serializable;
import java.util.Set;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "phase")
@SessionScoped
public class Phase implements Serializable {

    private int phaseID;
    private String phaseName;
    private String discription;
    private int beforePhaseID;
//  private Set<Task> tasks;

    public Phase() {
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public int getPhaseID() {
        return phaseID;
    }

    public String getDiscription() {
        return discription;
    }

    public int getBeforePhaseID() {
        return beforePhaseID;
    }

    public void setPhaseID(int phaseID) {
        this.phaseID = phaseID;
    }

    public void setDiscription(String discription) {
        this.discription = discription;
    }

    public void setBeforePhaseID(int beforePhaseID) {
        this.beforePhaseID = beforePhaseID;
    }

}
