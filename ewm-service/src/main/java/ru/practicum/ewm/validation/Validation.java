package ru.practicum.ewm.validation;

import ru.practicum.ewm.exception.DuplicatedDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class Validation {

    private final UserRepository userRepository;

    public void userEmailValidation(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicatedDataException("Email " + email + " уже используется");
        }
    }
}
