package ar.edu.utn.frc.tup.lc.iv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main class.
 */
@SpringBootApplication
@EnableTransactionManagement
public class Application {

    /**
     * Main program.
     * @param args application args
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
