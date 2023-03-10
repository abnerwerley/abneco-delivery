package com.abneco.delivery.user.service;

import com.abneco.delivery.exception.RequestException;
import com.abneco.delivery.exception.ResourceNotFoundException;
import com.abneco.delivery.user.entity.Seller;
import com.abneco.delivery.user.json.SellerForm;
import com.abneco.delivery.user.json.SellerResponse;
import com.abneco.delivery.user.json.SellerUpdateForm;
import com.abneco.delivery.user.repository.SellerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @InjectMocks
    private SellerService service;

    @Mock
    private SellerRepository repository;

    public static final String ID = "iyu230hskdf-dfoi7-462c-a47f-7afaade01517";
    public static final String NAME = "Name";
    public static final String NEW_NAME = "name";
    public static final String EMAIL = "name@email.com";
    public static final String NEW_EMAIL = "name@newemail.com";
    public static final String PASSWORD = "12345678";
    public static final String SHORT_PASSWORD = "1234567";
    public static final Long PHONE_NUMBER = 1112345678L;
    public static final String CNPJ = "09876543211234";
    public static final String SHORT_CNPJ = "0987654321123";
    public static final String LONG_CNPJ = "098765432112345";
    public static final String NEW_CNPJ = "49529348908734";
    public static final String EMAIL_WITHOUT_AT = "nameemail.org";
    public static final String SHORT_NAME = "Et";

    @Test
    void testRegisterSeller() {
        SellerForm form = new SellerForm(NAME, EMAIL, PASSWORD, PHONE_NUMBER, CNPJ);
        when(repository.findByEmail(form.getEmail())).thenReturn(Optional.empty());
        when(repository.findByCnpj(form.getCnpj())).thenReturn(Optional.empty());
        service.registerSeller(form);
        verify(repository).findByEmail(form.getEmail());
        verify(repository).findByCnpj(form.getCnpj());
        verify(repository).save(any(Seller.class));
    }

    @Test
    void testRegisterSellerEmailAlreadyInUse() {
        SellerForm form = new SellerForm(NAME, EMAIL, PASSWORD, PHONE_NUMBER, CNPJ);
        doReturn(optionalSeller()).when(repository).findByEmail(form.getEmail());
        Exception exception = assertThrows(RequestException.class, () -> service.registerSeller(form));
        assertNotNull(exception);
        assertEquals("Email already in use.", exception.getMessage());
        verify(repository).findByEmail(form.getEmail());
        verify(repository, never()).save(Mockito.any(Seller.class));
    }

    @Test
    void testRegisterSellerCnpjAlreadyInUse() {
        SellerForm form = new SellerForm(NAME, EMAIL, PASSWORD, PHONE_NUMBER, CNPJ);
        when(repository.findByEmail(form.getEmail())).thenReturn(Optional.empty());
        when(repository.findByCnpj(form.getCnpj())).thenReturn(Optional.of(new Seller()));
        Exception exception = assertThrows(RequestException.class, () -> service.registerSeller(form));
        assertNotNull(exception);
        assertEquals("Cnpj already in use.", exception.getMessage());
        verify(repository).findByEmail(form.getEmail());
        verify(repository, never()).save(Mockito.any(Seller.class));
    }

    @Test
    void testRegisterSellerWithNotAValidEmail() {
        SellerForm form = new SellerForm(NAME, EMAIL_WITHOUT_AT, PASSWORD, PHONE_NUMBER, CNPJ);
        Exception exception = assertThrows(RequestException.class, () -> service.registerSeller(form));
        assertNotNull(exception);
        assertEquals("Email has incorrect format.", exception.getMessage());
        verify(repository, never()).save(Mockito.any(Seller.class));
    }

    @Test
    void testRegisterSellerSaveValidations() {
        SellerForm shortCnpjForm = new SellerForm(NAME, EMAIL, PASSWORD, PHONE_NUMBER, SHORT_CNPJ);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(repository.findByCnpj(shortCnpjForm.getCnpj())).thenReturn(Optional.empty());
        Exception shortCnpj = assertThrows(RequestException.class, () -> service.registerSeller(shortCnpjForm));
        assertNotNull(shortCnpj);
        assertEquals("Cnpj must have 14 numbers, and numbers only.", shortCnpj.getMessage());

        SellerForm longCnpjForm = new SellerForm(NAME, EMAIL, PASSWORD, PHONE_NUMBER, LONG_CNPJ);
        Exception longCnpj = assertThrows(RequestException.class, () -> service.registerSeller(longCnpjForm));
        assertNotNull(longCnpj);
        assertEquals("Cnpj must have 14 numbers, and numbers only.", longCnpj.getMessage());
        verify(repository, never()).save(Mockito.any(Seller.class));

        SellerForm nullCnpjForm = new SellerForm(NAME, EMAIL, PASSWORD, PHONE_NUMBER, null);
        Exception nullCnpj = assertThrows(RequestException.class, () -> service.registerSeller(nullCnpjForm));
        assertNotNull(nullCnpj);
        assertEquals("Cnpj must have 14 numbers, and numbers only.", nullCnpj.getMessage());
        verify(repository, never()).save(Mockito.any(Seller.class));

        SellerForm shortNameForm = new SellerForm(SHORT_NAME, EMAIL, PASSWORD, PHONE_NUMBER, CNPJ);
        Exception shortName = assertThrows(RequestException.class, () -> service.registerSeller(shortNameForm));
        assertNotNull(shortName);
        assertEquals("Name must be neither null nor shorter than 3.", shortName.getMessage());
        verify(repository, never()).save(Mockito.any(Seller.class));

        SellerForm nullNameForm = new SellerForm(SHORT_NAME, EMAIL, PASSWORD, PHONE_NUMBER, CNPJ);
        Exception nullName = assertThrows(RequestException.class, () -> service.registerSeller(nullNameForm));
        assertNotNull(nullName);
        assertEquals("Name must be neither null nor shorter than 3.", nullName.getMessage());
        verify(repository, never()).save(Mockito.any(Seller.class));
    }

    @Test
    void testRegisterSellerShortPassword() {
        SellerForm shortPasswordForm = new SellerForm(NAME, EMAIL, SHORT_PASSWORD, PHONE_NUMBER, CNPJ);
        Exception shortPassword = assertThrows(RequestException.class, () -> service.registerSeller(shortPasswordForm));
        assertNotNull(shortPassword);
        assertEquals("Password must be at least 8 char long.", shortPassword.getMessage());
        verify(repository, never()).save(Mockito.any(Seller.class));
    }

    @Test
    void testRegisterSellerException() {
        SellerForm form = new SellerForm(NAME, EMAIL, PASSWORD, PHONE_NUMBER, CNPJ);
        when(repository.findByEmail(EMAIL)).thenThrow(RuntimeException.class);
        Exception exception = assertThrows(RequestException.class, () -> service.registerSeller(form));
        assertEquals("Could not register seller.", exception.getMessage());
    }

    @Test
    void testUpdateSeller() {
        SellerUpdateForm form = new SellerUpdateForm(ID, NEW_NAME, NEW_EMAIL, PHONE_NUMBER, NEW_CNPJ);
        doReturn(optionalSeller()).when(repository).findById(ID);
        SellerResponse response = service.updateSeller(form);
        assertNotNull(response);
        assertEquals("Name", response.getName());
        assertEquals(NEW_EMAIL, response.getEmail());
        assertEquals(NEW_CNPJ, response.getCnpj());
        verify(repository).findById(ID);
        verify(repository).save(any(Seller.class));
    }

    @Test
    void testUpdateSellerSellerNotFound() {
        SellerUpdateForm form = new SellerUpdateForm(ID, NEW_NAME, NEW_EMAIL, PHONE_NUMBER, NEW_CNPJ);
        when(repository.findById(ID)).thenReturn(Optional.empty());
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> service.updateSeller(form));
        assertNotNull(exception);
        assertEquals("Seller not found.", exception.getMessage());
    }

    @Test
    void testUpdateSellerRequestException() {
        SellerUpdateForm nullNameForm = new SellerUpdateForm(ID, SHORT_NAME, EMAIL, PHONE_NUMBER, CNPJ);
        when(repository.findById(nullNameForm.getId())).thenReturn(Optional.of(new Seller()));
        Exception nullName = assertThrows(RequestException.class, () -> service.updateSeller(nullNameForm));
        assertNotNull(nullName);
        assertEquals("Name must be neither null nor shorter than 3.", nullName.getMessage());
        verify(repository, never()).save(Mockito.any(Seller.class));
    }

    @Test
    void testUpdateSellerException() {
        SellerUpdateForm form = new SellerUpdateForm(ID, NAME, EMAIL, PHONE_NUMBER, CNPJ);
        when(repository.findById(form.getId())).thenThrow(RuntimeException.class);
        Exception exception = assertThrows(RequestException.class, () -> service.updateSeller(form));
        assertNotNull(exception);
        assertEquals("Could not update seller.", exception.getMessage());
        verify(repository, never()).save(Mockito.any(Seller.class));
    }

    @Test
    void testFindSellerById() {
        doReturn(optionalSeller()).when(repository).findById(ID);
        SellerResponse response = service.findSellerById(ID);
        assertNotNull(response);
        verify(repository).findById(ID);
    }

    @Test
    void testFindSellerByIdNotFound() {
        when(repository.findById(ID)).thenReturn(Optional.empty());
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> service.findSellerById(ID));
        assertNotNull(exception);
        assertEquals("Seller not found.", exception.getMessage());
        verify(repository).findById(ID);
    }

    @Test
    void testFindSellerByIdException() {
        when(repository.findById(ID)).thenThrow(RuntimeException.class);
        Exception exception = assertThrows(RequestException.class, () -> service.findSellerById(ID));
        assertNotNull(exception);
        assertEquals("Could not find seller by id: " + ID, exception.getMessage());
        verify(repository).findById(ID);
    }

    @Test
    void testDeleteSellerById() {
        doReturn(optionalSeller()).when(repository).findById(ID);
        doNothing().when(repository).deleteById(ID);
        service.deleteSellerById(ID);
        verify(repository).findById(ID);
        verify(repository).deleteById(ID);
    }

    @Test
    void testDeleteSellerByIdNotFound() {
        when(repository.findById(ID)).thenReturn(Optional.empty());
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> service.deleteSellerById(ID));
        assertNotNull(exception);
        assertEquals("Seller not found.", exception.getMessage());
        verify(repository).findById(ID);
        verify(repository, never()).deleteById(ID);
    }

    @Test
    void testDeleteSellerByIdException() {
        when(repository.findById(ID)).thenThrow(RuntimeException.class);
        Exception exception = assertThrows(RequestException.class, () -> service.deleteSellerById(ID));
        assertNotNull(exception);
        assertEquals("Could not delete seller with id: " + ID, exception.getMessage());
        verify(repository).findById(ID);
        verify(repository, never()).deleteById(ID);
    }

    @Test
    void testFindAllSellers() {
        when(repository.findAll()).thenReturn(List.of(getSeller()));
        List<SellerResponse> response = service.findAllSellers();
        assertNotNull(response);
        assertEquals(1, response.size());
    }

    public Optional<Seller> optionalSeller() {
        Seller seller = new Seller();
        seller.setId(ID);
        seller.setName(NAME);
        seller.setEmail(EMAIL);
        seller.setCnpj(CNPJ);
        seller.setEmailVerified(false);
        seller.setPassword(PASSWORD);
        seller.setCreatedAt("01/03/2023 14:47");
        return Optional.of(seller);
    }

    public Seller getSeller() {
        Seller seller = new Seller();
        seller.setId(ID);
        seller.setName(NAME);
        seller.setEmail(EMAIL);
        seller.setCnpj(CNPJ);
        seller.setEmailVerified(false);
        seller.setPassword(PASSWORD);
        seller.setCreatedAt("01/03/2023 14:47");
        return seller;
    }
}
