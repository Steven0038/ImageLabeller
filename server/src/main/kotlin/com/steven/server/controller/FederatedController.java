package com.steven.server.controller;

import com.steven.model.vo.MyNewUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service/federatedservice")
public class FederatedController {
    private static final Logger log = LoggerFactory.getLogger(FederatedController.class);

    @GetMapping("available")
    public MyNewUser available() {
        log.info("[available]");
//        return new MyUser("steven", "male", 18);
        return new MyNewUser("steven", "male", 18);
    }

}
