package netcracker.project.web.operations;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import netcracker.project.web.beans.Phase;
import netcracker.project.web.beans.Project;
import netcracker.project.web.beans.Task;
import netcracker.project.web.beans.TaskBeforeTask;
import netcracker.project.web.beans.TaskEmployee;
import netcracker.project.web.controllers.TaskStatusController;
import netcracker.project.web.dao.Database;
import netcracker.project.web.enums.TaskStatus;
import netcracker.project.web.roles.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "pmController")
@SessionScoped
@DependsOn("qualificationController")
public class PMController implements Serializable {

    @Autowired
    @Qualifier("user")
    private User user;

    @Autowired
    @Qualifier("phase")
    private Phase phase;

    @ManagedProperty(value = "#{task}")
    private Task task;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private Map<Project, String> allProjects;//список усіх проекті даного ПМ
    private String currentSelectedProject;//поточний вибраний проект,для управління
    private String currentProjectID;

    private Map<String, Integer> allPhases;//список усіх фаз
    private Map<String, Integer> allPhasesWithoutNull;//список усіх фаз, без незалежної фази
    private int currentPhase;//значення вибраної попередньої фази у випадаючому меню

    private Map<Task, Integer> allTasks = new HashMap<>();//список усіх задач
    private Map<Task, Integer> allTasksWithoutNull = new HashMap<>();//список усіх задач, без незалежної задачі
    private int currentTask;//значення вибраної задачі у випадаючому меню для Задача-Попер.Задача
    private int currentBeforeTask;//значення вибраної попередньої задачі у випадаючому меню
    private ArrayList<TaskBeforeTask> allRelTaskBeforeTask;//список усіх відношень: Задача - Залежна задача
    public int currentRelTaskBeforeTask;//значення відношення Задача-Попер.Задача для видалення

    private Map<User, Integer> allEmployees = new HashMap<>();//допоміжний список усіх працівників
    private int currentEmployee = 0;//значення вибраного працывника у випадаючому меню для Задача-Працівник
    private int currentTask2 = 0;//значення вибраної задачі у випадаючому меню для Задача-Працівник
    public int currentRelTaskEmployee; //значення відношення Задача-Працівник для видалення
    private ArrayList<TaskEmployee> allRelTaskEmployee;//список усіх відношень: Задача - Працівник

    private String currentQualification;//значення вибраної кваліфікації для задачі у випадаючому меню

    private Map<ArrayList<Integer>, String> allMessages;
    private int currentMessageID;  

    private Map<Task, ArrayList<Integer>> allTasksAndTheirBeforeTasks;//карта усіх задач із списком усіх попередніх задач
    private String currentTask3;//значення вибраної задачі у на сторінці встановлення пізнього старту та пізнього завершення задач
    private Date currentStartLateDate = new Date();//поточна дата пізнього початку вибраної задачі
    private Date currentEndLateDate = new Date();//поточна дата пізнього кінця вибраної задачі

    @ManagedProperty(value = "#{taskStatusController}")
    private TaskStatusController taskStatusController;

    public PMController() {
        taskStatusController = new TaskStatusController();
    }

    public void updateLateDatesForCurrTask() {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "UPDATE task SET late_start_date = '" + simpleDateFormat.format(currentStartLateDate) + "', late_end_date = '"
                        + simpleDateFormat.format(currentEndLateDate) + "' WHERE task_id = " + currentTask3;
                ps = con.prepareStatement(sql);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
    }

