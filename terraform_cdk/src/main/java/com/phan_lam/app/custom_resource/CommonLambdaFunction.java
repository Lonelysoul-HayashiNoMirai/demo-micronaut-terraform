/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.phan_lam.app.custom_resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashicorp.cdktf.AssetType;
import com.hashicorp.cdktf.ITerraformDependable;
import com.hashicorp.cdktf.TerraformAsset;
import com.hashicorp.cdktf.TerraformOutput;
import com.hashicorp.cdktf.providers.aws.api_gateway_deployment.ApiGatewayDeployment;
import com.hashicorp.cdktf.providers.aws.api_gateway_integration.ApiGatewayIntegration;
import com.hashicorp.cdktf.providers.aws.api_gateway_method.ApiGatewayMethod;
import com.hashicorp.cdktf.providers.aws.api_gateway_resource.ApiGatewayResource;
import com.hashicorp.cdktf.providers.aws.api_gateway_rest_api.ApiGatewayRestApi;
import com.hashicorp.cdktf.providers.aws.iam_role.IamRole;
import com.hashicorp.cdktf.providers.aws.iam_role_policy_attachment.IamRolePolicyAttachment;
import com.hashicorp.cdktf.providers.aws.iam_role_policy_attachment.IamRolePolicyAttachmentConfig;
import com.hashicorp.cdktf.providers.aws.lambda_function.LambdaFunction;
import com.hashicorp.cdktf.providers.aws.lambda_permission.LambdaPermission;
import com.hashicorp.cdktf.providers.aws.s3_bucket.S3Bucket;
import com.hashicorp.cdktf.providers.aws.s3_object.S3Object;
import static com.phan_lam.app.ResourceConstant.LAMBDA_EXECUTABLE_BUCKET_NAME;
import com.phan_lam.app.model.configuration.LambdaFunctionConfig;
import java.util.ArrayList;
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
    private final IamRolePolicyAttachmentConfig cloudWatchRolePolicy;
    private final LambdaFunction lambdaFunction;
    private final ApiGatewayResource apiGatewayResource;
    private final ApiGatewayMethod apiGatewayMethod;
    private final ApiGatewayIntegration apiGatewayIntegration;
    private final LambdaPermission lambdaPermission;
    
    public CommonLambdaFunction (
            Construct scope, String name, LambdaFunctionConfig config
    ){
        super (scope, name);
        ApiGatewayRestApi restApi;
        List<ITerraformDependable> integrationHolder 
                = new ArrayList<> ();  
        
        executable = TerraformAsset.Builder
                .create (this, "lambda-executable")
                .path (config.getExecutablePath ())
                .type (AssetType.FILE)
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
        
        cloudWatchRolePolicy = IamRolePolicyAttachmentConfig.builder ()
                .policyArn (
                        "arn:aws:iam::aws:policy/service-role"
                        + "/AWSLambdaBasicExecutionRole"
                )
                .role (executionRole.getName ())
                .build ();
        
        new IamRolePolicyAttachment (
                this, "lambda-managed-policy", cloudWatchRolePolicy
        );
        
        lambdaFunction = LambdaFunction.Builder
                .create (this, "lambda-function")
                .functionName (name)
                .s3Bucket (bucket.getBucket ())
                .s3Key (lambdaArchive.getKey ())
                .sourceCodeHash (executable.getAssetHash ())
                .handler (config.getHandlerPath ())
                .runtime (config.getRuntime ())
                .role (executionRole.getArn ())
                .build ();
        
        restApi = config.getRestApi ();
        apiGatewayResource = ApiGatewayResource.Builder
                .create (this, "api-gateway-resource")
                .restApiId (restApi.getId ())
                .parentId (restApi.getRootResourceId ())
                .pathPart (config.getRestResourcePathPart ())
                .build ();
        
        apiGatewayMethod = ApiGatewayMethod.Builder
                .create (this, "api-gateway-method")
                .restApiId (restApi.getId ())
                .resourceId (apiGatewayResource.getId ())
                .authorization ("NONE")
                .httpMethod (config.getRestMethod ())
                .build ();
        
        apiGatewayIntegration = ApiGatewayIntegration.Builder
                .create (this, "api-gateway-integration")
                .httpMethod (apiGatewayMethod.getHttpMethod ())
                .resourceId (apiGatewayResource.getId ())
                .restApiId (restApi.getId ())
                .type ("AWS_PROXY")
                .integrationHttpMethod ("POST")
                .uri (lambdaFunction.getInvokeArn ())
                .build ();
        
        lambdaPermission = LambdaPermission.Builder
                .create (this, "lambda-permission")
                .action ("lambda:InvokeFunction")
                .functionName (lambdaFunction.getFunctionName ())
                .principal ("apigateway.amazonaws.com")
                .sourceArn (restApi.getExecutionArn () + "/*/*")
                .build ();
    } 
}
