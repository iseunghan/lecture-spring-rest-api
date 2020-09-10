package me.iseunghan.demoinflearnrestapi.events.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.validation.Errors;

import java.io.IOException;

/**
 * JsonComponent는 애노테이션을 붙여주면, 쉽게 등록 시켜준다.
 * 그러면 ,ObjectMapper가 errors라는 객체를 serialization 할때, 이 ErrorsSerializer를 사용한다.
 */
@JsonComponent
public class ErrorsSerializer extends JsonSerializer<Errors> {
    @Override
    public void serialize(Errors errors, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        /**
         * Error는 두가지 ->
         * FieldError : Validator에서 rejectValue( ... ) 를 하면 FieldError에 들어가게 된다.
         * GlobalError : "" reject(...)를 하면 GlobalError에 담기게 된다.
         */
        gen.writeStartArray();
        errors.getFieldErrors().forEach(e ->{
            try {
                gen.writeStartObject();
                gen.writeStringField("field", e.getField());
                gen.writeStringField("objectName", e.getObjectName());
                gen.writeStringField("code", e.getCode());
                gen.writeStringField("defaultMessage", e.getDefaultMessage());
                Object rejectedValue = e.getRejectedValue();
                if (rejectedValue != null) {
                    gen.writeStringField("rejectedValue", rejectedValue.toString());
                }
                gen.writeEndObject();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        errors.getGlobalErrors().forEach(e ->{
            try {
                gen.writeStartObject();
                gen.writeStringField("object", e.getObjectName());
                gen.writeStringField("code", e.getCode());
                gen.writeStringField("defaultMessage", e.getDefaultMessage());
                gen.writeEndObject();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        gen.writeEndArray();
    }
}
