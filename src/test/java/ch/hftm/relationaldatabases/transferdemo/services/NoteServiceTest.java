package ch.hftm.relationaldatabases.transferdemo.services;

import ch.hftm.relationaldatabases.transferdemo.dtos.Note;
import ch.hftm.relationaldatabases.transferdemo.jpa.entities.NoteEntity;
import ch.hftm.relationaldatabases.transferdemo.jpa.repositories.NoteRepository;
import ch.hftm.relationaldatabases.transferdemo.mappers.NoteMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoteService Tests")
class NoteServiceTest {

  @Mock
  private NoteRepository repository;

  @Mock
  private NoteMapper mapper;

  @InjectMocks
  private NoteService noteService;

  private NoteEntity sampleEntity;
  private Note sampleNote;

  @BeforeEach
  void setUp() {
    var uuid = UUID.randomUUID();
    var now = Instant.now();

    sampleEntity = NoteEntity.builder()
        .uuid(uuid)
        .note("Sample note")
        .createdAt(now.minusSeconds(3600))
        .updatedAt(now)
        .build();

    sampleNote = Note.builder()
        .uuid(uuid)
        .note("Sample note")
        .createdAt(now.minusSeconds(3600))
        .updatedAt(now)
        .build();
  }

  @Test
  @DisplayName("getAll_shouldReturnAllNotesAsDtos")
  void getAll_shouldReturnAllNotesAsDtos() {
    // Arrange
    var entities = List.of(
        NoteEntity.builder().uuid(UUID.randomUUID()).note("First note").build(),
        NoteEntity.builder().uuid(UUID.randomUUID()).note("Second note").build()
    );
    var dtos = List.of(
        Note.builder().uuid(entities.get(0).getUuid()).note("First note").build(),
        Note.builder().uuid(entities.get(1).getUuid()).note("Second note").build()
    );

    when(repository.findAll()).thenReturn(entities);
    when(mapper.toDto(entities.get(0))).thenReturn(dtos.get(0));
    when(mapper.toDto(entities.get(1))).thenReturn(dtos.get(1));

    // Act
    var result = noteService.getAll();

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result).containsExactly(dtos.get(0), dtos.get(1));

