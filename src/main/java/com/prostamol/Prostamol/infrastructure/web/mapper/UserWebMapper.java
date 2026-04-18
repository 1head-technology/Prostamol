package com.prostamol.Prostamol.infrastructure.web.mapper;

import com.prostamol.Prostamol.domain.model.user.User;
import com.prostamol.Prostamol.infrastructure.web.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserWebMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getDefaultCurrency());
    }
}
