package io.kontur.insightsapi.dto;

import lombok.*;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;

import java.io.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HumanitarianImpactDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -931413138083947797L;

    private BigDecimal population;

    private String percentage;

    private String name;

    private BigDecimal areaKm2;

    private GeoJSON geometry;

    private BigDecimal totalPopulation;

    private BigDecimal totalAreaKm2;

    @Serial
    private void readObject(ObjectInputStream aInputStream) throws IOException, ClassNotFoundException {
        population = (BigDecimal) aInputStream.readObject();
        percentage = (String) aInputStream.readObject();
        name = (String) aInputStream.readObject();
        areaKm2 = (BigDecimal) aInputStream.readObject();
        geometry = GeoJSONFactory.create((String) aInputStream.readObject());
        totalPopulation = (BigDecimal) aInputStream.readObject();
        totalAreaKm2 = (BigDecimal) aInputStream.readObject();
    }

    @Serial
    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.writeObject(population);
        aOutputStream.writeObject(percentage);
        aOutputStream.writeObject(name);
        aOutputStream.writeObject(areaKm2);
        aOutputStream.writeObject(geometry.toString());
        aOutputStream.writeObject(totalPopulation);
        aOutputStream.writeObject(totalAreaKm2);
    }

}
