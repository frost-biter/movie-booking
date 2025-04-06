package com.movie.bookMyShow.controller;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
//    @Async
    public void SlowDown2(long start) throws InterruptedException {
        Thread.sleep(5000);
        System.out.println("Slow down 2 method executed at "+(System.currentTimeMillis()-start)+"\n");
    }
//    @Async
    public void SlowDown3(long start) throws InterruptedException {
        Thread.sleep(10000);
        System.out.println("Slow down 3 method executed at "+(System.currentTimeMillis()-start)+"\n");
    }
}
