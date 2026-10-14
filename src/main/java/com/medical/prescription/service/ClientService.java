package com.medical.prescription.service;

import com.medical.prescription.dto.request.CreateClientRequest;
import com.medical.prescription.dto.response.ClientResponse;
import com.medical.prescription.entity.Client;
import com.medical.prescription.exception.ResourceNotFoundException;
import com.medical.prescription.repository.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> findAll(Pageable pageable) {
        return clientRepository.findAll(pageable).map(ClientResponse::from);
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        return clientRepository.findById(id)
                .map(ClientResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));
    }

    @Transactional
    public ClientResponse create(CreateClientRequest request) {
        Client client = Client.builder()
                .name(request.name())
                .dateOfBirth(request.dateOfBirth())
                .phoneNumber(request.phoneNumber())
                .build();
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse update(Long id, CreateClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        client.setName(request.name());
        client.setDateOfBirth(request.dateOfBirth());
        client.setPhoneNumber(request.phoneNumber());

        return ClientResponse.from(clientRepository.save(client));
    }
}
