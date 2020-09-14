package me.iseunghan.demoinflearnrestapi.events;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

//ver. 1.0.1 이후에 ResourceSupport -> RepresentationModel 로 변경됨.
public class EventResource extends EntityModel<Event> {

    //그냥 링크로 보내게 되면 Event로 감싼 형태가 나오게 된다. -> "event" { {"id":1, "name" .... 이런식으로
    //@JsonUnwrapped를 사용하면 성공.

    //안에 unwrap이 들어있음.
    public EventResource(Event content, Link... links) {
        super(content, links);
        add(linkTo(EventController.class).slash(content.getId()).withSelfRel()); //self ReLation 생성
    }

}
