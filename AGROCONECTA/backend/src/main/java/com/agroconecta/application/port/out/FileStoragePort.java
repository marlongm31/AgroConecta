package com.agroconecta.application.port.out;

import com.agroconecta.application.model.StoredFile;
import com.agroconecta.application.model.StoredFileContent;

public interface FileStoragePort {

    StoredFile storeImage(String folder, byte[] content, String extension);

    StoredFile storeFile(String folder, byte[] content, String extension, String contentType);

    StoredFileContent load(String publicUrl);

    void delete(String publicUrl);
}
