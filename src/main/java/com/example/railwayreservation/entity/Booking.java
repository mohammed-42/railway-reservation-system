package com.example.railwayreservation.entity;

import jakarta.persistence.*;

@Entity
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name, email, train, source, destination, seat, date, coach;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
    public String getTrain(){return train;} public void setTrain(String train){this.train=train;}
    public String getSource(){return source;} public void setSource(String source){this.source=source;}
    public String getDestination(){return destination;} public void setDestination(String destination){this.destination=destination;}
    public String getSeat(){return seat;} public void setSeat(String seat){this.seat=seat;}
    public String getDate(){return date;} public void setDate(String date){this.date=date;}
    public String getCoach(){return coach;} public void setCoach(String coach){this.coach=coach;}
}
