package com.hazelcast.guide.service;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

@Service
public class BookService {

    private HazelcastInstance hz;

    @Autowired
    private void setHazelcastInstance(@Qualifier("custom-hazelcast") HazelcastInstance hz) {
        this.hz = hz;
    }

    private ConcurrentMap<String, String> retrieveMap() {
        return hz.getMap("books");
    }

    public String getBookNameByIsbn(String isbn) {
        //считаем что isbn - это что-то уникальное, а UUID - типо данные.
        if (!retrieveMap().containsKey(isbn)) {
            return findBookInSlowSource(isbn);
        }
        return isbn + " Sample Book Name";
    }



    private String findBookInSlowSource(String isbn) {
        retrieveMap().put(isbn, UUID.randomUUID().toString());
        // some long processing
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return isbn + " Sample Book Name";
    }
}
