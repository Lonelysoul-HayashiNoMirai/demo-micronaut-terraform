/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.phan_lam.app.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author Phan Lam
 */
@Data
@AllArgsConstructor
public class LambdaFunctionConfig {
    private String executablePath;
    private String handlerPath;
    private String runtime;
}
