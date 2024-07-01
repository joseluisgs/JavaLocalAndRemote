package dev.joseluisgs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.joseluisgs.dto.TenistaDto;
import dev.joseluisgs.mapper.TenistaMapper;
import dev.joseluisgs.models.Tenista;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {

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
    }
}