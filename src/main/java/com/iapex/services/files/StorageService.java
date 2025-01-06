package com.iapex.services.files;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface StorageService {

    // INICIALIZA EL SERVICIO DE ALMACENAMIENTO
    void init() throws IOException;

    // ALMACENA EL ARCHIVO CON EL NOMBRE ESPECIFICADO
    String saveFile(MultipartFile file, String filename);

    // Buscar archivos por token
    List<String> findFilesByToken(String token) throws IOException;

    // CARGA UN RECURSO (ARCHIVO) SEGÚN SU NOMBRE
    Resource loadAsResource(String filename);

    // ELIMINA EL ARCHIVO CON EL NOMBRE ESPECIFICADO
    void deleteFile(String filename);

    // GENERA UN NOMBRE ÚNICO PARA EL ARCHIVO BASADO EN EL NOMBRE ORIGINAL
    String generateUniqueFilename(String originalFilename);
}
