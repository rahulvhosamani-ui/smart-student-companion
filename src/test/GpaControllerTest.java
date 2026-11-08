package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class GpaControllerTest {
    @Mock GpaRepository repository;
    @Mock CurrentStudent currentStudent;
    @Mock Authentication authentication;

    private GpaController controller;
    private Student student;

    @BeforeEach
    void setUp() {
        controller = new GpaController(repository, currentStudent);
        student = new Student("A Student", "a@example.test", "encoded");
        student.setId(41L);
        when(currentStudent.require(authentication)).thenReturn(student);
    }

    @Test
    void createAttachesAuthenticatedOwnerAndReturnsCreated() {
        Gpa submitted = new Gpa(7, 8.8, 8.0);
        when(repository.save(any(Gpa.class))).thenAnswer(call -> call.getArgument(0));

        ResponseEntity<?> response = controller.saveGpa(submitted, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(student, submitted.getStudent());
        verify(repository).existsByStudent_IdAndSemester(41L, 7);
    }

    @Test
    void createRejectsDuplicateSemesterForSameStudent() {
        when(repository.existsByStudent_IdAndSemester(41L, 7)).thenReturn(true);

        ResponseEntity<?> response = controller.saveGpa(new Gpa(7, 8.8, 8.0), authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void updateUsesDatabaseIdAndAuthenticatedStudentId() {
        Gpa existing = new Gpa(7, 8.0, 8.0);
        when(repository.findByIdAndStudent_Id(900L, 41L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        ResponseEntity<?> response = controller.updateGpa(900L, new Gpa(7, 9.0, 8.5), authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(9.0, existing.getSgpa());
        verify(repository).findByIdAndStudent_Id(900L, 41L);
    }

    @Test
    void updateRejectsASecondRecordForTheSameSemester() {
        when(repository.existsByStudent_IdAndSemesterAndIdNot(41L, 8, 900L))
                .thenReturn(true);

        ResponseEntity<?> response = controller.updateGpa(
                900L, new Gpa(8, 9.0, 8.5), authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void anotherStudentsGpaCannotBeDeleted() {
        when(repository.existsByIdAndStudent_Id(900L, 41L)).thenReturn(false);

        ResponseEntity<Void> response = controller.deleteGpa(900L, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(repository, never()).deleteByIdAndStudent_Id(900L, 41L);
    }
}
