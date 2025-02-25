package com.prueba.cuenta.integration;

import com.prueba.cuenta.dto.*;
import com.prueba.cuenta.entity.Account;
import com.prueba.cuenta.entity.Movement;
import com.prueba.cuenta.repository.AccountRepository;
import com.prueba.cuenta.repository.MovementRepository;
import com.prueba.cuenta.service.AccountService;
import com.prueba.cuenta.service.MovementService;
import com.prueba.cuenta.service.client.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class AccountMovementIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private MovementService movementService;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private MovementRepository movementRepository;

    @MockBean
    private ClientService clientService;

    @MockBean
    private WebClient webClient;

    @BeforeEach
    void setUp() {
        // Configura MDC para evitar NullPointerException
        org.slf4j.MDC.put("uuid", "test-uuid");
    }

    @Test
    void testCreateAccountAndPerformMovement() {
        // 1. Preparar datos para la cuenta
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setNumeroCuenta("12345");
        accountDTO.setTipoCuenta("AHORROS");
        accountDTO.setSaldoInicial(new BigDecimal("1000.00"));
        accountDTO.setEstado("true");
        accountDTO.setIdentificacion("1234567890");

        Account account = new Account();
        account.setNumeroCuenta(12345);
        account.setTipoCuenta(com.prueba.cuenta.entity.AccountType.AHORROS);
        account.setSaldo(new BigDecimal("1000.00"));
        account.setStatus(true);
        account.setClienteId(1L);

        // 2. Mockear el servicio de cliente
        ClientResponseDTO clientResponseDTO = new ClientResponseDTO();
        ClientDetailsDTO clientDTO = new ClientDetailsDTO();
        clientDTO.setId(1L);
        clientDTO.setNombre("Juan Pérez");
        clientResponseDTO.setDetails(clientDTO);
        clientResponseDTO.setResponseProcess(new ResponseProcessDTO("200", "Success", "OK"));

        when(clientService.getClientIdByIdentification(anyString())).thenReturn(Mono.just(1L));
        when(clientService.getClientName(anyLong())).thenReturn(Mono.just("Juan Pérez"));

        // 3. Mockear el repositorio de cuentas
        when(accountRepository.findByNumeroCuenta(anyInt())).thenReturn(null);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(account));

        // 4. Crear la cuenta
        StepVerifier
                .create(accountService.createAccount(accountDTO))
                .assertNext(response -> {
                    assertThat(response.getResponseProcess().getCode()).isEqualTo("0");
                    assertThat(response.getDetails()).isNotNull();
                    assertThat(response.getDetails().getNumeroCuenta()).isEqualTo(12345);
                })
                .verifyComplete();

        // 5. Preparar datos para el movimiento
        MovementDTO movementDTO = new MovementDTO();
        movementDTO.setCuentaId("12345");
        movementDTO.setValor(new BigDecimal("500.00"));
        movementDTO.setFecha(LocalDate.now().toString());

        Movement movement = new Movement();
        movement.setId("test-uuid");
        movement.setFecha(LocalDate.now());
        movement.setTipoMovimiento(com.prueba.cuenta.entity.MovementType.DEPOSITO);
        movement.setValor(new BigDecimal("500.00"));
        movement.setCuenta(account);
        movement.setSaldo(new BigDecimal("1500.00"));

        // 6. Mockear el repositorio de movimientos
        when(movementRepository.save(any(Movement.class))).thenReturn(movement);

        // 7. Realizar el movimiento
        StepVerifier
                .create(movementService.createMovement(movementDTO))
                .assertNext(response -> {
                    assertThat(response.getResponseProcess().getCode()).isEqualTo("0");
                    assertThat(response.getDetails()).isNotNull();
                    assertThat(response.getDetails().getValor()).isEqualTo(new BigDecimal("500.00"));
                    assertThat(response.getDetails().getSaldo()).isEqualTo(new BigDecimal("1500.00"));
                })
                .verifyComplete();

        // 8. Verificar que el movimiento ha actualizado el saldo de la cuenta
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void testGenerateMovementReport() {
        // 1. Preparar datos
        Integer accountId = 12345;
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        Account account = new Account();
        account.setNumeroCuenta(accountId);
        account.setTipoCuenta(com.prueba.cuenta.entity.AccountType.AHORROS);
        account.setSaldo(new BigDecimal("1500.00"));
        account.setStatus(true);
        account.setClienteId(1L);

        Movement movement1 = new Movement();
        movement1.setId("mov-1");
        movement1.setFecha(LocalDate.now().minusDays(15));
        movement1.setTipoMovimiento(com.prueba.cuenta.entity.MovementType.DEPOSITO);
        movement1.setValor(new BigDecimal("500.00"));
        movement1.setCuenta(account);
        movement1.setSaldo(new BigDecimal("1500.00"));

        Movement movement2 = new Movement();
        movement2.setId("mov-2");
        movement2.setFecha(LocalDate.now().minusDays(10));
        movement2.setTipoMovimiento(com.prueba.cuenta.entity.MovementType.RETIRO);
        movement2.setValor(new BigDecimal("-200.00"));
        movement2.setCuenta(account);
        movement2.setSaldo(new BigDecimal("1300.00"));

        List<Movement> movements = List.of(movement1, movement2);

        // 2. Mockear repositorios y servicios
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(movementRepository.findByCuenta_NumeroCuentaAndFechaBetween(eq(accountId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(movements);
        when(clientService.getClientName(anyLong())).thenReturn(Mono.just("Juan Pérez"));

        // 3. Generar el reporte
        StepVerifier
                .create(movementService.generateReport(accountId, startDate, endDate))
                .assertNext(response -> {
                    assertThat(response.getResponseProcess().getCode()).isEqualTo("200");
                    assertThat(response.getDetails()).hasSize(2);

                    MovementReportDTO report1 = response.getDetails().get(0);
                    assertThat(report1.getCliente()).isEqualTo("Juan Pérez");
                    assertThat(report1.getNumeroCuenta()).isEqualTo("12345");
                    assertThat(report1.getMovimiento()).isEqualTo(new BigDecimal("500.00"));

                    MovementReportDTO report2 = response.getDetails().get(1);
                    assertThat(report2.getMovimiento()).isEqualTo(new BigDecimal("-200.00"));
                })
                .verifyComplete();
    }

}
