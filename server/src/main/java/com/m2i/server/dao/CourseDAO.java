package com.m2i.server.dao;

import com.m2i.shared.entities.Semester;
import jakarta.ejb.Remote;
import com.m2i.shared.entities.Course;
import java.util.List;

@Remote
public interface CourseDAO {

    public Course save(Course course);

    public Course findById(Long id);

    public List<Course> findAll();

    public List<Course> findByTeacherId(Long teacherId);
    public List<Course> findByStudentId(Long studentId);

    public void updateCourseContent(Long courseId, byte[] content);

    public void delete(Long id);

    List<Course> findAllBySemester(Semester semester);
    void enrollStudent(Long courseId, String apogee);
    void unEnrollStudent(Long courseId, String apogee);
    void assignTeacher(Long courseId, String teacherIdentifier);
    void unAssignTeacher(Long courseId, String teacherIdentifier);
}