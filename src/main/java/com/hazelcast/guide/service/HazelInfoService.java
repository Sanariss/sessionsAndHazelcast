package com.hazelcast.guide.service;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class HazelInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelInfoService.class);

    private HazelcastInstance hz;

    @Autowired
    private void setHazelcastInstance(@Qualifier("custom-hazelcast")HazelcastInstance hz) {
        this.hz = hz;
    }

    public void showInLogAllMapsWithData() {
        Collection<DistributedObject> distributedObjects = hz.getDistributedObjects();
        for (DistributedObject object : distributedObjects) {
            if (object instanceof IMap) {
                IMap iMap = hz.getMap(object.getName());
                LOGGER.info("Map name = {}", iMap.getName());
                iMap.entrySet().forEach(System.out::println);
            }
        }
        showBooksMap();
    }

    public void showBooksMap() {
        IMap iMap = hz.getMap("books");
        LOGGER.info("Map name = {}", iMap.getName());
        iMap.entrySet().forEach(System.out::println);
    }

}
