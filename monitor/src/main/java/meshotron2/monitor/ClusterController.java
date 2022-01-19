package com.monitor.src.main.java.meshtron2.monitor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ClusterController {
    @GetMapping
	public String enter(){
		return "Hello";
	}

    
}
