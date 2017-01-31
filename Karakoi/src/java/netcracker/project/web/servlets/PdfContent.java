package netcracker.project.web.servlets;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Map;
import javax.faces.annotation.ManagedProperty;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import netcracker.project.web.controllers.ReportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@WebServlet(name = "PdfContent",
        urlPatterns = {"/PdfContent"})
public class PdfContent extends HttpServlet {

//    @Autowired
//    @Qualifier("reportController")
    @ManagedProperty(value = "#{reportController}")
    ReportController reportController = new ReportController();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Document document = new Document();
        try {
            response.setContentType("application/pdf");
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Paragraph p = new Paragraph("<<<REPORT>>>");
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);
            reportController.setEnquireID(request.getParameter("enquireID"));
            reportController.createPDFReport(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        document.close();

//        Document document = new Document();       
//        try {
//            response.setHeader("Content-Disposition",
//                    "attachment;filename=downloadname.pdf");
//
//            document.open();
//            document.add(new Paragraph("SLA: dsfdsfsd"));
//            OutputStream os = response.getOutputStream();
//            PdfWriter.getInstance(document, response.getOutputStream());
//            os.flush();
//            os.close();
//
//        } catch (DocumentException de) {
//            System.err.println(de.getMessage());
//        } finally {
////            out.close();
//            document.close();
//        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
