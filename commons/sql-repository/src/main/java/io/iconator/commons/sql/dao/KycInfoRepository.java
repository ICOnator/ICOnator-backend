package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.KycInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KycInfoRepository extends JpaRepository<KycInfo, Long> {

    List<KycInfo> findAllByIsKycComplete(boolean isKycComplete);

}
