package ru.practicum.ewm.practicipation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.*;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.validation.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        System.out.println("rrr");
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        System.out.println("bbb");

        List<ParticipationRequest> updateRequests = new ArrayList<>();
        System.out.println("nnn");



        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        System.out.println("mmm");


        List<ViewRequest> targetRequest = participationRepository.findAllForUpdateByParam(request.requestIds(), eventId, userId);
        List<ParticipationRequest> requests = targetRequest.stream()
                .map(dto -> ParticipationRequest.builder()
                        .id(dto.getId())
                        .status(dto.getStatus())
                        .build())
                .toList();


        if (request.status().equals("CONFIRMED")) {
            requests.forEach(r -> {
                if (!event.getRequestModeration() || event.getParticipantLimit() != result.getConfirmedRequests().size()) {
                    r.setStatus(RequestStatus.CONFIRMED);
                    updateRequests.add(r);
                    result.getConfirmedRequests().add(requestParticipationMapper.toParticipationRequestDto(r));
                } else {
                    r.setStatus(RequestStatus.REJECTED);
                    updateRequests.add(r);
                    result.getRejectedRequests().add(requestParticipationMapper.toParticipationRequestDto(r));
                }
            });

        } else if (request.status().equals("REJECTED")) {
            requests.forEach(r -> {

                r.setStatus(RequestStatus.REJECTED);
                updateRequests.add(r);
                result.getRejectedRequests().add(requestParticipationMapper.toParticipationRequestDto(r));
            });
        }

        participationRepository.saveAll(updateRequests);

        return result;
    }

    public ParticipationRequestDto createRequestEvent(Long userId, Long eventId) {

        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие не найдено"));


        validation.dublicateRequests(userId, event);
        validation.currentUserValidation(userId, event);
        validation.noPublicEventValidation(event);
        validation.limitRequestsValidation(event);

        System.out.println("mmm");

        ParticipationRequest createdRequest = requestParticipationMapper.toParticipationRequest(event, userId);

        if (!event.getRequestModeration()) createdRequest.setStatus(RequestStatus.CONFIRMED);

        return requestParticipationMapper.toParticipationRequestDto(participationRepository.save(createdRequest));


    }
}
