package ru.practicum.ewm.validation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.practicum.ewm.exception.DuplicatedDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class Validation {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public void userEmailValidation(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicatedDataException("Email " + email + " уже используется");
        }
    }

    public void categoryNameValidation(String name) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new DuplicatedDataException("Name " + name + " уже используется");
        }
    }
}
