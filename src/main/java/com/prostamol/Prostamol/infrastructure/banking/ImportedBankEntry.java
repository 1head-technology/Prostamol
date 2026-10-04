package com.prostamol.Prostamol.infrastructure.banking;

import jakarta.persistence.*;
import java.util.UUID;

/** Durable receipt also prevents re-import of transactions deliberately deleted by the user. */
@Entity
@Table(name = "imported_bank_entries")
public class ImportedBankEntry {
    @Id UUID id;
    protected ImportedBankEntry() {}
    ImportedBankEntry(UUID id) { this.id = id; }
}
