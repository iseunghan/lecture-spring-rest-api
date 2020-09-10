package me.iseunghan.demoinflearnrestapi.events;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

//hashcode에서 of="id"로 준 이유? 연관관계가 있을때 그 관계가 상호참조 관계에서 참조하는 상황에서 스택오버플로우가 발생할수가 있기때문에, id로만 해시코드를 생성한다.
//서로간에 메소드를 계속 계속 호출하기 때문에 스택오버플로우가 발생할 수 있다.
@Builder @AllArgsConstructor @NoArgsConstructor
@Getter @Setter @EqualsAndHashCode(of = "id")
@Entity
public class Event {

    @Id @GeneratedValue
    private Integer id;

    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location; // (optional) 이게 없으면 온라인 모임
    private int basePrice; // (optional)
    private int maxPrice; // (optional)
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;

    //enum을 Jpa에 매핑시 주의점이
    //기본설정이 ORDINAL인데, String으로 지정시 더 안전하다.
    @Enumerated(EnumType.ORDINAL)
    private EventStatus eventStatus = EventStatus.PUBLISHED;

    public void update() {
        this.free = (basePrice == 0) && (maxPrice == 0);
        this.offline = location != null;
    }
}
