/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.app.resource_stack;

import com.hashicorp.cdktf.TerraformAsset;
import com.hashicorp.cdktf.TerraformStack;
import com.mycompany.app.model.configuration.LambdaFunctionConfig;
import java.util.UUID;
import software.constructs.Construct;

/**
 *
 * @author Phan Lam
 */
public class LambdaStack extends TerraformStack {
    
    public LambdaStack (
            Construct scope, String id, LambdaFunctionConfig config
    ){
        super (scope, id);
        
        TerraformAsset executable = TerraformAsset.Builder
                .create (this, "lambda-executable")
                .path (id)
    }
}
