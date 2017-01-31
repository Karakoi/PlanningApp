package netcracker.project.web.operations;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import netcracker.project.web.beans.Task;
import netcracker.project.web.controllers.TaskStatusController;
import netcracker.project.web.dao.Database;
import netcracker.project.web.enums.TaskStatus;
import netcracker.project.web.roles.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "employeeController")
@SessionScoped
public class EmployeeController implements Serializable {

    @Autowired
    @Qualifier("user")
    private User user;
    private DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    private ArrayList<Task> allTasks;//список усіх задач
    private ArrayList<Task> allNewTasks;//список нових задач

    @Autowired
    @Qualifier("task")
    private Task currentTask;//поточна задача для відображення працівнику
    private String currentSelectedTask;//поточний вибрана задача,для управління
    private String currentTaskID;
    private String currentTaskProgressToUpdate;//значення прогресу виконання задачі для зміни
    private boolean countTasksSwitch = true;//перекмикач для виведення кількості нових або усіх задач

    private Date currentDelayEndDate;//дата переносу задачі  

    public EmployeeController() {
    }

    public void sendMessagePMForDelayTaskEnd() {
        int i = 0;
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "INSERT INTO message_for_date_delay (project_manager_id, employee_id, task_id, delay_end_date, message_status) VALUES(?, ?, ?, ?, ?)";
                ps = con.prepareStatement(sql);
                ps.setInt(1, getProjectManagerForTaskID());
                ps.setInt(2, user.getIdByLogin(user.getLogin()));
                ps.setString(3, currentTaskID);
                ps.setString(4, formatter.format(currentDelayEndDate));
                ps.setString(5, "Unchecked");
                i = ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
    }

    public int getProjectManagerForTaskID() {
        int pmID = 0;
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT project_manager_id FROM project "
                        + "WHERE project_id = (SELECT ph.project_id FROM project pr INNER JOIN phase ph ON pr.project_id = ph.project_id "
                        + "WHERE ph.phase_id = (SELECT ph.phase_id FROM phase ph INNER JOIN task t ON ph.phase_id=t.phase_id "
                        + "WHERE t.task_id = " + currentTaskID + "))";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                pmID = rs.getInt("project_manager_id");
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
        return pmID;
    }

