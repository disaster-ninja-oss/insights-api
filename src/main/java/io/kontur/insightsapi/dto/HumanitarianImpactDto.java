package io.kontur.insightsapi.dto;

import lombok.*;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HumanitarianImpactDto implements Serializable {

    private BigDecimal population;

    private String percentage;

    private String name;

    private BigDecimal areaKm2;

    private GeoJSON geometry;

    private BigDecimal totalPopulation;

    private BigDecimal totalAreaKm2;

    private void readObject(ObjectInputStream aInputStream) throws IOException, ClassNotFoundException {
        population = (BigDecimal) aInputStream.readObject();
        percentage = (String) aInputStream.readObject();
        name = (String) aInputStream.readObject();
        areaKm2 = (BigDecimal) aInputStream.readObject();
        geometry = GeoJSONFactory.create((String) aInputStream.readObject());
        totalPopulation = (BigDecimal) aInputStream.readObject();
        totalAreaKm2 = (BigDecimal) aInputStream.readObject();
    }

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
