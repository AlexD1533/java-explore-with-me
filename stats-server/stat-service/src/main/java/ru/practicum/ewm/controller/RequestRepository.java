package ru.practicum.ewm.controller;

import ru.practicum.ewm.model.RequestInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<RequestInfo, Long> {


}
