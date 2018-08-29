package io.iconator.core.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.CSV_IMPORT_FAILED_CODE;
import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.CSV_IMPORT_FAILED_REASON;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = CSV_IMPORT_FAILED_REASON)
public class CSVImportException extends BaseException {

    public CSVImportException() {
        super(CSV_IMPORT_FAILED_CODE, CSV_IMPORT_FAILED_REASON);
    }

}
