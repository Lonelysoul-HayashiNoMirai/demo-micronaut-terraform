/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.phan_lam.app.model.configuration;

import com.hashicorp.cdktf.providers.aws.api_gateway_rest_api.ApiGatewayRestApi;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author Phan Lam
 */
@Data
@Builder
public class LambdaFunctionConfig {
    private String executablePath;
    private String handlerPath;
    private String runtime;
    private final ApiGatewayRestApi restApi;
    private String restResourcePathPart;
    private String restMethod;
}
