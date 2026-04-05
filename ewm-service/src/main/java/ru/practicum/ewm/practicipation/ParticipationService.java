package ru.practicum.ewm.practicipation;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.*;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.ewm.validation.Validation;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final RequestParticipationMapper requestParticipationMapper;
    private final Validation validation;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public List<ParticipationRequestDto> getParticipationByUserIdAndEventId(Long userId, Long eventId) {

        List<ParticipationRequest> participationRequests = participationRepository.findAllByEventIdAndInitiatorId(eventId, userId);

        System.out.println("!!! "+  participationRequests);
        return participationRequests.stream().map(requestParticipationMapper::toParticipationRequestDto).toList();

    }

    public EventRequestStatusUpdateResult updateRequests(Long eventId, Long userId, EventRequestStatusUpdateRequest request) {

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        List<ParticipationRequest> updateRequests = new ArrayList<>();
User requester = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));



        List<ParticipationRequest> requests = participationRepository.findAllForUpdateByParam(request.requestIds(), eventId, userId);


        Integer confirmedRequests = Math.toIntExact(event.getConfirmedRequests());

        if (request.status() == RequestStatus.CONFIRMED) {
            requests.forEach(r -> {
                if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
                    r.setStatus(RequestStatus.CONFIRMED);
                    updateRequests.add(r);
                    result.getConfirmedRequests().add(requestParticipationMapper.toParticipationRequestDto(r));
                } else if (event.getParticipantLimit() > confirmedRequests) {
                    r.setStatus(RequestStatus.CONFIRMED);
                    updateRequests.add(r);
                    result.getConfirmedRequests().add(requestParticipationMapper.toParticipationRequestDto(r));
                } else {
                    r.setStatus(RequestStatus.REJECTED);
                    updateRequests.add(r);
                    result.getRejectedRequests().add(requestParticipationMapper.toParticipationRequestDto(r));
                }
            });

        } else if (request.status() == RequestStatus.REJECTED) {
            requests.forEach(r -> {

                r.setStatus(RequestStatus.REJECTED);
                updateRequests.add(r);
                result.getRejectedRequests().add(requestParticipationMapper.toParticipationRequestDto(r));
            });
        }


        System.out.println(" rrr" + updateRequests);
        participationRepository.saveAll(updateRequests);

        System.out.println(" ttt" + result);


        return result;
    }

    public ParticipationRequestDto createRequestEvent(Long userId, Long eventId) {

        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие не найдено"));


        validation.dublicateRequests(userId, event);
        validation.currentUserValidation(userId, event);
        validation.noPublicEventValidation(event);
        validation.limitRequestsValidation(event);



        ParticipationRequest createdRequest = requestParticipationMapper.toParticipationRequest(event, userId);

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) createdRequest.setStatus(RequestStatus.CONFIRMED);

        return requestParticipationMapper.toParticipationRequestDto(participationRepository.save(createdRequest));


    }

    public List<ParticipationRequestDto> getParticipationByUserId(@NotNull Long userId) {
        List<ParticipationRequest> participationRequests = participationRepository.findAllByRequesterId(userId);
        return participationRequests.stream().map(requestParticipationMapper::toParticipationRequestDto).toList();

    }

    public ParticipationRequestDto cancelRequestsByUser(Long requestId, Long userId) {

        ParticipationRequest request = participationRepository.findByIdAndRequesterId(requestId, userId).orElseThrow(() ->
                new NotFoundException("Такой заявки не существует"));

        System.out.println("eee" + request);

        request.setStatus(RequestStatus.CANCELED);
        participationRepository.save(request);
        ParticipationRequestDto dto = requestParticipationMapper.toParticipationRequestDto(request);

        return requestParticipationMapper.toParticipationRequestDto(request);
    }
}
