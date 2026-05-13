package com.chenghua.controller;

import com.chenghua.entity.Address;
import com.chenghua.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public List<Address> getUserAddresses(@RequestParam(required = false) Long userId) {
        // Fallback to 1 if userId is not provided (for backward compatibility during dev)
        Long finalUserId = (userId != null) ? userId : 1L;
        return addressService.getUserAddresses(finalUserId);
    }

    @PostMapping
    public Address saveAddress(@RequestBody Address address) {
        if (address.getUserId() == null) {
            address.setUserId(1L);
        }
        return addressService.saveAddress(address);
    }

    @DeleteMapping("/{id}")
    public void deleteAddress(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        Long finalUserId = (userId != null) ? userId : 1L;
        addressService.deleteAddress(id, finalUserId);
    }
}
