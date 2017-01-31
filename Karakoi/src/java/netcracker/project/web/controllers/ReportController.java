package netcracker.project.web.controllers;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import netcracker.project.web.beans.Task;
import netcracker.project.web.dao.Database;
import netcracker.project.web.roles.User;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(name = "reportController")
@SessionScoped
public class ReportController implements Serializable {

    public ReportController() {

    }

    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//    private ArrayList<Task> allTasks = new ArrayList<>();
    private String enquireID;

    public Set<Task> getAllTasksInProject() {
        Set<Task> allTasks = new HashSet<>();
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT DISTINCT(t.task_id), t.task_name, t.START_DATE, t.END_DATE, t.PROGRESS, t.STATUS "
                        + "FROM task t "
                        + "WHERE t.phase_id IN  ("
                        + "SELECT ph.phase_id FROM phase ph "
                        + "INNER JOIN project pr ON ph.project_id = pr.project_id "
                        + "WHERE pr.enquire_id = " + enquireID + ")";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    Task task = new Task();
                    task.setTaskName(rs.getString("task_name"));
                    task.setTaskId(rs.getInt("task_id"));
                    task.setTaskProgress(rs.getInt("PROGRESS"));
                    task.setStatus(rs.getString("STATUS"));
                    try {
                        task.setStartDate(formatter.parse(rs.getString("START_DATE")));
                        task.setEndDate(formatter.parse(rs.getString("END_DATE")));
                    } catch (ParseException ex) {
                        Logger.getLogger(ReportController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    allTasks.add(task);
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
        return allTasks;
    }

    public Map<String, String> getOtherInfo() {
        Map<String, String> map = new HashMap<>();
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = Database.getConnection();
            if (con != null) {
                String sql = "SELECT pr.PROJECT_ID, pr.PROJECT_NAME, pr.START_DATE, pr.END_DATE, c.company_name "
                        + "FROM project pr INNER JOIN company c ON pr.company_id = c.company_id "
                        + "WHERE pr.enquire_id = " + enquireID;
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put("project_id", rs.getString("PROJECT_ID"));
                    map.put("project_name", rs.getString("PROJECT_NAME"));
                    map.put("company_name", rs.getString("company_name"));
                    map.put("start_date", rs.getString("START_DATE"));
                    map.put("end_date", rs.getString("END_DATE"));
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            Database.closeAll(con, ps, rs);
        }
        return map;
    }

    public Map<Integer, User> getAllEmployees(Set<Task> allTasks) {
        Map<Integer, User> userMap = new HashMap<>();
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet rs = null;
        for (Task task : allTasks) {
            try {
                con = Database.getConnection();
                if (con != null) {
                    String sql = "SELECT DISTINCT(e.user_id), e.age, e.firstname, e.lastname, e.qualification "
                            + "FROM users e INNER JOIN TASK_EMPLOYEE te ON e.user_id=te.employee_id "
                            + "WHERE te.task_id = " + task.getTaskId();
                    ps = con.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        User empl = new User();
                        empl.setUserID(rs.getInt("user_id"));
                        empl.setAge(rs.getInt("age"));
                        empl.setFirstName(rs.getString("firstname"));
                        empl.setLastName(rs.getString("lastname"));
                        empl.setQualification(rs.getString("qualification"));
                        userMap.put(task.getTaskId(), empl);
                    }
                }
            } catch (SQLException e) {
                System.out.println(e);
            } finally {
                Database.closeAll(con, ps, rs);
            }
        }
        return userMap;
    }

    public Document createPDFReport(Document document) throws DocumentException, BadElementException, IOException {
        System.out.println("enquireID "+enquireID);
//        Image image1 = Image.getInstance("temp.jpg");
//        //Fixed Positioning
//        image1.setAbsolutePosition(100f, 550f);
//        //Scale to new height and new width of image
//        image1.scaleAbsolute(200, 200);
//        //Add to document
//        document.add(image1);       
        Map<String, String> map = getOtherInfo();
        Paragraph repTitle = new Paragraph();
        repTitle.add("Enquire ID: " + enquireID);
        repTitle.add("\n");
        repTitle.add("Project ID: " + map.get("project_id"));
        repTitle.add("\n");
        repTitle.add("Project Name: " + map.get("project_name"));
        repTitle.add("\n");
        repTitle.add("Company: " + map.get("company_name"));
        repTitle.add("\n");
        repTitle.add("Start date: " + map.get("start_date"));
        repTitle.add("\n");
        repTitle.add("End date: " + map.get("end_date"));
        document.add(repTitle);

        Paragraph taskTitle = new Paragraph("Task in your project:");
        taskTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(taskTitle);

        Set<Task> allTasks = getAllTasksInProject();
        PdfPTable table = new PdfPTable(6); 
        table.setWidthPercentage(100); 
        table.setSpacingBefore(10f); 
        table.setSpacingAfter(10f); 
        float[] columnWidths = {1f, 2f, 2f, 2f, 2f, 2f};
        table.setWidths(columnWidths);
        for (Task task : allTasks) {

            PdfPCell cell1 = new PdfPCell(new Paragraph("ID: " + task.getTaskId()));
            cell1.setBorderColor(BaseColor.BLACK);
            cell1.setPaddingLeft(10);
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell2 = new PdfPCell(new Paragraph("Name: " + task.getTaskName()));
            cell2.setBorderColor(BaseColor.BLACK);
            cell2.setPaddingLeft(10);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell3 = new PdfPCell(new Paragraph("Status: " + task.getStatus()));
            cell3.setBorderColor(BaseColor.BLACK);
            cell3.setPaddingLeft(10);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell4 = new PdfPCell(new Paragraph("Progress: " + task.getTaskProgress()));
            cell3.setBorderColor(BaseColor.BLACK);
            cell3.setPaddingLeft(10);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell5 = new PdfPCell(new Paragraph("Start Date: " + formatter.format(task.getStartDate())));
            cell3.setBorderColor(BaseColor.BLACK);
            cell3.setPaddingLeft(10);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cell6 = new PdfPCell(new Paragraph("End Date: " + formatter.format(task.getEndDate())));
            cell3.setBorderColor(BaseColor.BLACK);
            cell3.setPaddingLeft(10);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);

            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);
            table.addCell(cell5);
            table.addCell(cell6);
        }
        document.add(table);

        Paragraph emplTitle = new Paragraph("Employees, which works on the project:");
        emplTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(emplTitle);
        List emplList = new List(List.UNORDERED);
        Map<Integer, User> userMap = getAllEmployees(allTasks);
        for (Map.Entry<Integer, User> m : userMap.entrySet()) {
            emplList.add(new ListItem("Task id: " + m.getKey() + ", firstname: " + m.getValue().getFirstName() + " lastname: "
                    + m.getValue().getLastName()) + ", age: " + m.getValue().getAge() + " years, qualification: " + m.getValue().getQualification());
        }
        document.add(emplList);

        return document;
    }

    
    public SimpleDateFormat getFormatter() {
        return formatter;
    }

    public String getEnquireID() {
        return enquireID;
    }

    public void setFormatter(SimpleDateFormat formatter) {
        this.formatter = formatter;
    }

    public void setEnquireID(String enquireID) {
        this.enquireID = enquireID;
    }

}
