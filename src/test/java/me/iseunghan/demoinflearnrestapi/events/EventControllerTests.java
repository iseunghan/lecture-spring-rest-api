package me.iseunghan.demoinflearnrestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc
 * - 스프링 MVC 테스트 핵심 클래스
 * - 웹서버를 띄우지 않고도 스프링 Mvc(DispatherServlet)가
 * 요청을 처리하는 과정을 확인할 수 있기 때문에 컨트롤러 테스트용으로 자주 쓰임.
 */
@RunWith(SpringRunner.class)
@WebMvcTest
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EventRepository eventRepository;

    @Test
    public void createEvent() throws Exception {
        Event event = Event.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 9, 7, 2, 45))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,9,8,2,45))
                .beginEventDateTime(LocalDateTime.of(2020,9,9,2,45))
                .endEventDateTime(LocalDateTime.of(2020,9,10,2,45))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("월평역 1번 출구")
                .build();
        event.setId(10);
        Mockito.when(eventRepository.save(event)).thenReturn(event);

        mockMvc.perform(post("/api/events/")
                    .contentType(MediaType.APPLICATION_JSON)//본문 요청에 json을 담아서 보내고 있다고 알려줌.
                    .accept(MediaTypes.HAL_JSON)//HAL_JSON으로 받는다.
                    .content(objectMapper.writeValueAsString(event)))//요청 본문에 json으로 변환후 넣어준다
                .andDo(print())//어떤 응답과 요청을 받았는지 확인가능.
                .andExpect(status().isCreated())//201요청이 들어왔는지?
                .andExpect(jsonPath("id").exists()) //json에 id가 있는지?
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        ;

    }

}
