package netcracker.project.web.controllers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import netcracker.project.web.enums.Qualification;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "qualificationController")
@SessionScoped
public class QualificationController  implements Serializable{
        
    private static final Map<Qualification, String> qualificationList = new HashMap<Qualification, String>();

    static{
        qualificationList.clear();        
        qualificationList.put(Qualification.JUNIOR_DEVELOPER, "Junior");
        qualificationList.put(Qualification.MIDDLE_DEVELOPER, "Middle");
        qualificationList.put(Qualification.SENIOR_DEVELOPER, "Senior");
        qualificationList.put(Qualification.ANALYST, "Analyst");
        qualificationList.put(Qualification.ARCHITECT, "Architect");
        qualificationList.put(Qualification.TEAM_LEADER, "Team Lead");
        qualificationList.put(Qualification.TESTER, "Tester");
        qualificationList.put(Qualification.PROJECT_MANAGER, "Project Manager");
    }
    
    public QualificationController() {
        
    }

    public Map<Qualification, String> getQualificationList() {
        return qualificationList;
    }
}
