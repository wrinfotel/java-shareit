package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto createNewItemRequest(ItemRequestCreateDto request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ItemRequest newItem = ItemRequest.builder()
                .requestor(user)
                .description(request.getDescription())
                .created(LocalDateTime.now())
                .build();
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(newItem));
    }

    @Override
    public List<ItemRequestExtendedDto> getUserRequests(Long userId) {
        List<ItemRequestExtendedDto> itemRequests = itemRequestRepository.findAllByRequestorId(userId)
                .stream()
                .map(itemRequest -> ItemRequestMapper.toItemRequestExtendedDto(itemRequest, null))
                .toList();
        List<Item> itemsList = itemRepository.findAllByRequestIdNotNull();
        if (itemsList != null && !itemsList.isEmpty()) {
            for (ItemRequestExtendedDto itemRequest : itemRequests) {
                List<Item> items = itemsList.stream()
                        .filter(item -> itemRequest.getId().equals(item.getRequest().getId()))
                        .toList();
                itemRequest.setItems(items);
            }
        }
        return itemRequests;
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        return itemRequestRepository.findAllByRequestorIdNot(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }

    @Override
    public ItemRequestExtendedDto getRequest(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Item request not found"));
        List<Item> items = itemRepository.findAllByRequestId(requestId);
        return ItemRequestMapper.toItemRequestExtendedDto(itemRequest, items);
    }
}
