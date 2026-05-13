package com.chenghua.service;

import com.chenghua.entity.Address;
import com.chenghua.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address saveAddress(Address address) {
        // If default, unset others
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            List<Address> addresses = addressRepository.findByUserId(address.getUserId());
            for (Address addr : addresses) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        }
        return addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long id, Long userId) {
        addressRepository.deleteByIdAndUserId(id, userId);
    }
}
