package tn.esprit.realestateservice.service;

import tn.esprit.realestateservice.dto.AddressDTO;

import java.util.List;

public interface AddressService {
    AddressDTO create(AddressDTO dto);
    AddressDTO findById(Long id);
    List<AddressDTO> findAll();
    AddressDTO update(Long id, AddressDTO dto);
    void delete(Long id);
}
