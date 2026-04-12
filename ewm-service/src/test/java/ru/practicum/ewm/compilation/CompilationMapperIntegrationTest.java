package ru.practicum.ewm.compilation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.compilation.Compilation;
import ru.practicum.ewm.compilation.CompilationRepository;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.CompilationMapper;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.location.Location;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class CompilationMapperIntegrationTest {

    @Autowired
    private CompilationMapper compilationMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompilationRepository compilationRepository;

    private Event event1;
    private Event event2;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .name("Test User")
                .email("test@test.com")
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Test Category")
                .build());

        event1 = createEvent(user, category, "Event 1");
        event2 = createEvent(user, category, "Event 2");
    }

    @Test
    void toCompilation_shouldMapNewCompilationDtoToEntity() {
        // given
        NewCompilationDto dto = new NewCompilationDto(
                Set.of(event1.getId(), event2.getId()),
                true,
                "Best Compilation"
        );

        // when
        Compilation compilation = compilationMapper.toCompilation(dto);

        // then
        assertThat(compilation).isNotNull();
        assertThat(compilation.getId()).isNull();
        assertThat(compilation.getTitle()).isEqualTo("Best Compilation");
        assertThat(compilation.getPinned()).isTrue();
        assertThat(compilation.getEvents()).hasSize(2);
        assertThat(compilation.getEvents())
                .extracting(Event::getId)
                .containsExactlyInAnyOrder(event1.getId(), event2.getId());
    }

    @Test
    void toCompilation_withNullEvents_shouldMapToEmptySet() {
        // given
        NewCompilationDto dto = new NewCompilationDto(null, false, "Empty Compilation");

        // when
        Compilation compilation = compilationMapper.toCompilation(dto);

        // then
        assertThat(compilation.getEvents()).isEmpty();
    }

    @Test
    void toCompilation_withNull_shouldReturnNull() {
        // when
        Compilation compilation = compilationMapper.toCompilation(null);

        // then
        assertThat(compilation).isNull();
    }

    @Test
    void toCompilationDto_shouldMapEntityToDto() {
        // given
        Compilation compilation = compilationRepository.save(Compilation.builder()
                .title("Test Compilation")
                .pinned(true)
                .events(Set.of(event1, event2))
                .build());

        // when
        CompilationDto dto = compilationMapper.toCompilationDto(compilation);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(compilation.getId());
        assertThat(dto.title()).isEqualTo("Test Compilation");
        assertThat(dto.pinned()).isTrue();
        assertThat(dto.events()).hasSize(2);
    }

    @Test
    void toCompilationDto_withNullEvents_shouldReturnEmptySet() {
        // given
        Compilation compilation = Compilation.builder()
                .title("No Events")
                .pinned(false)
                .events(null)
                .build();

        // when
        CompilationDto dto = compilationMapper.toCompilationDto(compilation);

        // then
        assertThat(dto.events()).isEmpty();
    }

    @Test
    void toCompilationDto_withNull_shouldReturnNull() {
        // when
        CompilationDto dto = compilationMapper.toCompilationDto(null);

        // then
        assertThat(dto).isNull();
    }

    @Test
    void updateCompilationFromDto_shouldUpdateAllFields() {
        // given
        Compilation compilation = compilationRepository.save(Compilation.builder()
                .title("Old Title")
                .pinned(false)
                .events(Set.of(event1))
                .build());

        UpdateCompilationRequest updateRequest = new UpdateCompilationRequest(
                Set.of(event2.getId()),
                true,
                "New Title"
        );

        // when
        compilationMapper.updateCompilationFromDto(updateRequest, compilation);

        // then
        assertThat(compilation.getTitle()).isEqualTo("New Title");
        assertThat(compilation.getPinned()).isTrue();
        assertThat(compilation.getEvents()).hasSize(1);
        assertThat(compilation.getEvents().iterator().next().getId()).isEqualTo(event2.getId());
    }

    @Test
    void updateCompilationFromDto_withNullFields_shouldNotUpdate() {
        // given
        Compilation compilation = compilationRepository.save(Compilation.builder()
                .title("Original Title")
                .pinned(false)
                .events(Set.of(event1))
                .build());

        String originalTitle = compilation.getTitle();
        Boolean originalPinned = compilation.getPinned();
        Set<Event> originalEvents = compilation.getEvents();

        UpdateCompilationRequest emptyUpdate = new UpdateCompilationRequest(null, null, null);

        // when
        compilationMapper.updateCompilationFromDto(emptyUpdate, compilation);

        // then
        assertThat(compilation.getTitle()).isEqualTo(originalTitle);
        assertThat(compilation.getPinned()).isEqualTo(originalPinned);

    }

    @Test
    void updateCompilationFromDto_withNullDto_shouldNotChangeEntity() {
        // given
        Compilation compilation = Compilation.builder()
                .title("Title")
                .pinned(true)
                .build();

        // when
        compilationMapper.updateCompilationFromDto(null, compilation);

        // then
        assertThat(compilation.getTitle()).isEqualTo("Title");
        assertThat(compilation.getPinned()).isTrue();
    }

    @Test
    void updateCompilationFromDto_withNullEntity_shouldNotThrow() {
        // given
        UpdateCompilationRequest request = new UpdateCompilationRequest(Set.of(), true, "Title");

        // when & then - should not throw
        compilationMapper.updateCompilationFromDto(request, null);
    }

    @Test
    void fullMappingCycle_shouldPreserveData() {
        // given - используем реальные события из БД
        NewCompilationDto newDto = new NewCompilationDto(
                Set.of(event1.getId()),  // реальные ID из БД
                true,
                "Cycle Test"
        );

        // when - создаем entity через маппер
        Compilation entity = compilationMapper.toCompilation(newDto);

        // Устанавливаем реальные Event объекты вместо заглушек с только ID
        entity.setEvents(Set.of(event1));

        Compilation saved = compilationRepository.save(entity);
        CompilationDto dto = compilationMapper.toCompilationDto(saved);

        // then
        assertThat(dto.title()).isEqualTo(newDto.title());
        assertThat(dto.pinned()).isEqualTo(newDto.pinned());
        assertThat(dto.events()).hasSize(1);
        assertThat(dto.events().iterator().next().id()).isEqualTo(event1.getId());
    }

    private Event createEvent(User user, Category category, String title) {
        return eventRepository.save(Event.builder()
                .annotation("Annotation for " + title)
                .category(category)
                .description("Description for " + title)
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(Location.builder().lat(55.75f).lon(37.61f).build())
                .paid(false)
                .participantLimit(0)
                .requestModeration(false)
                .state(EventState.PUBLISHED)
                .title(title)
                .initiator(user)
                .build());
    }
}