package ch.hftm.relationaldatabases.transferdemo.jpa.repositories;

import ch.hftm.relationaldatabases.transferdemo.jpa.entities.NoteEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@Import(NoteRepositoryTest.TestContainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("NoteRepository Integration Tests")
class NoteRepositoryTest {

  @TestConfiguration
  static class TestContainersConfiguration {
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
      return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
    }
  }

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private NoteRepository repository;

  @Test
  @DisplayName("save_withNewEntity_shouldGenerateUuidAndTimestamps")
  void save_withNewEntity_shouldGenerateUuidAndTimestamps() {
    // Arrange
    var entity = NoteEntity.builder()
        .note("Test note content")
        .build();

    var beforeSave = Instant.now();

    // Act
    var savedEntity = repository.save(entity);
    entityManager.flush(); // Force database write

    var afterSave = Instant.now();

    // Assert
    assertThat(savedEntity).isNotNull();
    assertThat(savedEntity.getUuid()).isNotNull();
    assertThat(savedEntity.getNote()).isEqualTo("Test note content");
    assertThat(savedEntity.getCreatedAt()).isNotNull();
    assertThat(savedEntity.getCreatedAt()).isBetween(beforeSave.minusSeconds(1), afterSave.plusSeconds(1));
    assertThat(savedEntity.getUpdatedAt()).isNotNull();
    assertThat(savedEntity.getUpdatedAt()).isBetween(beforeSave.minusSeconds(1), afterSave.plusSeconds(1));
    // For new entities, createdAt and updatedAt should be very close (within 1 second)
    assertThat(savedEntity.getCreatedAt()).isCloseTo(savedEntity.getUpdatedAt(), within(1000, java.time.temporal.ChronoUnit.MILLIS));
  }

  @Test
  @DisplayName("save_withExistingEntity_shouldUpdateTimestamp")
  void save_withExistingEntity_shouldUpdateTimestamp() throws InterruptedException {
    // Arrange - Create and save initial entity
    var entity = NoteEntity.builder()
        .note("Original content")
        .build();
    var savedEntity = repository.save(entity);
    entityManager.flush();

    var originalCreatedAt = savedEntity.getCreatedAt();
    var originalUpdatedAt = savedEntity.getUpdatedAt();

    // Small delay to ensure timestamp difference
    Thread.sleep(10);

    // Act - Update the entity
    savedEntity.setNote("Updated content");
    var updatedEntity = repository.save(savedEntity);
    entityManager.flush();

    // Assert
    assertThat(updatedEntity.getUuid()).isEqualTo(savedEntity.getUuid());
    assertThat(updatedEntity.getNote()).isEqualTo("Updated content");
    assertThat(updatedEntity.getCreatedAt()).isEqualTo(originalCreatedAt); // Should not change
    assertThat(updatedEntity.getUpdatedAt()).isAfter(originalUpdatedAt); // Should be updated
  }

  @Test
  @DisplayName("save_withNullNote_shouldThrowException")
  void save_withNullNote_shouldThrowException() {
    // Arrange
    var entity = NoteEntity.builder()
        .note(null)
        .build();

    // Act & Assert
    assertThatThrownBy(() -> {
      repository.save(entity);
      entityManager.flush();
    }).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("findById_withExistingUuid_shouldReturnEntity")
  void findById_withExistingUuid_shouldReturnEntity() {
    // Arrange
    var entity = NoteEntity.builder()
        .note("Findable note")
        .build();
    var savedEntity = repository.save(entity);
    entityManager.flush();

    // Act
    var foundEntity = repository.findById(savedEntity.getUuid());

    // Assert
    assertThat(foundEntity).isPresent();
    assertThat(foundEntity.get().getUuid()).isEqualTo(savedEntity.getUuid());
    assertThat(foundEntity.get().getNote()).isEqualTo("Findable note");
  }

  @Test
  @DisplayName("findById_withNonExistentUuid_shouldReturnEmpty")
  void findById_withNonExistentUuid_shouldReturnEmpty() {
    // Arrange
    var nonExistentUuid = UUID.randomUUID();

    // Act
    var result = repository.findById(nonExistentUuid);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("findAll_withMultipleEntities_shouldReturnAllInCorrectOrder")
  void findAll_withMultipleEntities_shouldReturnAllInCorrectOrder() {
    // Arrange
    var entity1 = repository.save(NoteEntity.builder().note("First note").build());
    var entity2 = repository.save(NoteEntity.builder().note("Second note").build());
    var entity3 = repository.save(NoteEntity.builder().note("Third note").build());
    entityManager.flush();

    // Act
    var allEntities = repository.findAll();

    // Assert
    assertThat(allEntities).hasSize(3);
    assertThat(allEntities).extracting(NoteEntity::getNote)
        .containsExactlyInAnyOrder("First note", "Second note", "Third note");
  }

  @Test
  @DisplayName("deleteById_withExistingEntity_shouldRemoveFromDatabase")
  void deleteById_withExistingEntity_shouldRemoveFromDatabase() {
    // Arrange
    var entity = repository.save(NoteEntity.builder().note("To be deleted").build());
    entityManager.flush();
    var entityId = entity.getUuid();

    // Verify entity exists
    assertThat(repository.findById(entityId)).isPresent();

    // Act
    repository.deleteById(entityId);
    entityManager.flush();

    // Assert
    assertThat(repository.findById(entityId)).isEmpty();
  }

  @Test
  @DisplayName("deleteById_withNonExistentUuid_shouldNotThrowException")
  void deleteById_withNonExistentUuid_shouldNotThrowException() {
    // Arrange
    var nonExistentUuid = UUID.randomUUID();

    // Act & Assert - Should not throw exception
    repository.deleteById(nonExistentUuid);
    entityManager.flush();
  }

  @Test
  @DisplayName("streamAllNotes_withMultipleEntities_shouldReturnStream")
  @Transactional // Required for streaming queries
  void streamAllNotes_withMultipleEntities_shouldReturnStream() {
    // Arrange
    var entity1 = repository.save(NoteEntity.builder().note("Stream note 1").build());
    var entity2 = repository.save(NoteEntity.builder().note("Stream note 2").build());
    var entity3 = repository.save(NoteEntity.builder().note("Stream note 3").build());
    entityManager.flush();

    // Act
    var noteTexts = repository.streamAllNotes()
        .map(NoteEntity::getNote)
        .sorted()
        .toList();

    // Assert
    assertThat(noteTexts).hasSize(3);
    assertThat(noteTexts).containsExactly("Stream note 1", "Stream note 2", "Stream note 3");
  }

  @Test
  @DisplayName("streamAllNotes_withEmptyDatabase_shouldReturnEmptyStream")
  @Transactional
  void streamAllNotes_withEmptyDatabase_shouldReturnEmptyStream() {
    // Act
    var result = repository.streamAllNotes().toList();

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("streamAllNotes_shouldWorkWithLargeDataset")
  @Transactional
  void streamAllNotes_shouldWorkWithLargeDataset() {
    // Arrange - Create multiple entities
    var expectedCount = 50;
    for (var i = 1; i <= expectedCount; i++) {
      repository.save(NoteEntity.builder().note("Note " + i).build());
    }
    entityManager.flush();

    // Act - Use stream to count without loading all into memory
    var count = repository.streamAllNotes().count();

    // Assert
    assertThat(count).isEqualTo(expectedCount);
  }

  @Test
  @DisplayName("count_withMultipleEntities_shouldReturnCorrectCount")
  void count_withMultipleEntities_shouldReturnCorrectCount() {
    // Arrange
    repository.save(NoteEntity.builder().note("Count note 1").build());
    repository.save(NoteEntity.builder().note("Count note 2").build());
    entityManager.flush();

    // Act
    var count = repository.count();

    // Assert
    assertThat(count).isEqualTo(2);
  }

  @Test
  @DisplayName("existsById_withExistingEntity_shouldReturnTrue")
  void existsById_withExistingEntity_shouldReturnTrue() {
    // Arrange
    var entity = repository.save(NoteEntity.builder().note("Existing note").build());
    entityManager.flush();

    // Act
    var exists = repository.existsById(entity.getUuid());

    // Assert
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("existsById_withNonExistentEntity_shouldReturnFalse")
  void existsById_withNonExistentEntity_shouldReturnFalse() {
    // Arrange
    var nonExistentUuid = UUID.randomUUID();

    // Act
    var exists = repository.existsById(nonExistentUuid);

    // Assert
    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("saveAll_withMultipleEntities_shouldSaveAllAndReturnList")
  void saveAll_withMultipleEntities_shouldSaveAllAndReturnList() {
    // Arrange
    var entities = List.of(
        NoteEntity.builder().note("Batch note 1").build(),
        NoteEntity.builder().note("Batch note 2").build(),
        NoteEntity.builder().note("Batch note 3").build()
    );

    // Act
    var savedEntities = repository.saveAll(entities);
    entityManager.flush();

    // Assert
    assertThat(savedEntities).hasSize(3);
    assertThat(savedEntities).allMatch(entity -> entity.getUuid() != null);
    assertThat(savedEntities).allMatch(entity -> entity.getCreatedAt() != null);
    assertThat(savedEntities).extracting(NoteEntity::getNote)
        .containsExactlyInAnyOrder("Batch note 1", "Batch note 2", "Batch note 3");
  }
}
