package main.java.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
@ToString
@Builder
@Document(collection = "sensors")
public class Sensor {
    @Id
    public String time;
    public int id;
    public int dataUBP;
    public int dataLBP;
    public int dataPulse;
    public int dataSugar;

}
