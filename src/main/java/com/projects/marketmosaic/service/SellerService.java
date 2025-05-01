package com.projects.marketmosaic.service;

import com.projects.marketmosaic.common.dto.resp.BaseRespDTO;

public interface SellerService {
    BaseRespDTO approveSeller(String username);

    BaseRespDTO rejectSeller(String username);

    BaseRespDTO getPendingSellers();

    BaseRespDTO revokeSeller(String username);
}