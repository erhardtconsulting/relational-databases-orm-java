# Transfer Demo - Spring Boot ORM Lernprojekt

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)

Ein umfassendes Lernprojekt für den Kurs **"Relationale Datenbanken"** der [Höheren Fachschule für Technik Mittelland](https://hftm.ch) zur praktischen Demonstration von Object Relational Mapping (ORM) mit modernen Java-Technologien.

🔗 **[Kurs-Website](https://relational-databases.erhardt.consulting/)**

## 📚 Lernziele

Dieses Projekt vermittelt Studierenden folgende Kernkonzepte:

- **Object Relational Mapping (ORM)** mit Hibernate/JPA
- **Spring Boot Fundamentals** - Dependency Injection, Auto-Configuration
- **Clean Architecture** - Separation of Concerns zwischen Entity-, DTO-, Service- und Repository-Schichten
- **Datenbankmanagement** - PostgreSQL Integration, Flyway Migrationen
- **Containerisierte Entwicklung** - Docker Compose für lokale Umgebung
- **Type-Safe Object Mapping** - MapStruct für sichere Objekttransformationen

## 🛠️ Technologie-Stack

### Backend
- **Spring Boot 3.4.6** - Enterprise Java Framework
- **Java 21** - Moderne Java-Features und Performance
- **Hibernate/JPA** - Object Relational Mapping
- **PostgreSQL 17** - Relationale Datenbank
- **Flyway** - Datenbankmigrationen

### Frontend
- **Thymeleaf** - Server-side HTML Templating
- **Bootstrap CSS** - Responsive Web Design

### Entwicklung & Testing
- **Maven** - Build Management und Dependency Resolution
- **Testcontainers** - Integration Testing mit containerisierten Datenbanken
- **Docker Compose** - Lokale Entwicklungsumgebung

## 🚀 Installation und Einrichtung

### Voraussetzungen

- **Java 21** oder höher ([OpenJDK](https://openjdk.java.net/) empfohlen)
- **Maven 3.6+** für Build Management
- **Docker & Docker Compose** für die Datenbankumgebung
- **Git** für Versionskontrolle

### 1. Projekt klonen

```bash
git clone <repository-url>
cd transferdemo
```

### 2. Datenbank starten

```bash
# PostgreSQL Datenbank mit Docker Compose starten
docker-compose -f docker-compose.db.yaml up -d

# Überprüfen, ob die Datenbank läuft
docker ps
```

### 3. Anwendung kompilieren und starten

```bash
# Dependencies installieren und Code kompilieren
mvn clean compile

# Anwendung starten
mvn spring-boot:run

# Alternative: Mit spezifischem Profil
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 4. Anwendung testen

- **Webanwendung:** http://localhost:8080
- **Datenbank:** PostgreSQL auf localhost:5432
  - Database: `transferdemo`
  - Username: `transferdemo`
  - Password: `transferdemo`

## 🏗️ Projektarchitektur

Das Projekt folgt einer **Layered Architecture** mit klarer Trennung der Verantwortlichkeiten:

```
src/main/java/ch/hftm/relationaldatabases/transferdemo/
├── web/                    # 🌐 Presentation Layer
│   └── NoteController      # HTTP Endpoints & Request Handling
├── services/               # 💼 Business Logic Layer  
│   └── NoteService         # Geschäftslogik & Transaktionsmanagement
├── mappers/                # 🔄 Object Mapping Layer
│   ├── NoteMapper          # Entity ↔ DTO Mapping
│   └── NoteFormMapper      # Form ↔ DTO Mapping
├── dtos/                   # 📋 Data Transfer Objects
│   ├── Note                # Immutable Read DTO
│   └── NoteForm            # Mutable Form DTO
└── jpa/                    # 💾 Data Access Layer
    ├── entities/           # JPA Entity Classes
    │   └── NoteEntity      # Datenbankentität mit Mapping
    └── repositories/       # Data Repository Interfaces
        └── NoteRepository  # Spring Data JPA Repository
```

### 🔄 Datenfluss

```
HTTP Request → Controller → Service → Repository → Database
              ↓
         Form/DTO ← Mapper ← Entity ← JPA/Hibernate
```

## 💡 Kernkonzepte und Patterns

### 1. **Entity-DTO Separation**
```java
// JPA Entity (interne Datenrepräsentation)
@Entity
@Table(name = "notes")
public class NoteEntity {
    @Id
    private UUID id;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    // ...
}

// DTO (externe API-Repräsentation)
@Value
@Builder
public class Note {
    UUID id;
    String content;
    LocalDateTime createdAt;
    // Keine JPA Annotations!
}
```

### 2. **Type-Safe Mapping mit MapStruct**
```java
@Mapper(componentModel = "spring")
public interface NoteMapper {
    Note entityToDto(NoteEntity entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    NoteEntity dtoToEntity(Note dto);
}
```

### 3. **Service Layer Pattern**
```java
@Service
@Transactional(readOnly = true)
public class NoteService {
    
    @Transactional // Schreibtransaktion
    public Note saveNote(Note note) {
        // Geschäftslogik hier
    }
}
```

### 4. **Repository Pattern mit Spring Data**
```java
@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, UUID> {
    
    @Query("SELECT n FROM NoteEntity n ORDER BY n.createdAt DESC")
    Stream<NoteEntity> streamAllNotes();
}
```

## 🧪 Testing

### Unit Tests ausführen
```bash
mvn test
```

### Integration Tests ausführen
```bash
mvn test -Dspring.profiles.active=test
```

### Test Coverage Report
```bash
mvn jacoco:report
# Report verfügbar unter: target/site/jacoco/index.html
```

## 🎯 Pädagogische Übungen

### Grundübungen
1. **CRUD Operationen verstehen** - Analysiere die vollständigen Create/Read/Update/Delete Workflows
2. **Mapping-Layer erkunden** - Untersuche, wie MapStruct Entity-DTO Transformationen durchführt
3. **Transaction-Management** - Verstehe `@Transactional` Annotations und ihre Auswirkungen

### Erweiterungsübungen
1. **Neue Entität hinzufügen** - Erstelle eine `Category` Entität mit Beziehung zu `Note`
2. **Validierung implementieren** - Füge Bean Validation für Eingabedaten hinzu
3. **Suchoption einbauen** - Implementiere Textsuche in Notizen
4. **Pagination hinzufügen** - Erweitere die Liste um Seitennummerierung

### Fortgeschrittene Übungen
1. **Many-to-Many Beziehungen** - Implementiere Tags für Notizen
2. **Optimistic Locking** - Füge Versionskontrolle für gleichzeitige Bearbeitung hinzu
3. **Custom Queries** - Erstelle komplexe JPQL/Native Queries
4. **Caching Strategy** - Implementiere Second-Level Caching mit Hibernate

## 📁 Wichtige Dateien

| Datei | Zweck | Lernfokus |
|-------|-------|-----------|
| `NoteEntity.java` | JPA Entity Definition | Datenbankmapping, Annotations |
| `NoteRepository.java` | Data Access Layer | Spring Data JPA, Query Methods |
| `NoteService.java` | Business Logic | Transaction Management, Service Pattern |
| `NoteMapper.java` | Object Mapping | MapStruct, Type Safety |
| `NoteController.java` | Web Layer | Spring MVC, HTTP Handling |
| `application.yaml` | Konfiguration | Spring Boot Properties |
| `V1.0__initial.sql` | Datenbankschema | Flyway Migration, DDL |

## 🐳 Docker Entwicklungsumgebung

Das Projekt nutzt Docker Compose für eine konsistente Entwicklungsumgebung:

```yaml
# docker-compose.db.yaml
services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: transferdemo
      POSTGRES_USER: transferdemo
      POSTGRES_PASSWORD: transferdemo
    ports:
      - "5432:5432"
```

### Nützliche Docker Befehle
```bash
# Datenbank starten
docker-compose -f docker-compose.db.yaml up -d

# Logs anzeigen
docker-compose -f docker-compose.db.yaml logs -f

# Datenbank stoppen
docker-compose -f docker-compose.db.yaml down

# Datenbank zurücksetzen
docker-compose -f docker-compose.db.yaml down -v
```

## 📖 Zusätzliche Ressourcen

### Dokumentation
- [Spring Boot Referenz](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Hibernate ORM](https://hibernate.org/orm/documentation/)
- [MapStruct Reference](https://mapstruct.org/documentation/stable/reference/html/)

### Lernmaterialien
- [Kurs: Relationale Datenbanken](https://relational-databases.erhardt.consulting/)
- [Spring Boot Getting Started](https://spring.io/guides/gs/spring-boot/)
- [JPA Fundamentals](https://docs.oracle.com/javaee/7/tutorial/persistence-intro.htm)

## 🤝 Beitragen

Dieses Projekt dient Bildungszwecken. Verbesserungsvorschläge und Erweiterungen sind willkommen:

1. Fork des Repositories erstellen
2. Feature Branch erstellen (`git checkout -b feature/new-feature`)
3. Änderungen committen (`git commit -am 'Add new feature'`)
4. Branch pushen (`git push origin feature/new-feature`)
5. Pull Request erstellen

## 📄 Lizenz

Dieses Projekt steht unter der MIT-Lizenz und dient ausschliesslich Bildungszwecken im Rahmen des Kurses "Relationale Datenbanken" der Höheren Fachschule für Technik Mittelland.
