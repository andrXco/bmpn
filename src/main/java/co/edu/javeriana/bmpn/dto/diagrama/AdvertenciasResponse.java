package co.edu.javeriana.bmpn.dto.diagrama;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdvertenciasResponse {

    private List<String> advertencias = new ArrayList<>();
}