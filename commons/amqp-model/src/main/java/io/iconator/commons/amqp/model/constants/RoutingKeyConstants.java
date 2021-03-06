package io.iconator.commons.amqp.model.constants;

public class RoutingKeyConstants {

    public static final String TRANSACTION_RECEIVED_ROUTING_KEY = "iconator.transaction.received";
    public static final String TRANSACTION_TOKENS_ALLOCATED_ROUTING_KEY = "iconator.transaction.tokens.allocated";

    public static final String REGISTER_CONFIRMATION_EMAIL_ROUTING_KEY = "iconator.register.confirmation";
    public static final String REGISTER_SUMMARY_EMAIL_ROUTING_KEY = "iconator.register.summary";

    public static final String ADDRESS_SET_WALLET_ROUTING_KEY = "iconator.address.set-wallet";

    public static final String KYC_START_EMAIL_ROUTING_KEY = "iconator.kyc.start";
    public static final String KYC_REMINDER_EMAIL_ROUTING_KEY = "iconator.kyc.reminder";

    public static final String BLOCK_NR_BITCOIN_ROUTING_KEY = "iconator.block_nr.bitcoin";
    public static final String BLOCK_NR_ETHEREUM_ROUTING_KEY = "iconator.block_nr.ethereum";

    public static final String KYC_START_EMAIL_SENT_ROUTING_KEY = "iconator.kyc.start.sent";
    public static final String KYC_REMINDER_EMAIL_SENT_ROUTING_KEY = "iconator.kyc.reminder.sent";

    public static final String RATES_EXCHANGE_REQUEST_ROUTING_KEY = "iconator.rates.request";

}
