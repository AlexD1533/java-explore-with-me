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
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.location.Location;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class CompilationServiceIntegrationTest {

    @Autowired
    private CompilationService compilationService;

    @Autowired
    private CompilationRepository compilationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Event event1;
    private Event event2;
    private Event event3;

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
        event3 = createEvent(user, category, "Event 3");
    }

    @Test
    void create_shouldSaveCompilationWithEvents() {
        // given
        NewCompilationDto dto = new NewCompilationDto(
                Set.of(event1.getId(), event2.getId()),
                true,
                "Best Events"
        );

        // when
        CompilationDto result = compilationService.create(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.title()).isEqualTo("Best Events");
        assertThat(result.pinned()).isTrue();
        assertThat(result.events()).hasSize(2);
    }

    @Test
    void create_withNullPinned_shouldSetDefaultFalse() {
        // given
        NewCompilationDto dto = new NewCompilationDto(
                Set.of(event1.getId()),
                null,
                "Unpinned Compilation"
        );

        // when
        CompilationDto result = compilationService.create(dto);

        // then
        assertThat(result.pinned()).isFalse();
    }

    @Test
    void create_withEmptyEvents_shouldCreateEmptyCompilation() {
        // given
        NewCompilationDto dto = new NewCompilationDto(Set.of(), false, "Empty");

        // when
        CompilationDto result = compilationService.create(dto);

        // then
        assertThat(result.events()).isEmpty();
    }

    @Test
    void delete_shouldRemoveCompilation() {
        // given
        Compilation compilation = compilationRepository.save(Compilation.builder()
                .title("To Delete")
                .pinned(false)
                .events(Set.of(event1))
                .build());

        // when
        compilationService.delete(compilation.getId());

        // then
        assertThat(compilationRepository.existsById(compilation.getId())).isFalse();
    }

    @Test
    void updateCompilation_shouldUpdateAllFields() {
        // given
        Compilation compilation = compilationRepository.save(Compilation.builder()
                .title("Original")
                .pinned(false)
                .events(Set.of(event1))
                .build());

        UpdateCompilationRequest updateRequest = new UpdateCompilationRequest(
                Set.of(event2.getId(), event3.getId()),
                true,
                "Updated Title"
        );

        // when
        CompilationDto result = compilationService.updateCompilation(compilation.getId(), updateRequest);

        // then
        assertThat(result.title()).isEqualTo("Updated Title");
        assertThat(result.pinned()).isTrue();
        assertThat(result.events()).hasSize(2);
        assertThat(result.events())
                .extracting(dto -> dto.id())
                .containsExactlyInAnyOrder(event2.getId(), event3.getId());
    }

    @Test
    void updateCompilation_withPartialUpdate_shouldUpdateOnlyProvidedFields() {
        // given
        Compilation compilation = compilationRepository.save(Compilation.builder()
                .title("Original Title")
                .pinned(false)
                .events(new HashSet<>(Set.of(event1)))
                .build());


        System.out.println("qqq " + compilation);
        // Обновляем только title
        UpdateCompilationRequest partialUpdate = new UpdateCompilationRequest(null, null, "New Title");

        // when
        CompilationDto result = compilationService.updateCompilation(compilation.getId(), partialUpdate);


        System.out.println("qqq " + compilation);


        // then
        assertThat(result.title()).isEqualTo("New Title");
        assertThat(result.pinned()).isFalse();
        assertThat(result.events()).hasSize(1);
    }

    @Test
    void updateCompilation_withEmptyEvents_shouldNotChangeEvents() {
        // given
        Compilation compilation = compilationRepository.save(Compilation.builder()
                .title("Title")
                .pinned(false)
                .events(new HashSet<>(Set.of(event1, event2)))
                .build());

        UpdateCompilationRequest updateWithEmptyEvents = new UpdateCompilationRequest(
                Set.of(), // пустой Set
                true,
                "New Title"
        );

        // when
        CompilationDto result = compilationService.updateCompilation(compilation.getId(), updateWithEmptyEvents);

        // then - события не должны измениться, т.к. в сервисе проверка !request.events().isEmpty()
        assertThat(result.events()).hasSize(2);
        assertThat(result.pinned()).isTrue();
    }

    @Test
    void updateCompilation_shouldThrowWhenCompilationNotFound() {
        // given
        UpdateCompilationRequest request = new UpdateCompilationRequest(Set.of(), false, "Title");

        // when & then
        assertThrows(NotFoundException.class, () ->
                compilationService.updateCompilation(999L, request)
        );
    }

    @Test
    void searchCompilationInfoByParmPublic_withoutPinnedFilter_shouldReturnAll() {
        // given
        compilationRepository.save(Compilation.builder().title("Pinned").pinned(true).events(Set.of(event1)).build());
        compilationRepository.save(Compilation.builder().title("Unpinned").pinned(false).events(Set.of(event2)).build());

        // when
        List<CompilationDto> result = compilationService.searchCompilationInfoByParmPublic(null, 0, 10);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void searchCompilationInfoByParmPublic_withPinnedTrue_shouldReturnOnlyPinned() {
        // given
        compilationRepository.save(Compilation.builder().title("Pinned 1").pinned(true).events(Set.of()).build());
        compilationRepository.save(Compilation.builder().title("Pinned 2").pinned(true).events(Set.of()).build());
        compilationRepository.save(Compilation.builder().title("Unpinned").pinned(false).events(Set.of()).build());

        // when
        List<CompilationDto> result = compilationService.searchCompilationInfoByParmPublic(true, 0, 10);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(CompilationDto::title).contains("Pinned 1", "Pinned 2");
    }

    @Test
    void searchCompilationInfoByParmPublic_withPinnedFalse_shouldReturnOnlyUnpinned() {
        // given
        compilationRepository.save(Compilation.builder().title("Pinned").pinned(true).events(Set.of()).build());
        compilationRepository.save(Compilation.builder().title("Unpinned 1").pinned(false).events(Set.of()).build());

        // when
        List<CompilationDto> result = compilationService.searchCompilationInfoByParmPublic(false, 0, 10);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Unpinned 1");
    }

    @Test
    void searchCompilationInfoByParmPublic_withPagination_shouldReturnCorrectPage() {
        // given
        for (int i = 1; i <= 5; i++) {
            compilationRepository.save(Compilation.builder()
                    .title("Compilation " + i)
                    .pinned(true)
                    .events(Set.of())
                    .build());
        }

        // when
        List<CompilationDto> firstPage = compilationService.searchCompilationInfoByParmPublic(true, 0, 2);
        List<CompilationDto> secondPage = compilationService.searchCompilationInfoByParmPublic(true, 2, 2);

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(2);
    }

    @Test
    void getCompilationByIdPublic_shouldReturnCompilation() {
        // given
        Compilation compilation = compilationRepository.save(Compilation.builder()
                .title("Public Compilation")
                .pinned(true)
                .events(new HashSet<>(Set.of(event1, event2)))
                .build());

        // when
        CompilationDto result = compilationService.getCompilationByIdPublic(compilation.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(compilation.getId());
        assertThat(result.title()).isEqualTo("Public Compilation");
        assertThat(result.events()).hasSize(2);
    }

    @Test
    void getCompilationByIdPublic_shouldThrowWhenNotFound() {
        // when & then
        assertThrows(NotFoundException.class, () ->
                compilationService.getCompilationByIdPublic(999L)
        );
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