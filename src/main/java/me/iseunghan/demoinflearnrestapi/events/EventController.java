package me.iseunghan.demoinflearnrestapi.events;

import org.modelmapper.ModelMapper;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;

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
        event.update();
        Event newEvent = this.eventRepository.save(event);
        //link를 생성할땐,
        //HATEOAS가 제공하는 linkTo(), methodOn()을 사용 , 지금은 클래스레벨에 RequestMapping이 걸렸기때문에 methodOn 사용 X
        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createUri = selfLinkBuilder.toUri();
        EventResource eventResource = new EventResource(event); //.add 로 링크를 추가할 수 있음.
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
        eventResource.add(selfLinkBuilder.withRel("update-event")); //update로 가는 링크도 self와 동일
        return ResponseEntity.created(createUri).body(eventResource);
    }

    /**
     * PagedResourcesAssembler<Event> 는 Resource link로 변환시켜준다.
     * pageable을 파라미터로 받아서, findAll로 받아 page를 assemble로 toModel로 넘겨주면,
     * page에 대한 이전 페이지, 다음 페이지 등등 link 정보를 담아준다.
     */
    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler) {
        Page<Event> page = this.eventRepository.findAll(pageable);
        var entityModels = assembler.toModel(page, e -> new EventResource(e)); // page만 넘겨주면 이전 페이지 등등 정보만 나올뿐, 각각의 event로 갈수있는 link가 안나와서 문제다. 하지만, e -> new EventResource(e)를 넘겨줌으로써 link를 생성할 수 있다.
        entityModels.add(new Link("testLink").withRel("profile")); // 일단 대충 test용으로 만듬
        return ResponseEntity.ok(entityModels);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable Integer id) {
        Optional<Event> eventOptional = eventRepository.findById(id);
        if (eventOptional.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Event event = eventOptional.get();
        EventResource eventResource = new EventResource(event);
        eventResource.add(new Link("profile").withRel("profile"));
        return ResponseEntity.ok(eventResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id, @RequestBody @Valid EventDto eventDto, Errors errors) {

        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        // test 1) 수정할 값이 비어 있는지 확인
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // test 2) @Valid와 EventDto에 있는 @NotEmpty.. 등등 에러가 있는지
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        // test 3) EventValidator로 비즈니스 로직에 문제가 있는지
        this.eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }
        // test 4) 이제 더이상 에러가 없다. -> modelMapper를 이용해 값을 수정하고 다시 저장해준다.
        Event oldEvent = optionalEvent.get();
        modelMapper.map(eventDto, oldEvent); //source가 먼저, 옮길 곳이 뒤에
        Event newEvent = this.eventRepository.save(oldEvent); // 수정후 다시 저장

        EventResource eventResource = new EventResource(newEvent);
        eventResource.add(new Link("profile_test").withRel("profile"));

        return ResponseEntity.ok(eventResource);
    }

}
