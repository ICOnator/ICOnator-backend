package io.iconator.commons.amqp.model.utils;

import io.iconator.commons.amqp.model.dto.InvestorMessageDTO;
import io.iconator.commons.model.db.Investor;

public class MessageDTOHelper {

    public static InvestorMessageDTO build(Investor investor) {
        return new InvestorMessageDTO(
                investor.getCreationDate(),
                investor.getEmail(),
                investor.getEmailConfirmationToken(),
                investor.getWalletAddress(),
                investor.getPayInEtherAddress(),
                investor.getPayInBitcoinAddress(),
                investor.getRefundEtherAddress(),
                investor.getRefundBitcoinAddress(),
                investor.getIpAddress()
        );
    }

}
