package com.subscription.customer.customer.service.impl;

import com.subscription.customer.auth.model.User;
import com.subscription.customer.auth.repository.UserRepository;
import com.subscription.customer.customer.dto.CustomerProfileResponse;
import com.subscription.customer.customer.service.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;

    public CustomerServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public CustomerProfileResponse getCurrentCustomerProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        return new CustomerProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRoles(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
