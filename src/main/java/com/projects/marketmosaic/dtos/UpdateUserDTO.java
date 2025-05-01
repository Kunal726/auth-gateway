package com.projects.marketmosaic.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateUserDTO {
    private String name;
    private String email;
    private String phoneNumber;
    private ShippingAddressDTO shippingAddress;
}