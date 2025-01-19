package com.m2i.server.dao.impl;

import com.m2i.server.dao.GradeDAO;
import com.m2i.server.dao.GradesReportDAO;
import com.m2i.server.dao.StudentDAO;
import com.m2i.shared.entities.Grade;
import com.m2i.shared.entities.GradesReport;
import com.m2i.shared.entities.Status;
import com.m2i.shared.entities.Student;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Stateless
public class GradesReportDAOImpl implements GradesReportDAO {
    @PersistenceContext
    private EntityManager em;
    @EJB
    private StudentDAO studentDAO;
    @EJB
    private GradeDAO gradeDAO;

    @Override
    public GradesReport save(GradesReport gradesReport) {
        if (gradesReport.getId() == null) {
            em.persist(gradesReport);
            return gradesReport;
        } else {
            return em.merge(gradesReport);
        }
    }

    @Override
    public GradesReport findById(Long id) {
        return em.find(GradesReport.class, id);
    }

    @Override
    public List<GradesReport> findByStudentId(Long studentId) {
        return studentDAO.findById(studentId).getReports();
    }

    @Override
    public List<GradesReport> findByStudentApogee(String apogee) {
        return studentDAO.findByApogee(apogee).getReports();
    }

    @Override
    public void generate(GradesReport gradesReport, String coordinatorFullName) {
        // First ensure the report exists in database
        GradesReport report = findById(gradesReport.getId());
        if(report == null) {
            report = save(gradesReport);
        }

        // Fetch all relevant grades for the student in the given semester
        List<Grade> grades = gradeDAO.findByStudentApogeeAndSemester(
                report.getStudent().getApogee(),
                report.getSemester()
        );

        try {
            // Generate PDF report
            byte[] reportData = generatePDFReport(grades, coordinatorFullName, report);

            // Update report entity
            report.setReport(reportData);
            report.setGenerated(true);
            report.setGenerationDate(LocalDate.now());
            report.setStatus(Status.GENERATED);

            // Save updated report
            save(report);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate grades report", e);
        }
    }

    private byte[] generatePDFReport(List<Grade> grades, String coordinatorFullName, GradesReport report) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Add header
            addHeader(document, report);

            // Add student information
            addStudentInfo(document, report.getStudent());

            // Add grades table
            addGradesTable(document, grades);

            // Add footer with coordinator signature
            addFooter(document, coordinatorFullName);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    private void addHeader(Document document, GradesReport report) throws DocumentException {
        Paragraph header = new Paragraph();
        header.add(new Paragraph("ACADEMIC TRANSCRIPT", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD)));
        header.add(new Paragraph("Semester: " + report.getSemester()));
        header.add(new Paragraph("Academic Year: " + report.getYear()));
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(20);
        document.add(header);
    }

    private void addStudentInfo(Document document, Student student) throws DocumentException {
        Paragraph studentInfo = new Paragraph();
        studentInfo.add(new Paragraph("Student Information:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
        studentInfo.add(new Paragraph("Name: " + student.getFirstName() + " " + student.getLastName()));
        studentInfo.add(new Paragraph("Student Apogee: " + student.getApogee()));
        studentInfo.setSpacingAfter(20);
        document.add(studentInfo);
    }

    private void addGradesTable(Document document, List<Grade> grades) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        // Add table headers
        table.addCell("Course");
        table.addCell("Grade");
        table.addCell("Year");
        table.addCell("Status");

        double score = 0;
        // Add grade rows
        for (Grade grade : grades) {
            table.addCell(grade.getCourse().getName());
            table.addCell(String.valueOf(grade.getValue()));
            table.addCell(String.valueOf(grade.getYear()));
            table.addCell(grade.getValue() >= 10 ? "PASS" : "FAIL");
            score += grade.getValue();
        }
        table.addCell("Average");
        table.addCell(String.valueOf(score / grades.size()));

        document.add(table);
    }

    private void addFooter(Document document, String coordinatorFullName) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.setSpacingBefore(30);

        // Add generation date
        footer.add(new Paragraph("Generated on: " + new SimpleDateFormat("dd/MM/yyyy").format(new Date())));

        // Add coordinator signature
        footer.add(new Paragraph("\n\nCoordinator: " + coordinatorFullName));
        footer.add(new Paragraph("Signature: ______"+ coordinatorFullName.toUpperCase() +"________"));

        document.add(footer);
    }

    @Override
    public void delete(Long id) {
        em.remove(em.find(GradesReport.class, id));
    }

    @Override
    public void reject(Long id) {
        em.createQuery("UPDATE GradesReport g SET g.status = :status WHERE g.id = :id")
                .setParameter("id", id)
                .setParameter("status", Status.REJECTED)
                .executeUpdate();
    }

    @Override
    public void approve(Long id) {
        em.createQuery("UPDATE GradesReport g SET g.status = :status WHERE g.id = :id")
                .setParameter("id", id)
                .setParameter("status", Status.APPROVED)
                .executeUpdate();
    }

    @Override
    public List<GradesReport> findAll() {
        return em.createQuery("SELECT g FROM GradesReport g", GradesReport.class).getResultList();
    }
}
