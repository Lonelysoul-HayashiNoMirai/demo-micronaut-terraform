package com.phan_lam.app;

import com.phan_lam.app.stack.BackendStack;

import com.hashicorp.cdktf.App;

public class Main {

    public static void main (String[] args){
        final App app = new App ();
        BackendStack stack;
        
        stack = new BackendStack (app, "backend-dev-stack");
        
        app.synth ();
    }
}
