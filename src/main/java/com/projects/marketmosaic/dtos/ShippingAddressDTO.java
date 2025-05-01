package com.projects.marketmosaic.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShippingAddressDTO {
    private Long id;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String phoneNumber;
    private String addressType;
    private boolean isDefault;
}