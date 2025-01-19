package com.m2i.server.dao.impl;


import com.m2i.server.dao.CourseDAO;
import com.m2i.server.dao.GradeDAO;
import com.m2i.server.dao.StudentDAO;
import com.m2i.server.dao.TeacherDAO;
import com.m2i.shared.entities.Grade;
import com.m2i.shared.entities.Semester;
import com.m2i.shared.entities.Student;
import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import com.m2i.shared.entities.Course;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Stateless
@Slf4j
@TransactionManagement(TransactionManagementType.CONTAINER)
public class CourseDAOImpl implements CourseDAO {
    @PersistenceContext
    private EntityManager em;
    @EJB
    private GradeDAO gradeDAO;
    @EJB
    private StudentDAO studentDAO;
    @EJB
    private TeacherDAO teacherDAO;

    @Override
    public Course save(Course course) {
        if (course.getId() == null) {
            em.persist(course);
            return course;
        } else {
            return em.merge(course);
        }
    }
    @Override
    public Course findById(Long id) {
        return em.find(Course.class, id);
    }
    @Override
    public List<Course> findAll() {
        return em.createQuery("SELECT c FROM Course c", Course.class)
                .getResultList();
    }
    @Override
    public List<Course> findByTeacherId(Long teacherId) {
        return em.createQuery(
                        "SELECT c FROM Course c WHERE c.teacher.id = :teacherId",
                        Course.class)
                .setParameter("teacherId", teacherId)
                .getResultList();
    }

    @Override
    public List<Course> findByStudentId(Long studentId) {
        return em.createQuery(
                        "SELECT c FROM Course c JOIN c.enrolledStudents s WHERE s.id = :studentId",
                        Course.class)
                .setParameter("studentId", studentId)
                .getResultList();
    }

    @Override
    public void updateCourseContent(Long courseId, byte[] content) {
        Course course = findById(courseId);
        if (course != null) {
            course.setContent(content);
            em.merge(course);
        }
    }
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void delete(Long id) {
        Course course = findById(id);
        if (course != null) {
            try {
                // Create safe copies of collections to avoid concurrent modification
                List<Student> studentsToUnEnroll = new ArrayList<>(course.getEnrolledStudents());
                List<Grade> gradesToDelete = new ArrayList<>(course.getGrades());

                // First clear collections
                course.getEnrolledStudents().clear();
                course.getGrades().clear();
                em.merge(course);
                em.flush();

                // Then handle the related entities
                for (Student student : studentsToUnEnroll) {
                    try {
                        unEnrollStudent(id, student.getApogee());
                    } catch (EntityNotFoundException e) {
                        log.error("Error un-enrolling student: " + student.getApogee(), e);
                    }
                }

                // Finally remove the course
                course = em.merge(course);  // Reattach the entity
                em.remove(course);
                em.flush();
            } catch (Exception e) {
                log.error("Error deleting course: " + id, e);
                throw new EJBException(e);
            }
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void unEnrollStudent(Long courseId, String apogee) {
        Course course = findById(courseId);
        if (course != null) {
            try {
                // First remove any grades associated with this student in this course
                List<Grade> gradesToRemove = em.createQuery(
                                "SELECT g FROM Grade g WHERE g.course.id = :courseId AND g.student.apogee = :apogee",
                                Grade.class)
                        .setParameter("courseId", courseId)
                        .setParameter("apogee", apogee)
                        .getResultList();

                for (Grade grade : gradesToRemove) {
                    Grade managedGrade = em.find(Grade.class, grade.getId());
                    if (managedGrade != null) {
                        em.remove(managedGrade);
                    }
                }
                em.flush();

                // Then remove the student from the course
                Student student = studentDAO.findByApogee(apogee);
                if (student != null) {
                    course = em.merge(course);  // Reattach the entity
                    course.getEnrolledStudents().remove(student);
                    em.merge(course);
                    em.flush();
                }
            } catch (Exception e) {
                log.error("Error un-enrolling student: " + apogee + " from course: " + courseId, e);
                throw new EJBException(e);
            }
        }
    }

    @Override
    public List<Course> findAllBySemester(Semester semester) {
        return em.createQuery(
                        "SELECT c FROM Course c WHERE c.semester = :semester",
                        Course.class)
                .setParameter("semester", semester)
                .getResultList();
    }

    @Override
    public void enrollStudent(Long courseId, String apogee) {
        Course course = findById(courseId);
        if (course != null) {
            course.getEnrolledStudents().add(studentDAO.findByApogee(apogee));
            em.merge(course);
        }
    }

    @Override
    public void assignTeacher(Long courseId, String teacherIdentifier) {
        Course course = findById(courseId);
        if (course != null) {
            course.setTeacher(teacherDAO.findByTeacherIdentifier(teacherIdentifier));
            em.merge(course);
        }
    }

    @Override
    @Transactional
    public void unAssignTeacher(Long courseId, String teacherIdentifier) {
        Course course = findById(courseId);
        if (course != null) {
            course.setTeacher(null);
            em.merge(course);
        }
    }
}