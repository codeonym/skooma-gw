package com.m2i.server.dao;

import com.m2i.shared.entities.GradesReport;
import jakarta.ejb.Remote;

import java.util.List;

@Remote
public interface GradesReportDAO {
    public GradesReport save(GradesReport gradesReport);
    public GradesReport findById(Long id);
    public List<GradesReport> findByStudentId(Long studentId);
    public List<GradesReport> findByStudentApogee(String apogee);
    public void generate(GradesReport gradesReport, String coordinatorFullName);
    public void delete(Long id);
    public void reject(Long id);

    public void approve(Long id);
}
