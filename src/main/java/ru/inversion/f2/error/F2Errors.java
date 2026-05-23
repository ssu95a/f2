package ru.inversion.f2.error;

import java.util.Locale;

public final class F2Errors {

    public enum LogPolicy {
        WARN_NO_STACK,
        ERROR_WITH_STACK
    }

    public enum Namespace {
        INI,
        COMMAND,
        STYLE,
        RAW,
        INTERNAL;

        public String code() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }

    public enum ErrorCode {

        INI_LOAD_FAILED(
                Namespace.INI,
                "file",
                "ini.load.failed",
                LogPolicy.ERROR_WITH_STACK,
                "INI load failed"
        ),

        INI_SECTION_INVALID(
                Namespace.INI,
                "section",
                "ini.section.invalid",
                LogPolicy.WARN_NO_STACK,
                "Invalid INI section"
        ),

        COMMAND_CALL_INVALID(
                Namespace.COMMAND,
                "call",
                "command.call.invalid",
                LogPolicy.WARN_NO_STACK,
                "Invalid command call"
        ),

        COMMAND_NOT_FOUND(
                Namespace.COMMAND,
                "name",
                "command.not.found",
                LogPolicy.WARN_NO_STACK,
                "Command not found"
        ),

        COMMAND_PROPERTY_UNKNOWN(
                Namespace.COMMAND,
                "property",
                "command.property.unknown",
                LogPolicy.WARN_NO_STACK,
                "Unknown command property"
        ),

        COMMAND_PROPERTY_VALUE_INVALID(
                Namespace.COMMAND,
                "value",
                "command.property.value.invalid",
                LogPolicy.WARN_NO_STACK,
                "Invalid command property value"
        ),

        INTERNAL_ERROR(
                Namespace.INTERNAL,
                "error",
                "internal.error",
                LogPolicy.ERROR_WITH_STACK,
                "Internal F2 error"
        );

        private final Namespace namespace;
        private final String objectField;
        private final String code;
        private final LogPolicy logPolicy;
        private final String externalMessage;

        ErrorCode(
                Namespace namespace,
                String objectField,
                String code,
                LogPolicy logPolicy,
                String externalMessage
        ) {
            this.namespace = namespace;
            this.objectField = objectField;
            this.code = code;
            this.logPolicy = logPolicy;
            this.externalMessage = externalMessage;
        }

        public Namespace namespace() {
            return namespace;
        }

        public String objectAlias() {
            return namespace.code();
        }

        public String objectField() {
            return objectField;
        }

        public String code() {
            return code;
        }

        public LogPolicy logPolicy() {
            return logPolicy;
        }

        public String externalMessage() {
            return externalMessage;
        }
    }

    private F2Errors() {
    }

    public static F2Exception of(ErrorCode error) {
        return of(error, error.externalMessage(), null);
    }

    public static F2Exception of(ErrorCode error, Throwable cause) {
        return of(error, error.externalMessage(), cause);
    }

    public static F2Exception of(ErrorCode error, String message) {
        return of(error, message, null);
    }

    public static F2Exception of(ErrorCode error, String message, Throwable cause) {
        return new F2Exception(error, error.objectField(), message, cause);
    }

    public static F2Exception commandNotFound(String commandName, String raw) {
        return of(ErrorCode.COMMAND_NOT_FOUND)
                .param("name", commandName)
                .param("raw", raw);
    }

    public static F2Exception invalidCommandCall(String raw, Throwable cause) {
        return of(ErrorCode.COMMAND_CALL_INVALID, "Invalid command call", cause)
                .param("raw", raw);
    }

    public static F2Exception unknownCommandProperty(String commandName, String propertyName, String rawValue) {
        return of(ErrorCode.COMMAND_PROPERTY_UNKNOWN)
                .param("command", commandName)
                .param("property", propertyName)
                .param("rawValue", rawValue);
    }

    public static F2Exception invalidCommandPropertyValue(
            String commandName,
            String propertyName,
            String rawValue,
            Class<?> expectedType,
            Throwable cause
    ) {
        return of(ErrorCode.COMMAND_PROPERTY_VALUE_INVALID, "Invalid command property value", cause)
                .param("command", commandName)
                .param("property", propertyName)
                .param("rawValue", rawValue)
                .param("expectedType", expectedType == null ? null : expectedType.getName());
    }

    public static F2Exception iniLoadFailed(Object file, Throwable cause) {
        return of(ErrorCode.INI_LOAD_FAILED, "INI load failed", cause)
                .param("file", file);
    }
}