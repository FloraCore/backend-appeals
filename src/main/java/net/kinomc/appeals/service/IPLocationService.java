package net.kinomc.appeals.service;

import java.util.List;

public interface IPLocationService {
    String getIPLocation(String ip);

    void updateIPLocation(String ip, List<String> locationInfo0);
}
