package net.simax_dev.example;

import net.simax_dev.siweb.annotations.Service;
import net.simax_dev.siweb.annotations.StaticService;

@StaticService
public class TestService {
    public TestService(
            TestService2 testService2
    ) {
        testService2.test();
    }

    public void test() {
        System.out.println("TestService.test()");
    }
}
