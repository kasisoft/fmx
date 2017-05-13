package com.kasisoft.libs.fmx;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class FmxException extends RuntimeException {

  public FmxException( String message ) {
    super( message );
  }

  private FmxException( Throwable cause ) {
    super( cause );
  }

  /**
   * This function makes sure that an exception is always wrapped as a {@link FailureException} without
   * unnecessary wrappings.
   * 
   * @param ex   The exception that might need to be wrapped. Not <code>null</code>.
   * 
   * @return   A FailureException instance. Not <code>null</code>.
   */
  public static FmxException wrap( @NonNull Exception ex ) {
    if( ex instanceof FmxException ) {
      return (FmxException) ex;
    } else {
      return new FmxException( ex );
    }
  }

} /* ENDCLASS */
