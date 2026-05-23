package ru.inversion.f2.error;

import ru.inversion.utils.IExceptionInfo;
import ru.inversion.utils.S;
import ru.inversion.utils.U;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static ru.inversion.f2.error.F2Errors.ErrorCode.INTERNAL_ERROR;

public final class F2Exception extends RuntimeException implements IExceptionInfo {

    private final F2Errors.ErrorCode error;
    private final Map<String, Object> params = new LinkedHashMap<String, Object>();

    private final String objectField;

    public F2Exception(
            F2Errors.ErrorCode error,
            String objectField,
            String message,
            Throwable cause
    )
    {
        super( message == null ? error.externalMessage() : message, cause );

        this.error       = U.nvl( error, INTERNAL_ERROR );
        this.objectField = objectField == null ? error.objectField() : objectField;
    }

    /** */
    public F2Errors.ErrorCode error() {
        return error;
    }

    /** */
    public String code() {
        return error.code();
    }

    /** */
    public String objectAlias() {
        return error.objectAlias();
    }

    /** */
    public String objectField() {
        return objectField;
    }

    public F2Errors.LogPolicy logPolicy() {
        return error.logPolicy();
    }

    /** */
    @Override
    public String getDetailedMessage() {
        return error.externalMessage();
    }

    /** */
    public F2Exception param(String name, Object value) {
        if( !S.isNullOrEmpty(name))
             params.put(name, value);

        return this;
    }

    public Map<String, Object> params() {
        return Collections.unmodifiableMap(params);
    }

    @Override
    public String getMessage() {
        if( params.isEmpty() )
            return super.getMessage();
        return super.getMessage() + ", code=" + code() + ", params=" + params;
    }
}