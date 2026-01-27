package com.subscription.customer.customer.service;

import com.subscription.customer.customer.dto.CustomerProfileResponse;

public interface CustomerService {
    CustomerProfileResponse getCurrentCustomerProfile(String email);
}
