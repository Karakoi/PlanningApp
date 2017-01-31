package netcracker.project.web.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import netcracker.project.web.enums.EnquireStatus;
import netcracker.project.web.roles.User;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "enquire")
@SessionScoped
public class Enquire implements Serializable {

  private int enquireID;  
  private String discription;
  private String status;
  private int consumerId;

  public Enquire() {
  }

  public Enquire getInstance() {
    return new Enquire();
  }

  public String getDiscription() {
    return discription;
  }

  public void setDiscription(String discription) {
    this.discription = discription;
  }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public int getEnquireID() {
        return enquireID;
    }

    public void setEnquireID(int enquireID) {
        this.enquireID = enquireID;
    }

    public int getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(int consumerId) {
        this.consumerId = consumerId;
    }
  
}
