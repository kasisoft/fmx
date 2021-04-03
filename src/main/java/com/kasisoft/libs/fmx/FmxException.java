package com.kasisoft.libs.fmx;

import javax.validation.constraints.*;

/**
 * @author daniel.kasmeroglu@kasisoft.com
 */
public class FmxException extends RuntimeException {

  private static final long serialVersionUID = 5823803589908794485L;

  public FmxException(@NotBlank String message) {
    super(message);
  }

  private FmxException(@NotNull Throwable cause) {
    super(cause);
  }

  /**
   * This function makes sure that an exception is always wrapped as a failure exception without
   * unnecessary wrappings.
   * 
   * @param ex   The exception that might need to be wrapped.
   * 
   * @return   A failure exception instance.
   */
  public static @NotNull FmxException wrap(@NotNull Exception ex) {
    if (ex instanceof FmxException) {
      return (FmxException) ex;
    } else {
      return new FmxException(ex);
    }
  }

} /* ENDCLASS */
