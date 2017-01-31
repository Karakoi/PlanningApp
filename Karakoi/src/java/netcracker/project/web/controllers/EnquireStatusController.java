package netcracker.project.web.controllers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import netcracker.project.web.enums.EnquireStatus;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "enquireStatusController")
@SessionScoped
public class EnquireStatusController implements Serializable{

    private static Map<EnquireStatus, String> statusList = new HashMap<EnquireStatus, String>();

    public EnquireStatusController() {
        statusList.clear();        
        statusList.put(EnquireStatus.Processing, "Processing");
        statusList.put(EnquireStatus.Accepted, "Accepted");
    }

    public Map<EnquireStatus, String> getStatusList() {
        return statusList;
    }
}
