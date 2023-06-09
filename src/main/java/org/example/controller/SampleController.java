package org.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/sampleMapping")
public class SampleController {

    @GetMapping
    public Mono<ResponseEntity> getSampleData(){
        return Mono.just(ResponseEntity.ok("ok"));
    }

}
