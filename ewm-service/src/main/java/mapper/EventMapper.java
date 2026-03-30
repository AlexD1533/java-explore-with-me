package mapper;
import dto.*;
import model.Category;
import model.Event;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring",
        uses = {CategoryMapper.class, UserMapper.class, LocationMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class EventMapper {

    protected static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    protected CategoryMapper categoryMapper;

    @Autowired
    protected UserMapper userMapper;

    @Autowired
    protected LocationMapper locationMapper;

    @Mapping(source = "eventDate", target = "eventDate", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "category", ignore = true)
    public abstract Event toEvent(NewEventDto newEventDto);

    @Mapping(source = "eventDate", target = "eventDate", qualifiedByName = "localDateTimeToString")
    @Mapping(source = "createdOn", target = "createdOn", qualifiedByName = "localDateTimeToString")
    @Mapping(source = "publishedOn", target = "publishedOn", qualifiedByName = "localDateTimeToString")
    public abstract EventFullDto toEventFullDto(Event event);

    @Mapping(source = "eventDate", target = "eventDate", qualifiedByName = "localDateTimeToString")
    public abstract EventShortDto toEventShortDto(Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "eventDate", target = "eventDate", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "state", ignore = true)
    public abstract void updateEventFromDto(UpdateEventAdminRequest dto, @MappingTarget Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "eventDate", target = "eventDate", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "state", ignore = true)
    public abstract void updateEventFromDto(UpdateEventUserRequest dto, @MappingTarget Event event);

    @Named("localDateTimeToString")
    protected String localDateTimeToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : null;
    }

    @Named("stringToLocalDateTime")
    protected LocalDateTime stringToLocalDateTime(String dateTime) {
        return dateTime != null ? LocalDateTime.parse(dateTime, FORMATTER) : null;
    }

    protected Category mapCategoryId(Long categoryId) {
        if (categoryId == null) return null;
        return Category.builder().id(categoryId).build();
    }

    protected Long mapCategory(Category category) {
        return category != null ? category.getId() : null;
    }
}
