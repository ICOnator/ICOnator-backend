package io.iconator.commons.model.db;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "kyc_info")
public class KycInfo {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "kyc_uuid", updatable = false, nullable = false)
    private UUID kycUuid;

    @Column(name = "investor_id", unique = true, nullable = false)
    private Long investorId;

    @Column(name = "is_start_kyc_email_sent")
    private Boolean isStartKycEmailSent;

    @Column(name = "no_of_reminders_sent")
    private Integer noOfRemindersSent;

    @Column(name = "is_kyc_complete")
    private Boolean isKycComplete;

    @Column(name = "kyc_uri")
    private String kycUri;

    public KycInfo() {
    }

    public KycInfo(Long investorId, Boolean isStartKycEmailSent, Integer noOfRemindersSent,
                   Boolean isKycComplete, String kycUri) {
        this.investorId = investorId;
        this.isStartKycEmailSent = isStartKycEmailSent;
        this.noOfRemindersSent = noOfRemindersSent;
        this.isKycComplete = isKycComplete;
        this.kycUri = kycUri;
    }

    public UUID getKycUuid() {
        return kycUuid;
    }

    public Long getInvestorId() {
        return investorId;
    }

    public KycInfo setInvestorId(long investorId) {
        this.investorId = investorId;
        return this;
    }

    public Boolean isStartKycEmailSent() {
        return isStartKycEmailSent;
    }

    public KycInfo setStartKycEmailSent(Boolean isStartKycEmailSent) {
        this.isStartKycEmailSent = isStartKycEmailSent;
        return this;
    }

    public Integer getNoOfRemindersSent() {
        return noOfRemindersSent;
    }

    public KycInfo setNoOfRemindersSent(Integer noOfRemindersSent) {
        this.noOfRemindersSent = noOfRemindersSent;
        return this;
    }

    public Boolean isKycComplete() {
        return isKycComplete;
    }

    public KycInfo setKycComplete(Boolean isKycComplete) {
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
