package io.iconator.commons.model.db;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "kyc_info")
public class KycInfo {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "kyc_uuid", updatable = false, nullable = false)
    private UUID kycUuid;

    @Column(name = "investor_id", nullable = false)
    private long investorId;

    @Column(name = "is_start_kyc_email_sent")
    private boolean isStartKycEmailSent;

    @Column(name = "no_of_reminders_sent")
    private int noOfRemindersSent;

    @Column(name = "is_kyc_complete")
    private boolean isKycComplete;

    @Column(name = "kyc_uri")
    private String kycUri;

    public KycInfo() {}

    public KycInfo(long investorId, boolean isKycComplete, String kycUri) {
        this.investorId = investorId;
        this.isKycComplete = isKycComplete;
        this.kycUri = kycUri;
    }

    public UUID getKycUuid() {
        return kycUuid;
    }

    public long getInvestorId() {
        return investorId;
    }

    public KycInfo setInvestorId(long investorId) {
        this.investorId = investorId;
        return this;
    }

    public boolean isStartKycEmailSent() {
        return isStartKycEmailSent;
    }

    public KycInfo setStartKycEmailSent(boolean isStartKycEmailSent) {
        this.isStartKycEmailSent = isStartKycEmailSent;
        return this;
    }

    public int getNoOfRemindersSent() {
        return noOfRemindersSent;
    }

    public KycInfo setNoOfRemindersSent(int noOfRemindersSent) {
        this.noOfRemindersSent = noOfRemindersSent;
        return this;
    }

    public boolean isKycComplete() {
        return isKycComplete;
    }

    public KycInfo setKycComplete(boolean isKycComplete) {
        this.isKycComplete = isKycComplete;
        return this;
    }

    public String getKycUri() {
        return kycUri;
    }

    public KycInfo setKycUri(String kycUri) {
        this.kycUri = kycUri;
        return this;
    }


}
