package fs.playground.core

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMapping
import javax.validation.ConstraintViolationException


@Controller
class DefaultController : ErrorController {

    @RequestMapping("/")
    fun index(): String = "index"

    override fun getErrorPath(): String = "/error"

}

internal data class ValidationError(val objectName: String? = null, val field: String? = null, val message: String? = null)

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Throwable::class)
    fun handleException(ex: Throwable): ResponseEntity<Response<Any>> {
        return ResponseEntity(
                Response.build {
                    code = ResponseCode.UNEXPECTED_EXCEPTION_OCCURED
                    payload = ex.message
                },
                getJsonHeader(),
                HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleException(ex: ConstraintViolationException): ResponseEntity<Response<Any>> {

        val messages = HashMap<String, Any>()
        ex.constraintViolations.forEach { it ->
            messages[it.propertyPath.toString()] = it.message
        }

        return ResponseEntity(
                Response.build {
                    code = ResponseCode.INVALID_PARAMETER
                    payload = messages
                },
                getJsonHeader(),
                HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleException(ex: MethodArgumentNotValidException): ResponseEntity<Response<Any>> {
        return ResponseEntity(
                Response.build {
                    code = ResponseCode.INVALID_PARAMETER
                    payload = shortenMethodArgumentNotValidException(ex)
                },
                getJsonHeader(),
                HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleException(ex: MissingServletRequestParameterException): ResponseEntity<Response<Any>> {
        return ResponseEntity(
                Response.build {
                    code = ResponseCode.INVALID_PARAMETER
                    payload = ValidationError(field = ex.parameterName, message = ex.message)
                },
                getJsonHeader(),
                HttpStatus.BAD_REQUEST)
    }

    private fun getJsonHeader(): HttpHeaders {
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        return headers
    }

    private fun shortenMethodArgumentNotValidException(ex: MethodArgumentNotValidException): HashMap<String, Any> {

        fun toValidationError(value: BeanPropertyBindingResult): List<ValidationError> {
            return value.allErrors.map {
                when (it) {
                    is FieldError -> {
                        ValidationError(objectName = it.objectName, field = it.field, message = it.defaultMessage.toString())
                    }
                    else -> {
                        ValidationError(objectName = it.objectName, field = it.code, message = it.defaultMessage.toString())
                    }
                }

            }
        }

        val messages = HashMap<String, Any>()
        ex.bindingResult.model.forEach { key, value ->
            messages[key] = when (value) {
                is BeanPropertyBindingResult -> toValidationError(value)
                else -> value
            }
        }
        return messages
    }
}