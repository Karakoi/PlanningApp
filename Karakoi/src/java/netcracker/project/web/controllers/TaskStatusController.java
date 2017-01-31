package netcracker.project.web.controllers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import netcracker.project.web.enums.TaskStatus;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "taskStatusController")
@SessionScoped
public class TaskStatusController implements Serializable {

    private static final Map<TaskStatus, String> taskStatusList = new HashMap<TaskStatus, String>();

    static {
        taskStatusList.clear();
        taskStatusList.put(TaskStatus.NOT_PERFORMED, "Not performed");
        taskStatusList.put(TaskStatus.PERFORMED, "Performed");
        taskStatusList.put(TaskStatus.COMPLETED, "Completed");
    }

    public TaskStatusController() {

    }

    public Map<TaskStatus, String> getTaskStatusList() {
        return taskStatusList;
    }

}
