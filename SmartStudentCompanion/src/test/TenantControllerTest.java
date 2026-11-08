package com.example.demo;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class TenantControllerTest {
    @Mock CurrentStudent currentStudent;
    @Mock Authentication authentication;
    @Mock AssignmentRepository assignments;
    @Mock AttendanceRepository attendance;
    @Mock NoteRepository notes;
    @Mock ReminderRepository reminders;
    @Mock FlashcardRepository flashcards;

    private final Student owner = new Student("Student", "owner@example.test", "encoded");

    @BeforeEach
    void setUp() {
        owner.setId(41L);
        when(currentStudent.require(authentication)).thenReturn(owner);
    }

    @Test
    void listEndpointsAlwaysQueryOnlyTheAuthenticatedOwner() {
        new AssignmentController(assignments, currentStudent).getAssignments(authentication);
        new AttendanceController(attendance, currentStudent).getAttendance(authentication);
        new NoteController(notes, currentStudent).getAllNotes(authentication);
        new ReminderController(reminders, currentStudent).getAllReminders(authentication);
        new FlashcardController(flashcards, currentStudent).getAll(authentication);

        verify(assignments).findAllByStudent_IdOrderByDueDateAsc(41L);
        verify(attendance).findAllByStudent_IdOrderByMonthDesc(41L);
        verify(notes).findAllByStudent_IdOrderByCreatedAtDesc(41L);
        verify(reminders).findAllByStudent_IdOrderByDueDateAsc(41L);
        verify(flashcards).findAllByStudent_IdOrderByIdDesc(41L);
    }
}
