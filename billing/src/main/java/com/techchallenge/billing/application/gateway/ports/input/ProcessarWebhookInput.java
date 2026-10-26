package com.techchallenge.billing.application.gateway.ports.input;

import com.techchallenge.billing.application.commands.ProcessarWebhookCommand;

public interface ProcessarWebhookInput {

    void execute(ProcessarWebhookCommand command);
}
