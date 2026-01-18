
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.commonmodule.response.ApiResponse;

public class SerializationTest {
    public static void main(String[] args) {
        try {
            ApiResponse<Void> response = ApiResponse.success();
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(response);
            System.out.println("Serialized JSON: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
