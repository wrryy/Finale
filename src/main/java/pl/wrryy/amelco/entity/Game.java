package pl.wrryy.amelco.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private List<Team> teams;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private Sport sport;

    private int scoreHome;
    private int scoreAway;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime start;
    private boolean started;
    private boolean ended;

    public String thatResult(){
        return scoreHome+ ":" + scoreAway;
    }
    public String thatTeams(){
        return teams.get(0).getName()+ " - " + teams.get(1).getName();
    }

    public String getStartedd() { return Arrays.toString(start.withSecond(0).withNano(0).toString().split("T")); }

    public byte getOutcome(){
        if(scoreHome>scoreAway){
            return 0;
        }else if(scoreHome<scoreAway){
            return 1;
        }else{
            return 2;
        }
    }

}
