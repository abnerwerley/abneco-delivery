package com.abneco.delivery.fee.controller;


import com.abneco.delivery.fee.dto.FeeResponse;
import com.abneco.delivery.fee.dto.CepForm;
import com.abneco.delivery.fee.service.FeeService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/v1")
public class FeeController {

    @Autowired
    private FeeService service;

    @PostMapping("/consulta-endereco")
    @ResponseStatus(HttpStatus.OK)
    public FeeResponse getDeliveryFeeByCep(@RequestBody(required = true) CepForm form) {
        return service.generateResponse(form.getCep());
    }
}

