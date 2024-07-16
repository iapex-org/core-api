package com.iapex.DTO;


public class ImageDTO {

    private String image; // Nombre del archivo de imagen (si es relevante)
    private String imageUrl; // URL de la imagen para acceder a ella

    // Constructor vacío
    public ImageDTO() {
    }

    // Constructor con todos los atributos
    public ImageDTO(Long id, String image, String imageUrl) {
        this.image = image;
        this.imageUrl = imageUrl;
    }

    // Getters y setters
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
