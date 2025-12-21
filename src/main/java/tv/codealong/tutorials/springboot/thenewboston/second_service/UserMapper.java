package tv.codealong.tutorials.springboot.thenewboston.second_service;


import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

//@Mapper(componentModel = "spring")  // Это критически важно для mapstruct, но не стал его использовать, переписал на компонент!
@Component // Важно! Добавляем @Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getStatus()
        );
    }

    public List<UserResponse> toResponseList(List<User> users) {
        if (users == null) {
            return List.of();
        }
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Метод по умолчанию для Page
    PageResponse<UserResponse> toPageResponse(Page<User> page) {
        return new PageResponse<>(
                toResponseList(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}

//Это генерирует мапстракт:
//package tv.codealong.tutorials.springboot.thenewboston.second_service;
//
//import java.util.ArrayList;
//import java.util.List;
//import javax.annotation.processing.Generated;
//import org.springframework.stereotype.Component;
//
//@Generated(
//    value = "org.mapstruct.ap.MappingProcessor",
//    date = "2025-12-21T16:53:18+0300",
//    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from kotlin-annotation-processing-gradle-1.9.25.jar, environment: Java 21.0.9 (Eclipse Adoptium)"
//)
//@Component
//public class UserMapperImpl implements UserMapper {
//
//    @Override
//    public UserResponse toResponse(User user) {
//        if ( user == null ) {
//            return null;
//        }
//
//        UserResponse userResponse = new UserResponse();
//
//        userResponse.setId( user.getId() );
//        userResponse.setEmail( user.getEmail() );
//        userResponse.setStatus( user.getStatus() );
//
//        return userResponse;
//    }
//
//    @Override
//    public List<UserResponse> toResponseList(List<User> users) {
//        if ( users == null ) {
//            return null;
//        }
//
//        List<UserResponse> list = new ArrayList<UserResponse>( users.size() );
//        for ( User user : users ) {
//            list.add( toResponse( user ) );
//        }
//
//        return list;
//    }
//}
//