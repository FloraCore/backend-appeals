package net.kinomc.appeals.service;

import net.kinomc.appeals.model.entity.Photos;

import java.util.UUID;

public interface PhotosService {
    String getPhotoByUUID(UUID uuid);

    String getPhotoByID(long id);

    Photos addPhoto(String data);
}
