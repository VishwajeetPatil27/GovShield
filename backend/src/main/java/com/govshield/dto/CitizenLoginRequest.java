package com.govshield.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitizenLoginRequest {
    private String aadhaar;
    private String ugid;
}
