package com.mycompany.app;

import software.constructs.Construct;

import com.hashicorp.cdktf.TerraformStack;
import com.hashicorp.cdktf.providers.aws.provider.AwsProvider;

public class MainStack extends TerraformStack
{
    public MainStack(final Construct scope, final String id) {
        super(scope, id);
        
        AwsProvider.Builder.create (this, "AWS-provider")
                .region ("us-west-2")
                .build ();
        
       
    }
}