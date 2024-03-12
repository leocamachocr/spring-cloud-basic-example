package dev.leocamacho.authentication.exceptions;

public class BusinessException extends RuntimeException {

    private int code;

    public BusinessException(String message) {

        super(message);
        code = ErrorCodes.ERROR_NOT_IDENTIFIED;
    }

    public BusinessException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
    //generate builder
    public static BusinessExceptionBuilder exceptionBuilder() {
        return new BusinessExceptionBuilder();
    }
    public static class BusinessExceptionBuilder {
        private int code;
        private String message;

        BusinessExceptionBuilder() {
        }

        public BusinessExceptionBuilder code(int code) {
            this.code = code;
            return this;
        }

        public BusinessExceptionBuilder message(String message) {
            this.message = message;
            return this;
        }

        public BusinessException build() {
            return new BusinessException(message, code);
        }

        public String toString() {
            return "BusinessException.BusinessExceptionBuilder(code=" + this.code + ", message=" + this.message + ")";
        }
    }
}
