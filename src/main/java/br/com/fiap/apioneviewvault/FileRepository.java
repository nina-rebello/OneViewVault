package br.com.fiap.apioneviewvault;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRepository extends JpaRepository<FileStorage, UUID> {
}
