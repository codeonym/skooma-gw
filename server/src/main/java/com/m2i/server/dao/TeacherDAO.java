package com.m2i.server.dao;

import jakarta.ejb.Remote;
import com.m2i.shared.entities.Teacher;
import com.m2i.shared.entities.Course;
import java.util.List;

@Remote
public interface TeacherDAO {
    public Teacher save(Teacher teacher);

    public Teacher findById(Long id);
    public Teacher findByUsername(String username);

    public List<Teacher> findAll();
    public List<Teacher> findByDepartment(String department);

    public List<Course> findTeacherCourses(Long teacherId);

    public void delete(Long id);
    public void deleteByTeacherIdentifier(String teacherIdentifier);

    Teacher findByTeacherIdentifier(String teacherIdentifier);
}