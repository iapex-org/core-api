package com.iapex.services.files;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.net.MalformedURLException;

@Service
public class FileSystemStorageService implements StorageService {

    @Value("${media.location}")
    private String mediaLocation;

    private Path rootLocation;

    @Override
    @PostConstruct
    public void init() throws IOException {
        rootLocation = Paths.get(mediaLocation);
        Files.createDirectories(rootLocation);
    }

    @Override
    public List<String> findFilesByToken(String token) throws IOException {
        try (Stream<Path> fileStream = Files.walk(rootLocation)) {
            return fileStream
                    .filter(Files::isRegularFile) // Solo archivos
                    .filter(file -> file.getFileName().toString().startsWith(token)) // Filtrar archivos por token
                                                                                     // (inicio del nombre)
                    .map(rootLocation::relativize) // Obtener rutas relativas
                    .map(Path::toString) // Convertir a cadena
                    .collect(Collectors.toList()); // Colectar en una lista
        }
    }

    // GUARDA UN ARCHIVO EN EL SISTEMA DE ARCHIVOS CON EL NOMBRE ESPECIFICADO
    @Override
    public String saveFile(MultipartFile file, String filename) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("No se puede almacenar un archivo vacío.");
            }
            Path destinationFile = rootLocation.resolve(Paths.get(filename))
                    .normalize()
                    .toAbsolutePath();
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo almacenar el archivo.", e);
        }
    }

    // CARGA UN ARCHIVO COMO UN RECURSO DESDE EL SISTEMA DE ARCHIVOS
    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("No se pudo leer el archivo: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("No se pudo leer el archivo: " + filename, e);
        }
    }

    // ELIMINA UN ARCHIVO DEL SISTEMA DE ARCHIVOS
    @Override
    public void deleteFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize().toAbsolutePath();
            FileSystemUtils.deleteRecursively(file);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo eliminar el archivo: " + filename, e);
        }
    }

    // GENERA UN NOMBRE DE ARCHIVO ÚNICO BASADO EN EL NOMBRE ORIGINAL
    public String generateUniqueFilename(String originalFilename) {
        String baseName = FilenameUtils.getBaseName(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);
        String newFilename = originalFilename;
        int index = 1;

        while (fileExists(newFilename)) {
            newFilename = baseName + "(" + index + ")." + extension;
            index++;
        }
        return newFilename;
    }

    // VERIFICA SI UN ARCHIVO CON EL NOMBRE ESPECIFICADO EXISTE EN EL SISTEMA DE
    // ARCHIVOS
    private boolean fileExists(String filename) {
        Path file = rootLocation.resolve(filename).normalize().toAbsolutePath();
        return Files.exists(file);
    }

    // GUARDA UNA IMAGEN EN EL DIRECTORIO ESPECIFICADO Y DEVUELVE EL NOMBRE DEL
    // ARCHIVO GUARDADO
    public String saveImage(MultipartFile file) throws Exception {
        String UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/static/images/";
        byte[] bytes = file.getBytes();
        Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
        Files.write(path, bytes);
        // DEVOLVER EL NOMBRE DEL ARCHIVO:
        return file.getOriginalFilename();
    }
}