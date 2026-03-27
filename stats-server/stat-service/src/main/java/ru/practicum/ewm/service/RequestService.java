package ru.practicum.ewm.service;


import dto.RequestInfoDto;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.RequestInfo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    public RequestInfo create(RequestInfoDto.NewRequestInfoDto request) {

        return requestRepository.save(requestMapper.toEntity(request));
    }
}
