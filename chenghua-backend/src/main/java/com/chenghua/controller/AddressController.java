package com.chenghua.controller;

import com.chenghua.entity.Address;
import com.chenghua.security.AuthenticatedUserService;
import com.chenghua.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @GetMapping
    public List<Address> getUserAddresses(@RequestParam(required = false) Long userId) {
        Long currentUserId = authenticatedUserService.requireCurrentUserId();
        if (userId != null && !authenticatedUserService.isAdmin() && !userId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        Long finalUserId = (userId != null && authenticatedUserService.isAdmin()) ? userId : currentUserId;
        return addressService.getUserAddresses(finalUserId);
    }

    @PostMapping
    public Address saveAddress(@RequestBody Address address) {
        Long currentUserId = authenticatedUserService.requireCurrentUserId();
        address.setUserId(currentUserId);
        return addressService.saveAddress(address);
    }

    @DeleteMapping("/{id}")
    public void deleteAddress(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        Long currentUserId = authenticatedUserService.requireCurrentUserId();
        if (userId != null && !authenticatedUserService.isAdmin() && !userId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        Long finalUserId = (userId != null && authenticatedUserService.isAdmin()) ? userId : currentUserId;
        addressService.deleteAddress(id, finalUserId);
    }
}
