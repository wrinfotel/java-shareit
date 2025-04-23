package ru.practicum.shareit.itemRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestDtoTest {
    private final JacksonTester<ItemRequestDto> json;
    private final JacksonTester<ItemRequestExtendedDto> jsonExtended;

    @Test
    void testItemRequestDto() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .requestorId(1L)
                .description("Test description")
                .created(LocalDateTime.of(2025, 5, 1, 10, 34, 5))
                .build();

        JsonContent<ItemRequestDto> result = json.write(itemRequestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(itemRequestDto.getDescription());
        assertThat(result).extractingJsonPathNumberValue("$.requestorId")
                .isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(itemRequestDto.getCreated().toString());
    }

    @Test
    void testItemRequestExtendedDto() throws Exception {

        ItemRequestExtendedDto itemRequestExtendedDto = ItemRequestExtendedDto.builder()
                .id(1L)
                .description("test description")
                .items(List.of(Item.builder()
                        .id(1L)
                        .available(true)
                        .description("test descr item").build()))
                .build();

        JsonContent<ItemRequestExtendedDto> result = jsonExtended.write(itemRequestExtendedDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(itemRequestExtendedDto.getDescription());

        ObjectMapper mapper = new ObjectMapper();
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id")
                .isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].description")
                .isEqualTo("test descr item");
    }
}
