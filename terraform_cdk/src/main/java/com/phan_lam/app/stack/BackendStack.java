package com.phan_lam.app.stack;

import com.hashicorp.cdktf.ITerraformDependable;
import com.hashicorp.cdktf.TerraformOutput;
import software.constructs.Construct;

import com.hashicorp.cdktf.TerraformStack;
import com.hashicorp.cdktf.providers.aws.api_gateway_deployment.ApiGatewayDeployment;
import com.hashicorp.cdktf.providers.aws.api_gateway_integration.ApiGatewayIntegration;
import com.hashicorp.cdktf.providers.aws.api_gateway_method.ApiGatewayMethod;
import com.hashicorp.cdktf.providers.aws.api_gateway_rest_api.ApiGatewayRestApi;
import com.hashicorp.cdktf.providers.aws.api_gateway_rest_api.ApiGatewayRestApiEndpointConfiguration;
import com.hashicorp.cdktf.providers.aws.provider.AwsProvider;
import static com.phan_lam.app.ResourceConstant.RELATIVE_BASE_PATH;
import com.phan_lam.app.custom_resource.CommonLambdaFunction;
import com.phan_lam.app.model.configuration.LambdaFunctionConfig;
import java.util.ArrayList;
import java.util.List;

public class BackendStack extends TerraformStack {
    
    private final ApiGatewayRestApi apiGatewayRestApi;
    private final ApiGatewayMethod rootMethod;
    private final ApiGatewayIntegration rootIntegration;
    private final CommonLambdaFunction micronautFunction;
    private final ApiGatewayDeployment apiGatewayDeployment;
    private final TerraformOutput output;
    
    public BackendStack (final Construct scope, final String id){
        super (scope, id);
        ApiGatewayRestApiEndpointConfiguration endpointConfiguration;
        List<ITerraformDependable> integrationHolder = new ArrayList<> ();
        LambdaFunctionConfig lambdaConfig;

        AwsProvider.Builder.create (this, "AWS-provider")
                .region ("us-west-2")
                .profile ("dev")
                .build ();
        
        endpointConfiguration 
                = ApiGatewayRestApiEndpointConfiguration.builder ()
                        .types (List.of ("REGIONAL"))
                        .build ();
        
        apiGatewayRestApi = ApiGatewayRestApi.Builder
                .create (this, "main-api-gateway")
                .name ("main-api-gateway")
                .endpointConfiguration (endpointConfiguration)
                .build ();
        
        
        
        lambdaConfig = LambdaFunctionConfig.builder ()
                .executablePath (
                        RELATIVE_BASE_PATH 
                        + "demo_micronaut_terraform-0.1.jar"
                )
                .handlerPath (
                        "io.micronaut.function.aws.proxy"
                        + ".MicronautLambdaHandler"
                )
                .runtime ("java11")
                .restApi (apiGatewayRestApi)
                .restResourcePathPart ("{proxy+}")
                .restMethod ("ANY")
                .build ();
        
        micronautFunction = new CommonLambdaFunction (
                this, "micronaut-function", lambdaConfig
        );
        integrationHolder.add (
                micronautFunction.getApiGatewayIntegration ()
        );
        
        rootMethod = ApiGatewayMethod.Builder
                .create (this, "root-method")
                .restApiId (apiGatewayRestApi.getId ())
                .resourceId (apiGatewayRestApi.getRootResourceId ())
                .authorization ("NONE")
                .httpMethod ("ANY")
                .build ();
        
        rootIntegration = ApiGatewayIntegration.Builder
                .create (this, "api-gateway-integration")
                .restApiId (apiGatewayRestApi.getId ())
                .resourceId (rootMethod.getResourceId ())
                .httpMethod (rootMethod.getHttpMethod ())
                .type ("AWS_PROXY")
                .integrationHttpMethod ("POST")
                .uri (
                        micronautFunction.getLambdaFunction ()
                                .getInvokeArn ()
                )
                .build ();
        integrationHolder.add (rootIntegration);
        
        apiGatewayDeployment = ApiGatewayDeployment.Builder
                .create (this, "api-gateway-deployment")
                .restApiId (apiGatewayRestApi.getId ())
                .dependsOn (integrationHolder)
                .stageName ("dev")
                .build ();
        
        output = TerraformOutput.Builder
                .create (this, "lambda-function-url")
                .value (apiGatewayDeployment.getInvokeUrl ())
                .build ();
    }
}
