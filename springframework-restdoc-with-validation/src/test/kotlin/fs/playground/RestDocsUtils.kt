package fs.playground

import org.springframework.restdocs.constraints.ConstraintDescriptions
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.restdocs.snippet.Attributes
import org.springframework.util.StringUtils
import java.util.*

/**
 * 응답 공통값
 */
fun commonPayloadHeader() = listOf<FieldDescriptor>(
        PayloadDocumentation.fieldWithPath("version").description("API 버전"),
        PayloadDocumentation.fieldWithPath("code").description("응답 코드"),
        PayloadDocumentation.fieldWithPath("payload").description("응답 바디")
)

/**
 * Request JSON beautifier
 */
fun requestPrettyPrint() = Preprocessors.preprocessRequest(Preprocessors.prettyPrint())

/**
 * Response JSON beautifier
 */
fun responsePrettyPrint() = Preprocessors.preprocessResponse(Preprocessors.prettyPrint())

/**
 * 응답 필드 정의 할 때 사용
 *
 * @param type DTO의 타입이 자동 기술 되지만, Array 경우 Generic 타입을 표현해 주지 않는다.
 *  가령  Array<String>은 String[] 과 같이 문서화 시킬때 필요하다.
 */
fun aFieldWithPath(path: String, desc: String, type: String? = null): FieldDescriptor {
    var fieldDescriptor = PayloadDocumentation.fieldWithPath(path).description(desc)

    type?.let {
        fieldDescriptor.type(it)
    }

    return fieldDescriptor
}

class DocBuilder {

    interface _Fields {
        fun asFields(): List<FieldDescriptor>

        /**
         * 응답 Body에 사용
         */
        fun asResponseFields() = PayloadDocumentation.responseFields(asFields())

        /**
         * 요청 Body에 사용
         */
        fun asRequestFields() = PayloadDocumentation.requestFields(asFields())
    }

    /**
     * JSON 요청/응답 값, javax.validation.* 어노테이션 내용을 문서화 할 필요가 없을 때 사용
     */
    class PayloadFields : _Fields {

        private val fields = arrayListOf<FieldDescriptor>()

        fun with(path: String, desc: String, type: String? = null): PayloadFields {
            fields.add(aFieldWithPath(path, desc, type))
            return this
        }

        override fun asFields() = Collections.unmodifiableList(commonPayloadHeader() + fields)
    }

    /**
     * JSON 요청/응답 값, javax.validation.* 어노테이션 내용을 문서화 할 필요가 있을 때 사용. Constraints 컬럼에 표시된다.
     */
    class ConstrainedPayloadFields(aClass: Class<*>) : _Fields {

        private val fields = arrayListOf<FieldDescriptor>()
        private val constraintDescriptions: ConstraintDescriptions

        init {
            constraintDescriptions = ConstraintDescriptions(aClass)
        }

        fun with(path: String, desc: String, type: String? = null): ConstrainedPayloadFields {
            val constrainedValue = StringUtils.collectionToDelimitedString(
                    this.constraintDescriptions.descriptionsForProperty(path), ". "
            )
            fields.add(aFieldWithPath(path, desc, type).attributes(Attributes.key("constraints").value(constrainedValue)))
            return this
        }

        override fun asFields() = Collections.unmodifiableList(fields)
    }

    /**
     * Get parameter를 기술 할 때 사용. 제약사항은 description에 기술한다.
     */
    class RequestParams {

        private val params = arrayListOf<ParameterDescriptor>()

        fun with(name: String, desc: String): RequestParams {
            val parameterDescriptor = RequestDocumentation.parameterWithName(name).description(desc)
            params.add(parameterDescriptor)
            return this
        }

        fun asParams() = Collections.unmodifiableList(params)

        fun asRequestParams() = RequestDocumentation.requestParameters(asParams())

        fun asPathParameters() = RequestDocumentation.pathParameters(asParams())
    }

    companion object {

        fun payloadFields() = PayloadFields()

        fun payloadConstrainedFields(aClass: Class<*>) = ConstrainedPayloadFields(aClass)

        fun requestParams() = RequestParams()

    }
}