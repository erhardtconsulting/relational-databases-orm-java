package ch.hftm.relationaldatabases.transferdemo.mappers;

import ch.hftm.relationaldatabases.transferdemo.dtos.Note;
import ch.hftm.relationaldatabases.transferdemo.dtos.NoteForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NoteFormMapper Tests")
class NoteFormMapperTest {

  private NoteFormMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(NoteFormMapper.class);
  }

  @Test
  @DisplayName("toForm_withCompleteNote_shouldMapCorrectFields")
  void toForm_withCompleteNote_shouldMapCorrectFields() {
    // Arrange
    var uuid = UUID.randomUUID();
    var noteText = "Test note content";
    var createdAt = Instant.now().minusSeconds(3600);
    var updatedAt = Instant.now();

    var note = Note.builder()
        .uuid(uuid)
        .note(noteText)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .build();

    // Act
    var form = mapper.toForm(note);

    // Assert
    assertThat(form).isNotNull();
    assertThat(form.getUuid()).isEqualTo(uuid);
    assertThat(form.getNote()).isEqualTo(noteText);
  }

  @Test
  @DisplayName("toForm_withMinimalNote_shouldMapRequiredFields")
  void toForm_withMinimalNote_shouldMapRequiredFields() {
    // Arrange
    var noteText = "Minimal note content";

    var note = Note.builder()
        .note(noteText)
        .build();

    // Act
    var form = mapper.toForm(note);

    // Assert
    assertThat(form).isNotNull();
    assertThat(form.getUuid()).isNull();
    assertThat(form.getNote()).isEqualTo(noteText);
  }

  @Test
  @DisplayName("toDto_withCompleteForm_shouldMapCorrectFields")
  void toDto_withCompleteForm_shouldMapCorrectFields() {
    // Arrange
    var uuid = UUID.randomUUID();
    var noteText = "Form note content";

    var form = new NoteForm(uuid, noteText);

    // Act
    var dto = mapper.toDto(form);

    // Assert
    assertThat(dto).isNotNull();
    assertThat(dto.getUuid()).isEqualTo(uuid);
    assertThat(dto.getNote()).isEqualTo(noteText);
    // Timestamps should be null (ignored in mapping)
    assertThat(dto.getCreatedAt()).isNull();
    assertThat(dto.getUpdatedAt()).isNull();
  }

  @Test
  @DisplayName("toDto_withNewForm_shouldMapWithoutUuid")
  void toDto_withNewForm_shouldMapWithoutUuid() {
    // Arrange
    var noteText = "New form content";
    var form = new NoteForm(null, noteText);

    // Act
    var dto = mapper.toDto(form);

    // Assert
    assertThat(dto).isNotNull();
    assertThat(dto.getUuid()).isNull();
    assertThat(dto.getNote()).isEqualTo(noteText);
    assertThat(dto.getCreatedAt()).isNull();
    assertThat(dto.getUpdatedAt()).isNull();
  }

  @Test
  @DisplayName("toDto_withEmptyNote_shouldMapEmptyString")
  void toDto_withEmptyNote_shouldMapEmptyString() {
    // Arrange
    var form = new NoteForm(UUID.randomUUID(), "");

    // Act
    var dto = mapper.toDto(form);

    // Assert
    assertThat(dto).isNotNull();
    assertThat(dto.getNote()).isEmpty();
  }

  @Test
  @DisplayName("toDto_withNullNote_shouldMapNull")
  void toDto_withNullNote_shouldMapNull() {
    // Arrange
    var form = new NoteForm(UUID.randomUUID(), null);

    // Act
    var dto = mapper.toDto(form);

    // Assert
    assertThat(dto).isNotNull();
    assertThat(dto.getNote()).isNull();
  }

  @Test
  @DisplayName("toForm_withNullNote_shouldReturnNull")
  void toForm_withNullNote_shouldReturnNull() {
    // Act
    var result = mapper.toForm(null);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("toDto_withNullForm_shouldReturnNull")
  void toDto_withNullForm_shouldReturnNull() {
    // Act
    var result = mapper.toDto(null);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("roundTripMapping_shouldPreserveData")
  void roundTripMapping_shouldPreserveData() {
    // Arrange
    var originalUuid = UUID.randomUUID();
    var originalNote = "Round trip test";

    var originalDto = Note.builder()
        .uuid(originalUuid)
        .note(originalNote)
        .createdAt(Instant.now().minusSeconds(3600))
        .updatedAt(Instant.now())
        .build();

    // Act - Convert to form and back to DTO
    var form = mapper.toForm(originalDto);
    var resultDto = mapper.toDto(form);

    // Assert
    assertThat(resultDto).isNotNull();
    assertThat(resultDto.getUuid()).isEqualTo(originalUuid);
    assertThat(resultDto.getNote()).isEqualTo(originalNote);
    // Timestamps are lost in form mapping (as expected)
    assertThat(resultDto.getCreatedAt()).isNull();
    assertThat(resultDto.getUpdatedAt()).isNull();
  }
}
