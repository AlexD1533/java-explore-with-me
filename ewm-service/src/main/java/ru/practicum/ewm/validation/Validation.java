package ru.practicum.ewm.validation;

import ru.practicum.ewm.exception.DuplicatedDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.user.UserRepository;

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
