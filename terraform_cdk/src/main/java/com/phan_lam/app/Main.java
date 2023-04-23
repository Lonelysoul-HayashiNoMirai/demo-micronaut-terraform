package com.phan_lam.app;

import com.phan_lam.app.stack.BackendStack;

import com.hashicorp.cdktf.App;
import com.hashicorp.cdktf.NamedCloudWorkspace;
import com.hashicorp.cdktf.CloudBackend;
import com.hashicorp.cdktf.CloudBackendConfig;

public class Main {

    public static void main (String[] args){
        final App app = new App ();
        BackendStack stack;
        
        stack = new BackendStack (app, "backend-dev-stack");
        new CloudBackend (
                stack
                , CloudBackendConfig.builder ()
                        .hostname ("app.terraform.io")
                        .organization ("HayashiNoMirai")
                        .workspaces (
                                new NamedCloudWorkspace ("terraform-cdk")
                        )
                        .build ()
        );
        
        app.synth ();
    }
}
