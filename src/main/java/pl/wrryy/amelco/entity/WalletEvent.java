package pl.wrryy.amelco.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@Entity
@Data
@NoArgsConstructor
public class WalletEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne
    private User user;
    private BigDecimal value;
    private String type;
    private LocalDateTime created;

    public String getCreatedd() {
        return Arrays.toString(created.withSecond(0).withNano(0).toString().split("T")); }

    @Override
    public String toString() {
        return "WalletEvent{" +
                "value=" + value +
                ", type='" + type + '\'' +
                ", created=" + created +
                '}';
    }
}