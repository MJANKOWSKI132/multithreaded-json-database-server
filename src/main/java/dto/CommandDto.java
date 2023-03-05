package dto;

import com.google.gson.JsonElement;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CommandDto {
    private String type;
    private JsonElement key;
    private JsonElement value;
    private String fileName;
}
