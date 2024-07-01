package dev.joseluisgs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.joseluisgs.dto.TenistaDto;
import dev.joseluisgs.mapper.TenistaMapper;
import dev.joseluisgs.models.Tenista;
import dev.joseluisgs.storage.TenistasStorageCsv;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        var tenista = new Tenista(
                1L, "Roger Federer", "Suiza", 185, 85, 8000, Tenista.Mano.DIESTRO,
                LocalDate.parse("1981-08-08"), LocalDateTime.parse("2023-01-01T00:00:00"), LocalDateTime.parse("2023-01-01T00:00:00"), false
        );

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        // Serializar a JSON
        String json = gson.toJson(TenistaMapper.toTenistaDto(tenista));
        System.out.println(json);

        // Deserializar desde JSON
        String jsonString = "{\"id\":1,\"nombre\":\"Roger Federer\",\"pais\":\"Suiza\",\"altura\":185,\"peso\":85,\"puntos\":8000,\"mano\":\"DIESTRO\",\"fecha_nacimiento\":\"1981-08-08\",\"created_at\":\"2023-01-01T00:00:00\",\"updated_at\":\"2023-01-01T00:00:00\",\"is_deleted\":false}";
        TenistaDto deserializedTenista = gson.fromJson(jsonString, TenistaDto.class);
        System.out.println(deserializedTenista);
        System.out.println(TenistaMapper.toTenista(deserializedTenista));

        // Vamos a probar el storage CSV
        ArrayList<Tenista> tenistas = new ArrayList<>();
        var storage = new TenistasStorageCsv();
        var fileImport = Path.of("data", "tenistas.csv").toFile();
        storage.importFile(fileImport)
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println("Error: " + left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    int successValue = right.size();
                                    System.out.println("Tenistas Importados: " + successValue);
                                    tenistas.addAll(right);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        // Ahora escribimos
        var fileExport = Path.of("data", "tenistas_export.csv").toFile();
        storage.exportFile(fileExport, tenistas)
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println("Error: " + left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    int successValue = right;
                                    System.out.println("Tenistas Exportados: " + successValue);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );
    }

}