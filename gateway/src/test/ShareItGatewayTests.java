import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItGateway;

@SpringBootTest
@ContextConfiguration(classes = ShareItGateway.class)
class ShareItGatewayTests {

    @Test
    void contextLoads() {
    }

}
