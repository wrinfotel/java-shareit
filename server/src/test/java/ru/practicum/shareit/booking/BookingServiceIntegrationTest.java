package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceIntegrationTest {
    private final EntityManager em;
    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;

    @Test
    void testSaveItem() {
        ItemDto itemDto = ItemDto.builder()
                .name("TestName")
                .description("test Description")
                .available(true)
                .build();

        UserDto userDto = UserDto.builder()
                .name("TestName")
                .email("test@email.com").build();

        userService.create(userDto);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail())
                .getSingleResult();

        itemService.addNewItem(user.getId(), itemDto);

        TypedQuery<Item> queryItem = em.createQuery("Select it from Item it where it.name = :name", Item.class);
        Item item = queryItem.setParameter("name", itemDto.getName())
                .getSingleResult();

        BookingDtoRequest bookingDtoRequest = BookingDtoRequest.builder()
                .start(LocalDateTime.now().plusMinutes(10))
                .end(LocalDateTime.now().plusMinutes(15))
                .itemId(item.getId())
                .build();

        bookingService.createNewBooking(bookingDtoRequest, user.getId());

        TypedQuery<Booking> queryBooking = em.createQuery("Select b from Booking b where b.id = :id", Booking.class);
        Booking booking = queryBooking.setParameter("id", 1L)
                .getSingleResult();
        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getStart(), equalTo(bookingDtoRequest.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingDtoRequest.getEnd()));
        assertThat(booking.getBooker().getId(), equalTo(user.getId()));
        assertThat(booking.getItem().getId(), equalTo(item.getId()));
        assertThat(booking.getStatus(), equalTo(BookingStatus.WAITING));
    }
}
