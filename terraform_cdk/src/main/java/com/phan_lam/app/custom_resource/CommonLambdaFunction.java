/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.phan_lam.app.custom_resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashicorp.cdktf.AssetType;
import com.hashicorp.cdktf.TerraformAsset;
import com.hashicorp.cdktf.providers.aws.iam_role.IamRole;
import com.hashicorp.cdktf.providers.aws.s3_bucket.S3Bucket;
import com.hashicorp.cdktf.providers.aws.s3_object.S3Object;
import static com.phan_lam.app.ResourceConstant.LAMBDA_EXECUTABLE_BUCKET_NAME;
import com.phan_lam.app.model.configuration.LambdaFunctionConfig;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.constructs.Construct;

/**
 *
 * @author Phan Lam
 */
@Getter
@Slf4j
public class CommonLambdaFunction extends Construct {
    
    private final TerraformAsset executable;
    private final S3Bucket bucket;
    private final S3Object lambdaArchive;
    
    private final Object lambdaRolePolicy = new Object (){
        public String Version = "2012-10-17";
        public List<Object> Statement = List.of (new Object (){
            public String Action = "sts:AssumeRole"; 
            public Object Principal = new Object (){
                public String Service = "lambda.amazonaws.com";
            };
            public String Effect = "Allow";
            public String Sid = "";
        });
    };
    
    private final ObjectMapper objectMapper = new ObjectMapper ();
    private final IamRole executionRole;
    
    public CommonLambdaFunction (
            Construct scope, String name, LambdaFunctionConfig config
    ){
        super (scope, name);
        
        executable = TerraformAsset.Builder
                .create (this, "lambda-executable")
                .path (config.getExecutablePath ())
                .type (AssetType.ARCHIVE)
                .build ();
        
        bucket = S3Bucket.Builder
                .create (this, "executable-container")
                .bucket (LAMBDA_EXECUTABLE_BUCKET_NAME)
                .build ();
        
        lambdaArchive = S3Object.Builder
                .create (this, "lambda-archive")
                .bucket (bucket.getBucket ())
                .key (name)
                .source (executable.getPath ())
                .build ();
        
        try {
            executionRole = IamRole.Builder
                    .create (this, "execution-role")
                    .name (name)
                    .assumeRolePolicy (objectMapper.writeValueAsString (
                            lambdaRolePolicy
                    ))
                    .build ();
        }
        catch (JsonProcessingException exception){
            log.error ("Invalid lambdaRolePolicy JSON !", exception);
            throw new RuntimeException (exception);
        }
    } 
}
