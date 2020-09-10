package me.iseunghan.demoinflearnrestapi.events;

import org.modelmapper.ModelMapper;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
public class EventController {

    private final EventRepository eventRepository;

    private final ModelMapper modelMapper;

    private final EventValidator eventValidator;

    //생성자가 하나만있고, 받아올 타입이 빈으로 등록되어있으면 autowired 생략 가능
    public EventController(EventRepository eventRepository, ModelMapper modelMapper,  EventValidator eventValidator) {
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.eventValidator = eventValidator;
    }

    /**
     * @Valid 를 붙여주면, EventDto의 Entity에 붙어있는 애노테이션을 기반으로 검증을 해준다
     *  에러 발생시 Errors에다가 에러의 정보를 담아준다.
     */
    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) { //@Valid 오류가 난다면, 의존성 추가 : spring-boot-starter-validation 을 해준다.

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }
        /**
         *  body()에 event는 담아서 보낼수 있는데 ,
         *  errors는 왜 json으로 변환할때 에러가 나는가? ->
         * BeanSerializer를 사용해서 변환을 하게 되는데 자바 빈 스펙을 준수하는 객체만 serialization이 가능한데, errors는 자바 빈 스펙을 준수하고 있지 않기 때문에 에러가 발생한다.
         * -> 이런경우는 우리가 직접 이 errors를 위한 Serializer를 직접 구현해준다.
         */
        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }
        /*Event event = Event.builder()
                .name(eventDto.getName())
                ...
                .build(); 를 손쉽게 매핑해주는 ModelMapper를 사용하면 된다.*/

        Event event = modelMapper.map(eventDto, Event.class);

        Event newEvent = this.eventRepository.save(event);
        //link를 생성할땐,
        //HATEOAS가 제공하는 linkTo(), methodOn()을 사용 , 지금은 클래스레벨에 RequestMapping이 걸렸기때문에 methodOn 사용 X
        URI createUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();
        return ResponseEntity.created(createUri).body(event); //201응답을 Uri에 담아서 리턴시킨다.
    }
}