    verify(repository).findAll();
    verify(mapper, times(2)).toDto(any(NoteEntity.class));
  }

  @Test
  @DisplayName("getAll_withEmptyRepository_shouldReturnEmptyList")
  void getAll_withEmptyRepository_shouldReturnEmptyList() {
    // Arrange
    when(repository.findAll()).thenReturn(List.of());

    // Act
    var result = noteService.getAll();

    // Assert
    assertThat(result).isEmpty();
    verify(repository).findAll();
    verifyNoInteractions(mapper);
  }

  @Test
  @DisplayName("findById_withExistingId_shouldReturnOptionalWithNote")
  void findById_withExistingId_shouldReturnOptionalWithNote() {
    // Arrange
    var noteId = UUID.randomUUID();
    when(repository.findById(noteId)).thenReturn(Optional.of(sampleEntity));
    when(mapper.toDto(sampleEntity)).thenReturn(sampleNote);

    // Act
    var result = noteService.findById(noteId);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(sampleNote);

    verify(repository).findById(noteId);
    verify(mapper).toDto(sampleEntity);
  }

  @Test
  @DisplayName("findById_withNonExistentId_shouldReturnEmptyOptional")
  void findById_withNonExistentId_shouldReturnEmptyOptional() {
    // Arrange
    var noteId = UUID.randomUUID();
    when(repository.findById(noteId)).thenReturn(Optional.empty());

    // Act
    var result = noteService.findById(noteId);

    // Assert
    assertThat(result).isEmpty();

    verify(repository).findById(noteId);
    verifyNoInteractions(mapper);
  }

  @Test
  @DisplayName("upsert_withNewNote_shouldCreateAndReturnSavedNote")
  void upsert_withNewNote_shouldCreateAndReturnSavedNote() {
    // Arrange
    var newNote = Note.builder()
        .note("New note content")
        .build(); // No UUID - indicates new note

    var newEntity = NoteEntity.builder()
        .note("New note content")
        .build();

    var savedEntity = NoteEntity.builder()
        .uuid(UUID.randomUUID())
        .note("New note content")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    var savedNote = Note.builder()
        .uuid(savedEntity.getUuid())
        .note("New note content")
        .createdAt(savedEntity.getCreatedAt())
        .updatedAt(savedEntity.getUpdatedAt())
        .build();

    when(mapper.toEntity(newNote)).thenReturn(newEntity);
    when(repository.save(newEntity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(savedNote);

    // Act
    var result = noteService.upsert(newNote);

    // Assert
    assertThat(result).isEqualTo(savedNote);

    verify(mapper).toEntity(newNote);
    verify(repository).save(newEntity);
    verify(mapper).toDto(savedEntity);
    verify(repository, never()).findById(any(UUID.class));
  }

  @Test
  @DisplayName("upsert_withExistingNote_shouldUpdateAndReturnSavedNote")
  void upsert_withExistingNote_shouldUpdateAndReturnSavedNote() {
    // Arrange
    var existingId = UUID.randomUUID();
    var updateNote = Note.builder()
        .uuid(existingId)
        .note("Updated content")
        .build();

    var existingEntity = NoteEntity.builder()
        .uuid(existingId)
        .note("Original content")
        .createdAt(Instant.now().minusSeconds(3600))
        .updatedAt(Instant.now().minusSeconds(1800))
        .build();

    var savedEntity = NoteEntity.builder()
        .uuid(existingId)
        .note("Updated content")
        .createdAt(existingEntity.getCreatedAt())
        .updatedAt(Instant.now())
        .build();

    var savedNote = Note.builder()
        .uuid(existingId)
        .note("Updated content")
        .createdAt(savedEntity.getCreatedAt())
        .updatedAt(savedEntity.getUpdatedAt())
        .build();

    when(repository.findById(existingId)).thenReturn(Optional.of(existingEntity));
    doNothing().when(mapper).updateEntity(updateNote, existingEntity);
    when(repository.save(existingEntity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(savedNote);

    // Act
    var result = noteService.upsert(updateNote);

    // Assert
    assertThat(result).isEqualTo(savedNote);

    verify(repository).findById(existingId);
    verify(mapper).updateEntity(updateNote, existingEntity);
    verify(repository).save(existingEntity);
    verify(mapper).toDto(savedEntity);
    verify(mapper, never()).toEntity(any(Note.class));
  }

  @Test
  @DisplayName("upsert_withUuidButNonExistentEntity_shouldCreateNewEntity")
  void upsert_withUuidButNonExistentEntity_shouldCreateNewEntity() {
    // Arrange
    var nonExistentId = UUID.randomUUID();
    var noteWithInvalidId = Note.builder()
        .uuid(nonExistentId)
        .note("Note with invalid ID")
        .build();

    var newEntity = NoteEntity.builder()
        .uuid(nonExistentId)
        .note("Note with invalid ID")
        .build();

    var savedEntity = NoteEntity.builder()
        .uuid(nonExistentId)
        .note("Note with invalid ID")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    var savedNote = Note.builder()
        .uuid(nonExistentId)
        .note("Note with invalid ID")
        .createdAt(savedEntity.getCreatedAt())
        .updatedAt(savedEntity.getUpdatedAt())
        .build();

    when(repository.findById(nonExistentId)).thenReturn(Optional.empty());
    when(mapper.toEntity(noteWithInvalidId)).thenReturn(newEntity);
    when(repository.save(newEntity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(savedNote);

    // Act
    var result = noteService.upsert(noteWithInvalidId);

    // Assert
    assertThat(result).isEqualTo(savedNote);

    verify(repository).findById(nonExistentId);
    verify(mapper).toEntity(noteWithInvalidId);
    verify(repository).save(newEntity);
    verify(mapper).toDto(savedEntity);
    verify(mapper, never()).updateEntity(any(Note.class), any(NoteEntity.class));
  }

  @Test
  @DisplayName("upsert_withNullNote_shouldCreateNewEntity")
  void upsert_withNullNoteContent_shouldCreateNewEntity() {
    // Arrange
    var noteWithNullContent = Note.builder()
        .note(null)
        .build();

    var newEntity = NoteEntity.builder()
        .note(null)
        .build();

    var savedEntity = NoteEntity.builder()
        .uuid(UUID.randomUUID())
        .note(null)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    var savedNote = Note.builder()
        .uuid(savedEntity.getUuid())
        .note(null)
        .createdAt(savedEntity.getCreatedAt())
        .updatedAt(savedEntity.getUpdatedAt())
        .build();

    when(mapper.toEntity(noteWithNullContent)).thenReturn(newEntity);
    when(repository.save(newEntity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(savedNote);

    // Act
    var result = noteService.upsert(noteWithNullContent);

    // Assert
    assertThat(result).isEqualTo(savedNote);

    verify(mapper).toEntity(noteWithNullContent);
    verify(repository).save(newEntity);
    verify(mapper).toDto(savedEntity);
  }

  @Test
  @DisplayName("deleteById_shouldCallRepositoryDeleteById")
  void deleteById_shouldCallRepositoryDeleteById() {
    // Arrange
    var noteId = UUID.randomUUID();
    doNothing().when(repository).deleteById(noteId);

    // Act
    noteService.deleteById(noteId);

    // Assert
    verify(repository).deleteById(noteId);
  }

  @Test
  @DisplayName("deleteById_withNonExistentId_shouldNotThrowException")
  void deleteById_withNonExistentId_shouldNotThrowException() {
    // Arrange
    var noteId = UUID.randomUUID();
    doNothing().when(repository).deleteById(noteId); // Repository handles non-existent gracefully

    // Act & Assert - Should not throw
    noteService.deleteById(noteId);

    verify(repository).deleteById(noteId);
  }

  @Test
  @DisplayName("upsert_withEmptyStringNote_shouldWork")
  void upsert_withEmptyStringNote_shouldWork() {
    // Arrange
    var emptyNote = Note.builder()
        .note("")
        .build();

    var newEntity = NoteEntity.builder()
        .note("")
        .build();

    var savedEntity = NoteEntity.builder()
        .uuid(UUID.randomUUID())
        .note("")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    var savedNote = Note.builder()
        .uuid(savedEntity.getUuid())
        .note("")
        .createdAt(savedEntity.getCreatedAt())
        .updatedAt(savedEntity.getUpdatedAt())
        .build();

    when(mapper.toEntity(emptyNote)).thenReturn(newEntity);
    when(repository.save(newEntity)).thenReturn(savedEntity);
    when(mapper.toDto(savedEntity)).thenReturn(savedNote);

    // Act
    var result = noteService.upsert(emptyNote);

    // Assert
    assertThat(result).isEqualTo(savedNote);
    assertThat(result.getNote()).isEmpty();

    verify(mapper).toEntity(emptyNote);
    verify(repository).save(newEntity);
    verify(mapper).toDto(savedEntity);
  }

  @Test
  @DisplayName("Service methods should have proper transactional behavior")
  void serviceMethods_shouldHaveTransactionalAnnotations() throws NoSuchMethodException {
    // This test verifies that the service methods have proper @Transactional annotations
    var getAllMethod = NoteService.class.getMethod("getAll");
    var findByIdMethod = NoteService.class.getMethod("findById", UUID.class);
    var upsertMethod = NoteService.class.getMethod("upsert", Note.class);
    var deleteByIdMethod = NoteService.class.getMethod("deleteById", UUID.class);

    // Verify read-only methods are marked as such
    assertThat(getAllMethod.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
        .isTrue();
    assertThat(findByIdMethod.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
        .isTrue();

    // Verify write methods have transactional annotations
    assertThat(upsertMethod.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
        .isTrue();
    assertThat(deleteByIdMethod.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
        .isTrue();
  }
}
