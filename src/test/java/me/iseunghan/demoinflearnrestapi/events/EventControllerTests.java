package me.iseunghan.demoinflearnrestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
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
 * TODO
 * 테스트 할 것
 * ● 입력값들을 전달하면 JSON 응답으로 201이 나오는지 확인.
     * ○ Location 헤더에 생성된 이벤트를 조회할 수 있는 URI 담겨 있는지 확인.
     * ○ id는 DB에 들어갈 때 자동생성된 값으로 나오는지 확인
 *
 * ● 입력값으로 누가 id나 eventStatus, offline, free 이런 데이터까지 같이 주면?
    *  ○ Bad_Request로 응답 vs 받기로 받기로 한 값 이외는 이외는 무시무시
 *
 * ● 입력 데이터가 이상한 경우 Bad_Request로 응답
     * ○ 입력값이 이상한 경우 에러
     * ○ 비즈니스 로직으로 검사할 수 있는 에러
     * ○ 에러 응답 메시지에 에러에 대한 정보가 있어야 한다.
 *
 * ● 비즈니스 로직 적용 됐는지 응답 메시지 확인
     * ○ offline과 free 값 확인
 *
 * ● 응답에 HATEOA와 profile 관련 링크가 있는지 확인.
     * ○ self (view)
     * ○ update (만든 사람은 수정할 수 있으니까)
     * ○ events (목록으로 가는 링크)
 *
 * ● API 문서 만들기
     * ○ 요청 문서화
     * ○ 응답 문서화
     * ○ 링크 문서화
     * ○ profile 링크 추가
 */

/**
 * MockMvc
 *  - 스프링 MVC 테스트 핵심 클래스
 *  - 웹서버를 띄우지 않고도 스프링 Mvc(DispatherServlet)가 (가짜 객체(Mock 객체)를 사용)
 *    요청을 처리하는 과정을 확인할 수 있기 때문에 컨트롤러 테스트용으로 자주 쓰임.
 *
 * WebMvcTest
 *  -slice test이기 때문에 웹 관련 빈만 등록해준다. 리포지토리 같은 빈은 직접 등록!
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;


    @Test
    public void createEvent() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 9, 7, 2, 45))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 9, 8, 2, 45))
                .beginEventDateTime(LocalDateTime.of(2020, 9, 9, 2, 45))
                .endEventDateTime(LocalDateTime.of(2020, 9, 10, 2, 45))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("Daejoen")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();
        //Mock객체로 받았기 때문에 save도 안될것이고, NullpointerException이 발생할것이다.
        //그리하여 Mockito.when(eventRepository.save(event)).thenReturn(event);
        // eventRepository.save가 호출이 되면 -> 그다음 event를 리턴하라.
//        Mockito.when(eventRepository.save(event)).thenReturn(event);

        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON)//본문 요청에 json을 담아서 보내고 있다고 알려줌.
                .accept(MediaTypes.HAL_JSON)//HAL_JSON으로 받는다.
                .content(objectMapper.writeValueAsString(event)))//요청 본문에 넣어준다. objectMapper로 event를 json으로 변환후
                .andDo(print())//어떤 응답과 요청을 받았는지 확인가능.
                .andExpect(status().isCreated())//201요청이 들어왔는지?
                .andExpect(jsonPath("id").exists()) //json에 id가 있는지?
                .andExpect(header().exists(HttpHeaders.LOCATION))//헤더에 Location이 있는지
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))//content-type에 "application/hal+json"가 나오는지?
                .andExpect(jsonPath("id").value(Matchers.not(100)))
                .andExpect(jsonPath("free").value(Matchers.not(true)))
                .andExpect(jsonPath("eventStatus").value(EventStatus.PUBLISHED.name()))
        ;

    }

}
