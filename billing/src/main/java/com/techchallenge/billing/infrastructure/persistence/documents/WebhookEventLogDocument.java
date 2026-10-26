package com.techchallenge.billing.infrastructure.persistence.documents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "webhook_events_log")
@CompoundIndex(
        name = "status_proxima_retentativa_idx",
        def = "{'statusProcessamento': 1, 'proximaRetentativa': 1}"
)
public class WebhookEventLogDocument {

    @Id
    private UUID id;

    private String mercadoPagoPaymentId;
    private String action;
    private LocalDateTime dataRecebimento;
    private Map<String, Object> payloadOriginal;
    private String statusProcessamento;
    private int tentativas;
    private LocalDateTime proximaRetentativa;
}
