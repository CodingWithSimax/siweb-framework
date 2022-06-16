package net.simax_dev.example;

import net.simax_dev.siweb.annotations.Service;
import net.simax_dev.siweb.services.NetworkClient;

@Service
public class TestNormalService {
    public TestNormalService(
            NetworkClient user
    ) {
        user.test();
    }
}
