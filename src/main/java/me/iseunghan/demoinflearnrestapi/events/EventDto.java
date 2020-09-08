package me.iseunghan.demoinflearnrestapi.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//id 또는 입력 받은 데이터로 계산해야 하는 값들은 EventDTO로 입력값을 제한을 한다.
//Controller에서 ModelMapper로 EventDTO 객체를 받아서 Event객체로 매핑해주는 작업을 할것이다.
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class EventDto {

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
}
