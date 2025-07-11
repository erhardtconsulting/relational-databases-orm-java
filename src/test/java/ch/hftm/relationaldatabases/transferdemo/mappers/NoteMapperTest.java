package ch.hftm.relationaldatabases.transferdemo.mappers;

import ch.hftm.relationaldatabases.transferdemo.dtos.Note;
import ch.hftm.relationaldatabases.transferdemo.jpa.entities.NoteEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NoteMapper Tests")
class NoteMapperTest {

  private NoteMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(NoteMapper.class);
  }

  @Test
  @DisplayName("toDto_withCompleteEntity_shouldMapAllFields")
  void toDto_withCompleteEntity_shouldMapAllFields() {
    // Arrange
    var uuid = UUID.randomUUID();
    var createdAt = Instant.now().minusSeconds(3600);
    var updatedAt = Instant.now();
    var noteText = "Test note content";

    var entity = NoteEntity.builder()
        .uuid(uuid)
        .note(noteText)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .build();

    // Act
    var dto = mapper.toDto(entity);

    // Assert
    assertThat(dto).isNotNull();
    assertThat(dto.getUuid()).isEqualTo(uuid);
    assertThat(dto.getNote()).isEqualTo(noteText);
    assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
    assertThat(dto.getUpdatedAt()).isEqualTo(updatedAt);
  }

  @Test
  @DisplayName("toDto_withMinimalEntity_shouldMapRequiredFields")
  void toDto_withMinimalEntity_shouldMapRequiredFields() {
    // Arrange
    var uuid = UUID.randomUUID();
    var noteText = "Minimal note";

    var entity = NoteEntity.builder()
        .uuid(uuid)
        .note(noteText)
        .build();

    // Act
    var dto = mapper.toDto(entity);

    // Assert
    assertThat(dto).isNotNull();
    assertThat(dto.getUuid()).isEqualTo(uuid);
    assertThat(dto.getNote()).isEqualTo(noteText);
    assertThat(dto.getCreatedAt()).isNull();
    assertThat(dto.getUpdatedAt()).isNull();
  }

  @Test
  @DisplayName("toEntity_withCompleteDto_shouldMapCorrectFields")
  void toEntity_withCompleteDto_shouldMapCorrectFields() {
    // Arrange
    var uuid = UUID.randomUUID();
    var noteText = "Test note content";
    var createdAt = Instant.now().minusSeconds(3600);
    var updatedAt = Instant.now();

    var dto = Note.builder()
        .uuid(uuid)
        .note(noteText)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .build();

    // Act
    var entity = mapper.toEntity(dto);

    // Assert
    assertThat(entity).isNotNull();
    assertThat(entity.getUuid()).isEqualTo(uuid);
    assertThat(entity.getNote()).isEqualTo(noteText);
    // Timestamps should be ignored in entity mapping (managed by JPA)
    assertThat(entity.getCreatedAt()).isNull();
    assertThat(entity.getUpdatedAt()).isNull();
  }

  @Test
  @DisplayName("toEntity_withMinimalDto_shouldMapRequiredFields")
  void toEntity_withMinimalDto_shouldMapRequiredFields() {
    // Arrange
    var noteText = "Required note content";

    var dto = Note.builder()
        .note(noteText)
        .build();

    // Act
    var entity = mapper.toEntity(dto);

    // Assert
    assertThat(entity).isNotNull();
    assertThat(entity.getUuid()).isNull(); // Will be generated by JPA
    assertThat(entity.getNote()).isEqualTo(noteText);
    assertThat(entity.getCreatedAt()).isNull(); // Managed by JPA
    assertThat(entity.getUpdatedAt()).isNull(); // Managed by JPA
  }

  @Test
  @DisplayName("updateEntity_withExistingEntity_shouldUpdateOnlyNoteField")
  void updateEntity_withExistingEntity_shouldUpdateOnlyNoteField() {
    // Arrange
    var originalUuid = UUID.randomUUID();
    var originalCreatedAt = Instant.now().minusSeconds(7200);
    var originalUpdatedAt = Instant.now().minusSeconds(3600);
    var originalNote = "Original note";

    var existingEntity = NoteEntity.builder()
        .uuid(originalUuid)
        .note(originalNote)
        .createdAt(originalCreatedAt)
        .updatedAt(originalUpdatedAt)
        .build();

    var updatedNoteText = "Updated note content";
    var updateDto = Note.builder()
        .uuid(UUID.randomUUID()) // Different UUID should be ignored
        .note(updatedNoteText)
        .createdAt(Instant.now()) // Should be ignored
        .updatedAt(Instant.now()) // Should be ignored
        .build();

    // Act
    mapper.updateEntity(updateDto, existingEntity);

    // Assert
    assertThat(existingEntity.getUuid()).isEqualTo(originalUuid); // Unchanged
    assertThat(existingEntity.getNote()).isEqualTo(updatedNoteText); // Updated
    assertThat(existingEntity.getCreatedAt()).isEqualTo(originalCreatedAt); // Unchanged
    assertThat(existingEntity.getUpdatedAt()).isEqualTo(originalUpdatedAt); // Unchanged (JPA manages this)
  }

  @Test
  @DisplayName("updateEntity_withNullNote_shouldSetNoteToNull")
  void updateEntity_withNullNote_shouldSetNoteToNull() {
    // Arrange
    var existingEntity = NoteEntity.builder()
        .uuid(UUID.randomUUID())
        .note("Existing note")
        .build();

    var updateDto = Note.builder()
        .note(null)
        .build();

    // Act
    mapper.updateEntity(updateDto, existingEntity);

    // Assert
    assertThat(existingEntity.getNote()).isNull();
  }

  @Test
  @DisplayName("toDto_withNullEntity_shouldReturnNull")
  void toDto_withNullEntity_shouldReturnNull() {
    // Act
    var result = mapper.toDto(null);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("toEntity_withNullDto_shouldReturnNull")
  void toEntity_withNullDto_shouldReturnNull() {
    // Act
    var result = mapper.toEntity(null);

    // Assert
    assertThat(result).isNull();
  }
}
