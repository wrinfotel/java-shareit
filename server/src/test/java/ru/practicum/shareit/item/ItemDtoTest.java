package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.user.User;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemDtoTest {
    private final JacksonTester<ItemDto> json;
    private final JacksonTester<ItemExtendedDto> jsonExtended;

    @Test
    void testItemDto() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .description("testDescription")
                .name("testName")
                .available(true)
                .requestId(1L)
                .ownerId(1L)
                .build();

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name")
                .isEqualTo(itemDto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(itemDto.getDescription());
        assertThat(result).extractingJsonPathNumberValue("$.requestId")
                .isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.ownerId")
                .isEqualTo(1);
        assertThat(result).extractingJsonPathBooleanValue("$.available")
                .isEqualTo(itemDto.getAvailable().booleanValue());
    }

    @Test
    void testItemExtendedDto() throws Exception {
        Comment commentResponseDto = Comment.builder()
                .id(1L)
                .text("commentText")
                .build();
        User user = User.builder()
                .id(3L)
                .name("test@test.ru")
                .build();
        ItemExtendedDto itemExtendedDto = ItemExtendedDto.builder()
                .id(1L)
                .description("test description")
                .name("name")
                .available(true)
                .lastBooking(null)
                .nextBooking(null)
                .owner(user)
                .comments(List.of(commentResponseDto))
                .build();

        JsonContent<ItemExtendedDto> result = jsonExtended.write(itemExtendedDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name")
                .isEqualTo(itemExtendedDto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(itemExtendedDto.getDescription());
        assertThat(result).extractingJsonPathNumberValue("$.owner.id")
                .isEqualTo(itemExtendedDto.getOwner().getId().intValue());
        ObjectMapper mapper = new ObjectMapper();
        assertThat(result).extractingJsonPathNumberValue("$.comments[0].id")
                .isEqualTo(commentResponseDto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text")
                .isEqualTo(commentResponseDto.getText());
    }
}
