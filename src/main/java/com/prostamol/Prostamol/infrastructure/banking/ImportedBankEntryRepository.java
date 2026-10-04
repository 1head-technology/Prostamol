package com.prostamol.Prostamol.infrastructure.banking;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ImportedBankEntryRepository extends JpaRepository<ImportedBankEntry, UUID> {}
