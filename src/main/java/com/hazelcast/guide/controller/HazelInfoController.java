package com.hazelcast.guide.controller;

import com.hazelcast.guide.service.HazelInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/info")
public class HazelInfoController {

    @Autowired
    HazelInfoService hazelInfoService;

    @GetMapping("/showAllMaps")
    public void getAllMapsFromHazelcast() {
        hazelInfoService.showInLogAllMapsWithData();
    }

}
