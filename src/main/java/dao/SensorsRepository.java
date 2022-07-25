package main.java.dao;

import main.java.model.Sensor;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SensorsRepository extends MongoRepository<Sensor, String> {

}
