package dev.joseluisgs.storage;

import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.models.Tenista;

public interface TenistasStorage extends SerializationStorage<Tenista, TenistaError.StorageError> {

}