    public void getAllTasksFromDB() {
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT t.task_id, t.task_name, t.progress, t.status FROM task t INNER JOIN task_employee te ON t.task_id = te.task_id WHERE te.employee_id = " + user.getIdByLogin(user.getLogin());
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                allTasks = new ArrayList<>();
                while (rs.next()) {
                    Task t = new Task();
                    t.setTaskId(Integer.valueOf(rs.getString("task_id")));
                    t.setTaskName(rs.getString("task_name"));
                    t.setTaskProgress(Integer.valueOf(rs.getString("progress")));
                    t.setStatus(rs.getString("status"));
                    allTasks.add(t);
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
    }

    public void getAllNewTasksFromDB() {
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT t.task_id, t.task_name, t.progress, t.status FROM task t INNER JOIN task_employee te ON t.task_id = te.task_id "
                        + "WHERE te.employee_id = " + user.getIdByLogin(user.getLogin()) + " AND t.status NOT LIKE 'Completed'";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                allNewTasks = new ArrayList<>();
                while (rs.next()) {
                    Task t = new Task();
                    t.setTaskId(Integer.valueOf(rs.getString("task_id")));
                    t.setTaskName(rs.getString("task_name"));
                    t.setTaskProgress(Integer.valueOf(rs.getString("progress")));
                    t.setStatus(rs.getString("status"));
                    allNewTasks.add(t);
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
    }

    public String getTaskByTaskIDFromDB() {
        getCurrentSelectedTask();
        String data1 = "";
        String data2 = "";
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT task_name, progress, discription, start_date, end_date, status FROM task WHERE task_id = " + currentTaskID;
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    currentTask.setTaskName(rs.getString("task_name"));
                    currentTask.setTaskProgress(Integer.valueOf(rs.getString("progress")));
                    currentTask.setDiscription(rs.getString("discription"));
                    String status = rs.getString("status");
                    if (status == null) {
                        TaskStatusController tsc = new TaskStatusController();
                        currentTask.setStatus(tsc.getTaskStatusList().get(TaskStatus.NOT_PERFORMED));
                    } else {
                        currentTask.setStatus(status);
                    }
                    try {
                        currentTask.setStartDate(formatter.parse(rs.getString("start_date")));
                        currentTask.setEndDate(formatter.parse(rs.getString("end_date")));
                    } catch (ParseException ex) {
                        Logger.getLogger(EmployeeController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
        if (currentTask != null) {
            return "show";
        } else {
            return "don't show";
        }
    }

    public void updateTaskStatusToPerfomed() {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                TaskStatusController tsc = new TaskStatusController();
                String sql = "UPDATE task SET status = '" + tsc.getTaskStatusList().get(TaskStatus.PERFORMED) + "' WHERE task_id =" + currentTaskID;
                ps = con.prepareStatement(sql);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
    }

    public void updateTaskStatusToCompleted() {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                TaskStatusController tsc = new TaskStatusController();
                String sql = "UPDATE task SET status = '" + tsc.getTaskStatusList().get(TaskStatus.COMPLETED) + "', progress = 100 WHERE task_id =" + currentTaskID;
                ps = con.prepareStatement(sql);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
    }

    public void updateTaskProgress() {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                int temp = 0;
                if (currentTaskProgressToUpdate == null || currentTaskProgressToUpdate.equals("")) {
                    temp = 0;
                } else {
                    temp = Integer.valueOf(currentTaskProgressToUpdate);
                }
                String sql = "UPDATE task SET progress = " + temp + ", status = 'Performed' WHERE task_id =" + currentTaskID;
                ps = con.prepareStatement(sql);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
    }

    public int countAllTasks() {
        int countTasks = 0;
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT COUNT(task_id) AS c FROM task_employee WHERE employee_id = " + user.getIdByLogin(user.getLogin());
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                countTasks = rs.getInt("c");
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
        return countTasks;
    }

    public int countNewTasks() {
        int countTasks = 0;
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT COUNT(te.task_id) AS c FROM task t INNER JOIN task_employee te ON t.task_id = te.task_id "
                        + "WHERE te.employee_id = " + user.getIdByLogin(user.getLogin()) + " AND t.status NOT LIKE 'Completed'";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                countTasks = rs.getInt("c");
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
        return countTasks;
    }

    //<editor-fold defaultstate="collapsed" desc="гетери сетери">
    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setAllTasks(ArrayList<Task> allTasks) {
        this.allTasks = allTasks;
    }

    public ArrayList<Task> getAllTasks() {
        return allTasks;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setFormatter(SimpleDateFormat formatter) {
        this.formatter = formatter;
    }

    public void setCurrentSelectedTask(String currentSelectedTask) {
        this.currentSelectedTask = currentSelectedTask;
    }

    public void setCurrentTaskID(String currentTaskID) {
        this.currentTaskID = currentTaskID;
    }

    public DateFormat getFormatter() {
        return formatter;
    }

    public String getCurrentSelectedTask() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
        currentSelectedTask = params.get("taskID");
        if (currentSelectedTask != null) {
            currentTaskID = currentSelectedTask;
        }
        return currentSelectedTask;
    }

    public String getCurrentTaskID() {
        return currentTaskID;
    }

    public void setFormatter(DateFormat formatter) {
        this.formatter = formatter;
    }

    public void setCurrentTaskProgressToUpdate(String currentTaskProgressToUpdate) {
        this.currentTaskProgressToUpdate = currentTaskProgressToUpdate;
    }

    public String getCurrentTaskProgressToUpdate() {
        return currentTaskProgressToUpdate;
    }

    public void setAllNewTasks(ArrayList<Task> allNewTasks) {
        this.allNewTasks = allNewTasks;
    }

    public ArrayList<Task> getAllNewTasks() {
        return allNewTasks;
    }

    public void setCountTasksSwitch(boolean countTasksSwitch) {
        this.countTasksSwitch = countTasksSwitch;
    }

    public boolean isCountTasksSwitch() {
        return countTasksSwitch;
    }

    public void setCurrentDelayEndDate(Date currentDelayEndDate) {
        this.currentDelayEndDate = currentDelayEndDate;
    }

    public Date getCurrentDelayEndDate() {
        return currentDelayEndDate;
    }

    //</editor-fold >
}
