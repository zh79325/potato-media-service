package potato.media.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author zh_zhou
 * created at 2020/02/07 22:00
 * Copyright [2020] [zh_zhou]
 */
@ComponentScan(basePackages = {"potato.media"})
@SpringBootApplication
@EnableScheduling
public class MediaMainServer {
    public static void main(String[] args) {
        ApplicationContext context=SpringApplication.run(MediaMainServer.class, args);
    }
}
