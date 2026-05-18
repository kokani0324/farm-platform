package com.farm.platform.dto;

import com.farm.platform.entity.FarmerCertType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 小農註冊:Phase A 暫以 Base64 字串接 cert_file(demo 階段免處理 multipart);
 * 註冊成功後 cert_passed=false,需管理員審核才能登入。
 */
@Data
public class FarmerRegisterRequest {

    @NotBlank @Email @Size(max = 50)
    private String email;

    @NotBlank @Size(min = 6, max = 64)
    private String password;

    @NotBlank @Size(max = 50)
    private String farmName;

    @NotBlank @Size(max = 100)
    private String farmAddress;

    @Pattern(regexp = "^$|^[0-9+\\-() ]{6,20}$")
    private String phone;

    @Size(max = 2000)
    private String farmDesc;

    private BigDecimal locLat;
    private BigDecimal locLong;

    /** 文件類型:IDENTITY / LAND / PRODUCT */
    private FarmerCertType certType;

    /** Base64 字串(demo 階段);Phase B 改 multipart */
    private String certFileBase64;
}
