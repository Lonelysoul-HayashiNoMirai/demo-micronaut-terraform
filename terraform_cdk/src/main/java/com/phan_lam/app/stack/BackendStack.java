package com.phan_lam.app.stack;

import software.constructs.Construct;

import com.hashicorp.cdktf.TerraformStack;
import com.hashicorp.cdktf.providers.aws.provider.AwsProvider;

public class BackendStack extends TerraformStack {

    public BackendStack (final Construct scope, final String id){
        super (scope, id);

        AwsProvider.Builder.create (this, "AWS-provider")
                .region ("us-west-2")
                .build ();
    }
}
