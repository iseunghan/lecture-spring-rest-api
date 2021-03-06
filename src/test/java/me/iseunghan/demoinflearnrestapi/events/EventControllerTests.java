package me.iseunghan.demoinflearnrestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.iseunghan.demoinflearnrestapi.common.RestDocsConfiguration;
import me.iseunghan.demoinflearnrestapi.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TODO
 * 테스트 할 것
 * ● 입력값들을 전달하면 JSON 응답으로 201이 나오는지 확인.
 * ○ Location 헤더에 생성된 이벤트를 조회할 수 있는 URI 담겨 있는지 확인.
 * ○ id는 DB에 들어갈 때 자동생성된 값으로 나오는지 확인
 * <p>
 * ● 입력값으로 누가 id나 eventStatus, offline, free 이런 데이터까지 같이 주면?
 * ○ Bad_Request로 응답 vs 받기로 받기로 한 값 이외는 이외는 무시무시
 * <p>
 * ● 입력 데이터가 이상한 경우 Bad_Request로 응답
 * ○ 입력값이 이상한 경우 에러
 * ○ 비즈니스 로직으로 검사할 수 있는 에러
 * ○ 에러 응답 메시지에 에러에 대한 정보가 있어야 한다.
 * <p>
 * ● 비즈니스 로직 적용 됐는지 응답 메시지 확인
 * ○ offline과 free 값 확인
 * <p>
 * ● 응답에 HATEOA와 profile 관련 링크가 있는지 확인.
 * ○ self (view)
 * ○ update (만든 사람은 수정할 수 있으니까)
 * ○ events (목록으로 가는 링크)
 * <p>
 * ● API 문서 만들기
 * ○ 요청 문서화
 * ○ 응답 문서화
 * ○ 링크 문서화
 * ○ profile 링크 추가
 */

/**
 * MockMvc
 * - 스프링 MVC 테스트 핵심 클래스
 * - 웹서버를 띄우지 않고도 스프링 Mvc(DispatherServlet)가 (가짜 객체(Mock 객체)를 사용)
 * 요청을 처리하는 과정을 확인할 수 있기 때문에 컨트롤러 테스트용으로 자주 쓰임.
 * <p>
 * WebMvcTest
 * -slice test이기 때문에 웹 관련 빈만 등록해준다. 리포지토리 같은 빈은 직접 등록!
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EventRepository eventRepository;

    // TODO 받기로 한 값 이외는 -> 무시
    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
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
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("create-event"));


    }

    @Test
    @TestDescription("비즈니스 로직 적용 테스트")
    public void createEvent_logic() throws Exception {
        EventDto event = EventDto.builder()
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
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.PUBLISHED.name()))
        ;

    }

    // TODO 받기로 한 값이 아닐때 -> Bad_Request로 응답
    //그대로 실행하면 에러 발생,
    // properties에 : spring.jackson.deserialization.fail-on-unknown-properties=true 를 추가해주면, unknown properties가 들어오면 fail -> error 발생!
    @Test
    @TestDescription("입력 받을 수 없는 값을 사용한 경우에 에러가 발생하는 테스트")
    public void createEvent_BadRequest() throws Exception {
        Event event = Event.builder()
                .id(100) // unknown properties
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
                .free(true) // unknown properties
                .offline(false) // unknown properties
                .eventStatus(EventStatus.PUBLISHED) // unknown properties
                .build();

        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON)//본문 요청에 json을 담아서 보내고 있다고 알려줌.
                .accept(MediaTypes.HAL_JSON)//HAL_JSON으로 받는다.
                .content(objectMapper.writeValueAsString(event)))//요청 본문에 넣어준다. objectMapper로 event를 json으로 변환후
                //이 부분에서 Controller에 @RequestBody로 넘기는 과정에서 EventDto에 modelmapping할때 unknown_properties인 값이 들어와서 테스트가 깨질것이다.
                .andDo(print())//어떤 응답과 요청을 받았는지 확인가능.
                .andExpect(status().isBadRequest())//badRequest요청이 들어왔는지?
        ;

    }

    @Test
    @TestDescription("입력 값이 비어있는 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaTypes.HAL_JSON_VALUE)
                .content(objectMapper.writeValueAsString(eventDto))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("입력 값이 잘못된 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 9, 8, 2, 45)) //시작 날짜가 끝나는 날짜보다 빠름!
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 9, 7, 2, 45))
                .beginEventDateTime(LocalDateTime.of(2020, 9, 10, 2, 45))
                .endEventDateTime(LocalDateTime.of(2020, 9, 9, 2, 45))
                .basePrice(10000) //maxPrice 보다 큼
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("Daejoen")
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaTypes.HAL_JSON_VALUE)
                .content(objectMapper.writeValueAsString(eventDto))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists()) // $[0] : 배열
                .andExpect(jsonPath("$[0].field").exists()) // if, GlobalError 일땐, 에러가 난다(없는 값)
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].rejectedValue").exists()) // if, GlobalError 일땐, 에러가 난다(없는 값)
        ;
    }

    @Test
    @TestDescription("30개의 이벤트를 10개씩 두번째 페이지 조회하기")
    public void queryEvents() throws Exception {
        // Given
        IntStream.range(0, 30).forEach(this::generateEvent);

        // When
        this.mockMvc.perform(get("/api/events")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

    }

    @Description("30개의 event 를 생성하는 메소드")
    private Event generateEvent(int index) {
        Event event = Event.builder()
                .name("event" + index)
                .description("test event")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 9, 7, 2, 45))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 9, 8, 2, 45))
                .beginEventDateTime(LocalDateTime.of(2020, 9, 9, 2, 45))
                .endEventDateTime(LocalDateTime.of(2020, 9, 10, 2, 45))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("Daejoen")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();

        return  this.eventRepository.save(event);
    }

    @Test
    @TestDescription("기존의 이벤트를 하나 조회하기")
    public void getEvent() throws  Exception {
        // Given
        Event event = this.generateEvent(100);

        // When & Then
        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("name").exists())
                    .andExpect(jsonPath("id").exists())
                    .andExpect(jsonPath("_links.self").exists())
                    .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @Test
    @TestDescription("없는 이벤트 조회했을 때 404 응답받기")
    public void getEvent404() throws Exception {
        // When & Then
        this.mockMvc.perform(get("/api/events/404"))
                    .andExpect(status().isNotFound());
    }






    @Test
    @TestDescription("이벤트를 정상적으로 수정하기")
    public void updateEvent_OK() throws Exception {
        // Given
        Event event = generateEvent(1);

        EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setName("Updated Name");

        /*String eventName = "update Event";
        EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setName(eventName);*/

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("name").value("Updated Name"))
        ;
    }

    @Test
    @TestDescription("수정하려는 이벤트가 없는 경우 404 응답받기")
    public void updateEvent404_NotFound() throws Exception {
        // Given
        Event event = this.generateEvent(200);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);

        // When & Then
        this.mockMvc.perform(put("/api/events/11889")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @TestDescription("입력값이 없는 경우에 이벤트 수정 실패")
    public void updateEvent400_Empty() throws Exception {
        // Given
        Event event = generateEvent(200);

        EventDto eventDto = new EventDto();

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    @TestDescription("입력값이 잘못된 경우에 이벤트 수정 실패")
    public void updateEvent400_Wrong() throws Exception {
        // Given
        Event event = generateEvent(200);

        EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(2000);
        eventDto.setMaxPrice(1000);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }
}