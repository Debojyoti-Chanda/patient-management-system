package com.pm.stack;

import software.amazon.awscdk.*;

//  A Stack represents a single unit of deployment in AWS CloudFormation. Everything you define within this class will
//  become part of a single CloudFormation stack.
public class LocalStack extends Stack   {
    // final App scope: This is the parent "scope" for the stack. An App is the top-level container for one or more stacks.
    // final String id: This is a unique identifier for the stack within its scope. AWS CDK uses this ID to generate a
    // logical ID for the CloudFormation stack.
    // final StackProps props: This is an object that contains properties for configuring the stack, such as the region,
    // account, or other stack-specific settings.
    public LocalStack(final App scope, final String id, final StackProps props){
        super(scope,id,props);
    }

    public static void main(String[] args) {
        //App app = new App(...): This creates a new instance of an App, which is the root of your AWS CDK application.
        App app = new  App(AppProps.builder().outdir("./cdk.out").build());
        // StackProps props = StackProps.builder()...: This creates an object to configure the stack.

        StackProps props = StackProps.builder().synthesizer(new BootstraplessSynthesizer()).build();
        new LocalStack(app,"localstack",props);
        // The synth() method is where the magic happens. It takes the programmatic definition of your infrastructure
        // (the LocalStack and any resources you've defined within it) and "synthesizes" it into a CloudFormation
        // template. This template is what AWS uses to create, update, or delete your resources. The synthesized template
        // is saved in the cdk.out directory.
        app.synth();
        System.out.println("App synthesizing in progress....");
    }
}
