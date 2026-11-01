package com.shastage.store.web.controller;

import com.shastage.store.web.security.Authorize;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Authorize
@RestController
@RequestMapping("/api/v1/")
@AllArgsConstructor
public class AddressController {
}
