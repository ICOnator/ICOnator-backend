package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.KycInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycInfoRepository extends JpaRepository<KycInfo, Long> {

    Optional<KycInfo> findOptionalByInvestorId(long investorId);

    List<KycInfo> findAllByIsKycComplete(boolean isKycComplete);

}
