package com.m2i.server.dao;

import com.m2i.shared.entities.Semester;
import jakarta.ejb.Remote;
import com.m2i.shared.entities.Grade;
import java.util.List;

@Remote
public interface GradeDAO {

    public Grade save(Grade grade);
    public Grade update(Grade grade);

    public Grade findById(Long id);

    public List<Grade> findByStudentId(Long studentId);

    public List<Grade> findByCourseId(Long courseId);

    public List<Grade> findByStudentAndCourse(Long studentId, Long courseId);
    public List<Grade> findByStudentApogeeAndTeacherUsername(String apogee, String teacherUsername);
    public List<Grade> findByStudentApogeeAndSemester(String apogee, Semester semester);

    public void delete(Long id);
}