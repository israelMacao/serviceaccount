package com.prueba.cuenta.service.client;

import com.prueba.cuenta.dto.ClientResponseDTO;
import com.prueba.cuenta.exception.AccountBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final WebClient webClient;

    @Value("${url.serviciocliente}")
    private String clientServiceUrl;

    @Value("${url.path.id}")
    private String idPath;

    @Value("${url.path.identificacion}")
    private String identificationPath;

    public Mono<String> getClientName(Long clientId) {
        String url = clientServiceUrl + idPath + clientId;
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(ClientResponseDTO.class)
                .flatMap(response -> {
                    log.info("Respuesta del servicio cliente: {}", response);
                    return Mono.just(response.getDetails().getNombre());
                })
                .onErrorResume(e -> handleClientServiceError(e, "Error al consultar el cliente: " + clientId));
    }

    public Mono<Long> getClientIdByIdentification(String identification) {
        String url = clientServiceUrl + identificationPath + identification;
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(ClientResponseDTO.class)
                .flatMap(response -> {
                    log.info("Respuesta micro cliente por identificación: {}", response);

                    if (response.getResponseProcess() != null &&
                            !"200".equals(response.getResponseProcess().getCode())) {
                        String errorMessage = response.getResponseProcess().getTechnicalMessage();
                        return Mono.error(new AccountBusinessException(errorMessage));
                    }

                    if (response.getDetails() == null) {
                        return Mono.error(new AccountBusinessException(
                                "Cliente no encontrado con identificación: " + identification));
                    }

                    return Mono.just(response.getDetails().getId());
                })
                .onErrorResume(e -> handleClientServiceError(e,
                        "Error al consultar el cliente con identificación: " + identification));
    }

    private <T> Mono<T> handleClientServiceError(Throwable e, String defaultMessage) {
        if (e instanceof AccountBusinessException) {
            return Mono.error(e);
        }
        return Mono.error(new AccountBusinessException(defaultMessage + ": " + e.getMessage()));
    }
}