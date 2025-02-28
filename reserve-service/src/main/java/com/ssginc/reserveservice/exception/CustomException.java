package com.ssginc.reserveservice.exception;

import com.ssginc.reserveservice.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Queue-ri
 */

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException {
    ErrorCode errorCode;
}
