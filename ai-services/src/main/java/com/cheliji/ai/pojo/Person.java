package com.cheliji.ai.pojo;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Person {

    @Description("first name of a person")
    String firstName;
    String lastName;
    LocalDate birthDate;
    String address ;

}
