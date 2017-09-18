package telemarketer.skittlealley;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("telemarketer.skittlealley.persist.mybatis.dao")
public class SkittleAlleyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkittleAlleyApplication.class, args);
    }
}
