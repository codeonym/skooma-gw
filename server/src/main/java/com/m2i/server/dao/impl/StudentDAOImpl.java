package com.m2i.server.dao.impl;

import com.m2i.server.dao.CourseDAO;
import com.m2i.server.dao.StudentDAO;
import com.m2i.shared.entities.Course;
import com.m2i.shared.entities.Student;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Objects;

@Stateless
public class StudentDAOImpl implements StudentDAO {
    @PersistenceContext
    private EntityManager em;
    @EJB
    private CourseDAO courseDAO;

    public Student save(Student student) {
        if (student.getId() == null) {
            student.setStudentId("STUD-"+ student.getApogee() + "-" + System.currentTimeMillis());
            em.persist(student);
            return student;
        } else {
            return em.merge(student);
        }
    }

    @Override
    public Student update(Student student) {
        return em.merge(student);
    }

    @Override
    public Student findById(Long id) {
        return em.find(Student.class, id);
    }
    @Override
    public List<Student> findAll() {
        return em.createQuery("SELECT s FROM Student s", Student.class)
                .getResultList();
    }
    @Override
    public List<Student> findByMajor(String major) {
        return em.createQuery("SELECT s FROM Student s WHERE s.major = :major", Student.class)
                .setParameter("major", major)
                .getResultList();
    }

    @Override
    public Student findByUsername(String username) {
        return em.createQuery("SELECT s FROM Student s WHERE s.username = :username", Student.class)
                .setParameter("username", username)
                .getSingleResult();
    }

    @Override
    public List<Student> findByEnrolledCourse(Long courseId) {
        return em.createQuery(
                        "SELECT DISTINCT s FROM Student s " +
                                "JOIN s.enrolledCourses ec " +
                                "WHERE ec.id = :courseId",
                        Student.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }

    @Override
    public void enrollInCourse(Long studentId, Long courseId) {
        Student student = findById(studentId);
        Course course = em.find(Course.class, courseId);

        if (student != null && course != null) {
            student.getEnrolledCourses().add(course);
            course.getEnrolledStudents().add(student);
            em.merge(student);
            em.merge(course);
        }
    }
    @Override
    public void delete(Long id) {
        Student student = findById(id);
        if (student != null) {
            em.remove(student);
        }
    }

    @Override
    @Transactional
    public void deleteByApogee(String apogee) {
        Student student = findByApogee(apogee);
        if (student != null) {
            // First remove all grades associated with the student
            em.createQuery("DELETE FROM Grade g WHERE g.student.id = :studentId")
                    .setParameter("studentId", student.getId())
                    .executeUpdate();

            // Remove all grade reports associated with the student
            em.createQuery("DELETE FROM GradesReport gr WHERE gr.student.id = :studentId")
                    .setParameter("studentId", student.getId())
                    .executeUpdate();

            // Remove the associations in the junction table courses_students
            em.createNativeQuery("DELETE FROM courses_students WHERE student_id = :studentId")
                    .setParameter("studentId", student.getId())
                    .executeUpdate();

            // Clear the collections to ensure clean state
            student.getGrades().clear();
            student.getReports().clear();
            student.getEnrolledCourses().clear();

            // Finally remove the student
            em.remove(em.contains(student) ? student : em.merge(student));
            em.flush();
        }
    }


    @Override
    public Student findByApogee(String apogee) {
        return em.createQuery("SELECT s FROM Student s WHERE s.apogee = :apogee", Student.class)
                .setParameter("apogee", apogee)
                .getSingleResult();
    }

    @Override
    public Student findByUsernameWithReports(String username) {
        return em.createQuery("SELECT s FROM Student s LEFT JOIN FETCH s.reports WHERE s.username = :username", Student.class)
                .setParameter("username", username)
                .getSingleResult();
    }
}