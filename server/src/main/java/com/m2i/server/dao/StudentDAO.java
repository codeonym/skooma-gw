package com.m2i.server.dao;

import jakarta.ejb.Remote;
import com.m2i.shared.entities.Student;
import java.util.List;

@Remote
public interface StudentDAO {
    public Student save(Student student);
    public Student update(Student student);
    public Student findById(Long id);

    public List<Student> findAll();
    public List<Student> findByMajor(String major);
    public Student findByUsername(String username);
    public List<Student> findByEnrolledCourse(Long courseId);

    public void enrollInCourse(Long studentId, Long courseId);
    public void delete(Long id);
    public void deleteByApogee(String apogee);

    public Student findByApogee(String apogee);

    Student findByUsernameWithReports(String username);
}