package com.projects.marketmosaic.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phoneNumber;
    private String profilePicture;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ShippingAddressDTO> shippingAddresses;
    private boolean isSeller;
    private String businessName; // Only populated if user is a seller
    private String businessAddress; // Only populated if user is a seller
    private String contactPhone; // Only populated if user is a seller
    private String sellerStatus; // Only populated if user is a seller
}