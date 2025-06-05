package ch.hftm.relationaldatabases.transferdemo.web;

import ch.hftm.relationaldatabases.transferdemo.dtos.Note;
import ch.hftm.relationaldatabases.transferdemo.dtos.NoteForm;
import ch.hftm.relationaldatabases.transferdemo.mappers.NoteFormMapper;
import ch.hftm.relationaldatabases.transferdemo.services.NoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
@DisplayName("NoteController Web Layer Tests")
class NoteControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private NoteService noteService;

  @MockitoBean
  private NoteFormMapper noteFormMapper;

  @Test
  @DisplayName("GET / should display list of notes")
  void listNotes_shouldDisplayNotesPage() throws Exception {
    // Arrange
    var notes = List.of(
        Note.builder()
            .uuid(UUID.randomUUID())
            .note("First test note")
            .createdAt(Instant.now().minusSeconds(3600))
            .updatedAt(Instant.now().minusSeconds(1800))
            .build(),
        Note.builder()
            .uuid(UUID.randomUUID())
            .note("Second test note")
            .createdAt(Instant.now().minusSeconds(1800))
            .updatedAt(Instant.now())
            .build()
    );

    when(noteService.getAll()).thenReturn(notes);

    // Act & Assert
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("notes/list"))
        .andExpect(model().attribute("notes", hasSize(2)))
        .andExpect(model().attribute("notes", contains(
            hasProperty("note", is("First test note")),
            hasProperty("note", is("Second test note"))
        )));

    verify(noteService).getAll();
  }

  @Test
  @DisplayName("GET / with empty notes list should display empty page")
  void listNotes_withEmptyList_shouldDisplayEmptyPage() throws Exception {
    // Arrange
    when(noteService.getAll()).thenReturn(List.of());

    // Act & Assert
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("notes/list"))
        .andExpect(model().attribute("notes", hasSize(0)));

    verify(noteService).getAll();
  }

  @Test
  @DisplayName("GET /notes/new should show create form")
  void showCreateForm_shouldDisplayNewNoteForm() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/notes/new"))
        .andExpect(status().isOk())
        .andExpect(view().name("notes/form"))
        .andExpect(model().attributeExists("noteForm"))
        .andExpect(model().attribute("noteForm", instanceOf(NoteForm.class)))
        .andExpect(model().attribute("isEdit", is(false)));

    verifyNoInteractions(noteService);
  }

  @Test
  @DisplayName("GET /notes/{uuid}/edit should show edit form for existing note")
  void showEditForm_withExistingNote_shouldDisplayEditForm() throws Exception {
    // Arrange
    var noteId = UUID.randomUUID();
    var existingNote = Note.builder()
        .uuid(noteId)
        .note("Existing note content")
        .createdAt(Instant.now().minusSeconds(3600))
        .updatedAt(Instant.now())
        .build();

    var noteForm = new NoteForm(noteId, "Existing note content");

    when(noteService.findById(noteId)).thenReturn(Optional.of(existingNote));
    when(noteFormMapper.toForm(existingNote)).thenReturn(noteForm);

    // Act & Assert
    mockMvc.perform(get("/notes/{uuid}/edit", noteId))
        .andExpect(status().isOk())
        .andExpect(view().name("notes/form"))
        .andExpect(model().attribute("noteForm", noteForm))
        .andExpect(model().attribute("isEdit", is(true)));

    verify(noteService).findById(noteId);
    verify(noteFormMapper).toForm(existingNote);
  }

  @Test
  @DisplayName("GET /notes/{uuid}/edit with non-existent note should redirect with error")
  void showEditForm_withNonExistentNote_shouldRedirectWithError() throws Exception {
    // Arrange
    var nonExistentId = UUID.randomUUID();
    when(noteService.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act & Assert
    mockMvc.perform(get("/notes/{uuid}/edit", nonExistentId))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("error", "Note not found"));

    verify(noteService).findById(nonExistentId);
    verifyNoInteractions(noteFormMapper);
  }

  @Test
  @DisplayName("POST /notes should create new note and redirect")
  void saveNote_withNewNote_shouldCreateAndRedirect() throws Exception {
    // Arrange
    var noteDto = Note.builder()
        .note("New note content")
        .build();
    var savedNote = Note.builder()
        .uuid(UUID.randomUUID())
        .note("New note content")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    when(noteFormMapper.toDto(any(NoteForm.class))).thenReturn(noteDto);
    when(noteService.upsert(noteDto)).thenReturn(savedNote);

    // Act & Assert
    mockMvc.perform(post("/notes")
            .param("note", "New note content"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("success", "Note created successfully"));

    verify(noteFormMapper).toDto(any(NoteForm.class));
    verify(noteService).upsert(noteDto);
  }

  @Test
  @DisplayName("POST /notes should update existing note and redirect")
  void saveNote_withExistingNote_shouldUpdateAndRedirect() throws Exception {
    // Arrange
    var noteId = UUID.randomUUID();
    var noteDto = Note.builder()
        .uuid(noteId)
        .note("Updated note content")
        .build();
    var updatedNote = Note.builder()
        .uuid(noteId)
        .note("Updated note content")
        .createdAt(Instant.now().minusSeconds(3600))
        .updatedAt(Instant.now())
        .build();

    when(noteFormMapper.toDto(any(NoteForm.class))).thenReturn(noteDto);
    when(noteService.upsert(noteDto)).thenReturn(updatedNote);

    // Act & Assert
    mockMvc.perform(post("/notes")
            .param("uuid", noteId.toString())
            .param("note", "Updated note content"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("success", "Note updated successfully"));

    verify(noteFormMapper).toDto(any(NoteForm.class));
    verify(noteService).upsert(noteDto);
  }

  @Test
  @DisplayName("POST /notes with empty note should still process")
  void saveNote_withEmptyNote_shouldProcess() throws Exception {
    // Arrange
    var noteDto = Note.builder()
        .note("")
        .build();
    var savedNote = Note.builder()
        .uuid(UUID.randomUUID())
        .note("")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    when(noteFormMapper.toDto(any(NoteForm.class))).thenReturn(noteDto);
    when(noteService.upsert(noteDto)).thenReturn(savedNote);

    // Act & Assert
    mockMvc.perform(post("/notes")
            .param("note", ""))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("success", "Note created successfully"));

    verify(noteService).upsert(noteDto);
  }

  @Test
  @DisplayName("POST /notes with service exception should redirect with error")
  void saveNote_withServiceException_shouldRedirectWithError() throws Exception {
    // Arrange
    var noteDto = Note.builder()
        .note("Test note")
        .build();

    when(noteFormMapper.toDto(any(NoteForm.class))).thenReturn(noteDto);
    when(noteService.upsert(noteDto)).thenThrow(new RuntimeException("Database error"));

    // Act & Assert
    mockMvc.perform(post("/notes")
            .param("note", "Test note"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("error", "Error saving note: Database error"));

    verify(noteService).upsert(noteDto);
  }

  @Test
  @DisplayName("POST /notes/{uuid}/delete should delete note and redirect")
  void deleteNote_withExistingNote_shouldDeleteAndRedirect() throws Exception {
    // Arrange
    var noteId = UUID.randomUUID();
    doNothing().when(noteService).deleteById(noteId);

    // Act & Assert
    mockMvc.perform(post("/notes/{uuid}/delete", noteId))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("success", "Note deleted successfully"));

    verify(noteService).deleteById(noteId);
  }

  @Test
  @DisplayName("POST /notes/{uuid}/delete with service exception should redirect with error")
  void deleteNote_withServiceException_shouldRedirectWithError() throws Exception {
    // Arrange
    var noteId = UUID.randomUUID();
    doThrow(new RuntimeException("Delete failed")).when(noteService).deleteById(noteId);

    // Act & Assert
    mockMvc.perform(post("/notes/{uuid}/delete", noteId))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("error", "Error deleting note: Delete failed"));

    verify(noteService).deleteById(noteId);
  }

  @Test
  @DisplayName("POST /notes/{uuid}/delete with non-existent note should still redirect successfully")
  void deleteNote_withNonExistentNote_shouldRedirectSuccessfully() throws Exception {
    // Arrange
    var noteId = UUID.randomUUID();
    doNothing().when(noteService).deleteById(noteId); // deleteById doesn't throw for non-existent IDs

    // Act & Assert
    mockMvc.perform(post("/notes/{uuid}/delete", noteId))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("success", "Note deleted successfully"));

    verify(noteService).deleteById(noteId);
  }

  @Test
  @DisplayName("Form submission should handle special characters correctly")
  void saveNote_withSpecialCharacters_shouldHandleCorrectly() throws Exception {
    // Arrange
    var specialNote = "Note with special chars: áéíóú & <script>alert('test')</script>";
    var noteDto = Note.builder()
        .note(specialNote)
        .build();
    var savedNote = Note.builder()
        .uuid(UUID.randomUUID())
        .note(specialNote)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    when(noteFormMapper.toDto(any(NoteForm.class))).thenReturn(noteDto);
    when(noteService.upsert(noteDto)).thenReturn(savedNote);

    // Act & Assert
    mockMvc.perform(post("/notes")
            .param("note", specialNote))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("success", "Note created successfully"));

    verify(noteService).upsert(noteDto);
  }

  @Test
  @DisplayName("Form submission with very long note should be handled")
  void saveNote_withLongNote_shouldHandle() throws Exception {
    // Arrange
    var longNote = "A".repeat(1000); // Very long note
    var noteDto = Note.builder()
        .note(longNote)
        .build();
    var savedNote = Note.builder()
        .uuid(UUID.randomUUID())
        .note(longNote)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    when(noteFormMapper.toDto(any(NoteForm.class))).thenReturn(noteDto);
    when(noteService.upsert(noteDto)).thenReturn(savedNote);

    // Act & Assert
    mockMvc.perform(post("/notes")
            .param("note", longNote))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("success", "Note created successfully"));

    verify(noteService).upsert(noteDto);
  }
}
