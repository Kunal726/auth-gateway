package com.projects.marketmosaic.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterReqDTO extends LoginReqDTO {
    private String name;
    private String email;
    private boolean registerAsSeller;
    private Seller seller;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Seller {
        private String businessName;
        private String contactPhone;
        private String address;
    }
}
