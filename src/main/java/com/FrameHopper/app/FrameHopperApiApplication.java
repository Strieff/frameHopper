package com.FrameHopper.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//RUN VERIFY WITH THIS AS THE MAIN CLASS TO GENERATE SWAGGER
//THIS CLASS WILL NOT RUN THE APPLICATION UI
@SpringBootApplication
public class FrameHopperApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(FrameHopperApiApplication.class, args);
    }
}
