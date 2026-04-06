package ru.practicum.ewm.user.dto.practicipation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.*;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.dto.practicipation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.user.dto.practicipation.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.user.dto.practicipation.dto.ParticipationRequestDto;
import ru.practicum.ewm.user.dto.practicipation.dto.RequestParticipationMapper;
import ru.practicum.ewm.validation.Validation;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final RequestParticipationMapper requestParticipationMapper;
    private final Validation validation;
    private final EventRepository eventRepository;

    public List<ParticipationRequestDto> getParticipationByUserIdAndEventId(Long userId, Long eventId) {

        List<ParticipationRequest> participationRequests =
                participationRepository.findAllByEventIdAndInitiatorId(eventId, userId);
        return participationRequests.stream().map(requestParticipationMapper::toParticipationRequestDto).toList();
    }

    public EventRequestStatusUpdateResult updateRequests(Long eventId, Long userId, EventRequestStatusUpdateRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        Integer confirmedRequests = participationRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        validation.limitRequestsValidation(event, confirmedRequests);


        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        List<ParticipationRequest> updateRequests = new ArrayList<>();

        List<ParticipationRequest> requests = participationRepository.findAllForUpdateByParam(request.getRequestIds(), eventId, userId);

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            requests.forEach(r -> {


                if (!r.getStatus().equals(RequestStatus.PENDING)) {
                    throw new ConflictException("Статус можно изменить только у заявок, находящихся в состоянии ожидания ");
                }

                if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
                    r.setStatus(RequestStatus.CONFIRMED);
                    updateRequests.add(r);
                    result.getConfirmedRequests().add(requestParticipationMapper.toParticipationRequestDto(r));
                }

                if (event.getParticipantLimit() > confirmedRequests && result.getConfirmedRequests().size() < event.getParticipantLimit()) {
                    r.setStatus(RequestStatus.CONFIRMED);
                    updateRequests.add(r);
                    result.getConfirmedRequests().add(requestParticipationMapper.toParticipationRequestDto(r));
                } else {
                    r.setStatus(RequestStatus.REJECTED);
                    updateRequests.add(r);
                    result.getRejectedRequests().add(requestParticipationMapper.toParticipationRequestDto(r));
                }
            });
        }

        if (request.getStatus() == RequestStatus.REJECTED) {
            requests.forEach(r -> {

                if (!r.getStatus().equals(RequestStatus.PENDING)) {
                    throw new ConflictException("Статус можно изменить только у заявок, находящихся в состоянии ожидания");
                }

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

        List<ParticipationRequest> requests = participationRepository.findAllByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        validation.dublicateRequests(userId, requests, eventId);
        validation.currentUserValidation(userId, event);
        validation.noPublicEventValidation(event);
        validation.limitRequestsValidation(event, requests.size());

        ParticipationRequest createdRequest = requestParticipationMapper.toParticipationRequest(event, userId);

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0)
            createdRequest.setStatus(RequestStatus.CONFIRMED);

        return requestParticipationMapper.toParticipationRequestDto(participationRepository.save(createdRequest));

    }

    public List<ParticipationRequestDto> getParticipationByUserId(Long userId) {
        List<ParticipationRequest> participationRequests = participationRepository.findAllByRequesterId(userId);
        return participationRequests.stream().map(requestParticipationMapper::toParticipationRequestDto).toList();

    }

    public ParticipationRequestDto cancelRequestsByUser(Long requestId, Long userId) {

        ParticipationRequest request = participationRepository.findByIdAndRequesterId(requestId, userId).orElseThrow(() ->
                new NotFoundException("Такой заявки не существует"));

        request.setStatus(RequestStatus.CANCELED);
        participationRepository.save(request);
        ParticipationRequestDto dto = requestParticipationMapper.toParticipationRequestDto(request);

        return requestParticipationMapper.toParticipationRequestDto(request);
    }
}
