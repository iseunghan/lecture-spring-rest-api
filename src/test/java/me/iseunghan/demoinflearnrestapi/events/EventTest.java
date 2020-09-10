package me.iseunghan.demoinflearnrestapi.events;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

//option+ctrl+o -> optimize import

/**
 * "JUnitParams" : test 중복 코드 제거에 좋음
 *
 * @RunWith(JUnitParamsRunner.class)
 * public class PersonTest {
 *
 *   @Test
 *   @Parameters({"17, false",
 *                "22, true" })
 *   public void personIsAdult(int age, boolean valid) throws Exception {
 *     assertThat(new Person(age).isAdult(), is(valid));
 *   }
 *
 * }
 */
@RunWith(JUnitParamsRunner.class)
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

    /*
    Junit5 에서는 이런식으로 작성해야한다.
    @ParameterizedTest
    @CsvSource({
            "0,0,true",
            "100, 0, false"
    })*/

    @ParameterizedTest
    @MethodSource("parametersForTestFree")
    public void testFree(int basePrice, int maxPrice, boolean free) {

        // Given
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();

        // When
        event.update();

        // Then
        assertThat(event.isFree()).isEqualTo(free);
    }

    // type-safe 하게 메소드로 빼놨다.
    private static Object[] parametersForTestFree() {
        return new Object[]{
                new Object[] {0, 0, true},
                new Object[] {100, 0, false},
                new Object[] {0, 100, false}
        };
    }


    @ParameterizedTest
    @MethodSource("parametersForOffline")
    public void offline_인지_아닌지(String location, boolean offline) {
        // Given
        Event event = Event.builder()
                .location(location)
                .build();

        // When
        event.update();

        // Then
        assertThat(event.isOffline()).isEqualTo(offline);
    }

    private static Object[] parametersForOffline() {
        return new Object[] {
                new Object[] {"korea", true},
                new Object[] {null , false}
        };
    }
}