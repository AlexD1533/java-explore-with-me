package ru.practicum.ewm.practicipation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.*;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.validation.Validation;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final RequestParticipationMapper requestParticipationMapper;
    private final EventService eventService;
    private final Validation validation;
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;

    public List<ParticipationRequestDto> getParticipationByUserIdAndEventId(Long userId, Long eventId) {

        List<ParticipationRequest> participationRequest = participationRepository.findAllByEventIdAndRequesterId(eventId, userId);
        return participationRequest.stream().map(requestParticipationMapper::toParticipationRequestDto).toList();
    }

    public EventRequestStatusUpdateResult updateRequests(Long eventId, Long userId, EventRequestStatusUpdateRequest request) {

        List<ViewRequest> targetRequest = participationRepository.findAllForUpdateByParam(request.requestIds(), eventId, userId);


        List<ParticipationRequest> requests = participationRepository.saveAll(
                targetRequest.stream()
                        .map(dto -> ParticipationRequest.builder()
                                .id(dto.getId())
                                .status(dto.getStatus())
                                .build())
                        .toList());


        System.out.println("/////////////////" + requests);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        requests.stream()
                .map(requestParticipationMapper::toParticipationRequestDto)
                .forEach(r -> {
                    if (r.status().equals(RequestStatus.CANCELED)) {
                        result.getRejectedRequests().add(r);
                    } else if (r.status().equals(RequestStatus.CONFIRMED)) {
                        result.getConfirmedRequests().add(r);
                    }
                });
        return result;
    }

    public ParticipationRequestDto createRequestEvent(Long userId, Long eventId) {

        Event event= eventRepository.findByIdAndInitiatorId(userId, eventId).orElseThrow(() ->
                new NotFoundException("Событие не найдено"));

        validation.dublicateRequests(userId, event);
        validation.currentUserValidation(userId, event);
        validation.noPublicEventValidation(event);
        validation.limitRequestsValidation(event);

        ParticipationRequest createdRequest = requestParticipationMapper.toParticipationRequest(event, userId);

        if (!event.getRequestModeration()) createdRequest.setStatus(RequestStatus.CONFIRMED);

        return requestParticipationMapper.toParticipationRequestDto(participationRepository.save(createdRequest));





    }
}
