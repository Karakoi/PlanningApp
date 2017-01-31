package netcracker.project.web.beans;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "company")
@SessionScoped
public class Company implements Serializable{
  
  private int id;  
  private String name;  
  private String address;
  private String phone;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
 
  public void setAddress(String address) {
    this.address = address;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }
  
  public String getAddress() {
    return address;
  }

  public String getPhone() {
    return phone;
  }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
  
  
}
