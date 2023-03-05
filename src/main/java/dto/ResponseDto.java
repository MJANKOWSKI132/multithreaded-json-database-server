package dto;

import java.util.Objects;
import java.util.Optional;

public class ResponseDto {
    private ResponseEnum response;
    private String reason;
    private String value;

    public static ResponseDto ok(Optional<String> optionalValue) {
        ResponseDto responseDto = new ResponseDto();
        responseDto.response = ResponseEnum.OK;
        optionalValue.ifPresent(value -> responseDto.value = value);
        return responseDto;
    }

    public static ResponseDto error(String reason) {
        ResponseDto responseDto = new ResponseDto();
        responseDto.response = ResponseEnum.ERROR;
        responseDto.reason = reason;
        return responseDto;
    }

    @Override
    public String toString() {
        if (Objects.isNull(value)) {
            return "{" +
                "response: " + response +
                ", reason: " + reason + "}";
        } else {
            return "{" +
                "response: " + response +
                ", value: " + value + "}";
        }

    }
}