    public void getAllTasksWithBeforeTasks() {
        getAllTasksForCurProjectFromDB();
        ArrayList<Integer> allBeforeTasks;
        allTasksAndTheirBeforeTasks = new HashMap<>();
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;

        for (Map.Entry<Task, Integer> task : allTasksWithoutNull.entrySet()) {
            allBeforeTasks = new ArrayList<>();
            try {
                con = Database.getConnection();
                if (con != null) {
                    String sql = "SELECT beforetask_id FROM task_beforetask WHERE task_id = " + task.getValue();
                    ps = con.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        allBeforeTasks.add(rs.getInt("beforetask_id"));
                    }
                }
            } catch (SQLException e) {
                System.out.println(e);
            } finally {
                Database.closeAll(con, ps, rs);
            }
            allTasksAndTheirBeforeTasks.put(task.getKey(), allBeforeTasks);
        }
    }

    public void getAllNewMessageForPM() {
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT message_id, employee_id, task_id, delay_end_date FROM message_for_date_delay WHERE project_manager_id = " + user.getIdByLogin(user.getLogin()) + " AND message_status = 'Unchecked'";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                allMessages = new HashMap<>();
                while (rs.next()) {
                    ArrayList<Integer> ids = new ArrayList<>();
                    ids.add(0, rs.getInt("message_id"));
                    ids.add(1, rs.getInt("employee_id"));
                    ids.add(2, rs.getInt("task_id"));
                    allMessages.put(ids, rs.getString("delay_end_date"));
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
    }

    public void checkMessage() {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "UPDATE message_for_date_delay SET message_status = 'Checked' WHERE message_id = " + currentMessageID;
                ps = con.prepareStatement(sql);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
    }

    public int countMessageForPM() {
       int countMessage = 0;
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT NVL(COUNT(message_id),0) AS c FROM message_for_date_delay WHERE project_manager_id = " + user.getIdByLogin(user.getLogin()) + " AND message_status = 'Unchecked'";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                countMessage = rs.getInt("c");
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
        return countMessage;
    }

    public int countProjects() {
        int countProj = 0;
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT NVL(COUNT(project_id),0) AS c FROM project WHERE project_manager_id = " + user.getIdByLogin(user.getLogin());
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                countProj = rs.getInt("c");
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
        return countProj;
    }

    public void getAllProjectsFromDB() {
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT project_id, project_name FROM project WHERE project_manager_id = " + user.getIdByLogin(user.getLogin());
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                allProjects = new HashMap<>();
                while (rs.next()) {
                    Project proj = new Project();
                    proj.setProjectID(Integer.valueOf(rs.getString("project_id")));
                    proj.setProjectName(rs.getString("project_name"));
                    allProjects.put(proj, String.valueOf(proj.getProjectID()));
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
    }

    public void getAllTasksForCurProjectFromDB() {
        allTasks.clear();
        allTasksWithoutNull.clear();
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT t.task_id, t.task_name, t.required_qualification, t.start_date, t.end_date FROM task t INNER JOIN phase p ON t.phase_id = p.phase_id WHERE p.project_id = " + currentProjectID;
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                Task t1 = new Task();
                t1.setTaskName("Independent");
                allTasks.put(t1, 0);
                while (rs.next()) {
                    Task t = new Task();
                    t.setTaskName(rs.getString("task_name"));
                    t.setRequiredQualification(rs.getString("required_qualification"));
                    t.setTaskId(rs.getInt("task_id"));
                    try {
                        t.setStartDate(simpleDateFormat.parse(rs.getString("start_date")));
                        t.setEndDate(simpleDateFormat.parse(rs.getString("end_date")));
                    } catch (ParseException ex) {
                        Logger.getLogger(PMController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    allTasks.put(t, t.getTaskId());
                    allTasksWithoutNull.put(t, t.getTaskId());
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
    }

    public String getCompanyIDByProjIDFromDB() {
        String companyId = "";
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT company_id FROM project WHERE project_id = " + currentProjectID;
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                allEmployees = new HashMap<>();
                while (rs.next()) {
                    companyId = rs.getString("company_id");
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }       
        return companyId;
    }

    public void getAllEmployeesFromDB() {
        String companyID = getCompanyIDByProjIDFromDB();
        allEmployees.clear();
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT u.user_id, u.firstname, u.lastname, u.qualification FROM users u INNER JOIN groups g ON u.group_id = g.group_id ";
                String sql2 = sql.concat("WHERE g.role = 'Employee' AND u.company_id = " + companyID);
                ps = con.prepareStatement(sql2);
                rs = ps.executeQuery();
                while (rs.next()) {
                    User emp = new User();
                    emp.setQualification(rs.getString("qualification"));
                    emp.setLastName(rs.getString("lastname"));
                    emp.setFirstName(rs.getString("firstname"));
                    allEmployees.put(emp, rs.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
    }

    public void getAllRelTBTFromDB() {
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT tbt.id, tbt.task_id, tbt.beforetask_id FROM task_beforetask tbt "
                        + "INNER JOIN task t ON tbt.task_id = t.task_id "
                        + "WHERE t.phase_id IN  ("
                        + "SELECT ph.phase_id FROM phase ph "
                        + "INNER JOIN project pr ON ph.project_id = pr.project_id "
                        + "WHERE pr.project_id = " + currentProjectID + ")";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                allRelTaskBeforeTask = new ArrayList<>();
                while (rs.next()) {
                    TaskBeforeTask tbt = new TaskBeforeTask();
                    tbt.setId(rs.getInt("id"));
                    tbt.setTaskId(rs.getInt("task_id"));
                    tbt.setBeforeTaskId(rs.getInt("beforetask_id"));
                    allRelTaskBeforeTask.add(tbt);
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
    }

    public void getAllRelTaskEmployeeFromDB() {
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT te.id, te.task_id, te.employee_id FROM task_employee te "
                        + "INNER JOIN task t ON te.task_id = t.task_id "
                        + "WHERE t.phase_id IN ("
                        + "SELECT ph.phase_id FROM phase ph "
                        + "INNER JOIN project pr ON ph.project_id = pr.project_id "
                        + "WHERE pr.project_id = " + currentProjectID + ")";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                allRelTaskEmployee = new ArrayList<>();
                while (rs.next()) {
                    TaskEmployee te = new TaskEmployee();
                    te.setId(rs.getInt("id"));
                    te.setTaskId(rs.getInt("task_id"));
                    te.setEmployeeId(rs.getInt("employee_id"));
                    allRelTaskEmployee.add(te);
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
    }

    public void deleteRelTBTFromDB() {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "DELETE FROM task_beforetask WHERE id = " + currentRelTaskBeforeTask;
                ps = con.prepareStatement(sql);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
    }

    public void deleteRelTaskEmployeeFromDB() {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "DELETE FROM task_employee WHERE id = " + currentRelTaskEmployee;
                ps = con.prepareStatement(sql);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
    }

    public void createRelationsTaskEmployee() {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "INSERT INTO task_employee (task_id, employee_id) VALUES(?, ?)";
                ps = con.prepareStatement(sql);
                ps.setInt(1, currentTask2);
                ps.setInt(2, currentEmployee);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
        
    }

    public void createRelationsTaskBeforeTask() {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                if (currentBeforeTask != 0) {
                    String sql = "INSERT INTO task_beforetask (task_id, beforetask_id) VALUES(?, ?)";
                    ps = con.prepareStatement(sql);
                    ps.setInt(1, currentTask);
                    ps.setInt(2, currentBeforeTask);
                } else {
                    String sql = "INSERT INTO task_beforetask (task_id) VALUES(?)";
                    ps = con.prepareStatement(sql);
                    ps.setInt(1, currentTask);
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
    }

    public Phase getNewPhase() {
        if (phase == null) {
            phase = new Phase();
        }
        return phase;
    }

    public String createPhase() {
        int i = 1;
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {

                if (currentPhase == 0) {
                    String sql = "INSERT INTO phase (phase_name, discription, project_id) VALUES(?, ?, ?)";
                    ps = con.prepareStatement(sql);
                    ps.setString(1, phase.getPhaseName());
                    ps.setString(2, phase.getDiscription());
                    ps.setString(3, currentProjectID);
                } else {
                    String sql = "INSERT INTO phase (phase_name, discription, before_phase_id, project_id) VALUES(?, ?, ?, ?)";
                    ps = con.prepareStatement(sql);
                    ps.setString(1, phase.getPhaseName());
                    ps.setString(2, phase.getDiscription());
                    ps.setInt(3, currentPhase);
                    ps.setString(4, currentProjectID);
                }
                i = ps.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
//        if (i > 0) {
//            return "successPhase";
//        } else {
//            return "unsuccessPhase";
//        }
        if (i > 0) {
            return "pmMainPage.xhtml?faces-redirect=true&projectID=" + currentProjectID;
        } else {
            return "createPhase.xhtml?faces-redirect=true&projectID=" + currentProjectID;
        }
    }

    public String createTask() {
        int i = 0;
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "INSERT INTO task (task_name, start_date, end_date, required_qualification, progress, phase_id, discription, status) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
                ps = con.prepareStatement(sql);
                ps.setString(1, task.getTaskName());
                ps.setString(2, simpleDateFormat.format(task.getStartDate()));
                ps.setString(3, simpleDateFormat.format(task.getEndDate()));
                ps.setString(4, currentQualification);
                ps.setInt(5, 0);
                ps.setInt(6, currentPhase);
                ps.setString(7, task.getDiscription());
                ps.setString(8, taskStatusController.getTaskStatusList().get(TaskStatus.NOT_PERFORMED));
                i = ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps);
        }
//        if (i > 0) {
//            return "successTask";
//        } else {
//            return "unsuccessTask";
//        }
        if (i > 0) {
            return "pmMainPage.xhtml?faces-redirect=true&projectID=" + currentProjectID;
        } else {
            return "createTask.xhtml?faces-redirect=true&projectID=" + currentProjectID;
        }
    }

    public void getAllPhasesFromDB() {
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT phase_id, discription, phase_name, before_phase_id FROM phase WHERE project_id = " + currentProjectID;
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                allPhases = new HashMap<>();
                allPhasesWithoutNull = new HashMap<>();
                while (rs.next()) {
                    Phase ph = new Phase();
                    ph.setPhaseID(Integer.valueOf(rs.getString("phase_id")));
                    ph.setDiscription(rs.getString("discription"));
                    ph.setPhaseName(rs.getString("phase_name"));
                    String test = rs.getString("before_phase_id");
                    if (test != null) {
                        ph.setBeforePhaseID(Integer.valueOf(test));
                    } else {
                        ph.setBeforePhaseID(0);
                    }
                    allPhases.put(ph.getPhaseName(), ph.getPhaseID());
                    allPhasesWithoutNull.put(ph.getPhaseName(), ph.getPhaseID());
                }
                allPhases.put("Independent", 0);
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
        System.out.println("pmController.getAllPhasesFromDB() -allPhases = " + allPhases);
    }

    //<editor-fold defaultstate="collapsed" desc="допоміжні методи">
    public void phasesControllerChanged(ValueChangeEvent e) {
        currentPhase = (int) e.getNewValue();
    }

    public void qualificationsControllerChanged(ValueChangeEvent e) {
        currentQualification = (String) e.getNewValue();
    }

    public void tasksControllerChanged(ValueChangeEvent e) {
        currentTask = (int) e.getNewValue();
    }

    public void tasksControllerChanged2(ValueChangeEvent e) {
        currentTask2 = (int) e.getNewValue();
    }

    public void employeesControllerChanged(ValueChangeEvent e) {
        currentEmployee = (int) e.getNewValue();
    }

    public void beforeTasksControllerChanged(ValueChangeEvent e) {
        currentBeforeTask = (int) e.getNewValue();
    }

    public Task newTask() {
        if (task == null) {
            task = new Task();
        }
        return task;
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="гетери сетери">
    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setAllProjects(Map<Project, String> allProjects) {
        this.allProjects = allProjects;
    }

    public Map<Project, String> getAllProjects() {
        return allProjects;
    }

    public void setCurrentSelectedProject(String currentSelectedProject) {
        this.currentSelectedProject = currentSelectedProject;
    }

    public String getCurrentSelectedProject() {
//        FacesContext fc = FacesContext.getCurrentInstance();
//        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
//        currentSelectedProject = params.get("projectID");
//        if (currentSelectedProject != null) {
//            currentProjectID = currentSelectedProject;
//        }
        return currentSelectedProject;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public void setAllPhases(Map<String, Integer> allPhases) {
        this.allPhases = allPhases;
    }

    public void setCurrentPhase(int currentPhase) {
        this.currentPhase = currentPhase;
    }

    public Phase getPhase() {
        return phase;
    }

    public Map<String, Integer> getAllPhases() {
        return allPhases;
    }

    public int getCurrentPhase() {
        return currentPhase;
    }

    public String getCurrentProjectID() {
        return currentProjectID;
    }

    public Task getTask() {
        return task;
    }

    public SimpleDateFormat getSimpleDateFormat() {
        return simpleDateFormat;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    public void setCurrentProjectID(String currentProjectID) {
        if (currentProjectID != null) {
            this.currentProjectID = currentProjectID;
        }
    }

    public void setCurrentQualification(String currentQualification) {
        this.currentQualification = currentQualification;
    }

    public String getCurrentQualification() {
        return currentQualification;
    }

    public void setAllPhasesWithoutNull(Map<String, Integer> allPhasesWithoutNull) {
        this.allPhasesWithoutNull = allPhasesWithoutNull;
    }

    public Map<String, Integer> getAllPhasesWithoutNull() {
        return allPhasesWithoutNull;
    }

    public void setAllTasks(Map<Task, Integer> allTasks) {
        this.allTasks = allTasks;
    }

    public void setCurrentTask(int currentTask) {
        this.currentTask = currentTask;
    }

    public void setCurrentBeforeTask(int currentBeforeTask) {
        this.currentBeforeTask = currentBeforeTask;
    }

    public Map<Task, Integer> getAllTasks() {
        return allTasks;
    }

    public int getCurrentTask() {
        return currentTask;
    }

    public int getCurrentBeforeTask() {
        return currentBeforeTask;
    }

    public void setAllTasksWithoutNull(Map<Task, Integer> allTasksWithoutNull) {
        this.allTasksWithoutNull = allTasksWithoutNull;
    }

    public Map<Task, Integer> getAllTasksWithoutNull() {
        return allTasksWithoutNull;
    }

    public void setAllRelTaskBeforeTask(ArrayList<TaskBeforeTask> allRelTaskBeforeTask) {
        this.allRelTaskBeforeTask = allRelTaskBeforeTask;
    }

    public ArrayList<TaskBeforeTask> getAllRelTaskBeforeTask() {
        return allRelTaskBeforeTask;
    }

    public void setCurrentRelTaskBeforeTask(int currentRelTaskBeforeTask) {
        this.currentRelTaskBeforeTask = currentRelTaskBeforeTask;
    }

    public int getCurrentRelTaskBeforeTask() {
        return currentRelTaskBeforeTask;
    }

    public void setAllEmployees(Map<User, Integer> allEmployees) {
        this.allEmployees = allEmployees;
    }

    public void setCurrentEmployee(int currentEmployee) {
        this.currentEmployee = currentEmployee;
    }

    public void setCurrentTask2(int currentTask2) {
        this.currentTask2 = currentTask2;
    }

    public void setCurrentRelTaskEmployee(int currentRelTaskEmployee) {
        this.currentRelTaskEmployee = currentRelTaskEmployee;
    }

    public void setAllRelTaskEmployee(ArrayList<TaskEmployee> allRelTaskEmployee) {
        this.allRelTaskEmployee = allRelTaskEmployee;
    }

    public Map<User, Integer> getAllEmployees() {
        return allEmployees;
    }

    public int getCurrentEmployee() {
        return currentEmployee;
    }

    public int getCurrentTask2() {
        return currentTask2;
    }

    public int getCurrentRelTaskEmployee() {
        return currentRelTaskEmployee;
    }

    public ArrayList<TaskEmployee> getAllRelTaskEmployee() {
        return allRelTaskEmployee;
    }

//    public void setQualificationController(QualificationController qualificationController) {
//        this.qualificationController = qualificationController;
//    }
//
//    public QualificationController getQualificationController() {
//        return qualificationController;
//    }
    public void setAllMessages(Map<ArrayList<Integer>, String> allMessages) {
        this.allMessages = allMessages;
    }

    public Map<ArrayList<Integer>, String> getAllMessages() {
        return allMessages;
    }

    public void setCurrentMessageID(int currentMessageID) {
        this.currentMessageID = currentMessageID;
    }

    public int getCurrentMessageID() {
        return currentMessageID;
    }

    public void setAllTasksAndTheirBeforeTasks(Map<Task, ArrayList<Integer>> allTasksAndTheirBeforeTasks) {
        this.allTasksAndTheirBeforeTasks = allTasksAndTheirBeforeTasks;
    }

    public void setCurrentTask3(String currentTask3) {
        this.currentTask3 = currentTask3;
    }

    public Map<Task, ArrayList<Integer>> getAllTasksAndTheirBeforeTasks() {
        return allTasksAndTheirBeforeTasks;
    }

    public String getCurrentTask3() {
        return currentTask3;
    }

    public void setCurrentStartLateDate(Date currentStartLateDate) {
        this.currentStartLateDate = currentStartLateDate;
    }

    public void setCurrentEndLateDate(Date currentEndLateDate) {
        this.currentEndLateDate = currentEndLateDate;
    }

    public Date getCurrentStartLateDate() {
        return currentStartLateDate;
    }

    public Date getCurrentEndLateDate() {
        return currentEndLateDate;
    }

    public void setTaskStatusController(TaskStatusController taskStatusController) {
        this.taskStatusController = taskStatusController;
    }

    public TaskStatusController getTaskStatusController() {
        return taskStatusController;
    }

    //</editor-fold >
}
