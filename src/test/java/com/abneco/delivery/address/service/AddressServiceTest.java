package com.abneco.delivery.address.service;

import com.abneco.delivery.address.dto.AddressForm;
import com.abneco.delivery.address.dto.AddressResponse;
import com.abneco.delivery.address.dto.AddressTO;
import com.abneco.delivery.address.entity.Address;
import com.abneco.delivery.address.repository.AddressRepository;
import com.abneco.delivery.exception.RequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @InjectMocks
    private AddressService service;

    @Mock
    private AddressRepository repository;

    public static final String CEP = "04555-000";
    public static final String CEP_NUMBERS = "04555000";
    public static final String CEP_9 = "123456789";
    public static final String CEP_LETTER = "04555A00";
    public static final String COMPLEMENTO = "";
    public static final Integer NUMERO = 123;

    public static final Address ADDRESS = new Address();

    @Test
    void testGetAddress() {
        RestTemplate restTemplate = new RestTemplate();
        AddressService service = new AddressService(restTemplate);
        AddressTO response = service.getAddressTemplate(CEP);
        assertNotNull(response);
        assertEquals("SP", response.getUf());
    }

    @Test
    void testGetAddressCepOnlyNumbers() {
        RestTemplate restTemplate = new RestTemplate();
        AddressService service = new AddressService(restTemplate);
        AddressTO response = service.getAddressTemplate(CEP_NUMBERS);
        assertNotNull(response);
        assertEquals("SP", response.getUf());
    }

    @Test
    void testGetAddressCepEmptyString() {
        RestTemplate restTemplate = new RestTemplate();
        AddressService service = new AddressService(restTemplate);
        Exception exception = Assertions.assertThrows(RequestException.class, () -> service.getAddressTemplate(""));
        assertEquals("Please verify if cep has 8 numbers, and numbers only.", exception.getMessage());
    }

    @Test
    void testGetAddressCepNineNumbers() {
        RestTemplate restTemplate = new RestTemplate();
        AddressService service = new AddressService(restTemplate);
        Exception exception = Assertions.assertThrows(RequestException.class, () -> service.getAddressTemplate(CEP_9));
        assertEquals("Please verify if cep has 8 numbers, and numbers only.", exception.getMessage());
    }

    @Test
    void testGetAddressCepWithLetterAmid() {
        RestTemplate restTemplate = new RestTemplate();
        AddressService service = new AddressService(restTemplate);
        Exception exception = Assertions.assertThrows(RequestException.class, () -> service.getAddressTemplate(CEP_LETTER));
        assertEquals("Please verify if cep has 8 numbers, and numbers only.", exception.getMessage());
    }

    @Test
    void testRegisterAddressByCep() {
        RestTemplate restTemplate = new RestTemplate();
        AddressService service = new AddressService(repository, restTemplate);
        AddressForm form = new AddressForm(CEP, COMPLEMENTO, NUMERO);
        service.registerAddressByCep(form);
        verify(repository).save(any(Address.class));
    }

    @Test
    void testRegisterAddressByCepRequestException() {
        RestTemplate restTemplate = new RestTemplate();
        AddressService service = new AddressService(repository, restTemplate);
        AddressForm form = new AddressForm(CEP_LETTER, COMPLEMENTO, NUMERO);
        Exception exception = Assertions.assertThrows(RequestException.class, () -> service.registerAddressByCep(form));
        Assertions.assertEquals("Please verify if cep has 8 numbers, and numbers only.", exception.getMessage());
        verify(repository, never()).save(any(Address.class));
    }

    @Test
    void testRegisterAddressByCepException() {
        RestTemplate restTemplate = new RestTemplate();
        AddressService service = new AddressService(repository, restTemplate);
        AddressForm form = new AddressForm(CEP, COMPLEMENTO, NUMERO);

        when(repository.save(any(Address.class))).thenThrow(RuntimeException.class);
        Exception exception = Assertions.assertThrows(RequestException.class, () -> service.registerAddressByCep(form));
        Assertions.assertEquals("Could not register address by cep.", exception.getMessage());
    }

    @Test
    void testRegisterAddressByCepLengthException() {
        RestTemplate restTemplate = new RestTemplate();
        AddressService service = new AddressService(repository, restTemplate);
        AddressForm form = new AddressForm(CEP, COMPLEMENTO, null);

        Exception exception = Assertions.assertThrows(RequestException.class, () -> service.registerAddressByCep(form));
        Assertions.assertEquals("Address number must not be null.", exception.getMessage());
    }

    @Test
    void testGetAllAddresses() {
        when(repository.findAll()).thenReturn(List.of(ADDRESS));
        List<AddressResponse> response = service.getAllAddresses();
        assertNotEquals(0, response.size());
        verify(repository).findAll();
    }
    @Test
    void testGetAllAddressesEmptyList() {
        when(repository.findAll()).thenReturn(List.of());
        List<AddressResponse> response = service.getAllAddresses();
        assertEquals(0, response.size());
        verify(repository).findAll();
    }
}