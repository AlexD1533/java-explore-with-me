package ru.practicum.ewm.controller;


import dto.RequestInfoDto;
import lombok.RequiredArgsConstructor;
import mapper.RequestMapper;
import model.RequestInfo;
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
