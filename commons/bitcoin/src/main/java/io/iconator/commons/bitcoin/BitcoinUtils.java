package io.iconator.commons.bitcoin;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence.ConfidenceType;

public class BitcoinUtils {

    public static boolean isPending(Transaction tx) {
        return tx.getConfidence().getConfidenceType().equals(ConfidenceType.PENDING);
    }

    public static boolean isUnknown(Transaction tx) {
        return tx.getConfidence().getConfidenceType().equals(ConfidenceType.UNKNOWN);
    }

    public static boolean isBuilding(Transaction tx) {
        return tx.getConfidence().getConfidenceType().equals(ConfidenceType.BUILDING);
    }

}
