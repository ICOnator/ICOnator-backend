package io.iconator.commons.model.db;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "kyc_info")
public class KycInfo {

    @Id
    @Column(name = "id", nullable = false)
    private long id;

    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "kyc_uuid", updatable = false, nullable = false)
    private UUID kycUuid;

    @Column(name = "no_of_emails_sent")
    private int noOfEmailsSent;

    @Column(name = "is_kyc_complete")
    private boolean isKycComplete;

    public KycInfo(long id, boolean isKycComplete) {
        this.id = id;
        this.isKycComplete = isKycComplete;
    }

    public long getId() {
        return id;
    }

    public KycInfo setId(long id) {
        this.id = id;
        return this;
    }

    public UUID getKycUuid() {
        return kycUuid;
    }

    public int getNoOfEmailsSent() {
        return noOfEmailsSent;
    }

    public KycInfo setNoOfEmailsSent(int noOfEmailsSent) {
        this.noOfEmailsSent = noOfEmailsSent;
        return this;
    }

    public boolean isKycComplete() {
        return isKycComplete;
    }

    public KycInfo setKycComplete(boolean isKycComplete) {
        this.isKycComplete = isKycComplete;
        return this;
    }


}
