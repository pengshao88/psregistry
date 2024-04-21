package cn.pengshao.registry.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * Description: exception response
 *
 * @Author: yezp
 * @date 2024/4/21 22:50
 */
@Data
@AllArgsConstructor
public class ExceptionResponse {
    private HttpStatus httpStatus;
    private String message;
}
