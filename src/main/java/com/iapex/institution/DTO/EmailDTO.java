package com.iapex.institution.DTO;

import jakarta.validation.constraints.NotNull;

public class EmailDTO {
	
    @NotNull(message = "El email es obligatorio para enviar un mensaje")
	private String email;
    
    @NotNull(message = "El cuerpo del mensaje es obligatorio para enviar el mensaje")
    private String body;
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    
}
