package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto createNewItemRequest(ItemRequestCreateDto request, Long userId);

    List<ItemRequestExtendedDto> getUserRequests(Long userId);

    List<ItemRequestDto> getAllRequests(Long userId);

    ItemRequestExtendedDto getRequest(Long requestId);
}
