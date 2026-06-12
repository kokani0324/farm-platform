package com.farm.platform.account.service;

import com.farm.platform.account.dto.PublicFarmerResponse;
import com.farm.platform.account.entity.AccountStatus;
import com.farm.platform.account.entity.Farmer;
import com.farm.platform.account.repository.FarmerRepository;
import com.farm.platform.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicFarmerService {

    private final FarmerRepository farmerRepository;

    public PageResponse<PublicFarmerResponse> listPublic(String keyword, Pageable pageable) {
        Page<Farmer> page = farmerRepository.searchPublic(
                true,
                AccountStatus.NORMAL,
                keyword,
                pageable
        );
        return PageResponse.of(page, PublicFarmerResponse::from);
    }

    public PublicFarmerResponse getPublicDetail(Long id) {
        Farmer farmer = farmerRepository.findPublicById(id, true, AccountStatus.NORMAL)
                .orElseThrow(() -> new IllegalArgumentException("小農不存在或尚未公開"));
        return PublicFarmerResponse.from(farmer);
    }
}
