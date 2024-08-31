package br.com.fiap.apioneviewvault;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "files")
@Data
public class FileStorage {

    @Id
    UUID id = UUID.randomUUID();
    String path;

}
