package com.m2i.server.dao.impl;

import com.m2i.server.dao.CourseDAO;
import com.m2i.server.dao.TeacherDAO;
import com.m2i.shared.entities.Course;
import com.m2i.shared.entities.Teacher;
import com.m2i.shared.entities.UserRole;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Stateless
@Slf4j
public class TeacherDAOImpl implements TeacherDAO {
    @PersistenceContext
    private EntityManager em;
    @EJB
    private CourseDAO courseDAO;
    @Override
    public Teacher save(Teacher teacher) {
        teacher.setTeacherId("TEACH-"+ teacher.getTeacherIdentifier() + "-" + System.currentTimeMillis());
        teacher.setRole(UserRole.TEACHER);
        if (teacher.getId() == null) {
            em.persist(teacher);
            return teacher;
        } else {
            return em.merge(teacher);
        }
    }
    @Override
    public Teacher findById(Long id) {
        return em.find(Teacher.class, id);
    }

    @Override
    public Teacher findByUsername(String username) {
        return em.createQuery(
                        "SELECT t FROM Teacher t WHERE t.username = :username",
                        Teacher.class)
                .setParameter("username", username)
                .getSingleResult();
    }

    @Override
    public List<Teacher> findAll() {
        return em.createQuery("SELECT t FROM Teacher t", Teacher.class)
                .getResultList();
    }
    @Override
    public List<Teacher> findByDepartment(String department) {
        return em.createQuery(
                        "SELECT t FROM Teacher t WHERE t.department = :department",
                        Teacher.class)
                .setParameter("department", department)
                .getResultList();
    }
    @Override
    public List<Course> findTeacherCourses(Long teacherId) {
        return courseDAO.findByTeacherId(teacherId);
    }
    @Override
    public void delete(Long id) {
        Teacher teacher = findById(id);
        if (teacher != null) {
            em.remove(teacher);
        }
    }

    @Override
    @Transactional
    public void deleteByTeacherIdentifier(String teacherIdentifier) {
        Teacher teacher = findByTeacherIdentifier(teacherIdentifier);
        if (teacher != null) {
            // delete the teacher ref from courses
            List<Course> courses = findTeacherCourses(teacher.getId());
            if(courses != null && !courses.isEmpty()) {
                for(Course c: courses){
                    c.setTeacher(null);
                    courseDAO.save(c);
                }
            }
            teacher.setCourses(null);
            em.merge(teacher);
            em.remove(teacher);
            em.flush();
        }
    }

    @Override
    public Teacher findByTeacherIdentifier(String teacherIdentifier) {
        return em.createQuery(
                        "SELECT t FROM Teacher t WHERE t.teacherIdentifier = :teacherIdentifier",
                        Teacher.class)
                .setParameter("teacherIdentifier", teacherIdentifier)
                .getSingleResult();
    }
}