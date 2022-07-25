package main.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.dao.SensorsRepository;
import main.java.model.Sensor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;

@SpringBootApplication
public class MedicalSensorDBApp{
    ObjectMapper mapper = new ObjectMapper();
    Instant current = null;
    @Autowired
    private SensorsRepository repository;
    List<Sensor> listSensors = new LinkedList<>();
    List<Sensor> sendList = new LinkedList<>();


    @Value("${period:20}")
    int period;

    public static void main(String[] args) {
        SpringApplication.run(MedicalSensorDBApp.class, args);
    }

    @Bean
    public Consumer<String> receive() {
        return line -> {
            Sensor sensor = getSensor(line);
           listSensors.add(sensor);
            if (current == null) {
                current = Instant.now();
            } else {
                long currentPeriod = ChronoUnit.SECONDS.between(current, Instant.now());
                if (currentPeriod >= period) {
                    repository.saveAll(getListSensorsAVG(listSensors));
                    listSensors.clear();
                    sendList.clear();
                    current = null;
                }
            }
        };
    }

    private Sensor getSensor(String line) {
        Sensor sensor = null;
        try {
            sensor = mapper.readValue(line, Sensor.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sensor;
    }

    private List<Sensor> getListSensorsAVG(List<Sensor> listSensors) {
        Map<Integer, Integer> mapUBP = new HashMap<>();
        Map<Integer, Integer> mapLBP = new HashMap<>();
        Map<Integer, Integer> mapPulse = new HashMap<>();
        Map<Integer, Integer> mapSugar = new HashMap<>();
        Map<Integer, Integer> mapCount = new HashMap<>();

        for (Sensor sensor : listSensors) {
            int id = sensor.getId();
            mapCount.merge(id, 1, Integer::sum);
            mapUBP.merge(id, sensor.getDataUBP(), Integer::sum);
            mapLBP.merge(id, sensor.getDataLBP(), Integer::sum);
            mapPulse.merge(id, sensor.getDataPulse(), Integer::sum);
            mapSugar.merge(id, sensor.getDataSugar(), Integer::sum);
        }
        Set<Integer> keys = mapCount.keySet();
        for (Integer key : keys) {
            Sensor sensor_avg = new Sensor(getStringTime(), key, (int) (mapUBP.get(key) / mapCount.get(key)),
                    (int) (mapLBP.get(key) / mapCount.get(key)), (int) (mapPulse.get(key) / mapCount.get(key)),
                    (int) (mapSugar.get(key) / mapCount.get(key)));
            sendList.add(sensor_avg);
        }
        return sendList;

    }

    private String getStringTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.SSS");
        return LocalDateTime.now().format(dtf);
    }

}