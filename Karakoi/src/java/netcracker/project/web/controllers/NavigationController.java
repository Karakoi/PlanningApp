package netcracker.project.web.controllers;

import javax.faces.bean.ManagedBean;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "navigationController")
public class NavigationController {

  public String moveToLoginPage() { 
    return "login";
  }

  public String moveToRegistrationPage() {
    return "registration";
  }

  public String moveToMainPage() {
    return "main";
  }
  
  public String moveFromCreateEnquireToMainPage() {  
    return "enquire-main";
  }
  
  public String moveFromMainToCreateEnquirePage() {  
    return "main-enquire";
  }
}
