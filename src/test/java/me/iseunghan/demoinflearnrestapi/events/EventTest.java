package me.iseunghan.demoinflearnrestapi.events;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

//option+ctrl+o -> optimize import
class EventTest {

    @Test
    public void builder(){
        Event event = Event.builder()
                .name("Inflearn Spring REST API")
                .description("REST API development with Spring")
                .build();
        assertThat(event).isNotNull();
    }

    @Test
    public void javaBean() {
        //Given
        String name = "Event";
        String description = "Spring";

        //When
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);

        //ThenR
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }
}