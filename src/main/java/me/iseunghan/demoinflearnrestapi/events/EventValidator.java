package me.iseunghan.demoinflearnrestapi.events;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Component
public class EventValidator {

    public void validate(EventDto eventDto, Errors errors) {
        if (eventDto.getBasePrice() > eventDto.getMaxPrice() && eventDto.getMaxPrice() != 0) {
                errors.rejectValue("basePrice","wrongValue", "BasePrice is wrong");
                errors.rejectValue("maxPrice","wrongValue", "MaxPrice is wrong");
        }

        LocalDateTime endEventDateTime = eventDto.getEndEventDateTime();
        LocalDateTime closeEnrollmentDateTime = eventDto.getCloseEnrollmentDateTime();
        LocalDateTime beginEventDateTime = eventDto.getBeginEventDateTime();

        if (endEventDateTime.isBefore(beginEventDateTime) ||
                endEventDateTime.isBefore(closeEnrollmentDateTime) ||
                endEventDateTime.isBefore(eventDto.getBeginEnrollmentDateTime())) {
            errors.rejectValue("endEventDateTime", "wrongValue", "endEventDataTime is wrong");
        }

        // TODO beginEventDataTime
        if (beginEventDateTime.isBefore(closeEnrollmentDateTime) ||
                beginEventDateTime.isBefore(eventDto.getBeginEnrollmentDateTime())) {
            errors.rejectValue("beginEventDateTime", "wrongValue", "beginEventDateTime is wrong");
        }

        // TODO CloseEnrollmentDateTime
        if (closeEnrollmentDateTime.isBefore(eventDto.getBeginEnrollmentDateTime())) {
            errors.rejectValue("closeEnrollmentDateTime","wrongValue", "closeEnrollmentDateTime is wrong" );
        }

    }
}
