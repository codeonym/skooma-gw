package com.m2i.server.dao.impl;

import com.m2i.server.dao.GradeDAO;
import com.m2i.shared.entities.Grade;
import com.m2i.shared.entities.Semester;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Stateless
public class GradeDAOImpl implements GradeDAO {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Grade save(Grade grade) {
        if (grade.getId() == null) {
            grade.setSemester(grade.getCourse().getSemester());
            grade.setDate(LocalDateTime.now()); // Set submission date for new grades
            em.persist(grade);
            return grade;
        } else {
            grade.setDate(LocalDateTime.now());
            return em.merge(grade);
        }
    }

    @Override
    public Grade update(Grade grade) {
        return em.merge(grade);
    }

    @Override
    public Grade findById(Long id) {
        return em.find(Grade.class, id);
    }
    @Override
    public List<Grade> findByStudentId(Long studentId) {
        return em.createQuery(
                        "SELECT g FROM Grade g WHERE g.student.id = :studentId",
                        Grade.class)
                .setParameter("studentId", studentId)
                .getResultList();
    }

    public List<Grade> findByCourseId(Long courseId) {
        return em.createQuery(
                        "SELECT g FROM Grade g WHERE g.course.id = :courseId",
                        Grade.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }
    @Override
    public List<Grade> findByStudentAndCourse(Long studentId, Long courseId) {
        return em.createQuery(
                        "SELECT g FROM Grade g WHERE g.student.id = :studentId AND g.course.id = :courseId",
                        Grade.class)
                .setParameter("studentId", studentId)
                .setParameter("courseId", courseId)
                .getResultList();
    }

    @Override
    public List<Grade> findByStudentApogeeAndTeacherUsername(String apogee, String teacherUsername) {
        return em.createQuery(
                        "SELECT g FROM Grade g WHERE g.student.apogee = :apogee AND g.course.teacher.username = :teacherUsername",
                        Grade.class)
                .setParameter("apogee", apogee)
                .setParameter("teacherUsername", teacherUsername)
                .getResultList();
    }

    @Override
    public List<Grade> findByStudentApogeeAndSemester(String apogee, Semester semester) {
        return em.createQuery(
                        "SELECT g FROM Grade g WHERE g.student.apogee = :apogee AND g.semester = :semester",
                        Grade.class)
                .setParameter("apogee", apogee)
                .setParameter("semester", semester)
                .getResultList();
    }

    @Override
    public void delete(Long id) {
        Grade grade = findById(id);
        if (grade != null) {
            em.remove(grade);
        }
    }
}