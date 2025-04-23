package ru.practicum.shareit.itemRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    User user1 = User.builder()
            .id(1L)
            .email("test@user.ru")
            .name("User 1")
            .build();

    ItemRequest itemRequest = ItemRequest.builder()
            .id(1L)
            .requestor(user1)
            .description("request description")
            .created(LocalDateTime.now())
            .build();

    Item item1 = Item.builder()
            .id(1L)
            .name("Item name")
            .description("Item Description")
            .available(true)
            .owner(user1)
            .request(itemRequest)
            .build();

    @Test
    void shouldCreateNewItemRequest() {
        ItemRequestCreateDto itemRequestCreateDto = new ItemRequestCreateDto();
        itemRequestCreateDto.setDescription("test Description");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user1));
        Mockito.when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);
        ItemRequestDto itemRequestDto = itemRequestService
                .createNewItemRequest(itemRequestCreateDto, 1L);
        Assertions.assertEquals(ItemRequestMapper.toItemRequestDto(itemRequest), itemRequestDto);
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void shouldNotCreateNewItemRequestUserNotFound() {
        ItemRequestCreateDto itemRequestCreateDto = new ItemRequestCreateDto();
        itemRequestCreateDto.setDescription("test Description");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> itemRequestService
                        .createNewItemRequest(itemRequestCreateDto, 1L));
        Assertions.assertEquals("User not found", exception.getMessage());
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void shouldGetUsersRequests() {
        List<ItemRequest> itemRequests = new ArrayList<>();
        itemRequests.add(itemRequest);
        Mockito.when(itemRequestRepository.findAllByRequestorId(1L)).thenReturn(itemRequests);
        Mockito.when(itemRepository.findAllByRequestIdNotNull()).thenReturn(new ArrayList<>());
        List<ItemRequestExtendedDto> dtoList = itemRequestService.getUserRequests(1L);
        Assertions.assertEquals(itemRequests.stream()
                .map(itemRequest -> ItemRequestMapper.toItemRequestExtendedDto(itemRequest, null))
                .toList(), dtoList);
    }

    @Test
    void shouldGetAllRequests() {
        List<ItemRequest> itemRequests = new ArrayList<>();
        itemRequests.add(itemRequest);
        Mockito.when(itemRequestRepository.findAllByRequestorIdNot(1L)).thenReturn(itemRequests);
        List<ItemRequestDto> dtoList = itemRequestService.getAllRequests(1L);
        Assertions.assertEquals(itemRequests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList(), dtoList);
    }

    @Test
    void shouldGetRequest() {
        List<Item> items = new ArrayList<>();
        items.add(item1);
        Mockito.when(itemRequestRepository.findById(1L)).thenReturn(Optional.ofNullable(itemRequest));
        Mockito.when(itemRepository.findAllByRequestId(1L)).thenReturn(items);
        ItemRequestExtendedDto testItem = ItemRequestExtendedDto.builder()
                .items(items)
                .id(itemRequest.getId())
                .created(itemRequest.getCreated())
                .requestorId(user1.getId())
                .description(itemRequest.getDescription())
                .build();

        ItemRequestExtendedDto serviceItem = itemRequestService.getRequest(1L);
        Assertions.assertEquals(testItem, serviceItem);
    }

    @Test
    void shouldNotGetRequestNotFound() {
        Mockito.when(itemRequestRepository.findById(1L)).thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequest(1L));

        Assertions.assertEquals("Item request not found", exception.getMessage());
    }

}
